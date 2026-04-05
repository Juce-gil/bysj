export type NotificationTagType = 'success' | 'warning' | 'info' | 'danger' | 'primary';

const normalizeType = (type: string) => type.trim().toLowerCase();

const tradeTypes = new Set([
  'favorite',
  'comment',
  'appointment_apply',
  'appointment_cancel',
  'appointment_complete',
]);

export const isTradeNotification = (type: string) => tradeTypes.has(normalizeType(type));

export const getNotificationTypeMeta = (type: string): { text: string; type: NotificationTagType } => {
  const normalized = normalizeType(type);

  if (normalized === 'announcement') {
    return { text: '公告通知', type: 'info' };
  }

  if (tradeTypes.has(normalized)) {
    return { text: '交易提醒', type: normalized === 'favorite' || normalized === 'comment' ? 'success' : 'warning' };
  }

  return { text: '系统通知', type: 'info' };
};

export const getNotificationActionLabel = (type: string) => {
  const normalized = normalizeType(type);

  if (normalized === 'announcement') {
    return '查看公告';
  }

  if (normalized === 'favorite' || normalized === 'comment') {
    return '查看商品';
  }

  if (normalized.startsWith('appointment_')) {
    return '查看预约';
  }

  return '查看详情';
};

export const resolveNotificationTarget = (type: string, relatedId?: number | null): string | null => {
  const normalized = normalizeType(type);

  if (normalized === 'announcement' && relatedId) {
    return `/announcements/${relatedId}`;
  }

  if ((normalized === 'favorite' || normalized === 'comment') && relatedId) {
    return `/goods/${relatedId}`;
  }

  if (normalized.startsWith('appointment_')) {
    return '/user/appointments';
  }

  return null;
};
