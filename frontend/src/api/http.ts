import axios from 'axios';
import { ElMessage } from 'element-plus';
import { storage } from '@/utils/storage';

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
      const message = payload.message || '请求失败，请稍后再试';
      ElMessage.error(message);
      return Promise.reject(new Error(message));
    }
    return response.data;
  },
  (error) => {
    const message = error?.response?.data?.message || error?.message || '请求失败，请稍后再试';
    ElMessage.error(message);
    return Promise.reject(error);
  },
);

export default service;
