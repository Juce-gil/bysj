<template>
  <div class="dashboard-layout qh-shell admin-layout">
    <aside class="side-panel qh-panel">
      <AppLogo subtitle="科成管理后台" />
      <div class="profile-card qh-panel--subtle admin-card">
        <strong>{{ userStore.displayName }}</strong>
        <span>当前角色：管理员</span>
        <p>从这里查看科成校园跳蚤市场的内容概览，并为后续审核与治理接口预留稳定骨架。</p>
      </div>
      <el-menu :default-active="route.path" router class="side-menu">
        <el-menu-item index="/admin/dashboard">控制台</el-menu-item>
        <el-menu-item index="/admin/goods">商品管理</el-menu-item>
        <el-menu-item index="/admin/users">用户管理</el-menu-item>
        <el-menu-item index="/admin/announcements">公告管理</el-menu-item>
      </el-menu>
      <div class="side-actions">
        <RouterLink to="/" class="mini-link">查看前台</RouterLink>
        <button type="button" class="mini-link danger" @click="handleLogout">退出登录</button>
      </div>
    </aside>

    <section class="content-panel">
      <header class="content-header qh-panel">
        <div>
          <p class="eyebrow">后台管理</p>
          <h1>{{ currentTitle }}</h1>
        </div>
        <p class="content-tip">当前以真实接口构建后台骨架，后续可平滑替换为审核、统计和治理接口。</p>
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
const currentTitle = computed(() => route.meta.title || '后台管理');
const handleLogout = () => {
  userStore.logout();
  ElMessage.success('管理员已退出');
  router.push('/');
};
</script>

<style scoped lang="scss">
.admin-layout .side-panel { background: linear-gradient(180deg, rgba(247, 251, 255, 0.98) 0%, rgba(237, 247, 255, 0.96) 100%); }
.admin-card { border: 1px solid rgba(100, 155, 220, 0.14); }
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
