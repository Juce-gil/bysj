const MESSAGE_MAP: Array<[RegExp, string]> = [
  [/account or password is incorrect/i, '账号或密码错误'],
  [/account is disabled/i, '账号已被禁用'],
  [/passwords do not match/i, '两次输入的密码不一致'],
  [/please login before accessing/i, '请先登录后再访问'],
  [/login session is no longer valid/i, '登录状态已失效，请重新登录'],
  [/this endpoint is only available to administrators/i, '当前接口仅管理员可访问'],
  [/conflicting access policy/i, '接口权限配置冲突'],
  [/server error/i, '服务器开小差了，请稍后重试'],
  [/network error/i, '网络异常，请检查连接后重试'],
  [/timeout/i, '请求超时，请稍后重试'],
  [/notification not found/i, '通知不存在'],
];

export const normalizeErrorMessage = (message?: string, fallback = '请求失败，请稍后重试') => {
  const source = (message || '').trim();
  if (!source) {
    return fallback;
  }
  for (const [pattern, replacement] of MESSAGE_MAP) {
    if (pattern.test(source)) {
      return replacement;
    }
  }
  return source;
};

export const extractErrorMessage = (error: unknown, fallback = '请求失败，请稍后重试') => {
  if (typeof error === 'string') {
    return normalizeErrorMessage(error, fallback);
  }
  if (error && typeof error === 'object') {
    const candidate = (error as { response?: { data?: { message?: string } }; message?: string }).response?.data?.message
      || (error as { message?: string }).message;
    return normalizeErrorMessage(candidate, fallback);
  }
  return fallback;
};
