import { AlertCircle, LoaderCircle, UsersRound } from 'lucide-react'

export function LoadingState({ label = 'Loading employees…' }) {
  return <div className="state-card" role="status"><LoaderCircle className="spin" aria-hidden="true" /><p>{label}</p></div>
}

export function ErrorState({ message, onRetry }) {
  return (
    <div className="state-card error-state" role="alert">
      <AlertCircle aria-hidden="true" /><h2>Unable to load</h2><p>{message}</p>
      {onRetry && <button className="button secondary" type="button" onClick={onRetry}>Try again</button>}
    </div>
  )
}

export function EmptyState({ filtered = false }) {
  return (
    <div className="state-card">
      <UsersRound aria-hidden="true" />
      <h2>{filtered ? 'No matching employees' : 'No employees yet'}</h2>
      <p>{filtered ? 'Try a different name, email, or department.' : 'Add your first employee to get started.'}</p>
    </div>
  )
}
