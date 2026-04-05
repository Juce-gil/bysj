<template>
  <div class="announcement-market">
    <section class="announcement-hero">
      <div class="announcement-hero__content">
        <div class="announcement-brand">
          <span class="announcement-brand__badge">公告</span>
          <div>
            <h1>科成最新通知都在这里</h1>
            <p>公告中心已统一黄黑风格，集中展示平台通知、活动预告与安全提醒，支持关键词、类型与排序快速查找。</p>
          </div>
        </div>

        <div class="announcement-search">
          <el-input
            v-model="keyword"
            class="announcement-search__input"
            clearable
            placeholder="搜索公告标题、摘要或正文关键词"
            @keyup.enter="handleSearch"
          >
            <template #prefix>📢</template>
          </el-input>
          <button class="announcement-search__button" type="button" @click="handleSearch">搜索</button>
        </div>

        <div class="announcement-hotwords">
          <span class="announcement-hotwords__label">热门标签</span>
          <button
            v-for="item in hotKeywords"
            :key="item"
            type="button"
            class="announcement-hotwords__item"
            @click="pickKeyword(item)"
          >
            {{ item }}
          </button>
        </div>

        <div class="announcement-stats">
          <article class="announcement-stat-card">
            <strong>{{ list.length }}</strong>
            <span>全部公告</span>
          </article>
          <article class="announcement-stat-card">
            <strong>{{ activityCount }}</strong>
            <span>活动类</span>
          </article>
          <article class="announcement-stat-card">
            <strong>{{ recommendedList.length }}</strong>
            <span>重点推荐</span>
          </article>
        </div>
      </div>
    </section>

    <section class="announcement-notice qh-panel--subtle page-section">
      <div>
        <strong>📢 公告中心已同步升级</strong>
        <p>现在可按公告类型、关键词与发布时间快速筛选，并统一为和首页、商品广场、求购大厅一致的黄黑风格。</p>
      </div>
      <span class="announcement-notice__meta">当前展示 {{ filteredList.length }} / {{ list.length }} 条公告</span>
    </section>

    <section class="announcement-toolbar qh-panel page-section">
      <div class="announcement-toolbar__top">
        <div class="sort-tabs">
          <button
            v-for="item in sortOptions"
            :key="item.value"
            type="button"
            class="sort-tab"
            :class="{ 'is-active': sortMode === item.value }"
            @click="sortMode = item.value"
          >
            {{ item.label }}
          </button>
        </div>

        <div class="toolbar-side-filters">
          <el-select v-model="level" clearable placeholder="全部类型">
            <el-option label="全部类型" value="" />
            <el-option v-for="item in levels" :key="item" :label="item" :value="item" />
          </el-select>
        </div>
      </div>

      <div class="quick-filters">
        <button
          type="button"
          class="quick-filter"
          :class="{ 'is-active': !level }"
          @click="level = ''"
        >
          全部类型
        </button>
        <button
          v-for="item in levels"
          :key="item"
          type="button"
          class="quick-filter"
          :class="{ 'is-active': level === item }"
          @click="level = item"
        >
          {{ item }}
        </button>
      </div>

      <div class="announcement-toolbar__bottom">
        <span class="toolbar-summary">
          {{ hasFilters ? `筛选后找到 ${filteredList.length} 条公告` : '当前展示全部公告，可通过上方条件继续筛选' }}
        </span>
        <div class="filter-actions">
          <el-button @click="resetFilters">重置筛选</el-button>
          <el-button type="primary" plain :loading="loading" @click="loadAnnouncements">刷新列表</el-button>
        </div>
      </div>
    </section>

    <section v-if="loading" class="announcement-board qh-panel page-section loading-panel">
      <div class="announcement-stack">
        <el-skeleton v-for="item in 5" :key="item" animated class="loading-card loading-card--row">
          <template #template>
            <div class="loading-card__inner">
              <el-skeleton-item variant="text" style="width: 18%;" />
              <el-skeleton-item variant="h3" style="width: 56%; margin-top: 14px;" />
              <el-skeleton-item variant="text" style="width: 92%; margin-top: 10px;" />
              <el-skeleton-item variant="text" style="width: 72%; margin-top: 8px;" />
            </div>
          </template>
        </el-skeleton>
      </div>
    </section>

    <section v-else-if="errorMessage" class="page-section qh-panel state-panel">
      <el-result icon="warning" title="公告列表加载失败" :sub-title="errorMessage">
        <template #extra>
          <el-button type="primary" @click="loadAnnouncements">重新加载</el-button>
        </template>
      </el-result>
    </section>

    <section v-else class="page-section announcement-board qh-panel">
      <div v-if="filteredList.length" class="announcement-board__inner">
        <div class="announcement-board__header">
          <div>
            <h2>为你找到 {{ filteredList.length }} 条公告</h2>
            <p>点击整行可进入公告详情页，继续查看完整通知内容。</p>
          </div>
          <el-tag effect="plain" type="warning">按 {{ activeSortLabel }} 排序</el-tag>
        </div>

        <div class="announcement-stack">
          <article
            v-for="item in filteredList"
            :key="item.id"
            class="announcement-row interactive-row"
            tabindex="0"
            @click="goDetail(item.id)"
            @keyup.enter="goDetail(item.id)"
          >
            <div class="announcement-row__icon">{{ item.level.slice(0, 1) }}</div>
            <div class="announcement-row__main">
              <div class="announcement-row__top">
                <el-tag effect="plain" :type="getAnnouncementLevelTagType(item.level)">{{ item.level }}</el-tag>
                <span class="subtle-text">发布时间：{{ item.publishedAt || '待补充' }}</span>
              </div>
              <h3>{{ item.title }}</h3>
              <p>{{ item.summary || '点击查看公告详情。' }}</p>
            </div>
            <span class="announcement-row__link">查看详情</span>
          </article>
        </div>
      </div>

      <div v-else class="announcement-empty">
        <div class="announcement-empty__icon">📣</div>
        <h2>暂时没有找到符合条件的公告</h2>
        <p>可以试着减少筛选条件，或者查看下方“重点推荐”的公告内容。</p>
        <div class="announcement-empty__actions">
          <el-button @click="resetFilters">清空筛选</el-button>
          <el-button type="primary" @click="loadAnnouncements">重新加载</el-button>
        </div>
      </div>
    </section>

    <section v-if="recommendedList.length" class="page-section recommend-section">
      <div class="recommend-section__header">
        <div>
          <h2>重点推荐</h2>
          <p>优先展示最新公告与活动通知，帮助用户更快获取平台重点信息。</p>
        </div>
        <el-tag effect="plain" type="danger">首页推荐风格</el-tag>
      </div>

      <div class="announcement-recommend-grid">
        <article
          v-for="item in recommendedList"
          :key="`recommend-${item.id}`"
          class="announcement-recommend-card interactive-card"
          tabindex="0"
          @click="goDetail(item.id)"
          @keyup.enter="goDetail(item.id)"
        >
          <div class="announcement-recommend-card__top">
            <el-tag effect="plain" :type="getAnnouncementLevelTagType(item.level)">{{ item.level }}</el-tag>
            <span class="subtle-text">{{ item.publishedAt || '待补充' }}</span>
          </div>
          <h3>{{ item.title }}</h3>
          <p>{{ item.summary || '点击查看公告详情。' }}</p>
          <span class="card-link">查看详情</span>
        </article>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getAnnouncementList, type AnnouncementItem } from '@/api/marketplace';
