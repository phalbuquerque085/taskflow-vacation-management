'use client';

import React, { useState } from 'react';
import { VacationRequest } from '@/types';
import {
  format,
  startOfMonth,
  endOfMonth,
  startOfWeek,
  endOfWeek,
  eachDayOfInterval,
  isSameMonth,
  isSameDay,
  addMonths,
  subMonths,
  isWithinInterval,
  parseISO,
} from 'date-fns';
import { ptBR } from 'date-fns/locale';
import { ChevronLeft, ChevronRight, Calendar as CalendarIcon, Info } from 'lucide-react';

interface VacationCalendarProps {
  requests: VacationRequest[];
}

export const VacationCalendar: React.FC<VacationCalendarProps> = ({ requests }) => {
  const [currentMonth, setCurrentMonth] = useState(new Date());

  const prevMonth = () => setCurrentMonth(subMonths(currentMonth, 1));
  const nextMonth = () => setCurrentMonth(addMonths(currentMonth, 1));

  const monthStart = startOfMonth(currentMonth);
  const monthEnd = endOfMonth(monthStart);
  const startDate = startOfWeek(monthStart, { weekStartsOn: 0 });
  const endDate = endOfWeek(monthEnd, { weekStartsOn: 0 });

  const days = eachDayOfInterval({ start: startDate, end: endDate });

  const getVacationsForDay = (day: Date) => {
    return requests.filter((req) => {
      if (req.status === 'CANCELLED' || req.status === 'REJECTED') return false;
      const start = parseISO(req.startDate);
      const end = parseISO(req.endDate);
      return isWithinInterval(day, { start, end });
    });
  };

  const weekDays = ['Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb'];

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-xl overflow-hidden shadow-xl">
      {/* Cabeçalho do Calendário */}
      <div className="p-6 border-b border-slate-800 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="p-2 bg-blue-500/10 rounded-lg text-blue-400">
            <CalendarIcon size={20} />
          </div>
          <div>
            <h2 className="text-lg font-bold text-white capitalize">
              {format(currentMonth, 'MMMM yyyy', { locale: ptBR })}
            </h2>
            <p className="text-xs text-slate-400">Visão geral dos períodos de ausência</p>
          </div>
        </div>

        <div className="flex items-center gap-2">
          <button
            onClick={prevMonth}
            className="p-2 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 hover:text-white transition-colors"
            title="Mês Anterior"
          >
            <ChevronLeft size={18} />
          </button>
          <button
            onClick={() => setCurrentMonth(new Date())}
            className="px-3 py-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-xs font-medium text-slate-300 hover:text-white transition-colors"
          >
            Hoje
          </button>
          <button
            onClick={nextMonth}
            className="p-2 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 hover:text-white transition-colors"
            title="Próximo Mês"
          >
            <ChevronRight size={18} />
          </button>
        </div>
      </div>

      {/* Grade de Dias da Semana */}
      <div className="grid grid-cols-7 border-b border-slate-800 bg-slate-950/50 text-center text-xs font-semibold text-slate-400 py-3">
        {weekDays.map((day, idx) => (
          <div key={idx}>{day}</div>
        ))}
      </div>

      {/* Grid de Dias do Mês */}
      <div className="grid grid-cols-7 auto-rows-fr bg-slate-950 divide-x divide-y divide-slate-800/60 border-b border-slate-800">
        {days.map((day, idx) => {
          const vacations = getVacationsForDay(day);
          const isCurrentMonth = isSameMonth(day, currentMonth);
          const isToday = isSameDay(day, new Date());

          return (
            <div
              key={idx}
              className={`min-h-[105px] p-2 transition-colors flex flex-col justify-between ${
                !isCurrentMonth ? 'bg-slate-950/30 text-slate-600' : 'bg-slate-900/40 text-slate-300'
              } hover:bg-slate-800/40`}
            >
              <div className="flex items-center justify-between">
                <span
                  className={`text-xs font-bold w-6 h-6 flex items-center justify-center rounded-full ${
                    isToday ? 'bg-blue-600 text-white font-black' : isCurrentMonth ? 'text-slate-300' : 'text-slate-600'
                  }`}
                >
                  {format(day, 'd')}
                </span>
                {vacations.length > 0 && (
                  <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse"></span>
                )}
              </div>

              <div className="mt-1 space-y-1 overflow-y-auto max-h-[65px] scrollbar-none">
                {vacations.map((vac) => {
                  const isPending = vac.status === 'PENDING';
                  return (
                    <div
                      key={vac.id}
                      title={`${vac.userName} (${isPending ? 'Pendente' : 'Aprovado'})`}
                      className={`text-[10px] px-1.5 py-0.5 rounded truncate font-medium border ${
                        isPending
                          ? 'bg-amber-500/10 text-amber-300 border-amber-500/30'
                          : 'bg-emerald-500/10 text-emerald-300 border-emerald-500/30'
                      }`}
                    >
                      {vac.userName.split(' ')[0]} ({isPending ? 'P' : 'A'})
                    </div>
                  );
                })}
              </div>
            </div>
          );
        })}
      </div>

      {/* Legenda */}
      <div className="p-4 bg-slate-950 flex flex-wrap items-center justify-between gap-4 text-xs text-slate-400">
        <div className="flex items-center gap-4">
          <div className="flex items-center gap-1.5">
            <span className="w-3 h-3 rounded bg-emerald-500/20 border border-emerald-500/50"></span>
            <span>Aprovado</span>
          </div>
          <div className="flex items-center gap-1.5">
            <span className="w-3 h-3 rounded bg-amber-500/20 border border-amber-500/50"></span>
            <span>Pendente</span>
          </div>
        </div>
        <div className="flex items-center gap-1 text-[11px] text-slate-500">
          <Info size={13} />
          <span>Férias canceladas ou rejeitadas não bloqueiam o calendário.</span>
        </div>
      </div>
    </div>
  );
};