import axios from 'axios'

const client = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
  timeout: 60000,
})

export const chatApi = {
  send: async (message, confirmationToken = null) =>
    (
      await client.post('/chat', {
        message,
        confirmationToken,
      })
    ).data,
}

export function getChatError(error) {
  if (!error.response) {
    return 'Cannot connect to the AI service. Check that the backend is running.'
  }

  if (error.response.status === 400) {
    return error.response.data?.message || 'Please enter a valid message.'
  }

  return (
    error.response.data?.message ||
    'The AI assistant is temporarily unavailable. Please try again.'
  )
}