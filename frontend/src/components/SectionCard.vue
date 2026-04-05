<template>
  <section class="section-card qh-panel">
    <header class="section-card__header" v-if="title || subtitle || $slots.extra">
      <div>
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
.section-card { padding: 24px; }
.section-card__header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 20px;
}
.section-card__header h3 { margin: 0; font-size: 20px; color: var(--qh-text-primary); }
.section-card__header p { margin: 8px 0 0; font-size: 14px; color: var(--qh-text-secondary); }
.section-card__body { color: var(--qh-text-regular); }
</style>
