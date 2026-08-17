'use client';

import React, { useState } from 'react';
import { useUser } from '@/context/UserContext';
import { Role } from '@/types';
import { api } from '@/services/api';
import { UserPlus, ShieldAlert, Trash2 } from 'lucide-react';

export default function EmployeesPage() {
  const { currentUser, usersList, refreshUsers } = useUser();
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [role, setRole] = useState<Role>('COLLABORATOR');
  const [managerId, setManagerId] = useState<number | ''>('');
  const [submitting, setSubmitting] = useState(false);

  const managers = usersList.filter((u) => u.role === 'MANAGER' || u.role === 'ADMIN');

  const handleCreateUser = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name || !email) return;

    setSubmitting(true);
    try {
      await api.post('/users', {
        name,
        email,
        role,
        managerId: role === 'COLLABORATOR' ? (managerId ? Number(managerId) : null) : null,
      });

      setName('');
      setEmail('');
      setRole('COLLABORATOR');
      setManagerId('');
      refreshUsers();
      alert('Colaborador cadastrado com sucesso!');
    } catch (err: any) {
      const msg = err.response?.data?.message || 'Erro ao cadastrar usuário.';
      alert(msg);
    } finally {
      setSubmitting(false);
    }
  };

  const handleDeleteUser = async (id: number) => {
    if (!confirm('Deseja excluir este colaborador?')) return;

    try {
      await api.delete(`/users/${id}`);
      refreshUsers();
    } catch (err: any) {
      const msg = err.response?.data?.message || 'Erro ao remover colaborador.';
      alert(msg);
    }
  };

  if (!currentUser || currentUser.role !== 'ADMIN') {
    return (
      <div className="bg-slate-900 border border-slate-800 rounded-xl p-8 text-center max-w-md mx-auto my-12">
        <ShieldAlert size={48} className="mx-auto text-amber-400 mb-4" />
        <h2 className="text-lg font-bold text-white mb-2">Acesso Restrito</h2>
        <p className="text-sm text-slate-400">
          Apenas o perfil Administrador pode cadastrar e gerenciar colaboradores.
        </p>
      </div>
    );
  }

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-2xl font-bold tracking-tight text-white">Gestão de Colaboradores</h1>
        <p className="text-slate-400 text-sm mt-1">
          Cadastre novos membros e organize a estrutura hierárquica da empresa.
        </p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div className="bg-slate-900 border border-slate-800 rounded-xl p-6 h-fit">
          <h2 className="text-lg font-semibold text-white mb-4 flex items-center gap-2">
            <UserPlus size={18} className="text-blue-400" />
            Novo Colaborador
          </h2>

          <form onSubmit={handleCreateUser} className="space-y-4">
            <div>
              <label className="block text-xs font-medium text-slate-300 mb-1">Nome Completo</label>
              <input
                type="text"
                value={name}
                onChange={(e) => setName(e.target.value)}
                required
                className="w-full bg-slate-950 border border-slate-700 rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:border-blue-500"
              />
            </div>

            <div>
              <label className="block text-xs font-medium text-slate-300 mb-1">Email</label>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                className="w-full bg-slate-950 border border-slate-700 rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:border-blue-500"
              />
            </div>

            <div>
              <label className="block text-xs font-medium text-slate-300 mb-1">Perfil</label>
              <select
                value={role}
                onChange={(e) => setRole(e.target.value as Role)}
                className="w-full bg-slate-950 border border-slate-700 rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:border-blue-500"
              >
                <option value="COLLABORATOR">Colaborador</option>
                <option value="MANAGER">Gestor</option>
                <option value="ADMIN">Administrador</option>
              </select>
            </div>

            {role === 'COLLABORATOR' && (
              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1">Gestor Responsável</label>
                <select
                  value={managerId}
                  onChange={(e) => setManagerId(e.target.value ? Number(e.target.value) : '')}
                  required
                  className="w-full bg-slate-950 border border-slate-700 rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:border-blue-500"
                >
                  <option value="">Selecione um Gestor...</option>
                  {managers.map((m) => (
                    <option key={m.id} value={m.id}>
                      {m.name} ({m.role})
                    </option>
                  ))}
                </select>
              </div>
            )}

            <button
              type="submit"
              disabled={submitting}
              className="w-full bg-blue-600 hover:bg-blue-500 disabled:opacity-50 text-white font-medium py-2 rounded-lg text-sm transition-colors"
            >
              {submitting ? 'Cadastrando...' : 'Cadastrar Usuário'}
            </button>
          </form>
        </div>

        <div className="lg:col-span-2 bg-slate-900 border border-slate-800 rounded-xl overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="bg-slate-950/60 text-slate-400 text-xs uppercase font-medium border-b border-slate-800">
                <tr>
                  <th className="px-6 py-3">Nome / Email</th>
                  <th className="px-6 py-3">Perfil</th>
                  <th className="px-6 py-3">Gestor</th>
                  <th className="px-6 py-3 text-right">Ação</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60">
                {usersList.map((u) => (
                  <tr key={u.id} className="hover:bg-slate-800/30 transition-colors">
                    <td className="px-6 py-4">
                      <div className="font-medium text-white">{u.name}</div>
                      <div className="text-xs text-slate-400">{u.email}</div>
                    </td>
                    <td className="px-6 py-4">
                      <span className="text-xs px-2 py-0.5 rounded bg-slate-800 text-slate-300 font-mono">
                        {u.role}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-xs text-slate-400">
                      {u.managerName || '-'}
                    </td>
                    <td className="px-6 py-4 text-right">
                      {u.id !== currentUser.id && (
                        <button
                          onClick={() => handleDeleteUser(u.id)}
                          className="text-rose-400 hover:text-rose-300 transition-colors"
                        >
                          <Trash2 size={16} />
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
}