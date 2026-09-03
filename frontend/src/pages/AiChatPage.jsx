import { useEffect, useRef, useState } from 'react'
import {
  Bot,
  LoaderCircle,
  Send,
  Sparkles,
  UserRound,
} from 'lucide-react'
import { chatApi, getChatError } from '../api/chatApi'

const initialMessage = {
  id: 'welcome',
  role: 'assistant',
  content:
    'Hello! I can answer questions about employees and help create or update employee records. Every data change requires your confirmation.',
}

export default function AiChatPage() {
  const [messages, setMessages] = useState([initialMessage])
  const [input, setInput] = useState('')
  const [isSending, setIsSending] = useState(false)
  const [confirmationToken, setConfirmationToken] = useState(null)
  const messagesEndRef = useRef(null)

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({
      behavior: 'smooth',
    })
  }, [messages, isSending])

  async function handleSubmit(event) {
    event.preventDefault()

    const message = input.trim()

    if (!message || isSending) {
      return
    }

    const userMessage = {
      id: crypto.randomUUID(),
      role: 'user',
      content: message,
    }

    setMessages((current) => [...current, userMessage])
    setInput('')
    setIsSending(true)

    try {
      const response = await chatApi.send(
        message,
        confirmationToken,
      )

      if (
        response.confirmationRequired &&
        response.confirmationToken
      ) {
        setConfirmationToken(response.confirmationToken)
      } else {
        setConfirmationToken(null)
      }

      if (response.dataChanged) {
        window.dispatchEvent(
          new CustomEvent('employees:changed'),
        )
      }

      setMessages((current) => [
        ...current,
        {
          id: crypto.randomUUID(),
          role: 'assistant',
          content: response.reply,
        },
      ])
    } catch (error) {
      setMessages((current) => [
        ...current,
        {
          id: crypto.randomUUID(),
          role: 'error',
          content: getChatError(error),
        },
      ])
    } finally {
      setIsSending(false)
    }
  }

  function handleKeyDown(event) {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault()
      event.currentTarget.form?.requestSubmit()
    }
  }

  return (
    <section className="page narrow-page chat-page">
      <header className="page-header">
        <div>
          <p className="eyebrow">AI ASSISTANT</p>
          <h1>Ask People</h1>
          <p className="subtitle">
            Query employee information and perform protected
            actions using natural language.
          </p>
        </div>

        <span className="ai-status">
          <span aria-hidden="true" />
          Protected actions
        </span>
      </header>

      <div className="chat-card">
        <div
          className="chat-messages"
          aria-live="polite"
          aria-label="Chat messages"
        >
          {messages.map((message) => {
            const isUser = message.role === 'user'
            const isError = message.role === 'error'

            return (
              <article
                className={`chat-message ${
                  isUser
                    ? 'user-message'
                    : 'assistant-message'
                } ${isError ? 'chat-error' : ''}`}
                key={message.id}
              >
                <span
                  className="message-avatar"
                  aria-hidden="true"
                >
                  {isUser ? (
                    <UserRound size={18} />
                  ) : (
                    <Bot size={18} />
                  )}
                </span>

                <div className="message-content">
                  {!isUser && (
                    <strong>
                      {isError
                        ? 'Unable to respond'
                        : 'People AI'}
                    </strong>
                  )}

                  <p>{message.content}</p>
                </div>
              </article>
            )
          })}

          {isSending && (
            <article className="chat-message assistant-message">
              <span
                className="message-avatar"
                aria-hidden="true"
              >
                <Sparkles size={18} />
              </span>

              <div className="message-content typing-message">
                <LoaderCircle
                  className="button-spinner"
                  size={18}
                  aria-hidden="true"
                />
                <span>Thinking…</span>
              </div>
            </article>
          )}

          <div ref={messagesEndRef} />
        </div>

        <form
          className="chat-composer"
          onSubmit={handleSubmit}
        >
          <label
            className="sr-only"
            htmlFor="chat-message"
          >
            Send a message to the employee assistant
          </label>

          <textarea
            id="chat-message"
            value={input}
            onChange={(event) =>
              setInput(event.target.value)
            }
            onKeyDown={handleKeyDown}
            placeholder={
              confirmationToken
                ? 'Type confirm or cancel…'
                : 'Ask about your employees…'
            }
            maxLength={1000}
            rows={1}
            disabled={isSending}
          />

          <button
            className="chat-send"
            type="submit"
            disabled={!input.trim() || isSending}
            aria-label="Send message"
          >
            <Send size={19} aria-hidden="true" />
          </button>
        </form>

        <p className="chat-hint">
          {confirmationToken
            ? 'A protected action is awaiting confirmation.'
            : 'Enter to send · Shift + Enter for a new line'}
        </p>
      </div>
    </section>
  )
}