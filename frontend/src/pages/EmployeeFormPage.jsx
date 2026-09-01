import { ArrowLeft } from 'lucide-react'
import { useCallback, useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { employeeApi, getApiError } from '../api/employeeApi'
import EmployeeForm from '../components/EmployeeForm'
import { ErrorState, LoadingState } from '../components/Feedback'
import { useToast } from '../components/toastContext'

export default function EmployeeFormPage() {
  const { id } = useParams(); const navigate = useNavigate(); const editing = Boolean(id)
  const { showToast } = useToast()
  const [employee, setEmployee] = useState(null); const [loading, setLoading] = useState(editing); const [loadError, setLoadError] = useState('')
  const [submitting, setSubmitting] = useState(false); const [serverError, setServerError] = useState(''); const [serverFields, setServerFields] = useState({})
  const load = useCallback(async () => {
    if (!editing) return
    setLoading(true); setLoadError('')
    try { setEmployee(await employeeApi.getById(id)) } catch (requestError) { const message = getApiError(requestError).message; setLoadError(message); showToast(message, 'error') } finally { setLoading(false) }
  }, [editing, id, showToast])
  useEffect(() => { void Promise.resolve().then(load) }, [load])
  const save = async (values) => {
    if (submitting) return
    setSubmitting(true); setServerError(''); setServerFields({})
    try { const saved = editing ? await employeeApi.update(id, values) : await employeeApi.create(values); showToast(editing ? 'Employee updated successfully' : 'Employee created successfully'); navigate(`/employees/${saved.id}`, { replace: true }) }
    catch (requestError) { const apiError = getApiError(requestError); setServerError(apiError.message); setServerFields(apiError.fields); showToast(apiError.message, 'error'); setSubmitting(false) }
  }
  const cancelPath = editing ? `/employees/${id}` : '/employees'
  return (
    <div className="page narrow-page">
      <Link className="back-link" to={cancelPath}><ArrowLeft size={18} /> {editing ? 'Employee profile' : 'Employees'}</Link>
      <header className="page-header"><div><p className="eyebrow">{editing ? 'Update profile' : 'New team member'}</p><h1>{editing ? 'Edit employee' : 'Add employee'}</h1><p className="subtitle">{editing ? 'Keep this employee’s information up to date.' : 'Enter the details for your new team member.'}</p></div></header>
      {loading ? <LoadingState label="Loading employee…" /> : loadError ? <ErrorState message={loadError} onRetry={load} /> : <EmployeeForm initialValues={employee} submitting={submitting} serverError={serverError} serverFields={serverFields} onSubmit={save} onCancel={() => navigate(cancelPath)} />}
    </div>
  )
}
