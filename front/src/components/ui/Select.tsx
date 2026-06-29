import type { SelectHTMLAttributes } from 'react'
import { forwardRef } from 'react'

interface Props extends SelectHTMLAttributes<HTMLSelectElement> {
  label?: string
  error?: string
}

export const Select = forwardRef<HTMLSelectElement, Props>(({ label, error, className = '', children, ...rest }, ref) => (
  <div className="flex flex-col gap-1">
    {label && <label className="text-sm font-medium text-gray-700">{label}</label>}
    <select
      ref={ref}
      className={`rounded-md border border-gray-300 px-3 py-2 text-sm shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500 disabled:bg-gray-50 ${error ? 'border-red-500' : ''} ${className}`}
      {...rest}
    >
      {children}
    </select>
    {error && <p className="text-xs text-red-600">{error}</p>}
  </div>
))
Select.displayName = 'Select'