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

    <section class="page-section">
      <div class="overview-banner qh-panel">
        <div class="overview-banner__main">
          <span class="overview-badge">科成校园个人中心</span>
          <h2>在个人中心，一站式管理我的通知、收藏、预约与发布。</h2>
          <p>
            用户端后台同步采用与前台一致的黄黑暖色风格，保留收藏、预约、消息、发布与资料管理的完整联动能力，
            更适合毕业设计展示和日常使用。
          </p>

          <div class="overview-actions">
            <RouterLink class="overview-action overview-action--primary" to="/user/favorites">
              <span>查看我的收藏</span>
            </RouterLink>
            <RouterLink class="overview-action overview-action--secondary" to="/user/appointments">
              <span>查看预约记录</span>
            </RouterLink>
            <RouterLink class="overview-action overview-action--ghost" to="/user/notifications">
              <span>前往通知中心</span>
            </RouterLink>
          </div>

          <div class="overview-banner__metrics">
            <article>
              <strong>{{ String(unreadCount).padStart(2, '0') }}</strong>
              <span>未读消息</span>
            </article>
            <article>
              <strong>{{ String(favoriteCount).padStart(2, '0') }}</strong>
              <span>收藏商品</span>
            </article>
            <article>
              <strong>{{ String(appointments.length).padStart(2, '0') }}</strong>
              <span>预约记录</span>
            </article>
            <article>
              <strong>{{ userStore.role === 'admin' ? 'AD' : 'US' }}</strong>
              <span>当前身份</span>
            </article>
          </div>
        </div>

        <aside class="overview-banner__aside">
          <div class="overview-aside__card">
            <span>今日推荐</span>
            <strong>让每一条通知、每一次预约和每一件收藏，都在统一风格里更清晰呈现。</strong>
            <p>适合展示用户中心与前台首页之间的风格一致性和交互联动性。</p>
          </div>
          <div class="overview-aside__stats">
            <article>
              <span>我的收藏</span>
              <strong>{{ favoriteCount }}</strong>
            </article>
            <article>
              <span>预约数量</span>
              <strong>{{ appointments.length }}</strong>
            </article>
            <article>
              <span>未读通知</span>
              <strong>{{ unreadCount }}</strong>
            </article>
            <article>
              <span>当前状态</span>
              <strong>在线</strong>
            </article>
          </div>
        </aside>
      </div>
    </section>

    <section class="page-section grid-layout">
      <SectionCard title="快捷入口" subtitle="把常用功能集中到一处，方便快速跳转和完整演示用户链路。">
        <div class="quick-actions">
          <RouterLink v-for="item in actions" :key="item.title" :to="item.to" class="quick-card qh-panel--subtle">
            <span class="quick-card__badge">{{ item.badge }}</span>
            <strong>{{ item.title }}</strong>
            <p>{{ item.description }}</p>
          </RouterLink>
        </div>
      </SectionCard>

      <SectionCard title="账号概览" subtitle="展示当前登录用户的基础资料与联系信息摘要。">
        <div class="profile-hero qh-panel--subtle">
          <div>
            <span>当前账号</span>
            <strong>{{ userStore.displayName }}</strong>
            <p>{{ userStore.profile?.school || '未设置学校' }} · {{ userStore.role === 'admin' ? '管理员' : '普通用户' }}</p>
          </div>
          <small>{{ userStore.profile?.slogan || '还没有填写个性签名，去资料页完善一下吧。' }}</small>
        </div>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="姓名">{{ userStore.displayName }}</el-descriptions-item>
          <el-descriptions-item label="学校">{{ userStore.profile?.school || '未设置学校' }}</el-descriptions-item>
          <el-descriptions-item label="角色">{{ userStore.role === 'admin' ? '管理员' : '普通用户' }}</el-descriptions-item>
          <el-descriptions-item label="电话">{{ userStore.profile?.phone || '未填写' }}</el-descriptions-item>
          <el-descriptions-item label="QQ">{{ userStore.profile?.qq || '未填写' }}</el-descriptions-item>
          <el-descriptions-item label="签名">{{ userStore.profile?.slogan || '还没有填写个性签名。' }}</el-descriptions-item>
        </el-descriptions>
      </SectionCard>
    </section>

    <section class="page-section grid-layout">
      <SectionCard title="最近通知" subtitle="优先展示最新消息，支持快速标记已读并跳转到对应页面。">
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
        <EmptyHint v-else title="暂时没有通知" description="新消息到达后，会在这里优先展示最近几条内容。" />
      </SectionCard>

      <SectionCard title="最近预约" subtitle="展示最近预约记录，帮助你快速回看线下面交安排。">
        <template #extra>
          <RouterLink class="table-link" to="/user/appointments">查看全部</RouterLink>
        </template>
        <el-skeleton v-if="loading" :rows="5" animated />
        <div v-else-if="appointments.length" class="appointment-list">
          <article v-for="item in appointments" :key="item.id" class="quick-card qh-panel--subtle">
            <div class="appointment-head">
              <strong>{{ item.goodsTitle }}</strong>
              <el-tag size="small" effect="light" :type="getAppointmentStatusMeta(item.status).type">
                {{ getAppointmentStatusMeta(item.status).text }}
              </el-tag>
            </div>
            <p>预约时间：{{ item.intendedTime }}</p>
            <p>面交地点：{{ item.intendedLocation || '待协商' }}</p>
          </article>
        </div>
        <EmptyHint v-else title="还没有预约记录" description="发起预约后，这里会展示最近的预约时间和地点。" />
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
import {
  getMyAppointments,
  getMyFavorites,
  getNotifications,
  getUnreadNotificationCount,
  markNotificationRead,
  type AppointmentItem,
  type NotificationItem,
} from '@/api/marketplace';
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
  { label: '收藏数量', value: String(favoriteCount.value).padStart(2, '0'), tip: '已接入真实收藏列表' },
  { label: '预约记录', value: String(appointments.value.length).padStart(2, '0'), tip: '展示最近预约动态' },
  { label: '未读通知', value: String(unreadCount.value).padStart(2, '0'), tip: '帮助你及时跟进待处理事项' },
  { label: '当前身份', value: userStore.role === 'admin' ? 'AD' : 'US', tip: '支持前后台角色联动' },
]);

