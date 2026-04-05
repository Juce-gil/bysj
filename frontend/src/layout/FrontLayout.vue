<template>
  <div class="front-layout">
    <header class="front-header">
      <div class="qh-shell front-header__top">
        <AppLogo variant="market" title="校园跳蚤市场" subtitle="科成校内闲置交换平台" />

        <div class="front-search-block">
          <div class="front-search-bar">
            <el-input
              v-model="searchKeyword"
              class="front-search-bar__input"
              clearable
              placeholder="搜索你想要的商品，如：手机壳 / 教材 / 耳机 / 台灯"
              @keyup.enter="submitSearch"
            />
            <button class="front-search-bar__button" type="button" @click="submitSearch">
              <el-icon><Search /></el-icon>
              <span>搜索</span>
            </button>
          </div>

          <div class="front-search-tags">
            <button
              v-for="item in searchSuggestions"
              :key="item"
              type="button"
              class="front-search-tags__item"
              @click="quickSearch(item)"
            >
              {{ item }}
            </button>
          </div>
        </div>

        <div class="front-header__actions">
          <template v-if="userStore.isLoggedIn">
            <RouterLink class="header-action" to="/goods">
              <el-icon><Shop /></el-icon>
              <span>商品</span>
            </RouterLink>
            <RouterLink class="header-action" to="/user/appointments">
              <el-icon><Tickets /></el-icon>
              <span>预约</span>
            </RouterLink>
            <RouterLink v-if="userStore.role === 'admin'" class="header-action" to="/admin/dashboard">
              <el-icon><Setting /></el-icon>
              <span>后台</span>
            </RouterLink>
            <RouterLink v-else class="header-action" to="/user/dashboard">
              <el-icon><User /></el-icon>
              <span>我的</span>
            </RouterLink>
            <button class="header-action header-action--ghost" type="button" @click="handleLogout">
              <el-icon><SwitchButton /></el-icon>
              <span>退出</span>
            </button>
          </template>
          <template v-else>
            <RouterLink class="header-action" to="/login">
              <el-icon><User /></el-icon>
              <span>登录</span>
            </RouterLink>
            <RouterLink class="header-action" to="/register">
              <el-icon><EditPen /></el-icon>
              <span>注册</span>
            </RouterLink>
          </template>
        </div>
      </div>

      <div class="front-header__bottom">
        <div class="qh-shell front-header__bottom-inner">
          <nav class="front-nav">
            <RouterLink
              v-for="item in navItems"
              :key="item.path"
              :to="item.path"
              class="front-nav__link"
              :class="{ 'is-active': isActive(item.path) }"
            >
              {{ item.label }}
            </RouterLink>
          </nav>
          <span class="front-header__tip">欢迎来到科成校园跳蚤市场，精选校内闲置持续更新中</span>
        </div>
      </div>
    </header>

    <main class="front-main qh-shell">
      <RouterView />
    </main>

    <footer class="front-footer">
      <div class="qh-shell front-footer__inner">
        <div>
          <strong>校园跳蚤市场</strong>
          <p>科成校内闲置交换平台，帮助每一次转手都更安心、更高效。</p>
        </div>
        <div class="front-footer__meta">
          <span>首页 / 用户中心 / 管理后台三端联调展示</span>
          <span>Vue 3 + Vite + TypeScript + Element Plus</span>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { EditPen, Search, Setting, Shop, SwitchButton, Tickets, User } from '@element-plus/icons-vue';
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router';
import AppLogo from '@/components/AppLogo.vue';
import { useUserStore } from '@/stores/user';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const searchKeyword = ref('');

const navItems = [
  { label: '首页', path: '/' },
  { label: '商品广场', path: '/goods' },
  { label: '求购大厅', path: '/wanted' },
  { label: '公告中心', path: '/announcements' },
];

const searchSuggestions = ['笔记本电脑', '教材', '手机壳', '耳机', '台灯', '运动鞋', '收纳箱', '电动车'];
const currentPath = computed(() => route.path);
const isActive = (path: string) => path === '/' ? currentPath.value === path : currentPath.value === path || currentPath.value.startsWith(`${path}/`);

watch(
  () => route.query.keyword,
  (value) => {
    searchKeyword.value = typeof value === 'string' ? value : '';
  },
  { immediate: true },
);

const submitSearch = async () => {
  const keyword = searchKeyword.value.trim();
  await router.push({
    path: '/goods',
    query: keyword ? { keyword } : {},
  });
};

