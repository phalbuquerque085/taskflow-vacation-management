'use client';

import React from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { useUser } from '@/context/UserContext';
import { Calendar, CheckSquare, Users, UserCheck } from 'lucide-react';

export const Navbar: React.FC = () => {
  const pathname = usePathname();
  const { currentUser, usersList, setCurrentUser } = useUser();

  const navItems = [
    { label: 'Minhas Férias', href: '/', icon: Calendar, roles: ['ADMIN', 'MANAGER', 'COLLABORATOR'] },
    { label: 'Aprovações', href: '/approvals', icon: CheckSquare, roles: ['ADMIN', 'MANAGER'] },
    { label: 'Colaboradores', href: '/employees', icon: Users, roles: ['ADMIN'] },
  ];

  return (
    <header className="bg-slate-900 border-b border-slate-800 text-white sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        <div className="flex items-center gap-8">
          <Link href="/" className="font-bold text-xl tracking-tight text-blue-400">
            TaskFlow <span className="text-slate-300 font-light">Vacation</span>
          </Link>

          <nav className="hidden md:flex items-center gap-1">
            {navItems
              .filter((item) => currentUser && item.roles.includes(currentUser.role))
              .map((item) => {
                const Icon = item.icon;
                const isActive = pathname === item.href;
                return (
                  <Link
                    key={item.href}
                    href={item.href}
                    className={`flex items-center gap-2 px-3 py-2 rounded-md text-sm font-medium transition-colors ${
                      isActive
                        ? 'bg-blue-600 text-white'
                        : 'text-slate-300 hover:bg-slate-800 hover:text-white'
                    }`}
                  >
                    <Icon size={16} />
                    {item.label}
                  </Link>
                );
              })}
          </nav>
        </div>

        <div className="flex items-center gap-3">
          <div className="flex items-center gap-2 bg-slate-800 border border-slate-700 px-3 py-1.5 rounded-lg text-xs">
            <UserCheck size={16} className="text-blue-400" />
            <span className="text-slate-400">Atuando como:</span>
            <select
              value={currentUser?.id || ''}
              onChange={(e) => {
                const selected = usersList.find((u) => u.id === Number(e.target.value));
                if (selected) setCurrentUser(selected);
              }}
              className="bg-transparent text-white font-medium focus:outline-none cursor-pointer"
            >
              {usersList.map((user) => (
                <option key={user.id} value={user.id} className="bg-slate-900 text-white">
                  {user.name} ({user.role})
                </option>
              ))}
            </select>
          </div>
        </div>
      </div>
    </header>
  );
};