import { getAnnouncementLevelTagType } from '@/utils/status';

type SortMode = 'latest' | 'notice-first' | 'activity-first' | 'tip-first';

const route = useRoute();
const router = useRouter();
const list = ref<AnnouncementItem[]>([]);
const loading = ref(false);
const errorMessage = ref('');
const keyword = ref('');
const level = ref('');
const sortMode = ref<SortMode>('latest');

const sortOptions: Array<{ label: string; value: SortMode }> = [
  { label: '最新发布', value: 'latest' },
  { label: '通知优先', value: 'notice-first' },
  { label: '活动优先', value: 'activity-first' },
  { label: '提示优先', value: 'tip-first' },
];

const publishedTime = (value: string) => {
  const time = new Date(value).getTime();
  return Number.isNaN(time) ? 0 : time;
};

const levelPriority = (itemLevel: string, mode: SortMode) => {
  if (mode === 'notice-first') return itemLevel === '通知' ? 1 : 0;
  if (mode === 'activity-first') return itemLevel === '活动' ? 1 : 0;
  if (mode === 'tip-first') return itemLevel === '提示' ? 1 : 0;
  return 0;
};

const loadAnnouncements = async () => {
  loading.value = true;
  errorMessage.value = '';
  try {
    list.value = await getAnnouncementList();
  } catch (error) {
    console.error('加载公告列表失败', error);
    errorMessage.value = '公告列表接口暂时不可用，请稍后重试。';
  } finally {
    loading.value = false;
  }
};

