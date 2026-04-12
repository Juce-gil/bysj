<template>
  <div class="notifications-page">
    <section class="grid-cards grid-cards--4">
      <article v-for="item in stats" :key="item.label" class="stat-card qh-panel">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
        <p>{{ item.tip }}</p>
      </article>
    </section>

    <SectionCard title="我的通知" subtitle="集中查看交易提醒、公告动态与系统消息，并支持在用户中心内快速联动跳转。">
      <template #extra>
        <div class="toolbar">
          <el-select v-model="typeFilter" placeholder="消息类型" class="toolbar-select">
            <el-option label="全部类型" value="" />
            <el-option label="交易消息" value="trade" />
            <el-option label="公告消息" value="announcement" />
            <el-option label="系统消息" value="system" />
          </el-select>
          <el-switch v-model="unreadOnly" inline-prompt active-text="仅未读" inactive-text="全部" />
          <el-button text :loading="loading" @click="loadNotifications">刷新</el-button>
        </div>
      </template>

      <div class="notifications-summary qh-panel--subtle">
        <article>
          <span>当前筛选</span>
          <strong>{{ currentFilterSummary }}</strong>
          <p>可按消息类型与已读状态快速定位内容</p>
        </article>
        <article>
          <span>未读消息</span>
          <strong>{{ unreadCount }}</strong>
          <p>优先处理交易相关或待确认提醒</p>
        </article>
        <article>
          <span>展示结果</span>
          <strong>{{ filteredNotifications.length }}</strong>
          <p>{{ notifications.length ? '当前列表已接入真实通知接口' : '暂时还没有收到新的通知' }}</p>
        </article>
      </div>

      <el-skeleton v-if="loading && !notifications.length && !errorMessage" :rows="8" animated />
      <div v-else-if="errorMessage" class="state-panel qh-panel--subtle">
        <el-result icon="warning" title="通知列表加载失败" :sub-title="errorMessage">
          <template #extra>
            <el-button type="primary" @click="loadNotifications">重新加载</el-button>
          </template>
        </el-result>
      </div>
      <div v-else-if="filteredNotifications.length" class="notification-list">
        <article v-for="item in filteredNotifications" :key="item.id" class="notification-card qh-panel--subtle">
          <div class="notification-card__head">
            <div class="notification-card__title">
              <strong>{{ item.title }}</strong>
              <span>{{ item.createTime }}</span>
            </div>
            <div class="notification-card__tags">
              <el-tag size="small" effect="plain" :type="getNotificationTypeMeta(item.type).type">
                {{ getNotificationTypeMeta(item.type).text }}
              </el-tag>
              <el-tag size="small" :type="item.isRead ? 'info' : 'primary'">{{ item.isRead ? '已读' : '未读' }}</el-tag>
            </div>
          </div>
          <p class="notification-card__content">{{ item.content }}</p>
          <div class="notification-card__actions">
            <el-button
              v-if="!item.isRead"
              link
              type="primary"
              :loading="isPending(item.id)"
              @click="handleMarkRead(item)"
            >
              标记已读
            </el-button>
            <el-button
              v-if="resolveNotificationTarget(item.type, item.relatedId)"
              link
              type="primary"
              :loading="isPending(item.id)"
              @click="handleOpenNotification(item)"
            >
              {{ getNotificationActionLabel(item.type) }}
            </el-button>
            <span v-else class="subtle-text">该通知当前没有可跳转的详情页面</span>
          </div>
        </article>
      </div>
      <EmptyHint
        v-else
        :title="notifications.length ? '没有符合条件的通知' : '暂时没有通知消息'"
        :description="notifications.length ? '可以切换筛选条件，查看其它类型或已读状态的消息。' : '新的交易提醒、公告或系统消息到来后，会第一时间汇总到这里。'"
      />
    </SectionCard>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';
import SectionCard from '@/components/SectionCard.vue';
import EmptyHint from '@/components/EmptyHint.vue';
import { getNotifications, getUnreadNotificationCount, markNotificationRead, type NotificationItem } from '@/api/marketplace';
import {
  getNotificationActionLabel,
  getNotificationTypeMeta,
  isTradeNotification,
  resolveNotificationTarget,
} from '@/utils/notification';

const router = useRouter();
const notifications = ref<NotificationItem[]>([]);
const unreadCount = ref(0);
const loading = ref(false);
const errorMessage = ref('');
const unreadOnly = ref(false);
const typeFilter = ref('');
const pendingIds = ref<number[]>([]);

const stats = computed(() => {
  const announcementCount = notifications.value.filter((item) => item.type === 'announcement').length;
  const tradeCount = notifications.value.filter((item) => isTradeNotification(item.type)).length;
  return [
    { label: '通知总数', value: String(notifications.value.length).padStart(2, '0'), tip: '展示当前所有通知消息' },
    { label: '未读消息', value: String(unreadCount.value).padStart(2, '0'), tip: '帮助你快速跟进待处理内容' },
    { label: '交易提醒', value: String(tradeCount).padStart(2, '0'), tip: '通常关联预约、商品或求购流程' },
    { label: '公告动态', value: String(announcementCount).padStart(2, '0'), tip: '便于及时查看平台公告' },
  ];
});

