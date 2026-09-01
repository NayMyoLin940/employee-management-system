import { ArrowRight, Building2, CalendarDays, UserRound, UsersRound } from 'lucide-react'
import { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { employeeApi, getApiError } from '../api/employeeApi'
import { EmptyState, ErrorState } from '../components/Feedback'
import { DashboardSkeleton } from '../components/Skeletons'
import { useToast } from '../components/toastContext'

const formatDate = (date) => date ? new Intl.DateTimeFormat(undefined, { year: 'numeric', month: 'short', day: 'numeric' }).format(new Date(`${date}T00:00:00`)) : 'Not set'

export default function DashboardPage() {
  const { showToast } = useToast()
  const [employees, setEmployees] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const load = useCallback(async () => {
    setLoading(true); setError('')
    try { setEmployees(await employeeApi.getAll()) }
    catch (requestError) { const message = getApiError(requestError).message; setError(message); showToast(message, 'error') }
    finally { setLoading(false) }
  }, [showToast])
  useEffect(() => { void Promise.resolve().then(load) }, [load])

  const departmentCounts = employees.reduce((counts, employee) => {
    const department = employee.department?.trim()
    if (!department) return counts
    const existing = [...counts.keys()].find((name) => name.toLowerCase() === department.toLowerCase())
    const name = existing || department
    counts.set(name, (counts.get(name) || 0) + 1)
    return counts
  }, new Map())
  const departments = departmentCounts.size
  const distribution = [...departmentCounts.entries()].sort((a, b) => b[1] - a[1])
  const recent = [...employees].filter((employee) => employee.hireDate).sort((a, b) => b.hireDate.localeCompare(a.hireDate)).slice(0, 5)

  return (
    <div className="page">
      <header className="page-header"><div><p className="eyebrow">Overview</p><h1>Dashboard</h1><p className="subtitle">A clear view of your growing team.</p></div></header>
      {loading ? <DashboardSkeleton /> : error ? <ErrorState message={error} onRetry={load} /> : (
        <>
          <section className="stats-grid" aria-label="Employee statistics">
            <article className="stat-card"><span className="stat-icon blue"><UsersRound /></span><div><p>Total employees</p><strong>{employees.length}</strong></div></article>
            <article className="stat-card"><span className="stat-icon violet"><Building2 /></span><div><p>Departments</p><strong>{departments}</strong></div></article>
            <article className="stat-card"><span className="stat-icon green"><CalendarDays /></span><div><p>Recent hires</p><strong>{recent.length}</strong></div></article>
          </section>
          <div className="dashboard-grid">
          <section className="content-card">
            <div className="section-heading"><div><h2>Recently joined</h2><p>Latest employees by hire date</p></div><Link className="text-link" to="/employees">View all <ArrowRight size={16} /></Link></div>
            {employees.length === 0 ? <EmptyState /> : recent.length === 0 ? <p className="inline-empty">Add hire dates to see recent employees here.</p> : (
              <div className="recent-list">{recent.map((employee) => (
                <Link className="recent-row" to={`/employees/${employee.id}`} key={employee.id}>
                  <span className="avatar"><UserRound /></span><span className="recent-person"><strong>{employee.name}</strong><small>{employee.position} · {employee.department}</small></span><time dateTime={employee.hireDate}>{formatDate(employee.hireDate)}</time><ArrowRight size={18} aria-hidden="true" />
                </Link>
              ))}</div>
            )}
          </section>
          <section className="content-card department-card">
            <div className="section-heading"><div><h2>Department distribution</h2><p>Team members by department</p></div></div>
            {distribution.length === 0 ? <p className="inline-empty">No department data yet.</p> : <div className="distribution-list">{distribution.map(([department, count]) => (
              <div className="distribution-row" key={department}><div><strong>{department}</strong><span>{count} {count === 1 ? 'employee' : 'employees'}</span></div><div className="distribution-track" aria-hidden="true"><span style={{ width: `${(count / employees.length) * 100}%` }} /></div></div>
            ))}</div>}
          </section>
          </div>
        </>
      )}
    </div>
  )
}
