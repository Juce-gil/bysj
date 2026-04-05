<template>
  <div class="notifications-page">
    <section class="grid-cards grid-cards--4">
      <article v-for="item in stats" :key="item.label" class="stat-card qh-panel">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
        <p>{{ item.tip }}</p>
      </article>
    </section>

    <SectionCard title="站内通知" subtitle="支持按状态与类型筛选，并根据通知类型跳转到商品详情、公告详情或预约列表。">
      <template #extra>
        <div class="toolbar">
          <el-select v-model="typeFilter" placeholder="全部类型" class="toolbar-select">
            <el-option label="全部类型" value="" />
            <el-option label="交易提醒" value="trade" />
            <el-option label="公告通知" value="announcement" />
            <el-option label="系统通知" value="system" />
          </el-select>
          <el-switch v-model="unreadOnly" inline-prompt active-text="仅未读" inactive-text="全部" />
          <el-button text :loading="loading" @click="loadNotifications">刷新</el-button>
        </div>
      </template>

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
            <span v-else class="subtle-text">当前通知仅支持站内查看与已读管理</span>
          </div>
        </article>
      </div>
      <EmptyHint
        v-else
        :title="notifications.length ? '没有符合条件的通知' : '暂无站内通知'"
        :description="notifications.length ? '可以切换类型或已读筛选后重试。' : '后续收藏、评论、预约和公告提醒都会汇总到这里。'"
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
import { getNotificationActionLabel, getNotificationTypeMeta, isTradeNotification, resolveNotificationTarget } from '@/utils/notification';

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
    { label: '通知总数', value: String(notifications.value.length).padStart(2, '0'), tip: '当前账号下全部站内通知数量' },
    { label: '未读通知', value: String(unreadCount.value).padStart(2, '0'), tip: '支持进入详情后自动标记已读' },
    { label: '交易提醒', value: String(tradeCount).padStart(2, '0'), tip: '收藏、评论与预约动态都会归档在这里' },
    { label: '公告通知', value: String(announcementCount).padStart(2, '0'), tip: '可直接跳转到公告详情页查看' },
  ];
});

const filteredNotifications = computed(() => notifications.value.filter((item) => {
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
}));

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
    ElMessage.info('该通知暂无可跳转的页面');
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
    const [list, unread] = await Promise.all([
      getNotifications(),
      getUnreadNotificationCount(),
    ]);
    notifications.value = list;
    unreadCount.value = unread;
  } catch (error) {
    console.error('加载通知列表失败', error);
    notifications.value = [];
    unreadCount.value = 0;
    errorMessage.value = '站内通知接口暂时不可用，请稍后刷新重试。';
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

@media (max-width: 960px) {
  .toolbar { width: 100%; flex-wrap: wrap; justify-content: flex-end; }
  .toolbar-select { width: 100%; }
  .notification-card__head { flex-direction: column; }
}
</style>
