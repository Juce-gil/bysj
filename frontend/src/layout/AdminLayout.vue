<template>
  <div class="admin-theme">
    <div class="qh-shell admin-shell">
      <header class="admin-topbar">
        <div class="admin-brand">
          <AppLogo
            variant="market"
            title="校园跳蚤市场"
            subtitle="后台运营管理中心"
          />
        </div>

        <div class="admin-command">
          <el-icon><Search /></el-icon>
          <div class="admin-command__content">
            <strong>统一运营视图</strong>
            <span>用仪表盘式布局集中查看商品、用户、公告与接口联调状态。</span>
          </div>
        </div>

        <div class="admin-shortcuts">
          <RouterLink
            v-for="shortcut in shortcuts"
            :key="shortcut.path"
            :to="shortcut.path"
            class="admin-shortcut"
          >
            <el-icon><component :is="shortcut.icon" /></el-icon>
            <span>{{ shortcut.label }}</span>
          </RouterLink>
          <button type="button" class="admin-shortcut admin-shortcut--danger" @click="handleLogout">
            <el-icon><SwitchButton /></el-icon>
            <span>退出</span>
          </button>
        </div>
      </header>

      <div class="admin-statusbar qh-panel">
        <div class="admin-statusbar__chips">
          <span v-for="chip in statusChips" :key="chip.label" class="status-chip">
            <small>{{ chip.label }}</small>
            <strong>{{ chip.value }}</strong>
          </span>
        </div>
        <p class="admin-statusbar__tip">
          欢迎回来，{{ userStore.displayName }}。当前正在查看“{{ currentPage.label }}”。
        </p>
      </div>

      <section class="admin-hero qh-panel">
        <div class="admin-hero__main">
          <div class="hero-heading">
            <span class="hero-badge">{{ currentPage.badge }}</span>
            <h1>{{ currentTitle }}</h1>
            <p class="hero-description">{{ currentPage.description }}</p>
          </div>

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
              <small>{{ metric.label }}</small>
              <strong>{{ metric.value }}</strong>
              <span>{{ metric.tip }}</span>
            </article>
          </div>
        </div>

        <aside class="admin-hero__aside">
          <div class="aside-card qh-panel--subtle">
            <p class="aside-card__eyebrow">当前会话</p>
            <h2>{{ userStore.displayName }}</h2>
            <p class="aside-card__meta">角色：{{ roleLabel }} · 视图：{{ currentPage.label }}</p>
            <p class="aside-card__desc">
              管理端已切换为更明显的仪表盘风格，适合演示后台业务、模块状态与管理动作的集中呈现。
            </p>
          </div>

          <div class="aside-stats">
            <article v-for="item in asideStats" :key="item.label" class="mini-stat">
              <span>{{ item.label }}</span>
              <strong>{{ item.value }}</strong>
              <p>{{ item.tip }}</p>
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
import { computed, onMounted, onUnmounted } from 'vue';
import { ElMessage } from 'element-plus';
import {
  ArrowRight,
  BellFilled,
  DataBoard,
  GoodsFilled,
  HomeFilled,
  Search,
  SwitchButton,
  UserFilled,
} from '@element-plus/icons-vue';
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router';
import AppLogo from '@/components/AppLogo.vue';
import { useUserStore } from '@/stores/user';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const navItems = [
  { path: '/admin/dashboard', label: '总览', icon: DataBoard },
  { path: '/admin/goods', label: '商品', icon: GoodsFilled },
  { path: '/admin/users', label: '用户', icon: UserFilled },
  { path: '/admin/announcements', label: '公告', icon: BellFilled },
];

const shortcuts = [
  { path: '/admin/goods', label: '商品', icon: GoodsFilled },
  { path: '/admin/users', label: '用户', icon: UserFilled },
  { path: '/admin/announcements', label: '公告', icon: BellFilled },
  { path: '/', label: '前台', icon: HomeFilled },
];

