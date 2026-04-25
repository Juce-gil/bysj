import http from '@/api/http';

interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

export interface CategoryItem {
  id: number;
  name: string;
  sortNum: number;
}

export interface GoodsItem {
  id: number;
  sellerId: number;
  title: string;
  price: number;
  originalPrice: number;
  category: string;
  campus: string;
  condition: string;
  seller: string;
  publishedAt: string;
  intro: string;
  description: string;
  tags: string[];
  imageUrls: string[];
  coverImageUrl: string;
  coverStyle: string;
  favoriteCount: number;
  favorited: boolean;
  status: string;
}

export interface UploadImageItem {
  url: string;
  originalName: string;
  fileName: string;
  size: number;
}

export interface CreateGoodsPayload {
  title: string;
  price: number;
  originalPrice?: number | null;
  category: string;
  campus: string;
  condition: string;
  intro: string;
  description: string;
  tags: string[];
  imageUrls: string[];
}

export interface WantedItem {
  id: number;
  title: string;
  budget: string;
  category: string;
  campus: string;
  publisher: string;
  deadline: string;
  intro: string;
  description: string;
  tags: string[];
  imageUrls: string[];
  coverImageUrl: string;
  coverStyle: string;
  contactVisible: boolean;
  phone: string;
  qq: string;
  status: string;
}

export interface CreateWantedPayload {
  title: string;
  budget: string;
  category: string;
  campus: string;
  deadline: string;
  intro: string;
  description: string;
  tags: string[];
  imageUrls?: string[];
}

export interface AnnouncementItem {
  id: number;
  title: string;
  summary: string;
  content: string;
  publishedAt: string;
  level: '通知' | '活动' | '提示';
}

export interface CommentItem {
  id: number;
  goodsId: number;
  userId: number;
  userName: string;
  content: string;
  createTime: string;
}

export interface AppointmentItem {
  id: number;
  goodsId: number;
  goodsTitle: string;
  buyerId: number;
  buyerName: string;
  sellerId: number;
  sellerName: string;
  intendedTime: string;
  intendedLocation: string;
  remark: string;
  status: string;
}

export interface NotificationItem {
  id: number;
  title: string;
  content: string;
  type: string;
  isRead: boolean;
  relatedId: number | null;
  createTime: string;
}

export interface BannerItem {
  id: number;
  title: string;
  imageUrl: string;
  jumpType: string;
  jumpTarget: string;
  sortNum: number;
}

export interface HomeData {
  stats: Array<{ label: string; value: string }>;
  featuredGoods: GoodsItem[];
  hotWanted: WantedItem[];
  latestAnnouncements: AnnouncementItem[];
  banners: BannerItem[];
}

type RecordValue = Record<string, unknown>;

const DEFAULT_COVER_STYLE = 'linear-gradient(135deg, #ffd666 0%, #ffb703 100%)';
const ANNOUNCEMENT_LEVELS: AnnouncementItem['level'][] = ['通知', '活动', '提示'];

const toRecord = (value: unknown): RecordValue => (value && typeof value === 'object' ? (value as RecordValue) : {});
const toNumber = (value: unknown, fallback = 0): number => {
  const next = typeof value === 'number' ? value : Number(value);
  return Number.isFinite(next) ? next : fallback;
};
const toString = (value: unknown, fallback = ''): string => typeof value === 'string' ? value : fallback;
const toBoolean = (value: unknown, fallback = false): boolean => typeof value === 'boolean' ? value : fallback;
const toStringArray = (value: unknown): string[] => Array.isArray(value) ? value.filter((item): item is string => typeof item === 'string') : [];

const normalizeCategoryItem = (value: unknown): CategoryItem => {
  const item = toRecord(value);
  return {
    id: toNumber(item.id),
    name: toString(item.name, '未分类'),
    sortNum: toNumber(item.sortNum),
  };
};

