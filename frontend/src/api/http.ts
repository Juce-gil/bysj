import axios from 'axios';
import { ElMessage } from 'element-plus';
import { storage } from '@/utils/storage';
import { normalizeErrorMessage } from '@/utils/error';
import { getRequestCancelManager } from '@/utils/requestCancel';

interface ApiEnvelope<T = unknown> {
  code?: number;
  message?: string;
  data?: T;
}

const service = axios.create({
  baseURL: '/api',
  timeout: 10000,
});

// 设置请求取消管理
const cancelManager = getRequestCancelManager();
cancelManager.setupInterceptors(service);

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
    // 如果是请求取消，不显示错误提示
    if (axios.isCancel(error)) {
      return Promise.reject(error);
    }

    // 处理 401 未授权错误
    if (error?.response?.status === 401) {
      storage.clearAll();
      ElMessage.warning('登录已过期，请重新登录');

      // 跳转到登录页，并保存当前路径用于登录后跳转
      const currentPath = window.location.pathname + window.location.search;
      if (currentPath !== '/login') {
        window.location.href = `/login?redirect=${encodeURIComponent(currentPath)}`;
      }
      return Promise.reject(new Error('登录已过期'));
    }

    const message = normalizeErrorMessage(error?.response?.data?.message || error?.message, '请求失败，请稍后重试');
    return Promise.reject(new Error(message));
  },
);

export default service;