const pageConfigs = {
  dashboard: {
    label: '管理员控制台',
    badge: '后台总览仪表盘',
    description: '集中展示平台核心模块、快捷入口与联调状态，让后台首页更像一个完整的运营仪表盘。',
    actions: [
      { label: '进入商品管理', to: '/admin/goods', variant: 'primary' },
      { label: '查看用户列表', to: '/admin/users', variant: 'secondary' },
      { label: '浏览公告管理', to: '/admin/announcements', variant: 'ghost' },
    ],
    metrics: [
      { label: '核心模块', value: '04', tip: '商品 / 用户 / 公告 / 控制台' },
      { label: '接口状态', value: 'API', tip: '真实接口联调中' },
      { label: '界面风格', value: 'Dashboard', tip: '统一仪表盘式布局' },
      { label: '演示状态', value: 'READY', tip: '适合项目展示与答辩' },
    ],
  },
  goods: {
    label: '商品管理',
    badge: '商品运营面板',
    description: '围绕商品状态、列表筛选、详情抽屉和上下架动作，构建具有仪表盘观感的管理页面。',
    actions: [
      { label: '返回控制台', to: '/admin/dashboard', variant: 'primary' },
      { label: '查看用户管理', to: '/admin/users', variant: 'secondary' },
      { label: '打开前台商品', to: '/goods', variant: 'ghost' },
    ],
    metrics: [
      { label: '重点动作', value: '上下架', tip: '突出商品状态治理' },
      { label: '筛选方式', value: '多条件', tip: '关键词 / 状态 / 分类' },
      { label: '详情模式', value: '抽屉', tip: '快速查看商品详情' },
      { label: '列表能力', value: '分页', tip: '适合后台批量管理' },
    ],
  },
  users: {
    label: '用户管理',
    badge: '账号治理面板',
    description: '把用户检索、角色识别、状态切换与分页列表放进统一仪表盘式界面，提高后台管理感。',
    actions: [
      { label: '返回控制台', to: '/admin/dashboard', variant: 'primary' },
      { label: '切换商品管理', to: '/admin/goods', variant: 'secondary' },
      { label: '查看公告管理', to: '/admin/announcements', variant: 'ghost' },
    ],
    metrics: [
      { label: '检索维度', value: '关键词', tip: '支持昵称、账号、学号' },
      { label: '角色区分', value: '双角色', tip: '管理员 / 普通用户' },
      { label: '状态维护', value: '启停用', tip: '快速治理账号状态' },
      { label: '数据刷新', value: '实时', tip: '面向后台即时操作' },
    ],
  },
  announcements: {
    label: '公告管理',
    badge: '公告运营面板',
    description: '用更强的仪表盘布局承载公告筛选、发布状态、置顶管理、编辑弹窗和详情查看。',
    actions: [
      { label: '返回控制台', to: '/admin/dashboard', variant: 'primary' },
      { label: '进入商品管理', to: '/admin/goods', variant: 'secondary' },
      { label: '查看前台公告', to: '/announcements', variant: 'ghost' },
    ],
    metrics: [
      { label: '内容操作', value: '增删改', tip: '支持完整公告维护' },
      { label: '发布状态', value: '双态', tip: '已发布 / 未发布' },
      { label: '重点标记', value: '置顶', tip: '突出重要公告' },
      { label: '联调模式', value: '真接口', tip: '可直接配合后端演示' },
    ],
  },
} as const;

const resolvePageKey = () => {
  if (route.path.startsWith('/admin/goods')) return 'goods';
  if (route.path.startsWith('/admin/users')) return 'users';
  if (route.path.startsWith('/admin/announcements')) return 'announcements';
  return 'dashboard';
};

const currentPage = computed(() => pageConfigs[resolvePageKey()]);
const currentTitle = computed(() => String(route.meta.title || currentPage.value.label));
const roleLabel = computed(() => (userStore.role === 'admin' ? '管理员' : '普通用户'));