const normalizeGoodsItem = (value: unknown): GoodsItem => {
  const item = toRecord(value);
  return {
    id: toNumber(item.id),
    sellerId: toNumber(item.sellerId),
    title: toString(item.title, '未命名商品'),
    price: toNumber(item.price),
    originalPrice: toNumber(item.originalPrice),
    category: toString(item.category, '未分类'),
    campus: toString(item.campus, '未知校区'),
    condition: toString(item.condition, '成色待补充'),
    seller: toString(item.seller, '匿名卖家'),
    publishedAt: toString(item.publishedAt),
    intro: toString(item.intro, '暂无简介'),
    description: toString(item.description, '暂无描述'),
    tags: toStringArray(item.tags),
    imageUrls: toStringArray(item.imageUrls),
    coverImageUrl: toString(item.coverImageUrl),
    coverStyle: toString(item.coverStyle, DEFAULT_COVER_STYLE),
    favoriteCount: toNumber(item.favoriteCount),
    favorited: toBoolean(item.favorited),
    status: toString(item.status, 'unknown'),
  };
};

const normalizeWantedItem = (value: unknown): WantedItem => {
  const item = toRecord(value);
  return {
    id: toNumber(item.id),
    title: toString(item.title, '未命名求购'),
    budget: toString(item.budget, '预算待定'),
    category: toString(item.category, '未分类'),
    campus: toString(item.campus, '未知校区'),
    publisher: toString(item.publisher, '匿名发布者'),
    deadline: toString(item.deadline),
    intro: toString(item.intro, '暂无简介'),
    description: toString(item.description, '暂无描述'),
    tags: toStringArray(item.tags),
    imageUrls: toStringArray(item.imageUrls),
    coverImageUrl: toString(item.coverImageUrl),
    coverStyle: toString(item.coverStyle, DEFAULT_COVER_STYLE),
    contactVisible: toBoolean(item.contactVisible),
    phone: toString(item.phone),
    qq: toString(item.qq),
    status: toString(item.status, 'unknown'),
  };
};
const normalizeAnnouncementItem = (value: unknown): AnnouncementItem => {
  const item = toRecord(value);
  const level = toString(item.level, '通知');
  return {
    id: toNumber(item.id),
    title: toString(item.title, '未命名公告'),
    summary: toString(item.summary),
    content: toString(item.content),
    publishedAt: toString(item.publishedAt),
    level: (ANNOUNCEMENT_LEVELS.includes(level as AnnouncementItem['level']) ? level : '通知') as AnnouncementItem['level'],
  };
};

const normalizeBannerItem = (value: unknown): BannerItem => {
  const item = toRecord(value);
  return {
    id: toNumber(item.id),
    title: toString(item.title),
    imageUrl: toString(item.imageUrl),
    jumpType: toString(item.jumpType),
    jumpTarget: toString(item.jumpTarget),
    sortNum: toNumber(item.sortNum),
  };
};

const normalizeCommentItem = (value: unknown): CommentItem => {
  const item = toRecord(value);
  return {
    id: toNumber(item.id),
    goodsId: toNumber(item.goodsId),
    userId: toNumber(item.userId),
    userName: toString(item.userName, '匿名用户'),
    content: toString(item.content),
    createTime: toString(item.createTime),
  };
};

const normalizeAppointmentItem = (value: unknown): AppointmentItem => {
  const item = toRecord(value);
  return {
    id: toNumber(item.id),
    goodsId: toNumber(item.goodsId),
    goodsTitle: toString(item.goodsTitle),
    buyerId: toNumber(item.buyerId),
    buyerName: toString(item.buyerName),
    sellerId: toNumber(item.sellerId),
    sellerName: toString(item.sellerName),
    intendedTime: toString(item.intendedTime),
    intendedLocation: toString(item.intendedLocation),
    remark: toString(item.remark),
    status: toString(item.status, 'pending'),
  };
};

const normalizeNotificationItem = (value: unknown): NotificationItem => {
  const item = toRecord(value);
  return {
    id: toNumber(item.id),
    title: toString(item.title),
    content: toString(item.content),
    type: toString(item.type, 'system'),
    isRead: toBoolean(item.isRead),
    relatedId: item.relatedId == null ? null : toNumber(item.relatedId),
    createTime: toString(item.createTime),
  };
};

