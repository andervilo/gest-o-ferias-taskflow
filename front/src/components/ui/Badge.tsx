import type { VacationStatus } from '../../types'

const colorMap: Record<VacationStatus, string> = {
  PENDING:   'bg-yellow-100 text-yellow-800',
  APPROVED:  'bg-green-100  text-green-800',
  REJECTED:  'bg-red-100    text-red-800',
  CANCELLED: 'bg-gray-100   text-gray-600',
}

const labelMap: Record<VacationStatus, string> = {
  PENDING:   'Pendente',
  APPROVED:  'Aprovado',
  REJECTED:  'Rejeitado',
  CANCELLED: 'Cancelado',
}

export function StatusBadge({ status }: { status: VacationStatus }) {
  return (
    <span className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ${colorMap[status]}`}>
      {labelMap[status]}
    </span>
  )
}
