<template>
  <img
    v-if="isLoaded || error"
    :src="error ? placeholder : currentSrc"
    :alt="alt"
    :class="imgClass"
    @error="handleError"
  />
  <div v-else :class="['lazy-image-placeholder', imgClass]">
    <span>加载中...</span>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';

interface Props {
  src: string;
  alt?: string;
  placeholder?: string;
  imgClass?: string;
}

const props = withDefaults(defineProps<Props>(), {
  alt: '',
  placeholder: 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 400 300"%3E%3Crect fill="%23f0f0f0" width="400" height="300"/%3E%3Ctext fill="%23999" x="50%25" y="50%25" dominant-baseline="middle" text-anchor="middle"%3E加载失败%3C/text%3E%3C/svg%3E',
  imgClass: '',
});

const currentSrc = ref('');
const isLoaded = ref(false);
const error = ref(false);
const observer = ref<IntersectionObserver | null>(null);
const imgElement = ref<HTMLImageElement | null>(null);

const loadImage = () => {
  const img = new Image();
  img.src = props.src;

  img.onload = () => {
    currentSrc.value = props.src;
    isLoaded.value = true;
  };

  img.onerror = () => {
    error.value = true;
    isLoaded.value = true;
  };
};

const handleError = () => {
  error.value = true;
};

onMounted(() => {
  // 使用 IntersectionObserver 实现懒加载
  if ('IntersectionObserver' in window) {
    observer.value = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting && !isLoaded.value) {
            loadImage();
            if (observer.value && entry.target) {
              observer.value.unobserve(entry.target);
            }
          }
        });
      },
      {
        rootMargin: '50px', // 提前50px开始加载
      }
    );

    // 观察占位元素
    const placeholder = document.querySelector('.lazy-image-placeholder');
    if (placeholder) {
      observer.value.observe(placeholder);
    }
  } else {
    // 不支持 IntersectionObserver 时直接加载
    loadImage();
  }
});

onUnmounted(() => {
  if (observer.value) {
    observer.value.disconnect();
  }
});
</script>

<style scoped>
.lazy-image-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  color: #999;
  min-height: 200px;
}
</style>