const normalizeCategoryList = (value: unknown): CategoryItem[] => Array.isArray(value) ? value.map(normalizeCategoryItem) : [];
const normalizeGoodsList = (value: unknown): GoodsItem[] => Array.isArray(value) ? value.map(normalizeGoodsItem) : [];
const normalizeWantedList = (value: unknown): WantedItem[] => Array.isArray(value) ? value.map(normalizeWantedItem) : [];
const normalizeAnnouncementList = (value: unknown): AnnouncementItem[] => Array.isArray(value) ? value.map(normalizeAnnouncementItem) : [];
const normalizeBannerList = (value: unknown): BannerItem[] => Array.isArray(value) ? value.map(normalizeBannerItem) : [];
const normalizeCommentList = (value: unknown): CommentItem[] => Array.isArray(value) ? value.map(normalizeCommentItem) : [];
const normalizeAppointmentList = (value: unknown): AppointmentItem[] => Array.isArray(value) ? value.map(normalizeAppointmentItem) : [];
const normalizeNotificationList = (value: unknown): NotificationItem[] => Array.isArray(value) ? value.map(normalizeNotificationItem) : [];
const normalizeLocalDateTime = (value: string): string => value.trim().replace(' ', 'T');

const normalizeHomeData = (value: unknown): HomeData => {
  const item = toRecord(value);
  return {
    stats: Array.isArray(item.stats)
      ? item.stats.map((stat) => {
          const record = toRecord(stat);
          return {
            label: toString(record.label, '--'),
            value: toString(record.value, '00'),
          };
        })
      : [],
    featuredGoods: normalizeGoodsList(item.featuredGoods),
    hotWanted: normalizeWantedList(item.hotWanted),
    latestAnnouncements: normalizeAnnouncementList(item.latestAnnouncements),
    banners: normalizeBannerList(item.banners),
  };
};

export const getHomeData = async (): Promise<HomeData> => {
  const response = await http.get('/home') as ApiResponse<unknown>;
  return normalizeHomeData(response.data);
};

export const getCategories = async (): Promise<CategoryItem[]> => {
  const response = await http.get('/categories') as ApiResponse<unknown>;
  return normalizeCategoryList(response.data);
};

export const getGoodsList = async (): Promise<GoodsItem[]> => {
  const response = await http.get('/goods') as ApiResponse<unknown>;
  return normalizeGoodsList(response.data);
};

export const getGoodsDetail = async (id: number): Promise<GoodsItem | null> => {
  const response = await http.get(`/goods/${id}`) as ApiResponse<unknown>;
  return response.data ? normalizeGoodsItem(response.data) : null;
};

export const createGoodsPost = async (payload: CreateGoodsPayload): Promise<GoodsItem> => {
  const response = await http.post('/goods', payload) as ApiResponse<unknown>;
  return normalizeGoodsItem(response.data);
};
export const uploadGoodsImages = async (files: File[]): Promise<UploadImageItem[]> => {
  const formData = new FormData();
  files.forEach((file) => formData.append('files', file));
  const response = await http.post('/uploads/goods-images', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  }) as ApiResponse<unknown>;
  return Array.isArray(response.data)
    ? response.data.map((item) => {
        const record = toRecord(item);
        return {
          url: toString(record.url),
          originalName: toString(record.originalName),
          fileName: toString(record.fileName),
          size: toNumber(record.size),
        };
      })
    : [];
};

export const toggleGoodsFavorite = async (id: number): Promise<boolean> => {
  const response = await http.post(`/goods/${id}/favorite`) as ApiResponse<boolean>;
  return response.data;
};

export const getMyFavorites = async (): Promise<GoodsItem[]> => {
  const response = await http.get('/favorites') as ApiResponse<unknown>;
  return normalizeGoodsList(response.data);
};

export const getComments = async (goodsId: number): Promise<CommentItem[]> => {
  const response = await http.get('/comments', { params: { goodsId } }) as ApiResponse<unknown>;
  return normalizeCommentList(response.data);
};

export const createComment = async (goodsId: number, content: string): Promise<CommentItem> => {
  const response = await http.post('/comments', { goodsId, content }) as ApiResponse<unknown>;
  return normalizeCommentItem(response.data);
};

export const getWantedList = async (): Promise<WantedItem[]> => {
  const response = await http.get('/wanted') as ApiResponse<unknown>;
  return normalizeWantedList(response.data);
};

export const getWantedDetail = async (id: number): Promise<WantedItem | null> => {
  const response = await http.get(`/wanted/${id}`) as ApiResponse<unknown>;
  return response.data ? normalizeWantedItem(response.data) : null;
};

