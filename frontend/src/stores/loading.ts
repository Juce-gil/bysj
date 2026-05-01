import { defineStore } from 'pinia';
import { ref } from 'vue';

/**
 * 全局 Loading 状态管理
 */
export const useLoadingStore = defineStore('loading', () => {
  const loading = ref(false);
  const loadingText = ref('加载中...');
  const requestCount = ref(0);

  /**
   * 显示 Loading
   */
  const showLoading = (text = '加载中...') => {
    requestCount.value++;
    loading.value = true;
    loadingText.value = text;
  };

  /**
   * 隐藏 Loading
   */
  const hideLoading = () => {
    requestCount.value--;
    if (requestCount.value <= 0) {
      requestCount.value = 0;
      loading.value = false;
    }
  };

  /**
   * 强制隐藏 Loading
   */
  const forceHideLoading = () => {
    requestCount.value = 0;
    loading.value = false;
  };

  return {
    loading,
    loadingText,
    showLoading,
    hideLoading,
    forceHideLoading,
  };
});
