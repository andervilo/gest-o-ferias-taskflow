import { useMemo } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import toast from 'react-hot-toast'
import { employeesApi } from '../../api/employees'
import type { Employee, Role } from '../../types'
import type { ApiError } from '../../types'
import { Input } from '../../components/ui/Input'
import { Select } from '../../components/ui/Select'
import { Button } from '../../components/ui/Button'
import { Modal } from '../../components/ui/Modal'

const createSchema = z
  .object({
    name: z.string().min(2, 'Nome muito curto'),
    email: z.string().email('E-mail inválido'),
    password: z.string().min(6, 'Mínimo 6 caracteres'),
    role: z.enum(['ADMIN', 'MANAGER', 'COLLABORATOR']),
    managerId: z.string().optional(),
  })
  .superRefine((data, ctx) => {
    if (data.role === 'COLLABORATOR' && !data.managerId) {
      ctx.addIssue({ code: z.ZodIssueCode.custom, path: ['managerId'], message: 'Colaborador deve ter um gerente' })
    }
    if (data.role !== 'COLLABORATOR' && data.managerId) {
      ctx.addIssue({ code: z.ZodIssueCode.custom, path: ['managerId'], message: 'Admin e Gerente não têm gerente' })
    }
  })

type CreateData = z.infer<typeof createSchema>

interface Props {
  open: boolean
  onClose: () => void
  editing?: Employee | null
}

function CreateForm({ onClose, managers }: { onClose: () => void; managers: Employee[] }) {
  const qc = useQueryClient()
  const { register, handleSubmit, watch, formState: { errors } } = useForm<CreateData>({
    resolver: zodResolver(createSchema),
    defaultValues: { role: 'COLLABORATOR' },
  })
  const role = watch('role')
  const managerRequired = role === 'COLLABORATOR'

  const { mutate, isPending } = useMutation({
    mutationFn: (data: CreateData) =>
      employeesApi.create({ name: data.name, email: data.email, password: data.password, role: data.role, managerId: data.managerId || null }),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['employees'] }); toast.success('Colaborador criado'); onClose() },
    onError: (err: ApiError) => toast.error(err?.message ?? 'Erro ao salvar'),
  })
  return (
    <form onSubmit={handleSubmit((d) => mutate(d))} className="flex flex-col gap-4" autoComplete="off">
      <Input label="Nome" autoComplete="off" error={errors.name?.message} {...register('name')} />
      <Input label="E-mail" type="email" autoComplete="off" error={errors.email?.message} {...register('email')} />
      <Input label="Senha" type="password" autoComplete="new-password" error={errors.password?.message} {...register('password')} />
      <Select label="Papel" error={errors.role?.message} {...register('role')}>
        <option value="COLLABORATOR">Colaborador</option>
        <option value="MANAGER">Gerente</option>
        <option value="ADMIN">Admin</option>
      </Select>
      <Select
        label={managerRequired ? 'Gerente' : 'Gerente (opcional)'}
        error={errors.managerId?.message}
        {...register('managerId')}
      >
        <option value="">— Nenhum —</option>
        {managers.map((m) => <option key={m.id} value={m.id}>{m.name}</option>)}
      </Select>
      <div className="flex justify-end gap-2 pt-2">
        <Button type="button" variant="secondary" onClick={onClose}>Cancelar</Button>
        <Button type="submit" disabled={isPending}>{isPending ? 'Salvando…' : 'Salvar'}</Button>
      </div>
    </form>
  )
}

function EditForm({ onClose, editing, managers }: { onClose: () => void; editing: Employee; managers: Employee[] }) {
  const qc = useQueryClient()
  const managerRequired = editing.role === 'COLLABORATOR'

  const editSchema = useMemo(
    () =>
      z
        .object({
          name: z.string().min(2, 'Nome muito curto'),
          email: z.string().email('E-mail inválido'),
          managerId: z.string().optional(),
        })
        .superRefine((data, ctx) => {
          if (managerRequired && !data.managerId) {
            ctx.addIssue({ code: z.ZodIssueCode.custom, path: ['managerId'], message: 'Colaborador deve ter um gerente' })
          }
        }),
    [managerRequired],
  )
  type EditData = z.infer<typeof editSchema>

  const { register, handleSubmit, formState: { errors } } = useForm<EditData>({
    resolver: zodResolver(editSchema),
    values: { name: editing.name, email: editing.email, managerId: editing.managerId ?? '' },
  })
  const { mutate, isPending } = useMutation({
    mutationFn: (data: EditData) =>
      employeesApi.update(editing.id, { name: data.name, email: data.email, managerId: data.managerId || null }),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['employees'] }); toast.success('Colaborador atualizado'); onClose() },
    onError: (err: ApiError) => toast.error(err?.message ?? 'Erro ao salvar'),
  })
  return (
    <form onSubmit={handleSubmit((d) => mutate(d))} className="flex flex-col gap-4">
      <Input label="Nome" error={errors.name?.message} {...register('name')} />
      <Input label="E-mail" type="email" error={errors.email?.message} {...register('email')} />
      <Select
        label={managerRequired ? 'Gerente' : 'Gerente (opcional)'}
        error={errors.managerId?.message}
        {...register('managerId')}
      >
        <option value="">— Nenhum —</option>
        {managers.map((m) => <option key={m.id} value={m.id}>{m.name}</option>)}
      </Select>
      <div className="flex justify-end gap-2 pt-2">
        <Button type="button" variant="secondary" onClick={onClose}>Cancelar</Button>
        <Button type="submit" disabled={isPending}>{isPending ? 'Salvando…' : 'Salvar'}</Button>
      </div>
    </form>
  )
}

export function EmployeeForm({ open, onClose, editing }: Props) {
  const { data: managers } = useQuery({
    queryKey: ['employees', 'managers'],
    queryFn: () => employeesApi.list({ role: 'MANAGER' as Role, size: 100 }),
    enabled: open,
  })
  const managerList = managers?.content ?? []

  return (
    <Modal title={editing ? 'Editar Colaborador' : 'Novo Colaborador'} open={open} onClose={onClose}>
      {editing
        ? <EditForm onClose={onClose} editing={editing} managers={managerList} />
        : <CreateForm onClose={onClose} managers={managerList} />
      }
    </Modal>
  )
}