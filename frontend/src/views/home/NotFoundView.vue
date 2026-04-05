<template>
  <div class="not-found qh-shell">
    <section class="not-found__card qh-panel">
      <p class="eyebrow">404</p>
      <h1>你访问的页面正在风里迷路。</h1>
      <p>当前路由不存在，建议返回首页继续浏览商品、求购或公告内容。</p>
      <div class="actions">
        <RouterLink to="/">返回首页</RouterLink>
        <RouterLink to="/goods">去商品广场</RouterLink>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { RouterLink } from 'vue-router';
</script>

<style scoped lang="scss">
.not-found { padding: 60px 0; }
.not-found__card { padding: 48px; text-align: center; }
.not-found__card h1 { margin: 12px 0 16px; color: var(--qh-text-primary); }
.not-found__card p { margin: 0; color: var(--qh-text-secondary); }
.eyebrow { margin: 0; font-size: 13px; color: var(--qh-primary-deep); }
.actions { margin-top: 24px; display: flex; justify-content: center; gap: 16px; }
.actions a { padding: 10px 18px; border-radius: 999px; text-decoration: none; background: rgba(150, 200, 250, 0.15); color: var(--qh-primary-deep); }
</style>