export const createWantedPost = async (payload: CreateWantedPayload): Promise<WantedItem> => {
  const response = await http.post('/wanted', payload) as ApiResponse<unknown>;
  return normalizeWantedItem(response.data);
};

export const getMyGoodsList = async (): Promise<GoodsItem[]> => {
  const response = await http.get('/users/me/goods') as ApiResponse<unknown>;
  return normalizeGoodsList(response.data);
};

export const getMyWantedList = async (): Promise<WantedItem[]> => {
  const response = await http.get('/users/me/wanted') as ApiResponse<unknown>;
  return normalizeWantedList(response.data);
};

export const getAnnouncementList = async (): Promise<AnnouncementItem[]> => {
  const response = await http.get('/announcements') as ApiResponse<unknown>;
  return normalizeAnnouncementList(response.data);
};

export const getAnnouncementDetail = async (id: number): Promise<AnnouncementItem | null> => {
  const response = await http.get(`/announcements/${id}`) as ApiResponse<unknown>;
  return response.data ? normalizeAnnouncementItem(response.data) : null;
};

export const createAppointment = async (payload: {
  goodsId: number;
  intendedTime: string;
  intendedLocation: string;
  remark: string;
}): Promise<AppointmentItem> => {
  const response = await http.post('/appointments', {
    ...payload,
    intendedTime: normalizeLocalDateTime(payload.intendedTime),
  }) as ApiResponse<unknown>;
  return normalizeAppointmentItem(response.data);
};

export const getMyAppointments = async (): Promise<AppointmentItem[]> => {
  const response = await http.get('/appointments/my') as ApiResponse<unknown>;
  return normalizeAppointmentList(response.data);
};

export const cancelAppointment = async (id: number): Promise<AppointmentItem> => {
  const response = await http.put(`/appointments/${id}/cancel`) as ApiResponse<unknown>;
  return normalizeAppointmentItem(response.data);
};

export const getNotifications = async (): Promise<NotificationItem[]> => {
  const response = await http.get('/notifications') as ApiResponse<unknown>;
  return normalizeNotificationList(response.data);
};

export const getUnreadNotificationCount = async (): Promise<number> => {
  const response = await http.get('/notifications/unread-count') as ApiResponse<number>;
  return response.data;
};

export const markNotificationRead = async (id: number): Promise<boolean> => {
  const response = await http.put(`/notifications/${id}/read`) as ApiResponse<boolean>;
  return response.data;
};

export const updateGoodsPost = async (id: number, payload: CreateGoodsPayload): Promise<GoodsItem> => {
  const response = await http.put(`/goods/${id}`, payload) as ApiResponse<unknown>;
  return normalizeGoodsItem(response.data);
};

export const offShelfGoodsPost = async (id: number): Promise<GoodsItem> => {
  const response = await http.put(`/goods/${id}/off-shelf`) as ApiResponse<unknown>;
  return normalizeGoodsItem(response.data);
};

export const relistGoodsPost = async (id: number): Promise<GoodsItem> => {
  const response = await http.put(`/goods/${id}/relist`) as ApiResponse<unknown>;
  return normalizeGoodsItem(response.data);
};

export const deleteGoodsPost = async (id: number): Promise<boolean> => {
  const response = await http.delete(`/goods/${id}`) as ApiResponse<boolean>;
  return response.data;
};

export const updateWantedPost = async (id: number, payload: CreateWantedPayload): Promise<WantedItem> => {
  const response = await http.put(`/wanted/${id}`, payload) as ApiResponse<unknown>;
  return normalizeWantedItem(response.data);
};

export const closeWantedPost = async (id: number): Promise<WantedItem> => {
  const response = await http.put(`/wanted/${id}/close`) as ApiResponse<unknown>;
  return normalizeWantedItem(response.data);
};

export const reopenWantedPost = async (id: number): Promise<WantedItem> => {
  const response = await http.put(`/wanted/${id}/reopen`) as ApiResponse<unknown>;
  return normalizeWantedItem(response.data);
};

export const deleteWantedPost = async (id: number): Promise<boolean> => {
  const response = await http.delete(`/wanted/${id}`) as ApiResponse<boolean>;
  return response.data;
};
