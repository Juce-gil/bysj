<template>
  <div class="dashboard-layout qh-shell">
    <aside class="side-panel qh-panel">
      <AppLogo subtitle="科成用户中心" />
      <div class="profile-card qh-panel--subtle">
        <strong>{{ userStore.displayName }}</strong>
        <span>{{ userStore.profile?.school || '科成' }}</span>
        <p>{{ userStore.profile?.slogan || '让每一件闲置，都在科成找到新的去处。' }}</p>
      </div>
      <el-menu :default-active="route.path" router class="side-menu">
        <el-menu-item index="/user/dashboard">个人首页</el-menu-item>
        <el-menu-item index="/user/notifications">通知中心</el-menu-item>
        <el-menu-item index="/user/favorites">我的收藏</el-menu-item>
        <el-menu-item index="/user/appointments">我的预约</el-menu-item>
        <el-menu-item index="/user/posts">我的发布</el-menu-item>
        <el-menu-item index="/user/profile">账号设置</el-menu-item>
      </el-menu>
      <div class="side-actions">
        <RouterLink to="/" class="mini-link">返回前台</RouterLink>
        <button type="button" class="mini-link danger" @click="handleLogout">退出登录</button>
      </div>
    </aside>

    <section class="content-panel">
      <header class="content-header qh-panel">
        <div>
          <p class="eyebrow">用户中心</p>
          <h1>{{ currentTitle }}</h1>
        </div>
        <p class="content-tip">集中查看我的通知、收藏、预约、发布与账号资料。</p>
      </header>
      <RouterView />
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { ElMessage } from 'element-plus';
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router';
import AppLogo from '@/components/AppLogo.vue';
import { useUserStore } from '@/stores/user';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const currentTitle = computed(() => route.meta.title || '个人中心');
const handleLogout = () => {
  userStore.logout();
  ElMessage.success('已退出登录');
  router.push('/');
};
</script>

<style scoped lang="scss">
.dashboard-layout { display: grid; grid-template-columns: 280px 1fr; gap: 24px; padding-top: 28px; padding-bottom: 40px; }
.side-panel { align-self: start; position: sticky; top: 24px; padding: 24px; }
.profile-card { margin: 24px 0; padding: 18px; }
.profile-card strong, .profile-card span, .profile-card p { display: block; }
.profile-card strong { color: var(--qh-text-primary); margin-bottom: 6px; }
.profile-card span, .profile-card p { color: var(--qh-text-secondary); margin: 0; }
.profile-card p { margin-top: 10px; line-height: 1.7; }
.side-menu { border-right: none; background: transparent; }
.side-actions { margin-top: 20px; display: flex; gap: 12px; flex-wrap: wrap; }
.mini-link { background: rgba(150, 200, 250, 0.15); color: var(--qh-primary-deep); border-radius: 999px; padding: 8px 14px; text-decoration: none; border: none; cursor: pointer; }
.mini-link.danger { color: #d46666; }
.content-panel { display: flex; flex-direction: column; gap: 24px; }
.content-header { padding: 26px 28px; display: flex; justify-content: space-between; align-items: center; gap: 16px; }
.content-header h1 { margin: 6px 0 0; font-size: 28px; color: var(--qh-text-primary); }
.eyebrow { margin: 0; color: var(--qh-primary-deep); font-size: 13px; letter-spacing: 0.08em; }
.content-tip { margin: 0; color: var(--qh-text-secondary); }
@media (max-width: 1024px) {
  .dashboard-layout { grid-template-columns: 1fr; }
  .side-panel { position: static; }
  .content-header { flex-direction: column; align-items: flex-start; }
}
</style>