const actions = [
  { badge: '01', title: '消息中心', description: '集中查看系统通知、交易提醒与公告消息。', to: '/user/notifications' },
  { badge: '02', title: '我的收藏', description: '回看收藏商品，继续比较、预约和查看详情。', to: '/user/favorites' },
  { badge: '03', title: '我的预约', description: '快速确认最近预约时间、地点与处理状态。', to: '/user/appointments' },
  { badge: '04', title: '资料设置', description: '维护电话、QQ 和个性签名等账号信息。', to: '/user/profile' },
  { badge: '05', title: '我的发布', description: '统一管理商品与求购内容，形成完整展示链路。', to: '/user/posts' },
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
  ElMessage.success('已标记为已读');
};

const handleOpenNotification = async (item: NotificationItem) => {
  const target = resolveNotificationTarget(item.type, item.relatedId);
  if (!target) {
    ElMessage.info('当前通知暂时没有可跳转的目标页面');
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
    console.error('加载用户中心概览失败', error);
    errorMessage.value = '用户中心概览加载失败，请稍后重新刷新。';
  } finally {
    loading.value = false;
  }
};

onMounted(loadData);
</script>

<style scoped lang="scss">
.stat-card {
  padding: 22px;
}

.stat-card span {
  color: var(--qh-text-secondary);
}

.stat-card strong {
  display: block;
  margin: 14px 0 10px;
  font-size: 30px;
  color: var(--qh-primary-deep);
}

.stat-card p {
  margin: 0;
  color: var(--qh-text-secondary);
}

.overview-banner {
  padding: 28px;
  display: grid;
  grid-template-columns: minmax(0, 1.42fr) minmax(340px, 0.58fr);
  gap: 26px;
  background: linear-gradient(180deg, rgba(255, 248, 219, 0.98) 0%, rgba(255, 239, 175, 0.96) 100%);
}

.overview-badge {
  display: inline-flex;
  align-items: center;
  min-height: 38px;
  padding: 0 16px;
  border-radius: 999px;
  background: rgba(32, 39, 51, 0.96);
  color: #ffe27a;
  font-size: 13px;
  font-weight: 700;
}

.overview-banner__main h2 {
  margin: 18px 0 14px;
  font-size: clamp(34px, 4vw, 58px);
  line-height: 1.14;
  color: var(--qh-text-primary);
}

.overview-banner__main > p {
  margin: 0;
  line-height: 1.9;
  color: var(--qh-text-secondary);
  font-size: 18px;
}

