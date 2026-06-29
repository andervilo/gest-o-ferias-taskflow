import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import toast from 'react-hot-toast'
import { Plus, Pencil, Trash2 } from 'lucide-react'
import { employeesApi } from '../../api/employees'
import type { Employee } from '../../types'
import type { ApiError } from '../../types'
import { useAuthStore } from '../../store/authStore'
import { Button } from '../../components/ui/Button'
import { Pagination } from '../../components/ui/Pagination'
import { EmployeeForm } from './EmployeeForm'

const roleLabel: Record<string, string> = {
  ADMIN: 'Admin',
  MANAGER: 'Gerente',
  COLLABORATOR: 'Colaborador',
}

export function EmployeesPage() {
  const role = useAuthStore((s) => s.user?.role)
  const isAdmin = role === 'ADMIN'
  const qc = useQueryClient()

  const [page, setPage] = useState(0)
  const [search, setSearch] = useState('')
  const [formOpen, setFormOpen] = useState(false)
  const [editing, setEditing] = useState<Employee | null>(null)

  const { data, isLoading } = useQuery({
    queryKey: ['employees', page, search],
    queryFn: () => employeesApi.list({ page, size: 10, q: search || undefined }),
  })

  const { mutate: remove } = useMutation({
    mutationFn: employeesApi.remove,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['employees'] })
      toast.success('Colaborador removido')
    },
    onError: (err: ApiError) => toast.error(err?.message ?? 'Erro ao remover'),
  })

  const openCreate = () => { setEditing(null); setFormOpen(true) }
  const openEdit = (e: Employee) => { setEditing(e); setFormOpen(true) }

  const confirmRemove = (e: Employee) => {
    if (window.confirm(`Remover ${e.name}?`)) remove(e.id)
  }

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">Colaboradores</h1>
        {isAdmin && (
          <Button onClick={openCreate}>
            <Plus size={16} /> Novo
          </Button>
        )}
      </div>

      <div className="mb-4">
        <input
          className="rounded-md border border-gray-300 px-3 py-2 text-sm w-64 focus:outline-none focus:ring-1 focus:ring-indigo-500"
          placeholder="Buscar por nome ou e-mail…"
          value={search}
          onChange={(e) => { setSearch(e.target.value); setPage(0) }}
        />
      </div>

      <div className="overflow-hidden rounded-xl border border-gray-200 bg-white shadow-sm">
        <table className="w-full text-sm">
          <thead className="bg-gray-50 text-gray-500 uppercase text-xs">
            <tr>
              <th className="px-4 py-3 text-left">Nome</th>
              <th className="px-4 py-3 text-left">E-mail</th>
              <th className="px-4 py-3 text-left">Papel</th>
              {isAdmin && <th className="px-4 py-3 text-right">Ações</th>}
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {isLoading && (
              <tr><td colSpan={4} className="px-4 py-8 text-center text-gray-400">Carregando…</td></tr>
            )}
            {!isLoading && data?.content.length === 0 && (
              <tr><td colSpan={4} className="px-4 py-8 text-center text-gray-400">Nenhum colaborador encontrado.</td></tr>
            )}
            {data?.content.map((emp) => (
              <tr key={emp.id} className="hover:bg-gray-50">
                <td className="px-4 py-3 font-medium text-gray-900">{emp.name}</td>
                <td className="px-4 py-3 text-gray-600">{emp.email}</td>
                <td className="px-4 py-3 text-gray-600">{roleLabel[emp.role] ?? emp.role}</td>
                {isAdmin && (
                  <td className="px-4 py-3 text-right">
                    <div className="flex justify-end gap-2">
                      <Button variant="ghost" size="sm" onClick={() => openEdit(emp)}><Pencil size={14} /></Button>
                      <Button variant="ghost" size="sm" className="text-red-500 hover:bg-red-50" onClick={() => confirmRemove(emp)}><Trash2 size={14} /></Button>
                    </div>
                  </td>
                )}
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <Pagination page={page} totalPages={data?.totalPages ?? 0} onPageChange={setPage} />

      <EmployeeForm open={formOpen} onClose={() => setFormOpen(false)} editing={editing} />
    </div>
  )
}