const syncKeywordQuery = async () => {
  const nextKeyword = keyword.value.trim();
  const nextQuery = { ...route.query };
  if (nextKeyword) {
    nextQuery.keyword = nextKeyword;
  } else {
    delete nextQuery.keyword;
  }
  await router.replace({ path: '/announcements', query: nextQuery });
};

const handleSearch = () => {
  keyword.value = keyword.value.trim();
  syncKeywordQuery();
};

const pickKeyword = (value: string) => {
  keyword.value = value;
  syncKeywordQuery();
};

const resetFilters = () => {
  keyword.value = '';
  level.value = '';
  sortMode.value = 'latest';
  syncKeywordQuery();
};

const goDetail = (id: number) => router.push(`/announcements/${id}`);
const hasFilters = computed(() => Boolean(keyword.value.trim() || level.value || sortMode.value !== 'latest'));
const levels = computed(() => [...new Set(list.value.map((item) => item.level).filter(Boolean))]);
const activeSortLabel = computed(() => sortOptions.find((item) => item.value === sortMode.value)?.label ?? '最新发布');
const activityCount = computed(() => list.value.filter((item) => item.level === '活动').length);
const hotKeywords = computed(() => {
  const bucket = new Set<string>();
  levels.value.forEach((item) => bucket.add(item));
  list.value.slice(0, 8).forEach((item) => {
    const title = item.title.trim();
    if (title) {
      bucket.add(title.length > 8 ? title.slice(0, 8) : title);
    }
  });
  return [...bucket].slice(0, 10);
});

const filteredList = computed(() => {
  const filtered = list.value.filter((item) => {
    const hitKeyword = !keyword.value || `${item.title}${item.summary}${item.content}`.toLowerCase().includes(keyword.value.toLowerCase());
    const hitLevel = !level.value || item.level === level.value;
    return hitKeyword && hitLevel;
  });

  return filtered.slice().sort((a, b) => {
    if (sortMode.value === 'latest') {
      return publishedTime(b.publishedAt) - publishedTime(a.publishedAt);
    }

    return levelPriority(b.level, sortMode.value) - levelPriority(a.level, sortMode.value)
      || publishedTime(b.publishedAt) - publishedTime(a.publishedAt);
  });
});

const recommendedList = computed(() => list.value
  .slice()
  .sort((a, b) => publishedTime(b.publishedAt) - publishedTime(a.publishedAt))
  .slice(0, 6));

watch(
  () => route.query.keyword,
  (value) => {
    keyword.value = typeof value === 'string' ? value : '';
  },
  { immediate: true },
);

onMounted(loadAnnouncements);
</script>

<style scoped lang="scss">
.announcement-market {
  position: relative;
  padding-bottom: 24px;
}

.announcement-hero {
  position: relative;
  overflow: hidden;
  border-radius: 32px;
  padding: 30px 34px;
  background: linear-gradient(135deg, #ffe35c 0%, #ffd640 48%, #fff4bc 100%);
  box-shadow: 0 22px 48px rgba(212, 175, 41, 0.22);
}

.announcement-hero::after {
  content: '';
  position: absolute;
  right: -60px;
  top: -60px;
  width: 240px;
  height: 240px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.56) 0%, rgba(255, 255, 255, 0) 72%);
}

.announcement-hero__content {
  position: relative;
  z-index: 1;
}

