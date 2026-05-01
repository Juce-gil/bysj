import { describe, it, expect, beforeEach } from 'vitest';
import { setActivePinia, createPinia } from 'pinia';
import { useUserStore } from '@/stores/user';

describe('useUserStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  it('should initialize with empty state', () => {
    const store = useUserStore();
    expect(store.token).toBe('');
    expect(store.profile).toBeNull();
    expect(store.isLoggedIn).toBe(false);
    expect(store.role).toBe('user');
    expect(store.displayName).toBe('访客');
  });

  it('should update isLoggedIn when token is set', () => {
    const store = useUserStore();
    expect(store.isLoggedIn).toBe(false);

    store.token = 'test-token';
    expect(store.isLoggedIn).toBe(true);
  });

  it('should clear state on logout', () => {
    const store = useUserStore();
    store.token = 'test-token';
    store.profile = {
      id: 1,
      name: '测试用户',
      role: 'user',
      school: '科成',
      slogan: '测试',
    };

    store.logout();

    expect(store.token).toBe('');
    expect(store.profile).toBeNull();
    expect(store.isLoggedIn).toBe(false);
  });

  it('should compute role correctly', () => {
    const store = useUserStore();
    expect(store.role).toBe('user');

    store.profile = {
      id: 1,
      name: '管理员',
      role: 'admin',
      school: '科成',
      slogan: '测试',
    };
    expect(store.role).toBe('admin');
  });

  it('should compute displayName correctly', () => {
    const store = useUserStore();
    expect(store.displayName).toBe('访客');

    store.profile = {
      id: 1,
      name: '张三',
      role: 'user',
      school: '科成',
      slogan: '测试',
    };
    expect(store.displayName).toBe('张三');
  });
});
