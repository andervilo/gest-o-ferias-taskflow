import { ChevronLeft, ChevronRight } from 'lucide-react'
import { Button } from './Button'

interface Props {
  page: number
  totalPages: number
  onPageChange: (p: number) => void
}

export function Pagination({ page, totalPages, onPageChange }: Props) {
  if (totalPages === 0) return null
  return (
    <div className="flex items-center justify-end gap-2 pt-4">
      <Button variant="secondary" size="sm" disabled={page === 0} onClick={() => onPageChange(page - 1)}>
        <ChevronLeft size={14} />
      </Button>
      <span className="text-sm text-gray-600">
        {page + 1} / {totalPages}
      </span>
      <Button variant="secondary" size="sm" disabled={page >= totalPages - 1} onClick={() => onPageChange(page + 1)}>
        <ChevronRight size={14} />
      </Button>
    </div>
  )
}