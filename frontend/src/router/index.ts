import { createRouter, createWebHistory } from 'vue-router';
import type { RouteRecordRaw } from 'vue-router';
import { ElMessage } from 'element-plus';
import FrontLayout from '@/layout/FrontLayout.vue';
import UserLayout from '@/layout/UserLayout.vue';
import AdminLayout from '@/layout/AdminLayout.vue';
import { pinia } from '@/stores';
import { useUserStore } from '@/stores/user';

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: FrontLayout,
    children: [
      { path: '', name: 'home', component: () => import('@/views/home/HomeView.vue'), meta: { title: '首页', layout: 'front' } },
      { path: 'goods', name: 'goods-list', component: () => import('@/views/goods/GoodsListView.vue'), meta: { title: '商品广场', layout: 'front' } },
      { path: 'goods/:id', name: 'goods-detail', component: () => import('@/views/goods/GoodsDetailView.vue'), meta: { title: '商品详情', layout: 'front' } },
      { path: 'wanted', name: 'wanted-list', component: () => import('@/views/wanted/WantedListView.vue'), meta: { title: '求购大厅', layout: 'front' } },
      { path: 'wanted/:id', name: 'wanted-detail', component: () => import('@/views/wanted/WantedDetailView.vue'), meta: { title: '求购详情', layout: 'front' } },
      { path: 'announcements', name: 'announcement-list', component: () => import('@/views/announcement/AnnouncementListView.vue'), meta: { title: '公告中心', layout: 'front' } },
      { path: 'announcements/:id', name: 'announcement-detail', component: () => import('@/views/announcement/AnnouncementDetailView.vue'), meta: { title: '公告详情', layout: 'front' } },
      { path: 'login', name: 'login', component: () => import('@/views/auth/LoginView.vue'), meta: { title: '登录', layout: 'front' } },
      { path: 'register', name: 'register', component: () => import('@/views/auth/RegisterView.vue'), meta: { title: '注册', layout: 'front' } },
    ],
  },
  {
    path: '/user',
    component: UserLayout,
    redirect: '/user/dashboard',
    meta: { requiresAuth: true, role: 'user', title: '个人中心', layout: 'user' },
    children: [
      { path: 'dashboard', name: 'user-dashboard', component: () => import('@/views/user/UserDashboardView.vue'), meta: { title: '个人中心首页', requiresAuth: true, role: 'user', layout: 'user' } },
      { path: 'notifications', name: 'user-notifications', component: () => import('@/views/user/UserNotificationsView.vue'), meta: { title: '通知中心', requiresAuth: true, role: 'user', layout: 'user' } },
      { path: 'favorites', name: 'user-favorites', component: () => import('@/views/user/UserFavoritesView.vue'), meta: { title: '我的收藏', requiresAuth: true, role: 'user', layout: 'user' } },
      { path: 'appointments', name: 'user-appointments', component: () => import('@/views/user/UserAppointmentsView.vue'), meta: { title: '我的预约', requiresAuth: true, role: 'user', layout: 'user' } },
      { path: 'posts', name: 'user-posts', component: () => import('@/views/user/UserPostsView.vue'), meta: { title: '我的发布', requiresAuth: true, role: 'user', layout: 'user' } },
      { path: 'profile', name: 'user-profile', component: () => import('@/views/user/UserProfileView.vue'), meta: { title: '账号设置', requiresAuth: true, role: 'user', layout: 'user' } },
    ],
  },
  {
    path: '/admin',
    component: AdminLayout,
    redirect: '/admin/dashboard',
    meta: { requiresAuth: true, role: 'admin', title: '后台管理', layout: 'admin' },
    children: [
      { path: 'dashboard', name: 'admin-dashboard', component: () => import('@/views/admin/AdminDashboardView.vue'), meta: { title: '管理员控制台', requiresAuth: true, role: 'admin', layout: 'admin' } },
      { path: 'goods', name: 'admin-goods', component: () => import('@/views/admin/AdminGoodsManageView.vue'), meta: { title: '商品管理', requiresAuth: true, role: 'admin', layout: 'admin' } },
      { path: 'users', name: 'admin-users', component: () => import('@/views/admin/AdminUserManageView.vue'), meta: { title: '用户管理', requiresAuth: true, role: 'admin', layout: 'admin' } },
      { path: 'announcements', name: 'admin-announcements', component: () => import('@/views/admin/AdminAnnouncementManageView.vue'), meta: { title: '公告管理', requiresAuth: true, role: 'admin', layout: 'admin' } },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: () => import('@/views/home/NotFoundView.vue'),
    meta: { title: '页面不存在' },
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 };
  },
});

router.beforeEach((to) => {
  const userStore = useUserStore(pinia);
  document.title = to.meta.title ? `${to.meta.title} - 校园跳蚤市场 · 科成` : '校园跳蚤市场 · 科成';

  if (!to.meta.requiresAuth) {
    return true;
  }

  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再访问该页面');
    return { name: 'login', query: { redirect: to.fullPath } };
  }

  if (to.meta.role === 'admin' && userStore.role !== 'admin') {
    ElMessage.warning('当前账号没有后台访问权限');
    return { name: 'user-dashboard' };
  }

  if (to.meta.role === 'user' && userStore.role === 'admin') {
    return { name: 'admin-dashboard' };
  }

  return true;
});

export default router;
