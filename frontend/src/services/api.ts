import axios from 'axios';

export const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api';

export const api = axios.create({
  baseURL: API_URL,
});

api.interceptors.request.use((config) => {
  if (typeof window !== 'undefined') {
    const activeUserId = localStorage.getItem('taskflow_active_user_id');
    if (activeUserId) {
      config.headers['X-User-Id'] = activeUserId;
    }
  }
  return config;
});