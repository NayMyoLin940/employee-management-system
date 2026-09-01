import { LoaderCircle } from 'lucide-react'
import { useState } from 'react'

const EMPTY = { name: '', email: '', phone: '', department: '', position: '', salary: '', hireDate: '' }

function validate(values) {
  const errors = {}
  if (!values.name.trim()) errors.name = 'Name is required'
  if (!values.email.trim()) errors.email = 'Email is required'
  else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(values.email)) errors.email = 'Email format is invalid'
  if (!values.department.trim()) errors.department = 'Department is required'
  if (!values.position.trim()) errors.position = 'Position is required'
  if (values.phone.length > 30) errors.phone = 'Phone must be 30 characters or fewer'
  if (values.salary !== '' && (Number.isNaN(Number(values.salary)) || Number(values.salary) < 0)) errors.salary = 'Salary cannot be negative'
  return errors
}

export default function EmployeeForm({ initialValues, submitting, serverError, serverFields, onSubmit, onCancel }) {
  const [values, setValues] = useState(() => ({ ...EMPTY, ...initialValues, salary: initialValues?.salary ?? '', hireDate: initialValues?.hireDate ?? '' }))
  const [localErrors, setLocalErrors] = useState({})
  const errors = { ...localErrors, ...serverFields }

  const change = (event) => {
    const { name, value } = event.target
    setValues((current) => ({ ...current, [name]: value }))
    setLocalErrors((current) => ({ ...current, [name]: undefined }))
  }

  const submit = (event) => {
    event.preventDefault()
    if (submitting) return
    const nextErrors = validate(values)
    setLocalErrors(nextErrors)
    if (Object.keys(nextErrors).length) return
    onSubmit({
      name: values.name.trim(), email: values.email.trim(), phone: values.phone.trim() || null,
      department: values.department.trim(), position: values.position.trim(),
      salary: values.salary === '' ? null : Number(values.salary), hireDate: values.hireDate || null,
    })
  }

  const field = (name, label, props = {}) => {
    const { required, ...inputProps } = props
    return (
      <label className="field">
        <span>{label}{required && <span className="required" aria-hidden="true"> *</span>}</span>
        <input name={name} value={values[name]} onChange={change} aria-invalid={Boolean(errors[name])} aria-describedby={errors[name] ? `${name}-error` : undefined} required={required} {...inputProps} />
        {errors[name] && <small id={`${name}-error`} className="field-error">{errors[name]}</small>}
      </label>
    )
  }

  return (
    <form className="form-card" onSubmit={submit} noValidate>
      {serverError && <p className="form-alert" role="alert">{serverError}</p>}
      <div className="form-grid">
        {field('name', 'Full name', { required: true, autoComplete: 'name', placeholder: 'e.g. Alex Morgan' })}
        {field('email', 'Email address', { required: true, type: 'email', autoComplete: 'email', placeholder: 'alex@company.com' })}
        {field('phone', 'Phone', { autoComplete: 'tel', maxLength: 30, placeholder: '09 123 456 789' })}
        {field('department', 'Department', { required: true, placeholder: 'e.g. Engineering' })}
        {field('position', 'Position', { required: true, placeholder: 'e.g. Product Designer' })}
        {field('salary', 'Salary (MMK)', { type: 'number', min: '0', step: '0.01', inputMode: 'decimal', placeholder: '1500000' })}
        {field('hireDate', 'Hire date', { type: 'date' })}
      </div>
      <div className="form-actions">
        <button className="button secondary" type="button" onClick={onCancel} disabled={submitting}>Cancel</button>
        <button className="button primary" type="submit" disabled={submitting} aria-busy={submitting}>{submitting && <LoaderCircle className="button-spinner" size={17} aria-hidden="true" />}{submitting ? 'Saving…' : 'Save employee'}</button>
      </div>
    </form>
  )
}
