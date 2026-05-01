<template>
  <RecycleScroller
    v-slot="{ item }"
    :items="items"
    :item-size="itemSize"
    :buffer="buffer"
    class="virtual-list"
    key-field="id"
  >
    <slot :item="item" />
  </RecycleScroller>
</template>

<script setup lang="ts" generic="T extends { id: number | string }">
import { RecycleScroller } from 'vue-virtual-scroller';
import 'vue-virtual-scroller/dist/vue-virtual-scroller.css';

interface Props {
  items: T[];
  itemSize?: number;
  buffer?: number;
}

withDefaults(defineProps<Props>(), {
  itemSize: 200,
  buffer: 200,
});
</script>

<style scoped>
.virtual-list {
  height: 100%;
}
</style>