const statusChips = computed(() => [
  { label: '导航模块', value: String(navItems.length).padStart(2, '0') },
  { label: '当前角色', value: roleLabel.value },
  { label: '布局模式', value: '仪表盘' },
  { label: '页面主题', value: currentPage.value.label },
]);

const asideStats = computed(() => [
  { label: '控制台', value: '01', tip: '后台首页总览' },
  { label: '商品模块', value: '02', tip: '商品运营治理' },
  { label: '用户模块', value: '03', tip: '账号状态维护' },
  { label: '公告模块', value: '04', tip: '公告发布管理' },
]);

const isActive = (path: string) => route.path.startsWith(path);

const handleLogout = () => {
  userStore.logout();
  ElMessage.success('管理员已退出登录');
  router.push('/');
};
onMounted(() => {
  document.body.classList.add('admin-body');
});

onUnmounted(() => {
  document.body.classList.remove('admin-body');
});
</script>

<style scoped lang="scss">
.admin-theme {
  min-height: 100%;
  background:
    radial-gradient(circle at top left, rgba(255, 223, 120, 0.2), transparent 32%),
    radial-gradient(circle at top right, rgba(255, 247, 216, 0.68), transparent 24%),
    linear-gradient(180deg, rgba(255, 248, 225, 0.96) 0%, rgba(248, 241, 213, 0.96) 100%);
}

.admin-shell {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding-bottom: 18px;
}

.admin-topbar {
  position: relative;
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  gap: 18px;
  align-items: center;
  padding: 18px 20px;
  border-radius: 32px;
  background:
    radial-gradient(circle at top right, rgba(255, 246, 210, 0.42), transparent 22%),
    linear-gradient(135deg, rgba(255, 216, 76, 0.98) 0%, rgba(255, 228, 126, 0.98) 100%);
  box-shadow: 0 22px 42px rgba(176, 133, 16, 0.16);
  border: 1px solid rgba(161, 119, 8, 0.16);
}

.admin-brand {
  min-width: 0;
}

.admin-command {
  min-height: 78px;
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 0 24px;
  border-radius: 22px;
  background: rgba(255, 251, 237, 0.9);
  border: 1px solid rgba(32, 39, 51, 0.1);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.56);
  color: rgba(64, 57, 33, 0.72);
}

.admin-command :deep(.el-icon) {
  font-size: 18px;
  color: var(--qh-text-primary);
}

.admin-command__content {
  display: grid;
  gap: 4px;
}

.admin-command__content strong {
  color: var(--qh-text-primary);
  font-size: 15px;
}

.admin-command__content span {
  line-height: 1.6;
  color: var(--qh-text-secondary);
}

.admin-shortcuts {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
  flex-wrap: wrap;
}

.admin-shortcut {
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
  transition: transform 0.2s ease, background 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
  box-shadow: inset 0 0 0 1px rgba(146, 109, 11, 0.08);
}

.admin-shortcut:hover {
  transform: translateY(-1px);
  background: rgba(255, 249, 224, 0.96);
  box-shadow: 0 10px 20px rgba(104, 78, 10, 0.08);
}

.admin-shortcut--danger {
  background: rgba(255, 235, 184, 0.96);
}

.admin-statusbar {
  padding: 16px 18px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  border-radius: 28px;
  background:
    linear-gradient(180deg, rgba(255, 252, 240, 0.86) 0%, rgba(255, 247, 220, 0.82) 100%);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.62);
}

