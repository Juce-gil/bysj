<template>
  <div class="empty-hint qh-panel--subtle">
    <h4>{{ title }}</h4>
    <p>{{ description }}</p>
    <div v-if="$slots.actions" class="empty-hint__actions">
      <slot name="actions" />
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{ title: string; description: string }>();
</script>

<style scoped lang="scss">
.empty-hint {
  padding: 28px;
  text-align: center;
}
.empty-hint h4 {
  margin: 0 0 8px;
  color: var(--qh-text-primary);
}
.empty-hint p {
  margin: 0;
  color: var(--qh-text-secondary);
  line-height: 1.7;
}
.empty-hint__actions {
  margin-top: 16px;
  display: flex;
  justify-content: center;
  gap: 10px;
  flex-wrap: wrap;
}
</style>
