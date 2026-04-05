<template>
  <div class="dashboard-page">
    <section class="grid-cards grid-cards--4">
      <article v-for="item in stats" :key="item.label" class="stat-card qh-panel">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
        <p>{{ item.tip }}</p>
      </article>
    </section>

    <el-alert v-if="errorMessage" class="page-section" type="warning" show-icon :closable="false" :title="errorMessage" />

    <section class="page-section grid-layout">
      <SectionCard title="快捷操作" subtitle="已补齐通知、收藏、预约、发布与账号设置入口。">
        <div class="quick-actions">
          <RouterLink v-for="item in actions" :key="item.title" :to="item.to" class="quick-card qh-panel--subtle">
            <strong>{{ item.title }}</strong>
            <p>{{ item.description }}</p>
          </RouterLink>
        </div>
      </SectionCard>
      <SectionCard title="个人资料概览" subtitle="资料与联系方式来自真实登录接口返回。">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="昵称">{{ userStore.displayName }}</el-descriptions-item>
          <el-descriptions-item label="学校">{{ userStore.profile?.school || '科成' }}</el-descriptions-item>
          <el-descriptions-item label="角色">{{ userStore.role === 'admin' ? '管理员' : '普通用户' }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ userStore.profile?.phone || '暂未提供' }}</el-descriptions-item>
          <el-descriptions-item label="QQ">{{ userStore.profile?.qq || '暂未提供' }}</el-descriptions-item>
          <el-descriptions-item label="个性签名">{{ userStore.profile?.slogan || '欢迎来到校园跳蚤市场。' }}</el-descriptions-item>
        </el-descriptions>
      </SectionCard>
    </section>

    <section class="page-section grid-layout">
      <SectionCard title="最新通知" subtitle="支持查看未读状态，并根据通知类型跳转到商品详情、公告详情或预约列表。">
        <template #extra>
          <div class="card-actions">
            <el-button text :loading="loading" @click="loadData">刷新</el-button>
            <RouterLink class="table-link" to="/user/notifications">查看全部</RouterLink>
          </div>
        </template>
        <el-skeleton v-if="loading" :rows="5" animated />
        <div v-else-if="notifications.length" class="notification-list">
          <article v-for="item in notifications" :key="item.id" class="notification-item qh-panel--subtle">
            <div class="notification-head">
              <div>
                <strong>{{ item.title }}</strong>
                <p>{{ item.content }}</p>
              </div>
              <div class="notification-tags">
                <el-tag size="small" effect="plain" :type="getNotificationTypeMeta(item.type).type">
                  {{ getNotificationTypeMeta(item.type).text }}
                </el-tag>
                <el-tag size="small" :type="item.isRead ? 'info' : 'primary'">{{ item.isRead ? '已读' : '未读' }}</el-tag>
              </div>
            </div>
            <div class="notification-actions">
              <span>{{ item.createTime }}</span>
              <div class="notification-actions__buttons">
                <el-button v-if="!item.isRead" link type="primary" @click="handleMarkRead(item)">标记已读</el-button>
                <el-button
                  v-if="resolveNotificationTarget(item.type, item.relatedId)"
                  link
                  type="primary"
                  @click="handleOpenNotification(item)"
                >
                  {{ getNotificationActionLabel(item.type) }}
                </el-button>
              </div>
            </div>
          </article>
        </div>
        <EmptyHint v-else title="暂无通知" description="后续预约、评论和收藏提醒都会汇总到这里。" />
      </SectionCard>
      <SectionCard title="我的预约概览" subtitle="已联调预约列表接口，可直接进入预约页继续查看。">
        <template #extra>
          <RouterLink class="table-link" to="/user/appointments">查看全部</RouterLink>
        </template>
        <el-skeleton v-if="loading" :rows="5" animated />
        <div v-else-if="appointments.length" class="appointment-list">
          <article v-for="item in appointments" :key="item.id" class="quick-card qh-panel--subtle">
            <div class="appointment-head">
              <strong>{{ item.goodsTitle }}</strong>
              <el-tag size="small" effect="light" :type="getAppointmentStatusMeta(item.status).type">{{ getAppointmentStatusMeta(item.status).text }}</el-tag>
            </div>
            <p>预约时间：{{ item.intendedTime }}</p>
            <p>地点：{{ item.intendedLocation || '待协商' }}</p>
          </article>
        </div>
        <EmptyHint v-else title="暂无预约记录" description="去商品详情页发起一次预约试试看。" />
      </SectionCard>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { RouterLink, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import SectionCard from '@/components/SectionCard.vue';
import EmptyHint from '@/components/EmptyHint.vue';
import { getMyAppointments, getMyFavorites, getNotifications, getUnreadNotificationCount, markNotificationRead, type AppointmentItem, type NotificationItem } from '@/api/marketplace';
import { useUserStore } from '@/stores/user';
import { getAppointmentStatusMeta } from '@/utils/status';
import { getNotificationActionLabel, getNotificationTypeMeta, resolveNotificationTarget } from '@/utils/notification';

const router = useRouter();
const userStore = useUserStore();
const notifications = ref<NotificationItem[]>([]);
const appointments = ref<AppointmentItem[]>([]);
const unreadCount = ref(0);
const favoriteCount = ref(0);
const loading = ref(false);
const errorMessage = ref('');

const stats = computed(() => [
  { label: '我的收藏', value: String(favoriteCount.value).padStart(2, '0'), tip: '已接入收藏列表接口' },
  { label: '我的预约', value: String(appointments.value.length).padStart(2, '0'), tip: '已接入预约列表接口' },
  { label: '未读消息', value: String(unreadCount.value).padStart(2, '0'), tip: '可点击跳转并同步标记已读' },
  { label: '账号角色', value: userStore.role === 'admin' ? 'AD' : 'US', tip: '当前登录身份' },
]);

const actions = [
  { title: '通知中心', description: '查看全部站内通知，并根据类型跳转到对应页面。', to: '/user/notifications' },
  { title: '我的收藏', description: '查看已收藏的商品，并支持快速取消收藏。', to: '/user/favorites' },
  { title: '我的预约', description: '查看预约时间、地点与当前处理状态。', to: '/user/appointments' },
  { title: '账号设置', description: '同步当前账号资料，查看手机号、QQ 与签名。', to: '/user/profile' },
  { title: '发布管理', description: '查看我发布的商品与求购，后续可继续扩展编辑和上下架。', to: '/user/posts' },
];

const syncReadState = (id: number) => {
  const target = notifications.value.find((item) => item.id === id);
  if (target && !target.isRead) {
    target.isRead = true;
    unreadCount.value = Math.max(0, unreadCount.value - 1);
  }
};

const handleMarkRead = async (item: NotificationItem) => {
  if (item.isRead) {
    return;
  }

  await markNotificationRead(item.id);
  syncReadState(item.id);
  ElMessage.success('通知已标记为已读');
};

const handleOpenNotification = async (item: NotificationItem) => {
  const target = resolveNotificationTarget(item.type, item.relatedId);
  if (!target) {
    ElMessage.info('该通知暂无可跳转的页面');
    return;
  }

  if (!item.isRead) {
    await markNotificationRead(item.id);
    syncReadState(item.id);
  }

  await router.push(target);
};

const loadData = async () => {
  loading.value = true;
  errorMessage.value = '';
  try {
    const [notificationList, appointmentList, unread, favorites] = await Promise.all([
      getNotifications(),
      getMyAppointments(),
      getUnreadNotificationCount(),
      getMyFavorites(),
    ]);
    notifications.value = notificationList.slice(0, 5);
    appointments.value = appointmentList.slice(0, 4);
    unreadCount.value = unread;
    favoriteCount.value = favorites.length;
  } catch (error) {
    console.error('加载用户中心数据失败', error);
    errorMessage.value = '用户中心部分数据加载失败，请稍后刷新重试。';
  } finally {
    loading.value = false;
  }
};

onMounted(loadData);
</script>

<style scoped lang="scss">
.stat-card { padding: 22px; }
.stat-card span { color: var(--qh-text-secondary); }
.stat-card strong { display: block; margin: 14px 0 10px; font-size: 30px; color: var(--qh-primary-deep); }
.stat-card p { margin: 0; color: var(--qh-text-secondary); }
.grid-layout { display: grid; grid-template-columns: 1.1fr 0.9fr; gap: 24px; }
.quick-actions, .appointment-list, .notification-list { display: grid; gap: 16px; }
.quick-card, .notification-item { padding: 18px; }
.quick-card { text-decoration: none; transition: transform 0.2s ease; }
.quick-card:hover { transform: translateY(-2px); }
.quick-card strong, .notification-item strong { display: block; margin-bottom: 8px; color: var(--qh-text-primary); }
.quick-card p, .notification-item p { margin: 0; line-height: 1.8; color: var(--qh-text-secondary); }
.notification-head, .appointment-head, .card-actions, .notification-actions, .notification-actions__buttons, .notification-tags {
  display: flex;
  gap: 12px;
}
.card-actions { align-items: center; }
.notification-head { justify-content: space-between; align-items: flex-start; }
.notification-tags { flex-wrap: wrap; }
.notification-actions { justify-content: space-between; align-items: center; margin-top: 12px; color: var(--qh-text-secondary); font-size: 13px; }
.notification-actions__buttons { flex-wrap: wrap; }
.table-link { color: var(--qh-primary-deep); text-decoration: none; }
@media (max-width: 960px) {
  .grid-layout { grid-template-columns: 1fr; }
  .notification-head, .notification-actions { flex-direction: column; align-items: flex-start; }
}
</style>