.admin-statusbar__chips {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.status-chip {
  display: inline-grid;
  gap: 4px;
  min-width: 120px;
  padding: 10px 14px;
  border-radius: 18px;
  background: rgba(255, 245, 212, 0.88);
  border: 1px solid rgba(180, 140, 29, 0.08);
}

.status-chip small {
  color: var(--qh-text-secondary);
  font-size: 12px;
}

.status-chip strong {
  color: var(--qh-text-primary);
  font-size: 15px;
}

.admin-statusbar__tip {
  margin: 0;
  color: var(--qh-text-secondary);
  line-height: 1.7;
  text-align: right;
}

.admin-hero {
  position: relative;
  overflow: hidden;
  display: grid;
  grid-template-columns: minmax(0, 1.5fr) 420px;
  gap: 24px;
  padding: 36px;
  background: linear-gradient(180deg, rgba(255, 248, 219, 0.98) 0%, rgba(255, 239, 175, 0.96) 100%);
}

.admin-hero::before {
  content: '';
  position: absolute;
  inset: 0;
  background:
    linear-gradient(transparent 95%, rgba(166, 126, 22, 0.06) 95%),
    linear-gradient(90deg, transparent 95%, rgba(166, 126, 22, 0.06) 95%);
  background-size: 28px 28px;
  pointer-events: none;
}

.admin-hero__main,
.admin-hero__aside {
  position: relative;
  z-index: 1;
}

.admin-hero__main {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.hero-heading {
  display: grid;
  gap: 16px;
}

.hero-badge {
  width: fit-content;
  padding: 10px 18px;
  border-radius: 999px;
  background: #202733;
  color: #ffe27a;
  font-size: 14px;
  font-weight: 700;
  box-shadow: 0 12px 24px rgba(32, 39, 51, 0.14);
}

.admin-hero__main h1 {
  margin: 0;
  font-size: clamp(36px, 4vw, 58px);
  line-height: 1.12;
  color: var(--qh-text-primary);
}

.hero-description {
  max-width: 880px;
  margin: 0;
  font-size: 18px;
  line-height: 1.85;
  color: var(--qh-text-secondary);
}

.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
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
  border: 1px solid transparent;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
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
  max-width: 860px;
}

.metric-card {
  padding: 18px 20px;
  border-radius: 24px;
  background: rgba(255, 252, 240, 0.92);
  border: 1px solid rgba(182, 142, 32, 0.08);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.52);
  min-height: 136px;
}

.metric-card small {
  display: block;
  color: var(--qh-text-secondary);
  font-size: 12px;
}

.metric-card strong {
  display: block;
  margin-top: 10px;
  font-size: 24px;
  color: var(--qh-text-primary);
}

.metric-card span {
  display: block;
  margin-top: 8px;
  color: var(--qh-text-secondary);
  line-height: 1.6;
}

.admin-hero__aside {
  padding: 18px;
  border-radius: 32px;
  background: linear-gradient(180deg, rgba(231, 238, 232, 0.9) 0%, rgba(243, 246, 237, 0.96) 100%);
  display: grid;
  gap: 18px;
  border: 1px solid rgba(134, 167, 136, 0.14);
}

.aside-card {
  padding: 28px;
}

.aside-card__eyebrow {
  margin: 0 0 14px;
  font-size: 14px;
  font-weight: 700;
  color: var(--qh-text-secondary);
}

.aside-card h2 {
  margin: 0;
  font-size: 28px;
  color: var(--qh-text-primary);
}

