<template>
  <RouterLink class="app-logo" :class="[`app-logo--${variant}`]" to="/">
    <div class="logo-mark">{{ variant === 'market' ? '闲置' : '科' }}</div>
    <div class="logo-text">
      <strong>{{ title }}</strong>
      <span>{{ subtitle }}</span>
    </div>
  </RouterLink>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    title?: string;
    subtitle?: string;
    variant?: 'default' | 'market';
  }>(),
  {
    title: '校园跳蚤市场',
    subtitle: '科成',
    variant: 'default',
  },
);
</script>

<style scoped lang="scss">
.app-logo {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  color: inherit;
  text-decoration: none;
}

.logo-mark {
  width: 42px;
  height: 42px;
  display: grid;
  place-items: center;
  border-radius: 14px;
  background: linear-gradient(135deg, #8bc6ff 0%, #dff2ff 100%);
  color: #1b4b7a;
  font-weight: 700;
  box-shadow: 0 10px 24px rgba(124, 175, 230, 0.24);
}

.logo-text {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
}

.logo-text strong {
  font-size: 16px;
  color: var(--qh-text-primary);
}

.logo-text span {
  font-size: 12px;
  color: var(--qh-text-secondary);
}

.app-logo--market {
  gap: 14px;
}

.app-logo--market .logo-mark {
  width: 72px;
  height: 72px;
  border-radius: 24px;
  background: #1f2530;
  color: #ffe25d;
  font-size: 20px;
  font-weight: 800;
  box-shadow: 0 14px 28px rgba(35, 31, 17, 0.18);
}

.app-logo--market .logo-text strong {
  font-size: 24px;
  color: #1f2530;
  font-weight: 800;
}

.app-logo--market .logo-text span {
  font-size: 13px;
  color: rgba(31, 37, 48, 0.7);
}
</style>
