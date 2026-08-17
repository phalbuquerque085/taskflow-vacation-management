import axios from 'axios';

export const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api';

export const api = axios.create({
  baseURL: API_URL,
});

api.interceptors.request.use((config) => {
  if (typeof window !== 'undefined') {
    const token = localStorage.getItem('taskflow_token');
    const activeUserId = localStorage.getItem('taskflow_active_user_id');

    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    if (activeUserId) {
      config.headers['X-User-Id'] = activeUserId;
    }
  }
  return config;
});