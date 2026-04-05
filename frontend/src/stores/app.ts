import { defineStore } from 'pinia';
import { ref } from 'vue';

export const useAppStore = defineStore('app', () => {
  const siteName = ref('校园跳蚤市场');
  const siteSlogan = ref('让科成每一件闲置，都更快遇见下一位主人。');

  return {
    siteName,
    siteSlogan,
  };
});
