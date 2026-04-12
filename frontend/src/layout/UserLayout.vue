<template>
  <div class="user-center-theme">
    <div class="qh-shell user-shell">
      <header class="user-topbar">
        <div class="user-brand">
          <AppLogo variant="market" title="校园跳蚤市场" subtitle="科成用户中心" />
        </div>

        <div class="user-command">
          <el-icon><Bell /></el-icon>
          <div class="user-command__content">
            <strong>用户中心联动视图</strong>
            <span>集中查看我的通知、收藏、预约、发布与账号资料，延续前台黄黑暖色风格。</span>
          </div>
        </div>

        <div class="user-shortcuts">
          <RouterLink v-for="shortcut in shortcuts" :key="shortcut.path" :to="shortcut.path" class="user-shortcut">
            <el-icon><component :is="shortcut.icon" /></el-icon>
            <span>{{ shortcut.label }}</span>
          </RouterLink>
          <button type="button" class="user-shortcut user-shortcut--danger" @click="handleLogout">
            <el-icon><SwitchButton /></el-icon>
            <span>退出</span>
          </button>
        </div>
      </header>

      <div class="user-nav-bar">
        <nav class="user-nav">
          <RouterLink
            v-for="item in navItems"
            :key="item.path"
            :to="item.path"
            class="user-nav__item"
            :class="{ 'is-active': isActive(item.path) }"
          >
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.label }}</span>
          </RouterLink>
        </nav>
        <p class="user-nav-bar__tip">欢迎回来，{{ userStore.displayName }}。当前正在查看“{{ currentPage.label }}”。</p>
      </div>

      <section class="user-hero qh-panel">
        <div class="user-hero__main">
          <span class="hero-badge">{{ currentPage.badge }}</span>
          <h1>{{ currentTitle }}</h1>
          <p class="hero-description">{{ currentPage.description }}</p>

          <div class="hero-actions">
            <RouterLink
              v-for="action in currentPage.actions"
              :key="action.to"
              :to="action.to"
              class="hero-action"
              :class="[`hero-action--${action.variant}`]"
            >
              <span>{{ action.label }}</span>
              <el-icon v-if="action.variant === 'primary'"><ArrowRight /></el-icon>
            </RouterLink>
          </div>

          <div class="hero-metrics">
            <article v-for="metric in currentPage.metrics" :key="metric.label" class="metric-card">
              <strong>{{ metric.value }}</strong>
              <span>{{ metric.label }}</span>
            </article>
          </div>
        </div>

        <aside class="user-hero__aside">
          <div class="profile-card qh-panel--subtle">
            <span class="profile-card__label">当前用户</span>
            <strong>{{ userStore.displayName }}</strong>
            <p>{{ userStore.profile?.school || '科成校园' }}</p>
            <small>{{ userStore.profile?.slogan || '让每一件闲置，都重新回到更需要它的人手中。' }}</small>
          </div>
          <div class="aside-stats">
            <article v-for="item in asideStats" :key="item.label" class="mini-stat">
              <span>{{ item.label }}</span>
              <strong>{{ item.value }}</strong>
            </article>
          </div>
        </aside>
      </section>

      <section class="content-panel">
        <RouterView />
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { ElMessage } from 'element-plus';
import {
  ArrowRight,
  Bell,
  ChatDotRound,
  Collection,
  EditPen,
  HomeFilled,
  Setting,
  Star,
  SwitchButton,
} from '@element-plus/icons-vue';
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router';
import AppLogo from '@/components/AppLogo.vue';
import { useUserStore } from '@/stores/user';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const navItems = [
  { path: '/user/dashboard', label: '首页', icon: HomeFilled },
  { path: '/user/notifications', label: '通知', icon: Bell },
  { path: '/user/favorites', label: '收藏', icon: Star },
  { path: '/user/appointments', label: '预约', icon: ChatDotRound },
  { path: '/user/posts', label: '发布', icon: EditPen },
  { path: '/user/profile', label: '设置', icon: Setting },
];

const shortcuts = [
  { path: '/user/favorites', label: '收藏', icon: Star },
  { path: '/user/appointments', label: '预约', icon: ChatDotRound },
  { path: '/user/posts', label: '发布', icon: EditPen },
  { path: '/', label: '前台', icon: Collection },
];

