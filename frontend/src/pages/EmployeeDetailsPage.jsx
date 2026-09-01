import { ArrowLeft, BriefcaseBusiness, Building2, CalendarDays, CircleDollarSign, Mail, Pencil, Phone, Trash2, UserRound } from 'lucide-react'
import { useCallback, useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { employeeApi, getApiError } from '../api/employeeApi'
import DeleteDialog from '../components/DeleteDialog'
import { ErrorState } from '../components/Feedback'
import { DetailsSkeleton } from '../components/Skeletons'
import { useToast } from '../components/toastContext'

const formatDate = (date) => date ? new Intl.DateTimeFormat(undefined, { dateStyle: 'long' }).format(new Date(`${date}T00:00:00`)) : 'Not provided'
const formatSalary = (salary) => salary == null
  ? 'Not provided'
  : `MMK ${new Intl.NumberFormat('en-US', { maximumFractionDigits: 2 }).format(salary)}`

export default function EmployeeDetailsPage() {
  const { id } = useParams(); const navigate = useNavigate()
  const { showToast } = useToast()
  const [employee, setEmployee] = useState(null); const [loading, setLoading] = useState(true); const [error, setError] = useState('')
  const [confirming, setConfirming] = useState(false); const [deleting, setDeleting] = useState(false); const [deleteError, setDeleteError] = useState('')
  const load = useCallback(async () => {
    setLoading(true); setError('')
    try { setEmployee(await employeeApi.getById(id)) } catch (requestError) { const message = getApiError(requestError).message; setError(message); showToast(message, 'error') } finally { setLoading(false) }
  }, [id, showToast])
  useEffect(() => { void Promise.resolve().then(load) }, [load])
  const remove = async () => {
    if (deleting) return
    setDeleting(true); setDeleteError('')
    try { await employeeApi.remove(id); showToast('Employee deleted successfully'); navigate('/employees', { replace: true }) }
    catch (requestError) { const message = getApiError(requestError).message; setDeleteError(message); showToast(message, 'error'); setDeleting(false) }
  }
  const info = employee && [
    { icon: Mail, label: 'Email', value: employee.email, href: `mailto:${employee.email}` },
    { icon: Phone, label: 'Phone', value: employee.phone || 'Not provided', href: employee.phone ? `tel:${employee.phone}` : null },
    { icon: Building2, label: 'Department', value: employee.department },
    { icon: BriefcaseBusiness, label: 'Position', value: employee.position },
    { icon: CircleDollarSign, label: 'Salary (MMK)', value: formatSalary(employee.salary) },
    { icon: CalendarDays, label: 'Hire date', value: formatDate(employee.hireDate) },
  ]
  return (
    <div className="page narrow-page">
      <Link className="back-link" to="/employees"><ArrowLeft size={18} /> Employees</Link>
      {loading ? <DetailsSkeleton /> : error ? <ErrorState message={error} onRetry={load} /> : employee && (
        <>
          <section className="profile-card"><span className="avatar profile-avatar"><UserRound /></span><div className="profile-title"><p className="eyebrow">Employee profile</p><h1>{employee.name}</h1><p>{employee.position} · {employee.department}</p></div><div className="profile-actions"><Link className="button secondary" to={`/employees/${id}/edit`}><Pencil size={17} /> Edit</Link><button className="button danger-quiet" type="button" onClick={() => setConfirming(true)}><Trash2 size={17} /> Delete</button></div></section>
          <section className="details-card"><h2>Employee information</h2><div className="details-grid">{info.map(({ icon: Icon, label, value, href }) => <div className="detail-item" key={label}><span className="detail-icon"><Icon size={20} /></span><div><small>{label}</small>{href ? <a href={href}>{value}</a> : <strong>{value}</strong>}</div></div>)}</div></section>
        </>
      )}
      {confirming && employee && <DeleteDialog employee={employee} busy={deleting} error={deleteError} onCancel={() => { setConfirming(false); setDeleteError('') }} onConfirm={remove} />}
    </div>
  )
}