const filteredNotifications = computed(() =>
  notifications.value.filter((item) => {
    if (unreadOnly.value && item.isRead) {
      return false;
    }

    if (typeFilter.value === 'trade') {
      return isTradeNotification(item.type);
    }

    if (typeFilter.value === 'announcement') {
      return item.type === 'announcement';
    }

    if (typeFilter.value === 'system') {
      return !isTradeNotification(item.type) && item.type !== 'announcement';
    }

    return true;
  }),
);

const currentFilterSummary = computed(() => {
  const parts: string[] = [];
  if (typeFilter.value === 'trade') parts.push('交易消息');
  if (typeFilter.value === 'announcement') parts.push('公告消息');
  if (typeFilter.value === 'system') parts.push('系统消息');
  if (unreadOnly.value) parts.push('仅未读');
  return parts.join(' · ') || '显示全部通知';
});

const isPending = (id: number) => pendingIds.value.includes(id);

const syncReadState = (id: number) => {
  const target = notifications.value.find((item) => item.id === id);
  if (target && !target.isRead) {
    target.isRead = true;
    unreadCount.value = Math.max(0, unreadCount.value - 1);
  }
};

const runWithPending = async (id: number, task: () => Promise<void>) => {
  if (isPending(id)) {
    return;
  }

  pendingIds.value = [...pendingIds.value, id];
  try {
    await task();
  } finally {
    pendingIds.value = pendingIds.value.filter((item) => item !== id);
  }
};

const handleMarkRead = async (item: NotificationItem) => {
  if (item.isRead) {
    return;
  }

  await runWithPending(item.id, async () => {
    await markNotificationRead(item.id);
    syncReadState(item.id);
    ElMessage.success('通知已标记为已读');
  });
};

const handleOpenNotification = async (item: NotificationItem) => {
  const target = resolveNotificationTarget(item.type, item.relatedId);
  if (!target) {
    ElMessage.info('当前通知暂时没有可跳转的目标页面');
    return;
  }

  await runWithPending(item.id, async () => {
    if (!item.isRead) {
      await markNotificationRead(item.id);
      syncReadState(item.id);
    }
    await router.push(target);
  });
};

const loadNotifications = async () => {
  loading.value = true;
  errorMessage.value = '';

  try {
    const [list, unread] = await Promise.all([getNotifications(), getUnreadNotificationCount()]);
    notifications.value = list;
    unreadCount.value = unread;
  } catch (error) {
    console.error('加载通知列表失败', error);
    notifications.value = [];
    unreadCount.value = 0;
    errorMessage.value = '通知列表暂时不可用，请稍后重新尝试。';
  } finally {
    loading.value = false;
  }
};

onMounted(loadNotifications);
</script>

<style scoped lang="scss">
.stat-card { padding: 22px; }
.stat-card span { color: var(--qh-text-secondary); }
.stat-card strong { display: block; margin: 14px 0 10px; font-size: 30px; color: var(--qh-primary-deep); }
.stat-card p { margin: 0; color: var(--qh-text-secondary); }
.toolbar { display: flex; align-items: center; gap: 12px; }
.toolbar-select { width: 180px; }
.notifications-summary {
  margin-bottom: 18px;
  padding: 16px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}
.notifications-summary article {
  padding: 16px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.78);
}
.notifications-summary span { display: block; color: var(--qh-text-secondary); }
.notifications-summary strong { display: block; margin-top: 10px; color: var(--qh-text-primary); font-size: 22px; }
.notifications-summary p { margin: 10px 0 0; color: var(--qh-text-secondary); line-height: 1.7; }
.notification-list { display: grid; gap: 16px; }
.notification-card { padding: 18px; }
.notification-card__head,
.notification-card__actions,
.notification-card__tags { display: flex; align-items: center; gap: 10px; }
.notification-card__head { justify-content: space-between; align-items: flex-start; }
.notification-card__title { display: grid; gap: 8px; }
.notification-card__title strong { color: var(--qh-text-primary); line-height: 1.5; }
.notification-card__title span { color: var(--qh-text-secondary); font-size: 13px; }
.notification-card__content { margin: 14px 0 0; color: var(--qh-text-secondary); line-height: 1.8; }
.notification-card__actions { margin-top: 16px; flex-wrap: wrap; }
.state-panel { padding: 12px; }

@media (max-width: 1100px) {
  .notifications-summary { grid-template-columns: 1fr; }
}

@media (max-width: 960px) {
  .toolbar { width: 100%; flex-wrap: wrap; justify-content: flex-end; }
  .toolbar-select { width: 100%; }
  .notification-card__head { flex-direction: column; }
}
</style>
