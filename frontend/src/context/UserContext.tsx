'use client';

import React, { createContext, useContext, useEffect, useState } from 'react';
import { User } from '@/types';
import { api } from '@/services/api';

interface UserContextType {
  currentUser: User | null;
  usersList: User[];
  setCurrentUser: (user: User) => void;
  refreshUsers: () => Promise<void>;
  loading: boolean;
}

const UserContext = createContext<UserContextType | undefined>(undefined);

export const UserProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [currentUser, setCurrentUserState] = useState<User | null>(null);
  const [usersList, setUsersList] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);

  const fetchUsers = async () => {
    try {
      const response = await api.get<User[]>('/users');
      setUsersList(response.data);

      const savedUserId = localStorage.getItem('taskflow_active_user_id');
      if (savedUserId) {
        const found = response.data.find((u) => u.id === Number(savedUserId));
        if (found) {
          setCurrentUserState(found);
          return;
        }
      }

      if (response.data.length > 0) {
        setCurrentUserState(response.data[0]);
        localStorage.setItem('taskflow_active_user_id', String(response.data[0].id));
      }
    } catch (error) {
      console.error('Erro ao carregar usuários:', error);
    } finally {
      setLoading(false);
    }
  };

  const setCurrentUser = (user: User) => {
    setCurrentUserState(user);
    localStorage.setItem('taskflow_active_user_id', String(user.id));
  };

  useEffect(() => {
    fetchUsers();
  }, []);

  return (
    <UserContext.Provider
      value={{
        currentUser,
        usersList,
        setCurrentUser,
        refreshUsers: fetchUsers,
        loading,
      }}
    >
      {children}
    </UserContext.Provider>
  );
};

export const useUser = () => {
  const context = useContext(UserContext);
  if (!context) {
    throw new Error('useUser deve ser utilizado dentro de um UserProvider');
  }
  return context;
};