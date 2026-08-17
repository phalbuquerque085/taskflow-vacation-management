'use client';

import React from 'react';
import { UserProvider } from '@/context/UserContext';
import { Navbar } from '@/components/Navbar';

export function Providers({ children }: { children: React.ReactNode }) {
  return (
    <UserProvider>
      <Navbar />
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {children}
      </main>
    </UserProvider>
  );
}