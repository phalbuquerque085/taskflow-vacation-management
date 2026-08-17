'use client';

import React, { useState, useEffect, useCallback } from 'react';
import { useUser } from '@/context/UserContext';
import { VacationRequest } from '@/types';
import { api } from '@/services/api';
import { StatusBadge } from '@/components/StatusBadge';
import { Calendar, Plus, XCircle, AlertCircle, CheckCircle2 } from 'lucide-react';
import { format, parseISO } from 'date-fns';

export default function HomePage() {
  const { currentUser } = useUser();
  const [requests, setRequests] = useState<VacationRequest[]>([]);
  const [loading, setLoading] = useState(true);
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [feedback, setFeedback] = useState<{ type: 'success' | 'error'; message: string } | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const fetchVacations = useCallback(async () => {
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
    fetchVacations();
  }, [fetchVacations]);

  const handleCreateRequest = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!currentUser || !startDate || !endDate) return;

    setSubmitting(true);
    setFeedback(null);

    try {
      await api.post('/vacations', {
        userId: currentUser.id,
        startDate,
        endDate,
      });

      setFeedback({ type: 'success', message: 'Solicitação de férias enviada com sucesso!' });
      setStartDate('');
      setEndDate('');
      fetchVacations();
    } catch (err: any) {
      const msg = err.response?.data?.message || 'Erro ao submeter pedido de férias.';
      setFeedback({ type: 'error', message: msg });
    } finally {
      setSubmitting(false);
    }
  };

  const handleCancel = async (id: number) => {
    if (!confirm('Deseja realmente cancelar este pedido de férias?')) return;

    try {
      await api.delete(`/vacations/${id}`);
      fetchVacations();
    } catch (err: any) {
      const msg = err.response?.data?.message || 'Erro ao cancelar pedido.';
      alert(msg);
    }
  };

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-2xl font-bold tracking-tight text-white">Minhas Férias</h1>
        <p className="text-slate-400 text-sm mt-1">
          Solicite e acompanhe seus agendamentos de férias na empresa.
        </p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div className="bg-slate-900 border border-slate-800 rounded-xl p-6 h-fit">
          <h2 className="text-lg font-semibold text-white mb-4 flex items-center gap-2">
            <Plus size={18} className="text-blue-400" />
            Nova Solicitação
          </h2>

          {feedback && (
            <div
              className={`p-3 rounded-lg text-sm mb-4 flex items-start gap-2 ${
                feedback.type === 'success'
                  ? 'bg-emerald-950/50 border border-emerald-800 text-emerald-300'
                  : 'bg-rose-950/50 border border-rose-800 text-rose-300'
              }`}
            >
              {feedback.type === 'success' ? <CheckCircle2 size={16} className="mt-0.5" /> : <AlertCircle size={16} className="mt-0.5" />}
              <span>{feedback.message}</span>
            </div>
          )}

          <form onSubmit={handleCreateRequest} className="space-y-4">
            <div>
              <label className="block text-xs font-medium text-slate-300 mb-1">Data Início</label>
              <input
                type="date"
                value={startDate}
                onChange={(e) => setStartDate(e.target.value)}
                required
                className="w-full bg-slate-950 border border-slate-700 rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:border-blue-500"
              />
            </div>

            <div>
              <label className="block text-xs font-medium text-slate-300 mb-1">Data Fim</label>
              <input
                type="date"
                value={endDate}
                onChange={(e) => setEndDate(e.target.value)}
                required
                className="w-full bg-slate-950 border border-slate-700 rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:border-blue-500"
              />
            </div>

            <button
              type="submit"
              disabled={submitting}
              className="w-full bg-blue-600 hover:bg-blue-500 disabled:opacity-50 text-white font-medium py-2 rounded-lg text-sm transition-colors"
            >
              {submitting ? 'Enviando...' : 'Solicitar Férias'}
            </button>
          </form>
        </div>

        <div className="lg:col-span-2 bg-slate-900 border border-slate-800 rounded-xl overflow-hidden">
          <div className="p-6 border-b border-slate-800">
            <h2 className="text-lg font-semibold text-white flex items-center gap-2">
              <Calendar size={18} className="text-blue-400" />
              Histórico de Solicitações
            </h2>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="bg-slate-950/60 text-slate-400 text-xs uppercase font-medium border-b border-slate-800">
                <tr>
                  <th className="px-6 py-3">Período</th>
                  <th className="px-6 py-3">Status</th>
                  <th className="px-6 py-3 text-right">Ação</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60">
                {loading ? (
                  <tr>
                    <td colSpan={3} className="px-6 py-8 text-center text-slate-500">
                      Carregando solicitações...
                    </td>
                  </tr>
                ) : requests.length === 0 ? (
                  <tr>
                    <td colSpan={3} className="px-6 py-8 text-center text-slate-500">
                      Nenhuma solicitação encontrada.
                    </td>
                  </tr>
                ) : (
                  requests.map((item) => (
                    <tr key={item.id} className="hover:bg-slate-800/30 transition-colors">
                      <td className="px-6 py-4">
                        <div className="font-medium text-white">
                          {format(parseISO(item.startDate), 'dd/MM/yyyy')} até {format(parseISO(item.endDate), 'dd/MM/yyyy')}
                        </div>
                      </td>
                      <td className="px-6 py-4">
                        <StatusBadge status={item.status} />
                      </td>
                      <td className="px-6 py-4 text-right">
                        {item.status === 'PENDING' && (
                          <button
                            onClick={() => handleCancel(item.id)}
                            className="text-rose-400 hover:text-rose-300 transition-colors inline-flex items-center gap-1 text-xs font-medium"
                          >
                            <XCircle size={14} />
                            Cancelar
                          </button>
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
    </div>
  );
}