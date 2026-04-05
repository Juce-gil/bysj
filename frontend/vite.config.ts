import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import Components from 'unplugin-vue-components/vite';
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers';
import { fileURLToPath, URL } from 'node:url';

const manualChunks = (id: string) => {
  if (!id.includes('node_modules')) {
    return undefined;
  }

  if (id.includes('element-plus')) {
    return 'element-plus';
  }

  if (id.includes('@element-plus/icons-vue')) {
    return 'element-plus-icons';
  }

  if (id.includes('vue-router')) {
    return 'vue-router';
  }

  if (id.includes('pinia')) {
    return 'pinia';
  }

  if (id.includes('axios')) {
    return 'axios';
  }

  if (id.includes('/vue/')) {
    return 'vendor';
  }

  return 'vendor';
};

export default defineConfig({
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
        target: 'http://127.0.0.1:8080',
        changeOrigin: true,
      },
      '/uploads': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true,
      },
    },
  },
});
