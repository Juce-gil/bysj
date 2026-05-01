<template>
  <el-loading
    v-model:visible="loadingStore.loading"
    :text="loadingStore.loadingText"
    fullscreen
    lock
  />
</template>

<script setup lang="ts">
import { useLoadingStore } from '@/stores/loading';

const loadingStore = useLoadingStore();
</script>
