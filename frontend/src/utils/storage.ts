const TOKEN_KEY = 'kecheng_market_token';
const PROFILE_KEY = 'kecheng_market_profile';
const LEGACY_TOKEN_KEY = 'qinghe_market_token';
const LEGACY_PROFILE_KEY = 'qinghe_market_profile';

export interface StoredProfile {
  id: number;
  name: string;
  role: 'user' | 'admin';
  school: string;
  slogan: string;
  phone?: string;
  qq?: string;
}

const safeParse = <T>(value: string | null): T | null => {
  if (!value) {
    return null;
  }

  try {
    return JSON.parse(value) as T;
  } catch (error) {
    console.warn('本地数据解析失败', error);
    return null;
  }
};

export const storage = {
  getToken() {
    return localStorage.getItem(TOKEN_KEY) || localStorage.getItem(LEGACY_TOKEN_KEY) || '';
  },
  setToken(token: string) {
    localStorage.setItem(TOKEN_KEY, token);
    localStorage.removeItem(LEGACY_TOKEN_KEY);
  },
  clearToken() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(LEGACY_TOKEN_KEY);
  },
  getProfile() {
    return safeParse<StoredProfile>(localStorage.getItem(PROFILE_KEY) || localStorage.getItem(LEGACY_PROFILE_KEY));
  },
  setProfile(profile: StoredProfile) {
    localStorage.setItem(PROFILE_KEY, JSON.stringify(profile));
    localStorage.removeItem(LEGACY_PROFILE_KEY);
  },
  clearProfile() {
    localStorage.removeItem(PROFILE_KEY);
    localStorage.removeItem(LEGACY_PROFILE_KEY);
  },
  clearAll() {
    this.clearToken();
    this.clearProfile();
  },
};
