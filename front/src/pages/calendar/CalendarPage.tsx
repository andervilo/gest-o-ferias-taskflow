import { useState } from 'react'
import FullCalendar from '@fullcalendar/react'
import dayGridPlugin from '@fullcalendar/daygrid'
import { useQuery } from '@tanstack/react-query'
import ptBrLocale from '@fullcalendar/core/locales/pt-br'
import { vacationsApi } from '../../api/vacations'

const statusColor: Record<string, string> = {
  APPROVED:  '#16a34a',
  PENDING:   '#d97706',
  REJECTED:  '#dc2626',
  CANCELLED: '#6b7280',
}

export function CalendarPage() {
  const now = new Date()
  const [from, setFrom] = useState(`${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-01`)
  const lastDay = new Date(now.getFullYear(), now.getMonth() + 1, 0).getDate()
  const [to, setTo]   = useState(`${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${lastDay}`)

  const { data = [] } = useQuery({
    queryKey: ['calendar', from, to],
    queryFn: () => vacationsApi.calendar(from, to),
  })

  const events = data.map((e) => ({
    id: e.id,
    title: e.employeeName ?? 'Colaborador',
    start: e.startDate,
    end: e.endDate,
    allDay: true,
    backgroundColor: statusColor[e.status] ?? '#6366f1',
    borderColor: statusColor[e.status] ?? '#6366f1',
  }))

  const handleDatesSet = (info: { startStr: string; endStr: string }) => {
    setFrom(info.startStr.slice(0, 10))
    setTo(info.endStr.slice(0, 10))
  }

  return (
    <div>
      <h1 className="mb-6 text-2xl font-bold text-gray-900">Calendário de Férias</h1>
      <div className="rounded-xl border border-gray-200 bg-white p-4 shadow-sm">
        <FullCalendar
          plugins={[dayGridPlugin]}
          initialView="dayGridMonth"
          locale={ptBrLocale}
          events={events}
          datesSet={handleDatesSet}
          headerToolbar={{ left: 'prev,next today', center: 'title', right: 'dayGridMonth,dayGridWeek' }}
          height="auto"
        />
      </div>
      <div className="mt-4 flex gap-4 text-sm">
        {Object.entries({ APPROVED: 'Aprovado', PENDING: 'Pendente' }).map(([k, label]) => (
          <span key={k} className="flex items-center gap-1.5">
            <span className="inline-block h-3 w-3 rounded-full" style={{ backgroundColor: statusColor[k] }} />
            {label}
          </span>
        ))}
      </div>
    </div>
  )
}