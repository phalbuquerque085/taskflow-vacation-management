'use client';

import React, { useState } from 'react';
import { useRouter } from 'next/navigation';
import { useUser } from '@/context/UserContext';
import { api } from '@/services/api';
import { Lock, Mail, AlertCircle, ArrowRight } from 'lucide-react';

export default function LoginPage() {
  const router = useRouter();
  const { setCurrentUser } = useUser();
  const [email, setEmail] = useState('admin@taskflow.com');
  const [password, setPassword] = useState('123456');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const res = await api.post('/auth/login', { email, password });
      const { token, ...userData } = res.data;

      localStorage.setItem('taskflow_token', token);
      setCurrentUser(userData);

      router.push('/');
    } catch (err: any) {
      const msg = err.response?.data?.message || 'Falha ao autenticar. Verifique suas credenciais.';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  const fillQuickAccount = (quickEmail: string) => {
    setEmail(quickEmail);
    setPassword('123456');
  };

  return (
    <div className="min-h-[75vh] flex flex-col justify-center items-center px-4">
      <div className="w-full max-w-md bg-slate-900 border border-slate-800 rounded-2xl p-8 shadow-2xl">
        <div className="text-center mb-8">
          <h1 className="text-2xl font-bold text-white tracking-tight">
            TaskFlow <span className="text-blue-400">Vacation</span>
          </h1>
          <p className="text-slate-400 text-sm mt-1">Acesse sua conta para gerenciar férias</p>
        </div>

        {error && (
          <div className="p-3 rounded-lg text-sm mb-6 bg-rose-950/50 border border-rose-800 text-rose-300 flex items-start gap-2">
            <AlertCircle size={16} className="mt-0.5 shrink-0" />
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleLogin} className="space-y-4">
          <div>
            <label className="block text-xs font-medium text-slate-300 mb-1">E-mail</label>
            <div className="relative">
              <Mail size={16} className="absolute left-3 top-3 text-slate-500" />
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                className="w-full bg-slate-950 border border-slate-700 rounded-lg pl-9 pr-3 py-2 text-sm text-white focus:outline-none focus:border-blue-500"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-medium text-slate-300 mb-1">Senha</label>
            <div className="relative">
              <Lock size={16} className="absolute left-3 top-3 text-slate-500" />
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                className="w-full bg-slate-950 border border-slate-700 rounded-lg pl-9 pr-3 py-2 text-sm text-white focus:outline-none focus:border-blue-500"
              />
            </div>
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full bg-blue-600 hover:bg-blue-500 disabled:opacity-50 text-white font-medium py-2.5 rounded-lg text-sm transition-colors flex items-center justify-center gap-2"
          >
            <span>{loading ? 'Entrando...' : 'Entrar no Sistema'}</span>
            <ArrowRight size={16} />
          </button>
        </form>

        <div className="mt-8 pt-6 border-t border-slate-800 text-xs">
          <p className="text-slate-400 mb-2 font-medium">Contas rápidas de teste (Senha padrão: 123456):</p>
          <div className="flex flex-wrap gap-1.5">
            <button
              onClick={() => fillQuickAccount('admin@taskflow.com')}
              className="px-2.5 py-1 bg-slate-800 hover:bg-slate-700 rounded-md text-slate-300 transition-colors"
            >
              Admin
            </button>
            <button
              onClick={() => fillQuickAccount('carlos.manager@taskflow.com')}
              className="px-2.5 py-1 bg-slate-800 hover:bg-slate-700 rounded-md text-slate-300 transition-colors"
            >
              Manager
            </button>
            <button
              onClick={() => fillQuickAccount('ana.dev@taskflow.com')}
              className="px-2.5 py-1 bg-slate-800 hover:bg-slate-700 rounded-md text-slate-300 transition-colors"
            >
              Ana (Dev)
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}