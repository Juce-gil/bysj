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

export interface AdminAnnouncementItem {
  id: number;
  title: string;
  summary: string;
  level: string;
  top: boolean;
  published: boolean;
  publishedAt: string;
}

export interface AdminAnnouncementDetail extends AdminAnnouncementItem {
  content: string;
}

export interface AdminAnnouncementQuery {
  keyword?: string;
  published?: boolean;
  pageNum?: number;
  pageSize?: number;
}

export interface AdminAnnouncementPayload {
  title: string;
  summary: string;
  content: string;
  level: string;
  top: boolean;
  published: boolean;
}

type RecordValue = Record<string, unknown>;

const toRecord = (value: unknown): RecordValue => (value && typeof value === 'object' ? value as RecordValue : {});
const toNumber = (value: unknown, fallback = 0): number => {
  const next = typeof value === 'number' ? value : Number(value);
  return Number.isFinite(next) ? next : fallback;
};
const toBoolean = (value: unknown, fallback = false): boolean => {
  if (typeof value === 'boolean') {
    return value;
  }
  if (typeof value === 'number') {
    return value !== 0;
  }
  if (typeof value === 'string') {
    const normalized = value.trim().toLowerCase();
    if (['true', '1', 'yes', 'y'].includes(normalized)) {
      return true;
    }
    if (['false', '0', 'no', 'n'].includes(normalized)) {
      return false;
    }
  }
  return fallback;
};

const pickString = (source: RecordValue, keys: string[], fallback = ''): string => {
  const hit = keys.map((key) => source[key]).find((value) => typeof value === 'string' && value.trim().length > 0);
  return typeof hit === 'string' ? hit : fallback;
};

const normalizeAdminAnnouncementItem = (value: unknown): AdminAnnouncementItem => {
  const item = toRecord(value);

  return {
    id: toNumber(item.id),
    title: pickString(item, ['title'], '未命名公告'),
    summary: pickString(item, ['summary'], '暂无摘要'),
    level: pickString(item, ['level'], '通知'),
    top: toBoolean(item.top),
    published: toBoolean(item.published),
    publishedAt: pickString(item, ['publishedAt']),
  };
};

const normalizeAdminAnnouncementDetail = (value: unknown): AdminAnnouncementDetail => {
  const detail = toRecord(value);
  const item = normalizeAdminAnnouncementItem(detail);

  return {
    ...item,
    content: pickString(detail, ['content'], '暂无正文内容'),
  };
};

const normalizePageResult = <T>(value: unknown, normalizeItem: (item: unknown) => T): PageResult<T> => {
  const page = toRecord(value);
  const recordsSource = Array.isArray(page.records)
    ? page.records
    : (Array.isArray(page.list) ? page.list : []);
  const records = recordsSource.map(normalizeItem);

  return {
    records,
    total: toNumber(page.total),
    pageNum: toNumber(page.pageNum ?? page.current, 1),
    pageSize: toNumber(page.pageSize ?? page.size, records.length || 10),
  };
};

export const getAdminAnnouncementPage = async (params: AdminAnnouncementQuery): Promise<PageResult<AdminAnnouncementItem>> => {
  const response = await http.get('/admin/announcements', { params }) as ApiResponse<unknown>;
  return normalizePageResult(response.data, normalizeAdminAnnouncementItem);
};

export const getAdminAnnouncementDetail = async (id: number): Promise<AdminAnnouncementDetail> => {
  const response = await http.get(`/admin/announcements/${id}`) as ApiResponse<unknown>;
  return normalizeAdminAnnouncementDetail(response.data);
};

export const createAdminAnnouncement = async (payload: AdminAnnouncementPayload): Promise<AdminAnnouncementDetail> => {
  const response = await http.post('/admin/announcements', payload) as ApiResponse<unknown>;
  return normalizeAdminAnnouncementDetail(response.data);
};

export const updateAdminAnnouncement = async (id: number, payload: AdminAnnouncementPayload): Promise<AdminAnnouncementDetail> => {
  const response = await http.put(`/admin/announcements/${id}`, payload) as ApiResponse<unknown>;
  return normalizeAdminAnnouncementDetail(response.data);
};

export const publishAdminAnnouncement = async (id: number): Promise<AdminAnnouncementDetail> => {
  const response = await http.put(`/admin/announcements/${id}/publish`) as ApiResponse<unknown>;
  return normalizeAdminAnnouncementDetail(response.data);
};

export const offlineAdminAnnouncement = async (id: number): Promise<AdminAnnouncementDetail> => {
  const response = await http.put(`/admin/announcements/${id}/offline`) as ApiResponse<unknown>;
  return normalizeAdminAnnouncementDetail(response.data);
};

export const deleteAdminAnnouncement = async (id: number): Promise<boolean> => {
  const response = await http.delete(`/admin/announcements/${id}`) as ApiResponse<unknown>;
  return toBoolean(response.data, true);
};
