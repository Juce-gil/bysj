export type StatusTagType = 'success' | 'warning' | 'info' | 'danger' | 'primary';

const normalizeStatus = (status: string) => status.trim().toLowerCase();

const goodsStatusTextMap: Record<string, string> = {
  on_sale: '在售中',
  selling: '在售中',
  reserved: '已预约',
  booked: '已预约',
  sold: '已售出',
  finished: '已售出',
  off_shelf: '已下架',
  offsale: '已下架',
  removed: '已下架',
};

const goodsStatusTypeMap: Record<string, StatusTagType> = {
  on_sale: 'success',
  selling: 'success',
  reserved: 'warning',
  booked: 'warning',
  sold: 'info',
  finished: 'info',
  off_shelf: 'danger',
  offsale: 'danger',
  removed: 'danger',
};

const wantedStatusTextMap: Record<string, string> = {
  buying: '求购中',
  open: '求购中',
  active: '求购中',
  finished: '已完成',
  completed: '已完成',
  matched: '已完成',
  closed: '已关闭',
  cancelled: '已关闭',
  expired: '已关闭',
};

const wantedStatusTypeMap: Record<string, StatusTagType> = {
  buying: 'success',
  open: 'success',
  active: 'success',
  finished: 'info',
  completed: 'info',
  matched: 'info',
  closed: 'warning',
  cancelled: 'warning',
  expired: 'warning',
};

const appointmentStatusTextMap: Record<string, string> = {
  pending: '待确认',
  waiting: '待确认',
  agreed: '已同意',
  confirmed: '已同意',
  rejected: '已拒绝',
  refused: '已拒绝',
  cancelled: '已取消',
  canceled: '已取消',
  completed: '已完成',
  done: '已完成',
};

const appointmentStatusTypeMap: Record<string, StatusTagType> = {
  pending: 'warning',
  waiting: 'warning',
  agreed: 'success',
  confirmed: 'success',
  rejected: 'danger',
  refused: 'danger',
  cancelled: 'info',
  canceled: 'info',
  completed: 'success',
  done: 'success',
};

const announcementLevelTypeMap: Record<string, StatusTagType> = {
  通知: 'info',
  活动: 'success',
  提示: 'warning',
};

const getStatusMeta = (
  status: string,
  textMap: Record<string, string>,
  typeMap: Record<string, StatusTagType>,
) => {
  const key = normalizeStatus(status || '');
  return {
    text: textMap[key] || status || '未知状态',
    type: typeMap[key] || 'info',
  };
};

export const getGoodsStatusMeta = (status: string) => getStatusMeta(status, goodsStatusTextMap, goodsStatusTypeMap);

export const getWantedStatusMeta = (status: string) => getStatusMeta(status, wantedStatusTextMap, wantedStatusTypeMap);

export const getAppointmentStatusMeta = (status: string) => getStatusMeta(status, appointmentStatusTextMap, appointmentStatusTypeMap);

export const getAnnouncementLevelTagType = (level: string): StatusTagType => announcementLevelTypeMap[level] || 'info';
