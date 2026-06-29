import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { Toaster } from 'react-hot-toast'
import { Login } from './pages/Login'
import { AppLayout } from './components/layout/AppLayout'
import { EmployeesPage } from './pages/employees/EmployeesPage'
import { VacationsPage } from './pages/vacations/VacationsPage'
import { CalendarPage } from './pages/calendar/CalendarPage'

const qc = new QueryClient({
  defaultOptions: { queries: { retry: 1, staleTime: 30_000 } },
})

export function App() {
  return (
    <QueryClientProvider client={qc}>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route element={<AppLayout />}>
            <Route index element={<Navigate to="/vacations" replace />} />
            <Route path="/vacations" element={<VacationsPage />} />
            <Route path="/employees" element={<EmployeesPage />} />
            <Route path="/calendar" element={<CalendarPage />} />
          </Route>
          <Route path="*" element={<Navigate to="/vacations" replace />} />
        </Routes>
      </BrowserRouter>
      <Toaster position="top-right" />
    </QueryClientProvider>
  )
}