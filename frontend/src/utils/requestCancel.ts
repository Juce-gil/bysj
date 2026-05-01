import axios, { type AxiosInstance, type InternalAxiosRequestConfig } from 'axios';

/**
 * 请求取消管理器
 */
export class RequestCancelManager {
  private static instance: RequestCancelManager;
  private pendingRequests: Map<string, AbortController>;

  private constructor() {
    this.pendingRequests = new Map();
  }

  public static getInstance(): RequestCancelManager {
    if (!RequestCancelManager.instance) {
      RequestCancelManager.instance = new RequestCancelManager();
    }
    return RequestCancelManager.instance;
  }

  /**
   * 生成请求的唯一标识
   */
  private generateRequestKey(config: InternalAxiosRequestConfig): string {
    const { method, url, params, data } = config;
    return [method, url, JSON.stringify(params), JSON.stringify(data)].join('&');
  }

  /**
   * 添加请求
   */
  public addRequest(config: InternalAxiosRequestConfig): void {
    const requestKey = this.generateRequestKey(config);

    // 如果已存在相同的请求，取消之前的请求
    if (this.pendingRequests.has(requestKey)) {
      this.cancelRequest(requestKey);
    }

    // 创建新的 AbortController
    const controller = new AbortController();
    config.signal = controller.signal;
    this.pendingRequests.set(requestKey, controller);
  }

  /**
   * 移除请求
   */
  public removeRequest(config: InternalAxiosRequestConfig): void {
    const requestKey = this.generateRequestKey(config);
    this.pendingRequests.delete(requestKey);
  }

  /**
   * 取消指定请求
   */
  public cancelRequest(requestKey: string): void {
    const controller = this.pendingRequests.get(requestKey);
    if (controller) {
      controller.abort();
      this.pendingRequests.delete(requestKey);
    }
  }

  /**
   * 取消所有请求
   */
  public cancelAllRequests(): void {
    this.pendingRequests.forEach((controller) => {
      controller.abort();
    });
    this.pendingRequests.clear();
  }

  /**
   * 为 axios 实例添加拦截器
   */
  public setupInterceptors(axiosInstance: AxiosInstance): void {
    // 请求拦截器
    axiosInstance.interceptors.request.use(
      (config) => {
        this.addRequest(config);
        return config;
      },
      (error) => {
        return Promise.reject(error);
      },
    );

    // 响应拦截器
    axiosInstance.interceptors.response.use(
      (response) => {
        this.removeRequest(response.config);
        return response;
      },
      (error) => {
        if (axios.isCancel(error)) {
          console.log('Request canceled:', error.message);
        } else if (error.config) {
          this.removeRequest(error.config);
        }
        return Promise.reject(error);
      },
    );
  }
}

/**
 * 获取请求取消管理器实例
 */
export function getRequestCancelManager(): RequestCancelManager {
  return RequestCancelManager.getInstance();
}
