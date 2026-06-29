import { useEffect, useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import toast from 'react-hot-toast'
import { Plus, Check, X, Ban, Pencil } from 'lucide-react'
import { vacationsApi } from '../../api/vacations'
import { employeesApi } from '../../api/employees'
import type { VacationRequest, VacationStatus } from '../../types'
import type { ApiError } from '../../types'
import { useAuthStore } from '../../store/authStore'
import { Button } from '../../components/ui/Button'
import { StatusBadge } from '../../components/ui/Badge'
import { Pagination } from '../../components/ui/Pagination'
import { ConfirmDialog } from '../../components/ui/ConfirmDialog'
import { VacationForm } from './VacationForm'

type PendingAction =
  | { type: 'approve'; id: string; name: string }
  | { type: 'reject';  id: string; name: string }
  | { type: 'cancel';  id: string; name: string }

const STATUS_OPTIONS: { value: VacationStatus; label: string; color: string; active: string }[] = [
  { value: 'PENDING',   label: 'Pendente',  color: 'border-amber-200 text-amber-700 bg-white',  active: 'border-amber-400 bg-amber-100 text-amber-800' },
  { value: 'APPROVED',  label: 'Aprovado',  color: 'border-green-200 text-green-700 bg-white',  active: 'border-green-500 bg-green-100 text-green-800' },
  { value: 'REJECTED',  label: 'Rejeitado', color: 'border-red-200 text-red-600 bg-white',      active: 'border-red-400 bg-red-100 text-red-700' },
  { value: 'CANCELLED', label: 'Cancelado', color: 'border-gray-200 text-gray-500 bg-white',    active: 'border-gray-400 bg-gray-100 text-gray-700' },
]

export function VacationsPage() {
  const user = useAuthStore((s) => s.user)
  const role = user?.role ?? 'COLLABORATOR'
  const qc = useQueryClient()

  const [page, setPage] = useState(0)
  const [selectedStatuses, setSelectedStatuses] = useState<VacationStatus[]>([])
  const [nameInput, setNameInput] = useState('')
  const [nameFilter, setNameFilter] = useState('')
  const [managerFilter, setManagerFilter] = useState('')
  const [fromFilter, setFromFilter] = useState('')
  const [toFilter, setToFilter] = useState('')
  const [formOpen, setFormOpen] = useState(false)
  const [editing, setEditing] = useState<VacationRequest | null>(null)
  const [pendingAction, setPendingAction] = useState<PendingAction | null>(null)

  useEffect(() => {
    const t = setTimeout(() => { setNameFilter(nameInput); setPage(0) }, 400)
    return () => clearTimeout(t)
  }, [nameInput])

  const { data, isLoading } = useQuery({
    queryKey: ['vacations', page, selectedStatuses, nameFilter, managerFilter, fromFilter, toFilter],
    queryFn: () => vacationsApi.list({
      page, size: 10,
      status: selectedStatuses.length > 0 ? selectedStatuses : undefined,
      name: nameFilter || undefined,
      managerId: managerFilter || undefined,
      from: fromFilter || undefined,
      to: toFilter || undefined,
    }),
  })

  const { data: managersData } = useQuery({
    queryKey: ['managers-for-filter'],
    queryFn: () => employeesApi.list({ size: 200, sort: 'name', dir: 'asc' }),
    enabled: role === 'ADMIN',
  })
  const managers = (managersData?.content ?? []).filter((e) => e.role === 'MANAGER')

  const invalidate = () => qc.invalidateQueries({ queryKey: ['vacations'] })
  const onErr = (err: ApiError) => toast.error(err?.message ?? 'Erro')
  const dismiss = () => setPendingAction(null)

  const approveMut = useMutation({
    mutationFn: vacationsApi.approve,
    onSuccess: () => { invalidate(); toast.success('Férias aprovadas'); dismiss() },
    onError: (err: ApiError) => { onErr(err); dismiss() },
  })
  const rejectMut = useMutation({
    mutationFn: vacationsApi.reject,
    onSuccess: () => { invalidate(); toast.success('Férias rejeitadas'); dismiss() },
    onError: (err: ApiError) => { onErr(err); dismiss() },
  })
  const cancelMut = useMutation({
    mutationFn: vacationsApi.cancel,
    onSuccess: () => { invalidate(); toast.success('Férias canceladas'); dismiss() },
    onError: (err: ApiError) => { onErr(err); dismiss() },
  })

  const canApproveReject = role === 'ADMIN' || role === 'MANAGER'
  const isAdmin = role === 'ADMIN'

  const toggleStatus = (s: VacationStatus) => {
    setSelectedStatuses((prev) => prev.includes(s) ? prev.filter((x) => x !== s) : [...prev, s])
    setPage(0)
  }

  const openCreate = () => { setEditing(null); setFormOpen(true) }
  const openEdit   = (v: VacationRequest) => { setEditing(v); setFormOpen(true) }
  const fmt = (d: string) => new Date(d + 'T00:00:00').toLocaleDateString('pt-BR')

  const confirmLabel = (action: PendingAction | null) => {
    if (!action) return {}
    const who = action.name ? `de ${action.name}` : ''
    if (action.type === 'approve') return { title: 'Aprovar férias', message: `Deseja aprovar as férias ${who}?`, label: 'Aprovar', variant: 'primary' as const }
    if (action.type === 'reject')  return { title: 'Rejeitar férias', message: `Deseja rejeitar as férias ${who}?`, label: 'Rejeitar', variant: 'danger' as const }
    return { title: 'Cancelar férias', message: `Deseja cancelar as férias ${who}? Esta ação não pode ser desfeita.`, label: 'Cancelar férias', variant: 'danger' as const }
  }

  const handleConfirm = () => {
    if (!pendingAction) return
    if (pendingAction.type === 'approve') approveMut.mutate(pendingAction.id)
    if (pendingAction.type === 'reject')  rejectMut.mutate(pendingAction.id)
    if (pendingAction.type === 'cancel')  cancelMut.mutate(pendingAction.id)
  }

  const confirm = confirmLabel(pendingAction)
  const isPending = approveMut.isPending || rejectMut.isPending || cancelMut.isPending

  const hasFilters = selectedStatuses.length > 0 || nameInput || managerFilter || fromFilter || toFilter
  const clearFilters = () => {
    setSelectedStatuses([])
    setNameInput(''); setNameFilter('')
    setManagerFilter('')
    setFromFilter(''); setToFilter('')
    setPage(0)
  }

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">Férias</h1>
        <Button onClick={openCreate}><Plus size={16} /> Solicitar</Button>
      </div>

      {}
      <div className="mb-4 flex flex-wrap items-end gap-3">
        {}
        <div className="flex flex-col gap-1">
          <label className="text-xs font-medium uppercase text-gray-500">Status</label>
          <div className="flex gap-1">
            {STATUS_OPTIONS.map((s) => (
              <button
                key={s.value}
                onClick={() => toggleStatus(s.value)}
                className={`rounded-full border px-3 py-1 text-xs font-medium transition-colors ${
                  selectedStatuses.includes(s.value) ? s.active : s.color + ' hover:bg-gray-50'
                }`}
              >
                {s.label}
              </button>
            ))}
          </div>
        </div>

        {}
        {canApproveReject && (
          <div className="flex flex-col gap-1">
            <label className="text-xs font-medium uppercase text-gray-500">Colaborador</label>
            <input
              type="text"
              placeholder="Buscar por nome..."
              className="rounded-md border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-1 focus:ring-indigo-500"
              value={nameInput}
              onChange={(e) => setNameInput(e.target.value)}
            />
          </div>
        )}

        {}
        {isAdmin && (
          <div className="flex flex-col gap-1">
            <label className="text-xs font-medium uppercase text-gray-500">Manager</label>
            <select
              className="rounded-md border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-1 focus:ring-indigo-500"
              value={managerFilter}
              onChange={(e) => { setManagerFilter(e.target.value); setPage(0) }}
            >
              <option value="">Todos</option>
              {managers.map((m) => (
                <option key={m.id} value={m.id}>{m.name}</option>
              ))}
            </select>
          </div>
        )}

        {}
        <div className="flex flex-col gap-1">
          <label className="text-xs font-medium uppercase text-gray-500">Período — início</label>
          <input
            type="date"
            className="rounded-md border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-1 focus:ring-indigo-500"
            value={fromFilter}
            max={toFilter || undefined}
            onChange={(e) => { setFromFilter(e.target.value); setPage(0) }}
          />
        </div>

        <div className="flex flex-col gap-1">
          <label className="text-xs font-medium uppercase text-gray-500">Período — fim</label>
          <input
            type="date"
            className="rounded-md border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-1 focus:ring-indigo-500"
            value={toFilter}
            min={fromFilter || undefined}
            onChange={(e) => { setToFilter(e.target.value); setPage(0) }}
          />
        </div>

        {hasFilters && (
          <button
            className="self-end pb-2 text-xs text-indigo-600 hover:underline"
            onClick={clearFilters}
          >
            Limpar filtros
          </button>
        )}
      </div>

      {}
      <div className="overflow-hidden rounded-xl border border-gray-200 bg-white shadow-sm">
        <table className="w-full text-sm">
          <thead className="bg-gray-50 text-xs uppercase text-gray-500">
            <tr>
              {canApproveReject && <th className="px-4 py-3 text-left">Colaborador</th>}
              {isAdmin && <th className="px-4 py-3 text-left">Manager</th>}
              <th className="px-4 py-3 text-left">Início</th>
              <th className="px-4 py-3 text-left">Fim</th>
              <th className="px-4 py-3 text-left">Status</th>
              <th className="px-4 py-3 text-left">Motivo</th>
              <th className="px-4 py-3 text-right">Ações</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {isLoading && (
              <tr><td colSpan={7} className="px-4 py-8 text-center text-gray-400">Carregando...</td></tr>
            )}
            {!isLoading && data?.content.length === 0 && (
              <tr><td colSpan={7} className="px-4 py-8 text-center text-gray-400">Nenhuma solicitação encontrada.</td></tr>
            )}
            {data?.content.map((v) => (
              <tr key={v.id} className="hover:bg-gray-50">
                {canApproveReject && (
                  <td className="px-4 py-3 font-medium text-gray-900">{v.employeeName ?? '-'}</td>
                )}
                {isAdmin && (
                  <td className="px-4 py-3 text-gray-500">{v.managerName ?? '-'}</td>
                )}
                <td className="px-4 py-3">{fmt(v.startDate)}</td>
                <td className="px-4 py-3">{fmt(v.endDate)}</td>
                <td className="px-4 py-3"><StatusBadge status={v.status} /></td>
                <td className="px-4 py-3 max-w-xs truncate text-gray-500">{v.reason ?? '-'}</td>
                <td className="px-4 py-3">
                  <div className="flex justify-end gap-1">
                    {v.status === 'PENDING' && v.employeeId === user?.id && (
                      <Button variant="ghost" size="sm" onClick={() => openEdit(v)}>
                        <Pencil size={14} />
                      </Button>
                    )}
                    {v.status === 'PENDING' && canApproveReject && (
                      <>
                        <Button
                          variant="ghost" size="sm"
                          className="text-green-600 hover:bg-green-50"
                          title="Aprovar"
                          onClick={() => setPendingAction({ type: 'approve', id: v.id, name: v.employeeName ?? '' })}
                        >
                          <Check size={14} />
                        </Button>
                        <Button
                          variant="ghost" size="sm"
                          className="text-red-500 hover:bg-red-50"
                          title="Rejeitar"
                          onClick={() => setPendingAction({ type: 'reject', id: v.id, name: v.employeeName ?? '' })}
                        >
                          <X size={14} />
                        </Button>
                      </>
                    )}
                    {(v.status === 'PENDING' || v.status === 'APPROVED') &&
                      (v.employeeId === user?.id || role === 'ADMIN') && (
                        <Button
                          variant="ghost" size="sm"
                          className="text-gray-500 hover:bg-gray-100"
                          title="Cancelar"
                          onClick={() => setPendingAction({ type: 'cancel', id: v.id, name: v.employeeName ?? v.employeeId })}
                        >
                          <Ban size={14} />
                        </Button>
                    )}
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <Pagination page={page} totalPages={data?.totalPages ?? 0} onPageChange={setPage} />

      <VacationForm open={formOpen} onClose={() => setFormOpen(false)} editing={editing} />

      <ConfirmDialog
        open={!!pendingAction}
        title={confirm.title ?? ''}
        message={confirm.message ?? ''}
        confirmLabel={isPending ? 'Aguarde...' : confirm.label}
        confirmVariant={confirm.variant ?? 'primary'}
        onConfirm={handleConfirm}
        onCancel={dismiss}
      />
    </div>
  )
}