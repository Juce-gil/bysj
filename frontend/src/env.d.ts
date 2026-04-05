/// <reference types="vite/client" />

import 'vue-router';

declare module 'vue-router' {
  interface RouteMeta {
    title?: string;
    requiresAuth?: boolean;
    role?: 'user' | 'admin';
    layout?: 'front' | 'user' | 'admin';
  }
}
