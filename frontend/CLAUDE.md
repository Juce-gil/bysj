# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is the **frontend** of a campus flea market platform (校园跳蚤市场 · 科成). It's a Vue 3 + TypeScript + Vite application using Element Plus for UI components. The platform enables students to buy/sell second-hand goods, post wanted ads, make appointments, and view announcements.

**Backend location**: `../backend` (Spring Boot application running on `http://127.0.0.1:8080`)

## Development Commands

```bash
# Install dependencies
npm install

# Start dev server (runs on http://localhost:5173)
npm run dev

# Type check and build for production
npm run build

# Preview production build
npm run preview
```

## Architecture

### Three-Layout System

The app uses three distinct layouts for different user contexts:

1. **FrontLayout** (`src/layout/FrontLayout.vue`) - Public-facing pages with yellow gradient header, search bar, and navigation
2. **UserLayout** (`src/layout/UserLayout.vue`) - Authenticated user dashboard with sidebar navigation
3. **AdminLayout** (`src/layout/AdminLayout.vue`) - Admin dashboard with dashboard-style hero section and metrics

### Routing & Authentication

- Routes defined in `src/router/index.ts` with three main sections: `/` (public), `/user/*` (authenticated users), `/admin/*` (admin only)
- Authentication uses JWT tokens stored in localStorage via `src/utils/storage.ts`
- Route guards check `requiresAuth` and `role` meta fields
- Admin users are automatically redirected from `/user/*` to `/admin/dashboard`

### State Management (Pinia)

- **useUserStore** (`src/stores/user.ts`) - Authentication state, login/logout, profile management
- **useAppStore** (`src/stores/app.ts`) - Global app settings (site name, slogan)

### API Layer

All API calls go through `src/api/http.ts` (axios instance with interceptors):
- Automatically adds `Authorization: Bearer <token>` header
- Base URL: `/api` (proxied to backend via Vite config)
- Response format: `{ code: number, message: string, data: T }`
- Normalizes errors and unwraps data envelope

**Key API modules**:
- `src/api/auth.ts` - Login, register, user profile
- `src/api/marketplace.ts` - Goods, wanted ads, comments, appointments, notifications, announcements (includes comprehensive type normalizers)
- `src/api/adminUser.ts` - Admin user management
- `src/api/adminGoods.ts` - Admin goods management
- `src/api/adminAnnouncement.ts` - Admin announcement management

### Data Normalization Pattern

`src/api/marketplace.ts` implements a defensive normalization pattern:
- All API responses are normalized through type-safe functions (`normalizeGoodsItem`, `normalizeWantedItem`, etc.)
- Provides fallback values for missing/invalid data
- Converts backend date formats (`normalizeLocalDateTime` replaces space with `T`)
- Use this pattern when adding new API endpoints

### Proxy Configuration

Vite dev server proxies two paths to backend (see `vite.config.ts`):
- `/api` → `http://127.0.0.1:8080/api`
- `/uploads` → `http://127.0.0.1:8080/uploads`

### Path Aliases

TypeScript path alias `@/*` maps to `src/*` (configured in `tsconfig.json` and `vite.config.ts`)

### Component Auto-Import

Element Plus components are auto-imported via `unplugin-vue-components`:
- No need to manually import Element Plus components
- Type definitions generated in `src/components.d.ts`
- Imports CSS automatically via `ElementPlusResolver({ importStyle: 'css' })`

### Styling

- Global styles in `src/assets/styles/index.scss`
- CSS custom properties for theming (e.g., `--qh-bg`, `--qh-text-primary`, `--qh-panel`)
- Utility classes: `.qh-shell` (max-width container), `.qh-panel` (card style), `.grid-cards` (grid layouts)
- Admin layout has distinct yellow/gold theme with dashboard-style metrics

### Build Configuration

- Manual code splitting in `vite.config.ts` separates Element Plus, icons, Vue Router, Pinia, and axios into separate chunks
- Chunk size warning limit: 800KB
- SCSS deprecation warnings silenced for `legacy-js-api`

## Key Patterns

### Authentication Flow

1. User logs in via `LoginView.vue` → calls `userStore.login()`
2. Store saves token + profile to localStorage
3. `http.ts` interceptor adds token to all requests
4. Router guard checks authentication before protected routes

### Image Uploads

Use `uploadGoodsImages()` from `src/api/marketplace.ts`:
- Accepts `File[]` array
- Returns `UploadImageItem[]` with `url`, `originalName`, `fileName`, `size`
- Use returned URLs in `imageUrls` field when creating goods/wanted posts

### Status Management

Goods and wanted ads have status fields:
- Goods: `on_shelf`, `off_shelf`, `sold`
- Wanted: `open`, `closed`
- Use dedicated API methods: `offShelfGoodsPost()`, `relistGoodsPost()`, `closeWantedPost()`, `reopenWantedPost()`

### Notification System

- `getUnreadNotificationCount()` for badge counts
- `markNotificationRead(id)` for single notification
- `markAllNotificationsRead()` for bulk marking

## Common Tasks

### Adding a New API Endpoint

1. Define TypeScript interfaces in `src/api/marketplace.ts` (or create new file)
2. Create normalizer functions following existing pattern
3. Add API method using `http.get/post/put/delete`
4. Export types and method

### Adding a New Route

1. Create view component in `src/views/`
2. Add route to `src/router/index.ts` under appropriate layout
3. Set `meta` fields: `title`, `requiresAuth`, `role`, `layout`

### Adding a New Store

1. Create file in `src/stores/`
2. Use `defineStore()` with composition API style
3. Export store hook (e.g., `useMyStore`)
4. Import in components via `import { useMyStore } from '@/stores/myStore'`

## Notes

- The project was previously named "qinghe_market" - legacy localStorage keys are still supported for backward compatibility
- Backend must be running on port 8080 for dev server proxy to work
- Element Plus Message and MessageBox styles are manually imported in `src/main.ts`
- All dates from backend use format `YYYY-MM-DD HH:mm:ss` and are normalized to ISO format for frontend use
