import { defineConfig, loadEnv } from 'vite';
import vue from '@vitejs/plugin-vue';
import Components from 'unplugin-vue-components/vite';
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers';
import { fileURLToPath, URL } from 'node:url';

const manualChunks = (id: string) => {
  if (!id.includes('node_modules')) {
    return undefined;
  }

  // ECharts 单独打包
  if (id.includes('echarts')) {
    return 'echarts';
  }

  // Element Plus 相关
  if (id.includes('element-plus')) {
    return 'element-plus';
  }

  if (id.includes('@element-plus/icons-vue')) {
    return 'element-plus-icons';
  }

  // 路由和状态管理
  if (id.includes('vue-router')) {
    return 'vue-router';
  }

  if (id.includes('pinia')) {
    return 'pinia';
  }

  // HTTP 请求库
  if (id.includes('axios')) {
    return 'axios';
  }

  // VueUse 工具库
  if (id.includes('@vueuse')) {
    return 'vueuse';
  }

  // 虚拟滚动
  if (id.includes('vue-virtual-scroller')) {
    return 'vue-virtual-scroller';
  }

  // Vue 核心
  if (id.includes('/vue/')) {
    return 'vue';
  }

  // 其他第三方库
  return 'vendor';
};

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');
  const apiBaseUrl = env.VITE_API_BASE_URL || 'http://127.0.0.1:8080';

  return {
    plugins: [
      vue(),
      Components({
        dts: 'src/components.d.ts',
        resolvers: [ElementPlusResolver({ importStyle: 'css' })],
      }),
    ],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url)),
      },
    },
    css: {
      preprocessorOptions: {
        scss: {
          silenceDeprecations: ['legacy-js-api'],
        },
      },
    },
    build: {
      chunkSizeWarningLimit: 800,
      rollupOptions: {
        output: {
          manualChunks,
        },
      },
    },
    server: {
      host: '0.0.0.0',
      port: 5173,
      proxy: {
        '/api': {
          target: apiBaseUrl,
          changeOrigin: true,
        },
        '/uploads': {
          target: apiBaseUrl,
          changeOrigin: true,
        },
      },
    },
  };
});