const quickSearch = (value: string) => {
  searchKeyword.value = value;
  submitSearch();
};

const handleLogout = () => {
  userStore.logout();
  ElMessage.success('已退出当前账号');
  router.push('/');
};
</script>

<style scoped lang="scss">
.front-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.front-header {
  position: sticky;
  top: 0;
  z-index: 20;
  background: linear-gradient(180deg, #ffe042 0%, #ffd736 100%);
  box-shadow: 0 10px 26px rgba(181, 150, 26, 0.14);
}

.front-header__top {
  padding: 18px 0 16px;
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  align-items: center;
  gap: 26px;
}

.front-search-block {
  min-width: 0;
}

.front-search-bar {
  display: flex;
  align-items: center;
  gap: 14px;
}

.front-search-bar__input :deep(.el-input__wrapper) {
  min-height: 54px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 0 0 2px rgba(31, 37, 48, 0.9) inset, 0 14px 28px rgba(112, 94, 17, 0.12);
}

.front-search-bar__button {
  height: 52px;
  min-width: 124px;
  border: none;
  border-radius: 999px;
  background: #1f2530;
  color: #ffef9c;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-weight: 700;
  cursor: pointer;
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}

.front-search-bar__button:hover {
  transform: translateY(-1px);
  box-shadow: 0 12px 24px rgba(62, 51, 14, 0.18);
}

.front-search-tags {
  margin-top: 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px 14px;
}

.front-search-tags__item {
  border: none;
  background: transparent;
  color: rgba(31, 37, 48, 0.82);
  cursor: pointer;
  padding: 0;
  font-size: 14px;
}

.front-search-tags__item:hover {
  color: #11151e;
  text-decoration: underline;
}

.front-header__actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.header-action {
  min-width: 74px;
  padding: 10px 12px;
  border-radius: 16px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  text-decoration: none;
  color: #1f2530;
  background: rgba(255, 255, 255, 0.32);
  border: 1px solid rgba(31, 37, 48, 0.08);
  cursor: pointer;
  transition: all 0.18s ease;
}

.header-action:hover,
.header-action--ghost:hover {
  background: rgba(255, 255, 255, 0.56);
  transform: translateY(-1px);
}

.header-action--ghost {
  border: none;
  font: inherit;
}

.front-header__bottom {
  border-top: 1px solid rgba(31, 37, 48, 0.08);
  background: rgba(255, 245, 190, 0.42);
}

.front-header__bottom-inner {
  min-height: 48px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
}

.front-nav {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.front-nav__link {
  padding: 8px 14px;
  border-radius: 999px;
  color: rgba(31, 37, 48, 0.78);
  text-decoration: none;
  transition: all 0.18s ease;
}

.front-nav__link.is-active,
.front-nav__link:hover {
  color: #11151e;
  background: rgba(255, 255, 255, 0.54);
}

.front-header__tip {
  color: rgba(31, 37, 48, 0.68);
  font-size: 13px;
  white-space: nowrap;
}

.front-main {
  flex: 1;
  padding-top: 26px;
  padding-bottom: 40px;
}

.front-footer {
  border-top: 1px solid rgba(146, 185, 220, 0.15);
  background: rgba(245, 251, 255, 0.72);
}

.front-footer__inner {
  padding: 22px 0 36px;
  display: flex;
  justify-content: space-between;
  gap: 24px;
  color: var(--qh-text-secondary);
}

.front-footer__inner strong {
  display: block;
  margin-bottom: 8px;
  color: var(--qh-text-primary);
}

.front-footer__inner p,
.front-footer__inner span {
  margin: 0;
  font-size: 13px;
}

.front-footer__meta {
  display: flex;
  flex-direction: column;
  gap: 8px;
  text-align: right;
}

@media (max-width: 1280px) {
  .front-header__top {
    grid-template-columns: 1fr;
  }

  .front-header__actions {
    justify-content: flex-start;
  }

  .front-header__bottom-inner {
    flex-direction: column;
    align-items: flex-start;
    padding: 10px 0;
  }

  .front-header__tip {
    white-space: normal;
  }
}

@media (max-width: 768px) {
  .front-search-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .front-search-bar__button {
    width: 100%;
  }

  .front-header__actions {
    width: 100%;
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .header-action {
    width: 100%;
  }

  .front-footer__inner {
    flex-direction: column;
  }

  .front-footer__meta {
    text-align: left;
  }
}
</style>
