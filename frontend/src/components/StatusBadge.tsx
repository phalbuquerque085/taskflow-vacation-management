'use client';

import React from 'react';
import { VacationStatus } from '@/types';

const statusMap: Record<VacationStatus, { label: string; className: string }> = {
  PENDING: { label: 'Pendente', className: 'bg-amber-500/10 text-amber-400 border-amber-500/20' },
  APPROVED: { label: 'Aprovado', className: 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20' },
  REJECTED: { label: 'Rejeitado', className: 'bg-rose-500/10 text-rose-400 border-rose-500/20' },
  CANCELLED: { label: 'Cancelado', className: 'bg-slate-500/10 text-slate-400 border-slate-500/20' },
};

export const StatusBadge: React.FC<{ status: VacationStatus }> = ({ status }) => {
  const current = statusMap[status] || { label: status, className: 'bg-slate-800 text-slate-300' };

  return (
    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border ${current.className}`}>
      {current.label}
    </span>
  );
};