import { AlertTriangle, LoaderCircle, X } from 'lucide-react'
import { useEffect, useRef } from 'react'

export default function DeleteDialog({ employee, busy, error, onCancel, onConfirm }) {
  const cancelRef = useRef(null)
  useEffect(() => {
    cancelRef.current?.focus()
    const close = (event) => event.key === 'Escape' && !busy && onCancel()
    window.addEventListener('keydown', close)
    return () => window.removeEventListener('keydown', close)
  }, [busy, onCancel])

  return (
    <div className="dialog-backdrop" role="presentation" onMouseDown={(event) => event.target === event.currentTarget && !busy && onCancel()}>
      <section className="dialog" role="alertdialog" aria-modal="true" aria-labelledby="delete-title">
        <button className="icon-button dialog-close" type="button" onClick={onCancel} disabled={busy} aria-label="Close dialog"><X size={20} /></button>
        <span className="danger-icon"><AlertTriangle aria-hidden="true" /></span>
        <h2 id="delete-title">Delete {employee.name}?</h2>
        <p>This employee record will be permanently removed. This action cannot be undone.</p>
        {error && <p className="form-alert" role="alert">{error}</p>}
        <div className="dialog-actions">
          <button ref={cancelRef} className="button secondary" type="button" onClick={onCancel} disabled={busy}>Cancel</button>
          <button className="button danger" type="button" onClick={onConfirm} disabled={busy} aria-busy={busy}>{busy && <LoaderCircle className="button-spinner" size={17} aria-hidden="true" />}{busy ? 'Deleting…' : 'Delete employee'}</button>
        </div>
      </section>
    </div>
  )
}
