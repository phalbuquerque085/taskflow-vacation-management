'use client';

import React, { useState, useEffect, useCallback } from 'react';
import { useUser } from '@/context/UserContext';
import { VacationRequest, VacationStatus } from '@/types';
import { api } from '@/services/api';
import { StatusBadge } from '@/components/StatusBadge';
import { Check, X, ShieldAlert } from 'lucide-react';
import { format, parseISO } from 'date-fns';

export default function ApprovalsPage() {
  const { currentUser } = useUser();
  const [requests, setRequests] = useState<VacationRequest[]>([]);
  const [loading, setLoading] = useState(true);

  const fetchApprovals = useCallback(async () => {
    if (!currentUser) return;
    try {
      setLoading(true);
      const res = await api.get<VacationRequest[]>('/vacations');
      setRequests(res.data);
    } catch (err: any) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  }, [currentUser]);

  useEffect(() => {
    fetchApprovals();
  }, [fetchApprovals]);

  const handleUpdateStatus = async (id: number, status: VacationStatus) => {
    try {
      await api.patch(`/vacations/${id}/status`, { status });
      fetchApprovals();
    } catch (err: any) {
      const msg = err.response?.data?.message || 'Erro ao processar alteração de status.';
      alert(msg);
    }
  };

  if (!currentUser || currentUser.role === 'COLLABORATOR') {
    return (
      <div className="bg-slate-900 border border-slate-800 rounded-xl p-8 text-center max-w-md mx-auto my-12">
        <ShieldAlert size={48} className="mx-auto text-amber-400 mb-4" />
        <h2 className="text-lg font-bold text-white mb-2">Acesso Restrito</h2>
        <p className="text-sm text-slate-400">
          Apenas Gestores e Administradores possuem permissão para aprovar ou rejeitar solicitações de férias.
        </p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold tracking-tight text-white">Painel de Aprovações</h1>
        <p className="text-slate-400 text-sm mt-1">
          Gerencie as solicitações de férias da sua equipe.
        </p>
      </div>

      <div className="bg-slate-900 border border-slate-800 rounded-xl overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm">
            <thead className="bg-slate-950/60 text-slate-400 text-xs uppercase font-medium border-b border-slate-800">
              <tr>
                <th className="px-6 py-3">Colaborador</th>
                <th className="px-6 py-3">Período</th>
                <th className="px-6 py-3">Status</th>
                <th className="px-6 py-3 text-right">Ações</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60">
              {loading ? (
                <tr>
                  <td colSpan={4} className="px-6 py-8 text-center text-slate-500">
                    Carregando solicitações...
                  </td>
                </tr>
              ) : requests.length === 0 ? (
                <tr>
                  <td colSpan={4} className="px-6 py-8 text-center text-slate-500">
                    Nenhuma solicitação pendente para sua gestão.
                  </td>
                </tr>
              ) : (
                requests.map((item) => (
                  <tr key={item.id} className="hover:bg-slate-800/30 transition-colors">
                    <td className="px-6 py-4">
                      <div className="font-medium text-white">{item.userName}</div>
                      <div className="text-xs text-slate-400">{item.userEmail}</div>
                    </td>
                    <td className="px-6 py-4">
                      <div className="font-medium text-white">
                        {format(parseISO(item.startDate), 'dd/MM/yyyy')} até {format(parseISO(item.endDate), 'dd/MM/yyyy')}
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <StatusBadge status={item.status} />
                    </td>
                    <td className="px-6 py-4 text-right">
                      {item.status === 'PENDING' ? (
                        <div className="flex items-center justify-end gap-2">
                          <button
                            onClick={() => handleUpdateStatus(item.id, 'APPROVED')}
                            className="bg-emerald-600/20 hover:bg-emerald-600 text-emerald-400 hover:text-white px-2.5 py-1.5 rounded-lg text-xs font-medium transition-colors inline-flex items-center gap-1 border border-emerald-600/40"
                          >
                            <Check size={14} /> Aprovar
                          </button>
                          <button
                            onClick={() => handleUpdateStatus(item.id, 'REJECTED')}
                            className="bg-rose-600/20 hover:bg-rose-600 text-rose-400 hover:text-white px-2.5 py-1.5 rounded-lg text-xs font-medium transition-colors inline-flex items-center gap-1 border border-rose-600/40"
                          >
                            <X size={14} /> Rejeitar
                          </button>
                        </div>
                      ) : (
                        <span className="text-xs text-slate-500">Concluído</span>
                      )}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}