.announcement-brand {
  display: flex;
  align-items: center;
  gap: 18px;
  margin-bottom: 26px;
}

.announcement-brand__badge {
  min-width: 74px;
  height: 74px;
  display: grid;
  place-items: center;
  border-radius: 24px;
  background: rgba(30, 33, 41, 0.9);
  color: #ffe35c;
  font-size: 24px;
  font-weight: 800;
  box-shadow: 0 14px 28px rgba(45, 41, 24, 0.2);
}

.announcement-brand h1 {
  margin: 0;
  color: #2f2b1c;
  font-size: 34px;
}

.announcement-brand p {
  margin: 10px 0 0;
  color: rgba(53, 47, 29, 0.8);
  font-size: 15px;
}

.announcement-search {
  display: flex;
  align-items: center;
  gap: 14px;
}

.announcement-search__input :deep(.el-input__wrapper) {
  min-height: 56px;
  border-radius: 999px;
  box-shadow: 0 0 0 2px rgba(39, 43, 57, 0.86) inset, 0 14px 26px rgba(99, 86, 19, 0.12);
  background: rgba(255, 255, 255, 0.96);
}

.announcement-search__button {
  height: 52px;
  min-width: 118px;
  border: none;
  border-radius: 999px;
  background: #1f2530;
  color: #fff7bd;
  font-weight: 700;
  cursor: pointer;
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}

.announcement-search__button:hover {
  transform: translateY(-1px);
  box-shadow: 0 12px 24px rgba(57, 52, 30, 0.18);
}

.announcement-hotwords {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  margin-top: 18px;
}

.announcement-hotwords__label {
  color: rgba(47, 43, 28, 0.7);
  font-size: 14px;
}

.announcement-hotwords__item {
  border: none;
  background: rgba(255, 255, 255, 0.4);
  color: #3b3621;
  border-radius: 999px;
  padding: 8px 14px;
  cursor: pointer;
  transition: background 0.18s ease, transform 0.18s ease;
}

.announcement-hotwords__item:hover {
  background: rgba(255, 255, 255, 0.7);
  transform: translateY(-1px);
}

.announcement-stats {
  margin-top: 24px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.announcement-stat-card {
  padding: 16px 18px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.36);
  border: 1px solid rgba(255, 255, 255, 0.46);
  backdrop-filter: blur(8px);
}

.announcement-stat-card strong {
  display: block;
  color: #292516;
  font-size: 28px;
}

.announcement-stat-card span {
  display: block;
  margin-top: 8px;
  color: rgba(53, 47, 29, 0.78);
  font-size: 13px;
}

.announcement-notice {
  padding: 18px 22px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.announcement-notice strong {
  display: block;
  color: var(--qh-text-primary);
}

.announcement-notice p {
  margin: 6px 0 0;
  color: var(--qh-text-secondary);
}

.announcement-notice__meta {
  color: var(--qh-primary-deep);
  white-space: nowrap;
}

.announcement-toolbar {
  padding: 22px;
}

.announcement-toolbar__top,
.announcement-toolbar__bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
}

.announcement-toolbar__bottom {
  margin-top: 18px;
}

