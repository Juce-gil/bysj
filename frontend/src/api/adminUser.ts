import http from '@/api/http';

interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

export interface PageResult<T> {
  records: T[];
  total: number;
  pageNum: number;
  pageSize: number;
}

export interface AdminUserItem {
  id: number;
  username: string;
  displayName: string;
  role: string;
  studentNo: string;
  phone: string;
  school: string;
  disabled: boolean;
  registerTime: string;
}

export interface AdminUserQuery {
  keyword?: string;
  role?: string;
  disabled?: boolean;
  pageNum?: number;
  pageSize?: number;
}

type RecordValue = Record<string, unknown>;

const toRecord = (value: unknown): RecordValue => (value && typeof value === 'object' ? value as RecordValue : {});
const toNumber = (value: unknown, fallback = 0): number => {
  const next = typeof value === 'number' ? value : Number(value);
  return Number.isFinite(next) ? next : fallback;
};
const toString = (value: unknown, fallback = ''): string => typeof value === 'string' ? value : fallback;

const pickString = (source: RecordValue, keys: string[], fallback = ''): string => {
  const hit = keys.map((key) => source[key]).find((value) => typeof value === 'string' && value.trim().length > 0);
  return typeof hit === 'string' ? hit : fallback;
};

const resolveDisabled = (source: RecordValue): boolean => {
  if (typeof source.disabled === 'boolean') {
    return source.disabled;
  }

  if (typeof source.status === 'string') {
    return ['disabled', 'banned', 'forbidden', 'inactive'].includes(source.status.trim().toLowerCase());
  }

  return false;
};

const normalizeAdminUserItem = (value: unknown): AdminUserItem => {
  const item = toRecord(value);

  return {
    id: toNumber(item.id),
    username: pickString(item, ['username', 'userName'], '未知账号'),
    displayName: pickString(item, ['displayName', 'name', 'nickname'], '未设置昵称'),
    role: pickString(item, ['role'], 'user'),
    studentNo: pickString(item, ['studentNo', 'studentId', 'studentCode']),
    phone: pickString(item, ['phone', 'mobile', 'phoneNumber']),
    school: pickString(item, ['school', 'campus'], '科成'),
    disabled: resolveDisabled(item),
    registerTime: pickString(item, ['registerTime', 'registeredAt', 'createdAt', 'createTime']),
  };
};

const normalizePageResult = <T>(value: unknown, normalizeItem: (item: unknown) => T): PageResult<T> => {
  const page = toRecord(value);
  const records = Array.isArray(page.records) ? page.records.map(normalizeItem) : [];

  return {
    records,
    total: toNumber(page.total),
    pageNum: toNumber(page.pageNum, 1),
    pageSize: toNumber(page.pageSize, records.length || 10),
  };
};

export const getAdminUserPage = async (params: AdminUserQuery): Promise<PageResult<AdminUserItem>> => {
  const response = await http.get('/admin/users', { params }) as ApiResponse<unknown>;
  return normalizePageResult(response.data, normalizeAdminUserItem);
};

export const updateAdminUserStatus = async (id: number, disabled: boolean): Promise<AdminUserItem> => {
  const response = await http.put(`/admin/users/${id}/status`, { disabled }) as ApiResponse<unknown>;
  return normalizeAdminUserItem(response.data);
};