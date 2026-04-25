import axios from 'axios';
import { storage } from '@/utils/storage';
import { normalizeErrorMessage } from '@/utils/error';

interface ApiEnvelope<T = unknown> {
  code?: number;
  message?: string;
  data?: T;
}

const service = axios.create({
  baseURL: '/api',
  timeout: 10000,
});

service.interceptors.request.use((config) => {
  const token = storage.getToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

service.interceptors.response.use(
  (response) => {
    const payload = response.data as ApiEnvelope | undefined;
    if (payload && typeof payload === 'object' && typeof payload.code === 'number' && payload.code !== 200) {
      return Promise.reject(new Error(normalizeErrorMessage(payload.message, '请求失败，请稍后重试')));
    }
    return response.data;
  },
  (error) => {
    const message = normalizeErrorMessage(error?.response?.data?.message || error?.message, '请求失败，请稍后重试');
    return Promise.reject(new Error(message));
  },
);

export default service;