.sort-tabs,
.quick-filters {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.quick-filters {
  margin-top: 18px;
}

.sort-tab,
.quick-filter {
  border: 1px solid rgba(150, 178, 210, 0.22);
  background: #f7fbff;
  color: var(--qh-text-regular);
  border-radius: 999px;
  padding: 10px 16px;
  cursor: pointer;
  transition: all 0.18s ease;
}

.sort-tab.is-active,
.quick-filter.is-active,
.sort-tab:hover,
.quick-filter:hover {
  color: #5a4600;
  border-color: rgba(255, 209, 61, 0.7);
  background: rgba(255, 232, 140, 0.52);
}

.toolbar-side-filters {
  display: grid;
  grid-template-columns: minmax(180px, 220px);
  gap: 12px;
}

.toolbar-summary {
  color: var(--qh-text-secondary);
}

.filter-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.announcement-board {
  padding: 24px;
}

.announcement-board__header,
.recommend-section__header {
  margin-bottom: 22px;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.announcement-board__header h2,
.recommend-section__header h2,
.announcement-empty h2 {
  margin: 0;
  color: var(--qh-text-primary);
}

.announcement-board__header p,
.recommend-section__header p,
.announcement-empty p {
  margin: 10px 0 0;
  color: var(--qh-text-secondary);
}

.announcement-stack {
  display: grid;
  gap: 16px;
}

.announcement-row {
  display: grid;
  grid-template-columns: 72px minmax(0, 1fr) auto;
  gap: 18px;
  align-items: center;
  padding: 20px;
  border-radius: 22px;
  background: rgba(255, 250, 233, 0.5);
  border: 1px solid rgba(237, 211, 116, 0.22);
}

.announcement-row__icon {
  width: 72px;
  height: 72px;
  display: grid;
  place-items: center;
  border-radius: 22px;
  background: linear-gradient(135deg, #1f2530 0%, #6d5127 100%);
  color: #ffe89a;
  font-size: 28px;
  font-weight: 800;
}

.announcement-row__main {
  min-width: 0;
}

.announcement-row__top {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.announcement-row h3,
.announcement-recommend-card h3 {
  margin: 12px 0 10px;
  color: var(--qh-text-primary);
}

.announcement-row p,
.announcement-recommend-card p {
  margin: 0;
  line-height: 1.8;
  color: var(--qh-text-secondary);
}

.announcement-row__link {
  white-space: nowrap;
  color: var(--qh-primary-deep);
  font-weight: 600;
}

.announcement-recommend-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
}

.announcement-recommend-card {
  padding: 20px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid rgba(146, 185, 220, 0.18);
  box-shadow: 0 14px 34px rgba(122, 173, 225, 0.1);
}

.announcement-recommend-card__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.loading-panel {
  overflow: hidden;
}

.loading-card {
  padding: 18px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.56);
}

.loading-card--row {
  background: rgba(255, 250, 233, 0.56);
}

.loading-card__inner {
  padding: 2px;
}

.state-panel {
  padding: 12px;
}

.interactive-card,
.interactive-row {
  cursor: pointer;
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}

.interactive-card:hover,
.interactive-card:focus-visible,
.interactive-row:hover,
.interactive-row:focus-visible {
  transform: translateY(-3px);
  box-shadow: 0 18px 38px rgba(122, 173, 225, 0.16);
  outline: none;
}

.card-link {
  display: inline-flex;
  margin-top: 16px;
  color: var(--qh-primary-deep);
  white-space: nowrap;
  font-weight: 600;
}

.announcement-empty {
  min-height: 420px;
  display: grid;
  place-items: center;
  text-align: center;
  padding: 32px 20px;
}

.announcement-empty__icon {
  width: 110px;
  height: 110px;
  margin: 0 auto 18px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  font-size: 48px;
  background: radial-gradient(circle, rgba(255, 239, 169, 0.95) 0%, rgba(255, 248, 222, 0.64) 62%, rgba(255, 255, 255, 0) 100%);
}

.announcement-empty__actions {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  gap: 12px;
}

@media (max-width: 1200px) {
  .announcement-toolbar__top,
  .announcement-toolbar__bottom,
  .announcement-board__header,
  .recommend-section__header,
  .announcement-notice,
  .announcement-row {
    flex-direction: column;
    align-items: flex-start;
  }

  .announcement-row {
    display: flex;
  }

  .toolbar-side-filters {
    width: 100%;
    grid-template-columns: 1fr;
  }

  .announcement-recommend-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .announcement-market {
    padding-bottom: 12px;
  }

  .announcement-hero {
    padding: 22px 18px;
    border-radius: 24px;
  }

  .announcement-brand {
    flex-direction: column;
    align-items: flex-start;
  }

  .announcement-brand h1 {
    font-size: 26px;
  }

  .announcement-search {
    flex-direction: column;
    align-items: stretch;
  }

  .announcement-stats,
  .filter-actions,
  .announcement-empty__actions,
  .announcement-recommend-grid {
    grid-template-columns: 1fr;
    width: 100%;
    display: grid;
  }

  .announcement-row__top,
  .announcement-recommend-card__top {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