.aside-card__meta,
.aside-card__desc {
  margin: 12px 0 0;
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
  border: 1px solid rgba(123, 142, 112, 0.08);
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

.mini-stat p {
  margin: 8px 0 0;
  color: var(--qh-text-secondary);
  line-height: 1.6;
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
  position: relative;
  overflow: hidden;
  border: 1px solid rgba(179, 138, 22, 0.1);
  box-shadow: 0 24px 42px rgba(138, 109, 22, 0.1);
}

.content-panel :deep(.section-card::before) {
  content: '';
  position: absolute;
  inset: 0 0 auto 0;
  height: 5px;
  background: linear-gradient(90deg, #202733 0%, #ffd85b 40%, rgba(255, 216, 91, 0.08) 100%);
}

.content-panel :deep(.section-card__body) {
  gap: 18px;
}

.content-panel :deep(.stat-card) {
  min-height: 164px;
  padding: 26px;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  border: 1px solid rgba(181, 139, 23, 0.08);
  box-shadow: 0 16px 32px rgba(140, 112, 28, 0.08);
}

.content-panel :deep(.stat-card:nth-child(1)) {
  background: linear-gradient(180deg, rgba(255, 250, 231, 0.96), rgba(255, 242, 195, 0.96));
}

.content-panel :deep(.stat-card:nth-child(2)) {
  background: linear-gradient(180deg, rgba(255, 249, 234, 0.96), rgba(246, 240, 221, 0.96));
}

.content-panel :deep(.stat-card:nth-child(3)) {
  background: linear-gradient(180deg, rgba(248, 248, 236, 0.96), rgba(235, 242, 230, 0.96));
}

.content-panel :deep(.stat-card:nth-child(4)) {
  background: linear-gradient(180deg, rgba(255, 251, 234, 0.96), rgba(252, 239, 201, 0.96));
}

.content-panel :deep(.stat-card strong) {
  font-size: 40px;
  font-weight: 800;
  color: var(--qh-text-primary);
}

.content-panel :deep(.stat-card p) {
  line-height: 1.7;
}

.content-panel :deep(.filter-overview),
.content-panel :deep(.table-headline),
.content-panel :deep(.announcement-summary),
.content-panel :deep(.session-banner) {
  border: 1px solid rgba(181, 139, 23, 0.08);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.42);
}

.content-panel :deep(.filter-overview article),
.content-panel :deep(.announcement-summary article),
.content-panel :deep(.detail-summary article) {
  border: 1px solid rgba(181, 139, 23, 0.06);
  box-shadow: 0 12px 24px rgba(113, 85, 16, 0.04);
}

.content-panel :deep(.table-wrapper) {
  margin-top: 18px;
  padding: 18px 20px;
  border-radius: 24px;
  border: 1px solid rgba(176, 142, 39, 0.12);
  background: rgba(255, 252, 239, 0.72);
}

.content-panel :deep(.el-alert) {
  border-radius: 20px;
  border: 1px solid rgba(181, 139, 23, 0.08);
}

.content-panel :deep(.info-chip) {
  border: 1px solid rgba(181, 139, 23, 0.06);
}

.content-panel :deep(.detail-actions),
.content-panel :deep(.switch-group),
.content-panel :deep(.todo-item),
.content-panel :deep(.state-panel),
.content-panel :deep(.detail-content) {
  background: rgba(255, 252, 240, 0.78);
  border: 1px solid rgba(181, 139, 23, 0.06);
}

.content-panel :deep(.pagination-bar) {
  margin-top: 22px;
}

.content-panel :deep(.el-table th.el-table__cell) {
  background: rgba(255, 245, 214, 0.86);
  color: var(--qh-text-primary);
}

.content-panel :deep(.el-table tr) {
  background: transparent;
}

.content-panel :deep(.el-table__row:hover > td.el-table__cell) {
  background: rgba(255, 248, 226, 0.78);
}

@media (max-width: 1280px) {
  .admin-topbar {
    grid-template-columns: 1fr;
  }

  .admin-shortcuts {
    justify-content: flex-start;
  }

  .admin-statusbar,
  .admin-hero {
    grid-template-columns: 1fr;
  }

  .admin-statusbar {
    flex-direction: column;
    align-items: flex-start;
  }

  .admin-hero {
    padding: 28px;
  }
}

@media (max-width: 900px) {
  .hero-metrics,
  .aside-stats {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .admin-statusbar__tip {
    text-align: left;
  }
}

@media (max-width: 768px) {
  .admin-topbar,
  .admin-hero {
    padding: 20px;
  }

  .admin-command {
    min-height: auto;
    padding: 16px 18px;
    align-items: flex-start;
  }

  .hero-actions,
  .admin-shortcuts,
  .admin-statusbar__chips {
    width: 100%;
  }

  .hero-action,
  .admin-shortcut {
    justify-content: center;
    flex: 1 1 calc(50% - 8px);
  }

  .hero-metrics,
  .aside-stats {
    grid-template-columns: 1fr;
  }

  .status-chip {
    flex: 1 1 calc(50% - 8px);
  }
}
</style>
