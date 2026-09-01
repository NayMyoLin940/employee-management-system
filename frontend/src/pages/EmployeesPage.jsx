import { ChevronRight, Plus, Search, UserRound } from 'lucide-react'
import { useCallback, useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { employeeApi, getApiError } from '../api/employeeApi'
import { EmptyState, ErrorState } from '../components/Feedback'
import { EmployeesSkeleton } from '../components/Skeletons'
import { useToast } from '../components/toastContext'

export default function EmployeesPage() {
  const { showToast } = useToast()
  const [employees, setEmployees] = useState([])
  const [query, setQuery] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const load = useCallback(async () => {
    setLoading(true); setError('')
    try { setEmployees(await employeeApi.getAll()) }
    catch (requestError) { const message = getApiError(requestError).message; setError(message); showToast(message, 'error') }
    finally { setLoading(false) }
  }, [showToast])
  useEffect(() => { void Promise.resolve().then(load) }, [load])
  const filtered = useMemo(() => {
    const needle = query.trim().toLowerCase()
    if (!needle) return employees
    return employees.filter((employee) => [employee.name, employee.email, employee.department, employee.position].some((value) => value?.toLowerCase().includes(needle)))
  }, [employees, query])

  return (
    <div className="page">
      <header className="page-header actions-header"><div><p className="eyebrow">Directory</p><h1>Employees</h1><p className="subtitle">Find and manage everyone on your team.</p></div><Link className="button primary" to="/employees/new"><Plus size={18} /> Add employee</Link></header>
      <div className="toolbar"><label className="search-box"><Search size={19} aria-hidden="true" /><span className="sr-only">Search employees</span><input type="search" value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Search by name, email, department, or position" /></label>{!loading && !error && <span className="result-count">{filtered.length} {filtered.length === 1 ? 'employee' : 'employees'}</span>}</div>
      {loading ? <EmployeesSkeleton /> : error ? <ErrorState message={error} onRetry={load} /> : filtered.length === 0 ? <EmptyState filtered={Boolean(query.trim())} /> : (
        <section className="employee-list" aria-label="Employee list">
          <div className="employee-list-head" aria-hidden="true"><span>Employee</span><span>Department</span><span>Position</span><span /></div>
          {filtered.map((employee) => <Link className="employee-list-row" to={`/employees/${employee.id}`} key={employee.id}>
            <span className="employee-identity"><span className="avatar"><UserRound /></span><span><strong>{employee.name}</strong><small>{employee.email}</small></span></span>
            <span className="department-pill">{employee.department}</span><span className="employee-position">{employee.position}</span><ChevronRight className="card-arrow" size={20} aria-hidden="true" />
          </Link>)}
        </section>
      )}
    </div>
  )
}
