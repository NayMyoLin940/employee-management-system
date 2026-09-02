import {
  LayoutDashboard,
  MessageCircle,
  UsersRound,
} from 'lucide-react'
import { NavLink, Outlet } from 'react-router-dom'
import { ToastProvider } from './Toast'

const navigation = [
  {
    to: '/dashboard',
    label: 'Dashboard',
    icon: LayoutDashboard,
  },
  {
    to: '/employees',
    label: 'Employees',
    icon: UsersRound,
  },
  {
    to: '/ai-chat',
    label: 'AI Chat',
    icon: MessageCircle,
  },
]

function Navigation({ mobile = false }) {
  return (
    <nav
      className={mobile ? 'mobile-nav' : 'side-nav'}
      aria-label="Primary navigation"
    >
      {navigation.map(({ to, label, icon: Icon }) => (
        <NavLink
          key={to}
          to={to}
          className={({ isActive }) =>
            isActive ? 'nav-link active' : 'nav-link'
          }
        >
          <Icon size={20} strokeWidth={2} aria-hidden="true" />
          <span>{label}</span>
        </NavLink>
      ))}
    </nav>
  )
}

export default function AppLayout() {
  return (
    <div className="app-shell">
      <aside className="sidebar">
        <NavLink
          className="brand"
          to="/dashboard"
          aria-label="People home"
        >
          <span className="brand-mark">P</span>
          <span>People</span>
        </NavLink>

        <Navigation />

        <p className="sidebar-note">Employee workspace</p>
      </aside>

      <ToastProvider>
        <main className="main-content">
          <Outlet />
        </main>
      </ToastProvider>

      <Navigation mobile />
    </div>
  )
}