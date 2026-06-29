import { NavLink } from 'react-router-dom'
import { CalendarDays, Users, ClipboardList, LogOut } from 'lucide-react'
import { useAuthStore } from '../../store/authStore'

const navItems = [
  { to: '/vacations', label: 'Férias', icon: ClipboardList, roles: ['ADMIN', 'MANAGER', 'COLLABORATOR'] },
  { to: '/employees', label: 'Colaboradores', icon: Users, roles: ['ADMIN', 'MANAGER'] },
  { to: '/calendar', label: 'Calendário', icon: CalendarDays, roles: ['ADMIN', 'MANAGER', 'COLLABORATOR'] },
] as const

export function Sidebar() {
  const { user, logout } = useAuthStore()
  const role = user?.role ?? 'COLLABORATOR'

  return (
    <aside className="flex h-screen w-56 flex-col bg-indigo-900 text-white">
      <div className="px-4 py-5 border-b border-indigo-800">
        <p className="text-xs font-semibold uppercase tracking-widest text-indigo-300">TaskFlow</p>
        <p className="mt-1 text-sm font-medium truncate">{user?.name}</p>
        <span className="inline-block mt-1 rounded bg-indigo-700 px-1.5 py-0.5 text-xs">{role}</span>
      </div>

      <nav className="flex-1 space-y-1 px-2 py-4">
        {navItems
          .filter((item) => (item.roles as readonly string[]).includes(role))
          .map(({ to, label, icon: Icon }) => (
            <NavLink
              key={to}
              to={to}
              className={({ isActive }) =>
                `flex items-center gap-3 rounded-lg px-3 py-2 text-sm transition-colors ${
                  isActive ? 'bg-indigo-700 text-white' : 'text-indigo-200 hover:bg-indigo-800 hover:text-white'
                }`
              }
            >
              <Icon size={16} />
              {label}
            </NavLink>
          ))}
      </nav>

      <button
        onClick={logout}
        className="flex items-center gap-3 px-5 py-4 text-sm text-indigo-300 hover:text-white border-t border-indigo-800 transition-colors"
      >
        <LogOut size={16} />
        Sair
      </button>
    </aside>
  )
}