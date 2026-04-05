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

export interface AdminGoodsItem {
  id: number;
  title: string;
  price: number;
  category: string;
  sellerName: string;
  favoriteCount: number;
  status: string;
  publishedAt: string;
}

export interface AdminGoodsDetail extends AdminGoodsItem {
  originalPrice: number;
  campus: string;
  condition: string;
  intro: string;
  description: string;
  tags: string[];
}

export interface AdminGoodsQuery {
  keyword?: string;
  status?: string;
  category?: string;
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
const toBoolean = (value: unknown, fallback = false): boolean => {
  if (typeof value === 'boolean') return value;
  if (typeof value === 'number') return value !== 0;
  if (typeof value === 'string') {
    const normalized = value.trim().toLowerCase();
    if (['true', '1', 'yes', 'y'].includes(normalized)) return true;
    if (['false', '0', 'no', 'n'].includes(normalized)) return false;
  }
  return fallback;
};
const toStringArray = (value: unknown): string[] => Array.isArray(value)
  ? value.filter((item): item is string => typeof item === 'string')
  : [];

const normalizeAdminGoodsItem = (value: unknown): AdminGoodsItem => {
  const item = toRecord(value);
  return {
    id: toNumber(item.id),
    title: toString(item.title, '未命名商品'),
    price: toNumber(item.price),
    category: toString(item.category, '未分类'),
    sellerName: toString(item.sellerName ?? item.seller, '未知卖家'),
    favoriteCount: toNumber(item.favoriteCount),
    status: toString(item.status, 'unknown'),
    publishedAt: toString(item.publishedAt),
  };
};

const normalizeAdminGoodsDetail = (value: unknown): AdminGoodsDetail => {
  const detail = toRecord(value);
  const item = normalizeAdminGoodsItem(detail);
  return {
    ...item,
    originalPrice: toNumber(detail.originalPrice),
    campus: toString(detail.campus, '未知校区'),
    condition: toString(detail.condition, '成色待补充'),
    intro: toString(detail.intro, '暂无简介'),
    description: toString(detail.description, '暂无描述'),
    tags: toStringArray(detail.tags),
  };
};

const normalizePageResult = <T>(value: unknown, normalizeItem: (item: unknown) => T): PageResult<T> => {
  const page = toRecord(value);
  const recordsSource = Array.isArray(page.records)
    ? page.records
    : (Array.isArray(page.list) ? page.list : []);
  return {
    records: recordsSource.map(normalizeItem),
    total: toNumber(page.total),
    pageNum: toNumber(page.pageNum ?? page.current, 1),
    pageSize: toNumber(page.pageSize ?? page.size, recordsSource.length || 10),
  };
};

export const getAdminGoodsPage = async (params: AdminGoodsQuery): Promise<PageResult<AdminGoodsItem>> => {
  const response = await http.get('/admin/goods', { params }) as ApiResponse<unknown>;
  return normalizePageResult(response.data, normalizeAdminGoodsItem);
};

export const getAdminGoodsDetail = async (id: number): Promise<AdminGoodsDetail> => {
  const response = await http.get(`/admin/goods/${id}`) as ApiResponse<unknown>;
  return normalizeAdminGoodsDetail(response.data);
};

export const offShelfAdminGoods = async (id: number): Promise<AdminGoodsDetail> => {
  const response = await http.put(`/admin/goods/${id}/off-shelf`) as ApiResponse<unknown>;
  return normalizeAdminGoodsDetail(response.data);
};

export const relistAdminGoods = async (id: number): Promise<AdminGoodsDetail> => {
  const response = await http.put(`/admin/goods/${id}/relist`) as ApiResponse<unknown>;
  return normalizeAdminGoodsDetail(response.data);
};

export const deleteAdminGoods = async (id: number): Promise<boolean> => {
  const response = await http.delete(`/admin/goods/${id}`) as ApiResponse<unknown>;
  return toBoolean(response.data, true);
};