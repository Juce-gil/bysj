<template>
  <section class="section-card qh-panel">
    <header class="section-card__header" v-if="title || subtitle || $slots.extra">
      <div class="section-card__main">
        <h3 v-if="title">{{ title }}</h3>
        <p v-if="subtitle">{{ subtitle }}</p>
      </div>
      <div v-if="$slots.extra" class="section-card__extra">
        <slot name="extra" />
      </div>
    </header>
    <div class="section-card__body">
      <slot />
    </div>
  </section>
</template>

<script setup lang="ts">
defineProps<{ title?: string; subtitle?: string }>();
</script>

<style scoped lang="scss">
.section-card {
  padding: 24px;
}

.section-card__header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 20px;
}

.section-card__main {
  display: grid;
  gap: 8px;
}

.section-card__header h3 {
  margin: 0;
  font-size: 20px;
  color: var(--qh-text-primary);
  position: relative;
  padding-left: 14px;
}

.section-card__header h3::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  width: 6px;
  height: 22px;
  border-radius: 999px;
  background: linear-gradient(180deg, #ffd85b 0%, #202733 100%);
  transform: translateY(-50%);
}

.section-card__header p {
  margin: 0;
  font-size: 14px;
  color: var(--qh-text-secondary);
  line-height: 1.7;
}

.section-card__extra {
  display: flex;
  align-items: center;
  gap: 12px;
}

.section-card__body {
  color: var(--qh-text-regular);
}
</style>
