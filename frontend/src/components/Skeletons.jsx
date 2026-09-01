function Bar({ width = '100%' }) {
  return <span className="skeleton-bar" style={{ width }} />
}

export function DashboardSkeleton() {
  return (
    <div className="skeleton-wrap" role="status" aria-live="polite" aria-label="Loading dashboard">
      <span className="sr-only">Loading dashboard</span>
      <section className="stats-grid" aria-hidden="true">{[1, 2, 3].map((item) => <article className="stat-card skeleton-stat" key={item}><span className="skeleton-circle" /><div><Bar width="96px" /><Bar width="52px" /></div></article>)}</section>
      <section className="content-card skeleton-panel" aria-hidden="true"><Bar width="170px" />{[1, 2, 3, 4].map((item) => <div className="skeleton-list-row" key={item}><span className="skeleton-circle small" /><div><Bar width="150px" /><Bar width="220px" /></div><Bar width="80px" /></div>)}</section>
    </div>
  )
}

export function EmployeesSkeleton() {
  return (
    <section className="employee-list skeleton-wrap" role="status" aria-live="polite" aria-label="Loading employees">
      <span className="sr-only">Loading employees</span>
      {[1, 2, 3, 4, 5, 6].map((item) => <div className="employee-list-row skeleton-employee" key={item} aria-hidden="true"><span className="skeleton-circle" /><div><Bar width="140px" /><Bar width="190px" /></div><Bar width="110px" /><Bar width="120px" /><Bar width="18px" /></div>)}
    </section>
  )
}

export function DetailsSkeleton() {
  return (
    <div className="skeleton-wrap" role="status" aria-live="polite" aria-label="Loading employee details">
      <span className="sr-only">Loading employee details</span>
      <section className="profile-card skeleton-profile" aria-hidden="true"><span className="skeleton-circle profile" /><div><Bar width="100px" /><Bar width="240px" /><Bar width="180px" /></div></section>
      <section className="details-card skeleton-panel" aria-hidden="true"><Bar width="190px" /><div className="details-grid">{[1,2,3,4,5,6].map((item) => <div className="skeleton-list-row" key={item}><span className="skeleton-circle small" /><div><Bar width="75px" /><Bar width="130px" /></div></div>)}</div></section>
    </div>
  )
}