.overview-actions {
  margin-top: 28px;
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.overview-action {
  min-height: 58px;
  padding: 0 24px;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  text-decoration: none;
  font-weight: 800;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.overview-action:hover {
  transform: translateY(-1px);
  box-shadow: 0 12px 24px rgba(128, 100, 14, 0.12);
}

.overview-action--primary {
  background: #202733;
  color: #ffe27a;
}

.overview-action--secondary {
  background: rgba(255, 251, 238, 0.96);
  color: var(--qh-text-primary);
}

.overview-action--ghost {
  background: rgba(77, 56, 13, 0.92);
  color: #ffe7a0;
}

.overview-banner__metrics {
  margin-top: 28px;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.overview-banner__metrics article {
  padding: 18px 20px;
  border-radius: 24px;
  background: rgba(255, 252, 240, 0.92);
}

.overview-banner__metrics strong {
  display: block;
  font-size: 22px;
  color: var(--qh-text-primary);
}

.overview-banner__metrics span {
  display: block;
  margin-top: 8px;
  color: var(--qh-text-secondary);
}

.overview-banner__aside {
  padding: 18px;
  border-radius: 32px;
  background: rgba(231, 238, 232, 0.9);
  display: grid;
  gap: 18px;
}

.overview-aside__card {
  padding: 28px;
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.78);
}

.overview-aside__card span {
  display: block;
  color: #8a6b20;
  font-size: 14px;
  font-weight: 700;
}

.overview-aside__card strong {
  display: block;
  margin-top: 16px;
  font-size: 28px;
  line-height: 1.45;
  color: var(--qh-text-primary);
}

.overview-aside__card p {
  margin: 18px 0 0;
  color: var(--qh-text-secondary);
  line-height: 1.8;
}

.overview-aside__stats {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.overview-aside__stats article,
.profile-hero {
  padding: 18px 20px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.86);
}

.overview-aside__stats span,
.profile-hero span {
  display: block;
  color: var(--qh-text-secondary);
}

.overview-aside__stats strong,
.profile-hero strong {
  display: block;
  margin-top: 10px;
  color: var(--qh-text-primary);
  font-size: 24px;
}

.profile-hero {
  margin-bottom: 18px;
}

.profile-hero p,
.profile-hero small {
  display: block;
  margin-top: 10px;
  line-height: 1.8;
  color: var(--qh-text-secondary);
}

.grid-layout {
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  gap: 24px;
}

.quick-actions,
.appointment-list,
.notification-list {
  display: grid;
  gap: 16px;
}

.quick-actions {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.quick-card,
.notification-item {
  padding: 18px;
}

.quick-card {
  text-decoration: none;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.quick-card:hover {
  transform: translateY(-2px);
}

.quick-card__badge {
  display: inline-flex;
  align-items: center;
  min-height: 32px;
  padding: 0 12px;
  border-radius: 999px;
  background: rgba(32, 39, 51, 0.94);
  color: #ffe27a;
  font-size: 12px;
  font-weight: 700;
}

.quick-card strong,
.notification-item strong {
  display: block;
  margin: 12px 0 8px;
  color: var(--qh-text-primary);
}

.quick-card p,
.notification-item p {
  margin: 0;
  line-height: 1.8;
  color: var(--qh-text-secondary);
}

.notification-head,
.appointment-head,
.card-actions,
.notification-actions,
.notification-actions__buttons,
.notification-tags {
  display: flex;
  gap: 12px;
}

.card-actions {
  align-items: center;
}

.notification-head {
  justify-content: space-between;
  align-items: flex-start;
}

.notification-tags {
  flex-wrap: wrap;
}

.notification-actions {
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
  color: var(--qh-text-secondary);
  font-size: 13px;
}

.notification-actions__buttons {
  flex-wrap: wrap;
}

.table-link {
  color: var(--qh-primary-deep);
  text-decoration: none;
}

@media (max-width: 1200px) {
  .overview-banner,
  .grid-layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 960px) {
  .quick-actions {
    grid-template-columns: 1fr;
  }

  .notification-head,
  .notification-actions {
    flex-direction: column;
    align-items: flex-start;
  }
}

@media (max-width: 768px) {
  .overview-actions,
  .overview-banner__metrics,
  .overview-aside__stats {
    grid-template-columns: 1fr;
  }

  .overview-banner__metrics {
    display: grid;
  }
}
</style>
