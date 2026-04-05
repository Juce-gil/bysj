import { computed, ref } from 'vue';
import { defineStore } from 'pinia';
import { storage, type StoredProfile } from '@/utils/storage';
import {
  fetchCurrentUser,
  login,
  register,
  updateCurrentUser,
  type LoginForm,
  type RegisterForm,
  type UpdateProfilePayload,
} from '@/api/auth';

export const useUserStore = defineStore('user', () => {
  const token = ref(storage.getToken());
  const profile = ref<StoredProfile | null>(storage.getProfile());

  const isLoggedIn = computed(() => Boolean(token.value));
  const role = computed(() => profile.value?.role || 'user');
  const displayName = computed(() => profile.value?.name || '访客');

  const persistState = () => {
    if (token.value) {
      storage.setToken(token.value);
    }
    if (profile.value) {
      storage.setProfile(profile.value);
    }
  };

  const loginAction = async (payload: LoginForm) => {
    const result = await login(payload);
    token.value = result.token;
    profile.value = result.profile;
    persistState();
    return result;
  };

  const registerAction = async (payload: RegisterForm) => register(payload);

  const refreshProfile = async () => {
    if (!token.value) return null;
    const current = await fetchCurrentUser();
    profile.value = current;
    persistState();
    return current;
  };

  const updateProfile = async (payload: UpdateProfilePayload) => {
    const current = await updateCurrentUser(payload);
    profile.value = current;
    persistState();
    return current;
  };

  const logout = () => {
    token.value = '';
    profile.value = null;
    storage.clearAll();
  };

  return {
    token,
    profile,
    role,
    displayName,
    isLoggedIn,
    login: loginAction,
    register: registerAction,
    refreshProfile,
    updateProfile,
    logout,
  };
});
