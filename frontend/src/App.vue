<template>
  <RouterView v-slot="{ Component }">
    <KeepAlive :include="['GoodsListView', 'WantedListView', 'AnnouncementListView']">
      <component :is="Component" />
    </KeepAlive>
  </RouterView>
  <GlobalLoading />
</template>

<script setup lang="ts">
import { RouterView } from 'vue-router';
import GlobalLoading from '@/components/GlobalLoading.vue';
</script>
