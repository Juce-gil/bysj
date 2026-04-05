import http from '@/api/http';
import type { StoredProfile } from '@/utils/storage';

interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

export interface LoginForm {
  username: string;
  password: string;
}

export interface RegisterForm {
  username: string;
  nickname: string;
  password: string;
  confirmPassword: string;
}

export interface UpdateProfilePayload {
  name: string;
  phone: string;
  qq?: string;
  slogan?: string;
}

export interface LoginResult {
  token: string;
  profile: StoredProfile;
}

export const login = async (payload: LoginForm): Promise<LoginResult> => {
  const response = await http.post('/auth/login', payload) as ApiResponse<LoginResult>;
  return response.data;
};

export const register = async (payload: RegisterForm) => {
  const response = await http.post('/auth/register', payload) as ApiResponse<StoredProfile>;
  return {
    message: response.message,
    profile: response.data,
  };
};

export const fetchCurrentUser = async (): Promise<StoredProfile> => {
  const response = await http.get('/users/me') as ApiResponse<StoredProfile>;
  return response.data;
};

export const updateCurrentUser = async (payload: UpdateProfilePayload): Promise<StoredProfile> => {
  const response = await http.put('/users/me', payload) as ApiResponse<StoredProfile>;
  return response.data;
};