const pageConfigs = {
  dashboard: {
    label: '个人中心首页',
    badge: '用户中心总览',
    description: '在统一的暖色运营风格中查看我的通知、收藏、预约、发布和账号资料，更适合项目展示与日常使用。',
    actions: [
      { label: '查看我的收藏', to: '/user/favorites', variant: 'primary' },
      { label: '查看预约记录', to: '/user/appointments', variant: 'secondary' },
      { label: '前往通知中心', to: '/user/notifications', variant: 'ghost' },
    ],
    metrics: [
      { label: '消息入口', value: '06' },
      { label: '当前身份', value: userStore.role === 'admin' ? 'ADMIN' : 'USER' },
      { label: '资料状态', value: '在线' },
      { label: '展示模式', value: '统一' },
    ],
  },
  notifications: {
    label: '通知中心',
    badge: '消息与提醒',
    description: '统一查看系统通知、预约提醒和公告动态，让用户中心也保持与前台一致的暖色展示体验。',
    actions: [
      { label: '返回个人首页', to: '/user/dashboard', variant: 'primary' },
      { label: '查看我的预约', to: '/user/appointments', variant: 'secondary' },
      { label: '返回前台', to: '/', variant: 'ghost' },
    ],
    metrics: [
      { label: '消息类型', value: '多种' },
      { label: '联动能力', value: '跳转' },
      { label: '阅读状态', value: '同步' },
      { label: '展示层级', value: '清晰' },
    ],
  },
  favorites: {
    label: '我的收藏',
    badge: '重点商品清单',
    description: '集中管理已收藏的商品内容，支持搜索、查看详情和取消收藏，整体风格同步前台暖黄配色。',
    actions: [
      { label: '返回个人首页', to: '/user/dashboard', variant: 'primary' },
      { label: '去商品广场', to: '/goods', variant: 'secondary' },
      { label: '查看我的预约', to: '/user/appointments', variant: 'ghost' },
    ],
    metrics: [
      { label: '卡片模式', value: '双列' },
      { label: '支持搜索', value: 'YES' },
      { label: '操作能力', value: '取消' },
      { label: '联调状态', value: '真实' },
    ],
  },
  appointments: {
    label: '我的预约',
    badge: '线下面交安排',
    description: '统一展示预约时间、地点与状态变化，帮助你快速掌握交易过程中的面交安排。',
    actions: [
      { label: '返回个人首页', to: '/user/dashboard', variant: 'primary' },
      { label: '查看我的收藏', to: '/user/favorites', variant: 'secondary' },
      { label: '查看我的发布', to: '/user/posts', variant: 'ghost' },
    ],
    metrics: [
      { label: '预约流程', value: '闭环' },
      { label: '状态展示', value: '清晰' },
      { label: '信息维度', value: '时间地点' },
      { label: '交互风格', value: '统一' },
    ],
  },
  posts: {
    label: '我的发布',
    badge: '内容管理中心',
    description: '在同一套暖色卡片体系下管理商品与求购发布，形成完整的用户端展示链路。',
    actions: [
      { label: '返回个人首页', to: '/user/dashboard', variant: 'primary' },
      { label: '查看我的收藏', to: '/user/favorites', variant: 'secondary' },
      { label: '前往账号设置', to: '/user/profile', variant: 'ghost' },
    ],
    metrics: [
      { label: '发布类型', value: '商品/求购' },
      { label: '管理能力', value: '统一' },
      { label: '展示视图', value: '清晰' },
      { label: '答辩友好', value: '高' },
    ],
  },
  profile: {
    label: '账号设置',
    badge: '资料与联系信息',
    description: '展示并维护用户基础资料、联系方式与个性签名，让个人中心风格与前台主站保持一致。',
    actions: [
      { label: '返回个人首页', to: '/user/dashboard', variant: 'primary' },
      { label: '查看我的发布', to: '/user/posts', variant: 'secondary' },
      { label: '返回前台', to: '/', variant: 'ghost' },
    ],
    metrics: [
      { label: '账号资料', value: '完整' },
      { label: '联系方式', value: '联调' },
      { label: '个性签名', value: '支持' },
      { label: '页面风格', value: '暖黄' },
    ],
  },
} as const;

const resolvePageKey = () => {
  if (route.path.startsWith('/user/notifications')) return 'notifications';
  if (route.path.startsWith('/user/favorites')) return 'favorites';
  if (route.path.startsWith('/user/appointments')) return 'appointments';
  if (route.path.startsWith('/user/posts')) return 'posts';
  if (route.path.startsWith('/user/profile')) return 'profile';
  return 'dashboard';
};

