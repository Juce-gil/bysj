<template>
  <div class="empty-hint qh-panel--subtle">
    <div class="empty-hint__icon">QH</div>
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
  padding: 30px 28px;
  text-align: center;
  border-radius: 24px;
}

.empty-hint__icon {
  width: 58px;
  height: 58px;
  margin: 0 auto 16px;
  display: grid;
  place-items: center;
  border-radius: 18px;
  background: linear-gradient(135deg, rgba(125, 182, 255, 0.26), rgba(216, 236, 255, 0.94));
  color: var(--qh-primary-deep);
  font-weight: 800;
  letter-spacing: 0.06em;
}

.empty-hint h4 {
  margin: 0 0 10px;
  color: var(--qh-text-primary);
  font-size: 20px;
}

.empty-hint p {
  max-width: 520px;
  margin: 0 auto;
  color: var(--qh-text-secondary);
  line-height: 1.8;
}

.empty-hint__actions {
  margin-top: 18px;
  display: flex;
  justify-content: center;
  gap: 10px;
  flex-wrap: wrap;
}
</style>
