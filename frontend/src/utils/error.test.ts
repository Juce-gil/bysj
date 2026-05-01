import { describe, it, expect } from 'vitest';
import { normalizeErrorMessage, extractErrorMessage } from '@/utils/error';

describe('error utils', () => {
  describe('normalizeErrorMessage', () => {
    it('should normalize known error messages', () => {
      expect(normalizeErrorMessage('account or password is incorrect')).toBe('账号或密码错误');
      expect(normalizeErrorMessage('Account is disabled')).toBe('账号已被禁用');
      expect(normalizeErrorMessage('server error')).toBe('服务器开小差了，请稍后重试');
    });

    it('should return original message for unknown errors', () => {
      expect(normalizeErrorMessage('unknown error')).toBe('unknown error');
    });

    it('should return fallback for empty message', () => {
      expect(normalizeErrorMessage('')).toBe('请求失败，请稍后重试');
      expect(normalizeErrorMessage(undefined, '自定义fallback')).toBe('自定义fallback');
    });
  });

  describe('extractErrorMessage', () => {
    it('should extract message from string', () => {
      expect(extractErrorMessage('network error')).toBe('网络异常，请检查连接后重试');
    });

    it('should extract message from error object', () => {
      const error = new Error('timeout');
      expect(extractErrorMessage(error)).toBe('请求超时，请稍后重试');
    });

    it('should extract message from axios error', () => {
      const axiosError = {
        response: {
          data: {
            message: 'account or password is incorrect',
          },
        },
      };
      expect(extractErrorMessage(axiosError)).toBe('账号或密码错误');
    });

    it('should return fallback for unknown error', () => {
      expect(extractErrorMessage(null)).toBe('请求失败，请稍后重试');
      expect(extractErrorMessage({})).toBe('请求失败，请稍后重试');
    });
  });
});
