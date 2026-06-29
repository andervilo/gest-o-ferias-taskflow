import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import toast from 'react-hot-toast'
import { vacationsApi } from '../../api/vacations'
import { employeesApi } from '../../api/employees'
import type { VacationRequest } from '../../types'
import type { ApiError } from '../../types'
import { useAuthStore } from '../../store/authStore'
import { Input } from '../../components/ui/Input'
import { Button } from '../../components/ui/Button'
import { Modal } from '../../components/ui/Modal'

const today = new Date().toISOString().slice(0, 10)

const schema = z.object({
  startDate: z.string()
    .min(1, 'Data de início obrigatória')
    .refine((d) => d >= today, { message: 'Data de início não pode ser no passado' }),
  endDate: z.string()
    .min(1, 'Data de fim obrigatória'),
  reason: z.string().optional(),
})
  .refine((d) => d.endDate >= d.startDate, {
    message: 'Data de fim deve ser igual ou posterior à data de início',
    path: ['endDate'],
  })

type FormData = z.infer<typeof schema>

interface Props {
  open: boolean
  onClose: () => void
  editing?: VacationRequest | null
}

export function VacationForm({ open, onClose, editing }: Props) {
  const user = useAuthStore((s) => s.user)
  const isAdmin = user?.role === 'ADMIN'
  const isEdit = !!editing
  const qc = useQueryClient()

  const [targetEmployeeId, setTargetEmployeeId] = useState<string>('')

  const { data: employeesData } = useQuery({
    queryKey: ['employees-all'],
    queryFn: () => employeesApi.list({ size: 200, sort: 'name', dir: 'asc' }),
    enabled: isAdmin && !isEdit && open,
  })
  const employees = employeesData?.content ?? []

  const { register, handleSubmit, formState: { errors }, reset } = useForm<FormData>({
    resolver: zodResolver(schema),
    values: editing
      ? { startDate: editing.startDate, endDate: editing.endDate, reason: editing.reason ?? '' }
      : undefined,
  })

  const { mutate, isPending } = useMutation({
    mutationFn: (data: FormData) =>
      isEdit
        ? vacationsApi.update(editing!.id, data)
        : vacationsApi.create({
            ...data,
            employeeId: isAdmin && targetEmployeeId ? targetEmployeeId : undefined,
          }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['vacations'] })
      toast.success(isEdit ? 'Férias atualizadas' : 'Solicitação criada')
      reset()
      setTargetEmployeeId('')
      onClose()
    },
    onError: (err: ApiError) => toast.error(err?.message ?? 'Erro ao salvar'),
  })

  const handleClose = () => {
    reset()
    setTargetEmployeeId('')
    onClose()
  }

  return (
    <Modal title={isEdit ? 'Editar Férias' : 'Solicitar Férias'} open={open} onClose={handleClose}>
      <form onSubmit={handleSubmit((d) => mutate(d))} className="flex flex-col gap-4">

        {isAdmin && !isEdit && (
          <div className="flex flex-col gap-1">
            <label className="text-sm font-medium text-gray-700">Solicitando para</label>
            <select
              className="rounded-md border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-1 focus:ring-indigo-500"
              value={targetEmployeeId}
              onChange={(e) => setTargetEmployeeId(e.target.value)}
            >
              <option value="">Minhas férias</option>
              {employees.map((emp) => (
                <option key={emp.id} value={emp.id}>
                  {emp.name} ({emp.role})
                </option>
              ))}
            </select>
          </div>
        )}

        <Input
          label="Data de início"
          type="date"
          min={today}
          error={errors.startDate?.message}
          {...register('startDate')}
        />
        <Input
          label="Data de fim"
          type="date"
          min={today}
          error={errors.endDate?.message}
          {...register('endDate')}
        />
        <div className="flex flex-col gap-1">
          <label className="text-sm font-medium text-gray-700">Motivo (opcional)</label>
          <textarea
            className="rounded-md border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-1 focus:ring-indigo-500"
            rows={3}
            {...register('reason')}
          />
        </div>
        <div className="flex justify-end gap-2 pt-2">
          <Button type="button" variant="secondary" onClick={handleClose}>Cancelar</Button>
          <Button type="submit" disabled={isPending}>{isPending ? 'Salvando...' : 'Salvar'}</Button>
        </div>
      </form>
    </Modal>
  )
}