const currentPage = computed(() => pageConfigs[resolvePageKey()]);
const currentTitle = computed(() => String(route.meta.title || currentPage.value.label));
const asideStats = computed(() => [
  { label: '学校', value: userStore.profile?.school || '科成校园' },
  { label: '角色', value: userStore.role === 'admin' ? '管理员' : '用户' },
  { label: '主入口', value: '06' },
  { label: '状态', value: '在线' },
]);

const isActive = (path: string) => route.path.startsWith(path);

const handleLogout = () => {
  userStore.logout();
  ElMessage.success('已退出当前账号');
  router.push('/');
};
</script>

<style scoped lang="scss">
.user-center-theme {
  min-height: 100vh;
  padding: 28px 0 52px;
  background:
    radial-gradient(circle at top left, rgba(255, 221, 96, 0.22), transparent 26%),
    radial-gradient(circle at top right, rgba(255, 247, 211, 0.8), transparent 22%),
    linear-gradient(180deg, rgba(236, 246, 255, 0.96) 0 220px, #f8fbff 220px 100%);
}

.user-shell {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.user-topbar {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  gap: 18px;
  align-items: center;
  padding: 18px 20px;
  border-radius: 34px;
  background: linear-gradient(180deg, rgba(255, 216, 76, 0.98) 0%, rgba(255, 225, 106, 0.96) 100%);
  box-shadow: 0 18px 38px rgba(176, 133, 16, 0.14);
}

.user-brand {
  min-width: 0;
}

.user-command {
  min-height: 68px;
  padding: 0 22px;
  display: flex;
  align-items: center;
  gap: 12px;
  border-radius: 26px;
  background: rgba(255, 251, 237, 0.9);
  border: 2px solid rgba(32, 39, 51, 0.12);
}

.user-command :deep(.el-icon) {
  font-size: 18px;
  color: var(--qh-text-primary);
}

.user-command__content {
  display: grid;
  gap: 4px;
}

.user-command__content strong {
  color: var(--qh-text-primary);
  font-size: 15px;
}

.user-command__content span {
  line-height: 1.7;
  color: var(--qh-text-secondary);
}

.user-shortcuts {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.user-shortcut {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-height: 52px;
  padding: 0 18px;
  border: none;
  border-radius: 18px;
  background: rgba(255, 244, 185, 0.82);
  color: var(--qh-text-primary);
  text-decoration: none;
  font-weight: 700;
  cursor: pointer;
  transition: transform 0.2s ease, background 0.2s ease, box-shadow 0.2s ease;
}

.user-shortcut:hover {
  transform: translateY(-1px);
  background: rgba(255, 249, 224, 0.96);
  box-shadow: 0 10px 20px rgba(104, 78, 10, 0.08);
}

.user-shortcut--danger {
  background: rgba(255, 235, 184, 0.96);
  color: #b44d36;
}

.user-nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 10px 14px;
  border-radius: 26px;
  background: rgba(255, 236, 143, 0.44);
}

.user-nav {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.user-nav__item {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-height: 46px;
  padding: 0 18px;
  border-radius: 999px;
  color: rgba(32, 39, 51, 0.72);
  text-decoration: none;
  font-weight: 700;
  transition: all 0.2s ease;
}

.user-nav__item:hover,
.user-nav__item.is-active {
  background: rgba(255, 250, 233, 0.94);
  color: var(--qh-text-primary);
}

.user-nav-bar__tip {
  margin: 0;
  color: var(--qh-text-secondary);
  line-height: 1.6;
  text-align: right;
}

.user-hero {
  display: grid;
  grid-template-columns: minmax(0, 1.42fr) 400px;
  gap: 28px;
  padding: 36px;
  background: linear-gradient(180deg, rgba(255, 248, 219, 0.98) 0%, rgba(255, 239, 175, 0.96) 100%);
}

.user-hero__main {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.hero-badge {
  width: fit-content;
  padding: 10px 18px;
  border-radius: 999px;
  background: #202733;
  color: #ffe27a;
  font-size: 14px;
  font-weight: 700;
}

.user-hero__main h1 {
  margin: 0;
  font-size: clamp(34px, 4vw, 56px);
  line-height: 1.15;
  color: var(--qh-text-primary);
}

.hero-description {
  margin: 0;
  max-width: 860px;
  font-size: 18px;
  line-height: 1.9;
  color: var(--qh-text-secondary);
}

.hero-actions {
  display: flex;
  gap: 14px;
  flex-wrap: wrap;
}

.hero-action {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-height: 58px;
  padding: 0 24px;
  border-radius: 999px;
  text-decoration: none;
  font-weight: 800;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.hero-action:hover {
  transform: translateY(-1px);
  box-shadow: 0 12px 24px rgba(128, 100, 14, 0.12);
}

.hero-action--primary {
  background: #202733;
  color: #ffe27a;
}

.hero-action--secondary {
  background: rgba(255, 251, 238, 0.96);
  color: var(--qh-text-primary);
}

.hero-action--ghost {
  background: rgba(77, 56, 13, 0.92);
  color: #ffe7a0;
}

.hero-metrics {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  max-width: 760px;
}

.metric-card {
  padding: 18px 22px;
  border-radius: 24px;
  background: rgba(255, 252, 240, 0.92);
}

.metric-card strong {
  display: block;
  font-size: 22px;
  color: var(--qh-text-primary);
}

.metric-card span {
  display: block;
  margin-top: 8px;
  color: var(--qh-text-secondary);
}

.user-hero__aside {
  padding: 18px;
  border-radius: 32px;
  background: rgba(231, 238, 232, 0.9);
  display: grid;
  gap: 18px;
}

.profile-card {
  padding: 28px;
}

.profile-card__label {
  display: block;
  color: var(--qh-text-secondary);
  font-size: 13px;
}

.profile-card strong {
  display: block;
  margin-top: 10px;
  font-size: 32px;
  color: var(--qh-text-primary);
}

.profile-card p,
.profile-card small {
  display: block;
  margin-top: 12px;
  line-height: 1.8;
  color: var(--qh-text-secondary);
}

.aside-stats {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.mini-stat {
  padding: 20px 22px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.78);
}

.mini-stat span {
  display: block;
  color: var(--qh-text-secondary);
}

.mini-stat strong {
  display: block;
  margin-top: 10px;
  font-size: 24px;
  color: var(--qh-text-primary);
}

.content-panel {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.content-panel :deep(.grid-cards) {
  gap: 20px;
}

.content-panel :deep(.section-card) {
  border: 1px solid rgba(176, 142, 39, 0.1);
  box-shadow: 0 20px 36px rgba(138, 109, 22, 0.08);
}

.content-panel :deep(.stat-card) {
  min-height: 152px;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.content-panel :deep(.stat-card:hover) {
  transform: translateY(-2px);
}

.content-panel :deep(.stat-card strong) {
  color: var(--qh-text-primary);
}

.content-panel :deep(.stat-card),
.content-panel :deep(.table-wrapper),
.content-panel :deep(.overview-banner),
.content-panel :deep(.profile-hero),
.content-panel :deep(.quick-card),
.content-panel :deep(.notification-item),
.content-panel :deep(.appointment-list .quick-card),
.content-panel :deep(.filter-overview),
.content-panel :deep(.table-headline) {
  border: 1px solid rgba(176, 142, 39, 0.08);
}

.content-panel :deep(.table-wrapper) {
  margin-top: 18px;
  padding: 18px 20px;
  border-radius: 24px;
  background: rgba(255, 252, 239, 0.72);
}

.content-panel :deep(.table-link) {
  color: var(--qh-primary-deep);
  font-weight: 600;
}

.content-panel :deep(.el-alert) {
  border-radius: 20px;
}

.content-panel :deep(.el-table th.el-table__cell) {
  background: rgba(255, 245, 214, 0.86);
  color: var(--qh-text-primary);
}

.content-panel :deep(.el-table__row:hover > td.el-table__cell) {
  background: rgba(255, 248, 226, 0.78);
}

@media (max-width: 1280px) {
  .user-topbar {
    grid-template-columns: 1fr;
  }

  .user-shortcuts {
    justify-content: flex-start;
  }

  .user-hero {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 900px) {
  .user-nav-bar {
    flex-direction: column;
    align-items: flex-start;
  }

  .user-nav-bar__tip {
    text-align: left;
  }

  .hero-metrics,
  .aside-stats {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .user-center-theme {
    padding: 18px 0 40px;
  }

  .user-topbar,
  .user-hero {
    padding: 20px;
  }

  .user-command {
    min-height: auto;
    padding: 16px 18px;
    align-items: flex-start;
  }

  .user-shortcuts,
  .user-nav,
  .hero-actions {
    width: 100%;
  }

  .user-shortcut,
  .user-nav__item,
  .hero-action {
    justify-content: center;
    flex: 1 1 calc(50% - 8px);
  }

  .hero-metrics,
  .aside-stats {
    grid-template-columns: 1fr;
  }
}
</style>
