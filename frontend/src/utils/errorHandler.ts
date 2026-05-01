import { ElMessage } from 'element-plus';
import type { AxiosError } from 'axios';

/**
 * 全局错误处理器
 */
export class GlobalErrorHandler {
  private static instance: GlobalErrorHandler;

  private constructor() {
    this.setupGlobalHandlers();
  }

  public static getInstance(): GlobalErrorHandler {
    if (!GlobalErrorHandler.instance) {
      GlobalErrorHandler.instance = new GlobalErrorHandler();
    }
    return GlobalErrorHandler.instance;
  }

  /**
   * 设置全局错误处理
   */
  private setupGlobalHandlers() {
    // 捕获未处理的 Promise 错误
    window.addEventListener('unhandledrejection', (event) => {
      console.error('Unhandled promise rejection:', event.reason);
      this.handleError(event.reason);
      event.preventDefault();
    });

    // 捕获全局 JavaScript 错误
    window.addEventListener('error', (event) => {
      console.error('Global error:', event.error);
      // 不显示资源加载错误的提示
      if (event.target !== window) {
        return;
      }
      this.handleError(event.error);
    });
  }

  /**
   * 统一错误处理
   */
  public handleError(error: unknown): void {
    // 如果是 401 错误，已经在 http.ts 中处理，不再重复提示
    if (this.is401Error(error)) {
      return;
    }

    const message = this.extractErrorMessage(error);

    // 避免重复显示相同的错误消息
    if (this.shouldShowError(message)) {
      ElMessage.error(message);
    }
  }

  /**
   * 判断是否是 401 错误
   */
  private is401Error(error: unknown): boolean {
    if (error && typeof error === 'object') {
      const axiosError = error as AxiosError;
      return axiosError.response?.status === 401;
    }
    return false;
  }

  /**
   * 提取错误消息
   */
  private extractErrorMessage(error: unknown): string {
    if (typeof error === 'string') {
      return error;
    }

    if (error instanceof Error) {
      return error.message;
    }

    if (error && typeof error === 'object') {
      const axiosError = error as AxiosError<{ message?: string }>;
      return axiosError.response?.data?.message || axiosError.message || '未知错误';
    }

    return '未知错误';
  }

  /**
   * 判断是否应该显示错误
   * 避免短时间内重复显示相同错误
   */
  private lastErrorMessage = '';
  private lastErrorTime = 0;
  private readonly ERROR_THROTTLE_TIME = 3000; // 3秒内不重复显示相同错误

  private shouldShowError(message: string): boolean {
    const now = Date.now();

    if (
      message === this.lastErrorMessage &&
      now - this.lastErrorTime < this.ERROR_THROTTLE_TIME
    ) {
      return false;
    }

    this.lastErrorMessage = message;
    this.lastErrorTime = now;
    return true;
  }
}

/**
 * 初始化全局错误处理
 */
export function initGlobalErrorHandler(): void {
  GlobalErrorHandler.getInstance();
}
