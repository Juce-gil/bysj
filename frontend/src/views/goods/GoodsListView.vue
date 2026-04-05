<template>
  <div class="goods-market">
    <section class="market-hero">
      <div class="market-hero__content">
        <div class="market-brand">
          <span class="market-brand__badge">科成</span>
          <div>
            <h1>校园闲置好物广场</h1>
            <p>像逛校园跳蚤市场一样浏览商品，支持搜索、分类、价格和状态组合筛选。</p>
          </div>
        </div>

        <div class="market-search">
          <el-input
            v-model="keyword"
            class="market-search__input"
            clearable
            placeholder="搜索商品名称、描述或你想要的关键词"
            @keyup.enter="handleSearch"
          >
            <template #prefix>🔎</template>
          </el-input>
          <button class="market-search__button" type="button" @click="handleSearch">搜索</button>
        </div>

        <div class="market-hotwords">
          <span class="market-hotwords__label">热门搜索</span>
          <button
            v-for="item in hotKeywords"
            :key="item"
            type="button"
            class="market-hotwords__item"
            @click="pickKeyword(item)"
          >
            {{ item }}
          </button>
        </div>

        <div class="market-stats">
          <article class="market-stat-card">
            <strong>{{ list.length }}</strong>
            <span>全部商品</span>
          </article>
          <article class="market-stat-card">
            <strong>{{ onSaleCount }}</strong>
            <span>正在出售</span>
          </article>
          <article class="market-stat-card">
            <strong>{{ recommendedGoods.length }}</strong>
            <span>猜你喜欢</span>
          </article>
        </div>
      </div>
    </section>

    <section class="market-notice qh-panel--subtle page-section">
      <div>
        <strong>📢 商品广场已升级</strong>
        <p>现在可以按关键词、分类、价格、校区和商品状态快速筛选，收藏量高的商品会优先推荐。</p>
      </div>
      <span class="market-notice__meta">当前展示 {{ filteredList.length }} / {{ list.length }} 件商品</span>
    </section>

    <section class="market-toolbar qh-panel page-section">
      <div class="market-toolbar__top">
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
          <el-select v-model="campus" clearable placeholder="全部校区">
            <el-option label="全部校区" value="" />
            <el-option v-for="item in campuses" :key="item" :label="item" :value="item" />
          </el-select>
          <el-input v-model="minPrice" placeholder="最低价" clearable />
          <el-input v-model="maxPrice" placeholder="最高价" clearable />
        </div>
      </div>

      <div class="category-tabs">
        <button
          type="button"
          class="category-tab"
          :class="{ 'is-active': !category }"
          @click="category = ''"
        >
          全部
        </button>
        <button
          v-for="item in categories"
          :key="item"
          type="button"
          class="category-tab"
          :class="{ 'is-active': category === item }"
          @click="category = item"
        >
          {{ item }}
        </button>
      </div>

      <div class="quick-filters">
        <button
          type="button"
          class="quick-filter"
          :class="{ 'is-active': !status }"
          @click="status = ''"
        >
          全部状态
        </button>
        <button
          v-for="item in statuses"
          :key="item.value"
          type="button"
          class="quick-filter"
          :class="{ 'is-active': status === item.value }"
          @click="status = item.value"
        >
          {{ item.label }}
        </button>
        <button
          type="button"
          class="quick-filter"
          :class="{ 'is-active': !condition }"
          @click="condition = ''"
        >
          全部成色
        </button>
        <button
          v-for="item in conditions"
          :key="item"
          type="button"
          class="quick-filter"
          :class="{ 'is-active': condition === item }"
          @click="condition = item"
        >
          {{ item }}
        </button>
      </div>

      <div class="market-toolbar__bottom">
        <span class="toolbar-summary">
          {{ hasFilters ? `筛选后找到 ${filteredList.length} 件商品` : '当前展示全部商品，可通过上方条件继续筛选' }}
        </span>
        <div class="filter-actions">
          <el-button @click="resetFilters">重置筛选</el-button>
          <el-button type="primary" plain :loading="loading" @click="loadGoods">刷新列表</el-button>
        </div>
      </div>
    </section>

    <section v-if="loading" class="grid-cards market-grid page-section">
      <el-skeleton v-for="item in 10" :key="item" animated class="loading-card">
        <template #template>
          <div class="loading-card__inner">
            <el-skeleton-item variant="image" style="width: 100%; height: 200px; border-radius: 20px;" />
            <el-skeleton-item variant="h3" style="width: 70%; margin-top: 16px;" />
            <el-skeleton-item variant="text" style="width: 100%; margin-top: 12px;" />
            <el-skeleton-item variant="text" style="width: 72%; margin-top: 8px;" />
          </div>
        </template>
      </el-skeleton>
    </section>

    <section v-else-if="errorMessage" class="page-section qh-panel state-panel">
      <el-result icon="warning" title="商品列表加载失败" :sub-title="errorMessage">
        <template #extra>
          <el-button type="primary" @click="loadGoods">重新加载</el-button>
        </template>
      </el-result>
    </section>

    <section v-else class="page-section goods-board qh-panel">
      <div v-if="filteredList.length" class="goods-board__inner">
        <div class="goods-board__header">
          <div>
            <h2>为你找到 {{ filteredList.length }} 件商品</h2>
            <p>点击卡片可进入详情页查看留言、收藏与预约信息。</p>
          </div>
          <el-tag effect="plain" type="warning">按 {{ activeSortLabel }} 排序</el-tag>
        </div>

        <section class="grid-cards market-grid">
          <article
            v-for="item in filteredList"
            :key="item.id"
            class="goods-card interactive-card"
            tabindex="0"
            @click="goDetail(item.id)"
            @keyup.enter="goDetail(item.id)"
          >
            <div class="goods-card__cover mock-cover" :style="{ background: getGoodsCardBackground(item) }">
              <span class="goods-card__cover-badge">{{ item.category }}</span>
              <span class="goods-card__cover-mark">{{ item.title.slice(0, 2) }}</span>
            </div>

            <div class="goods-card__body">
              <div class="goods-card__title-row">
                <h3>{{ item.title }}</h3>
                <span class="price-text">{{ formatPrice(item.price) }}</span>
              </div>

              <div class="goods-card__status-row">
                <el-tag size="small" effect="light" :type="getGoodsStatusMeta(item.status).type">
                  {{ getGoodsStatusMeta(item.status).text }}
                </el-tag>
                <span class="subtle-text">收藏 {{ item.favoriteCount }}</span>
              </div>

              <p class="goods-card__intro">{{ item.intro }}</p>
              <div class="card-meta goods-card__meta">
                <span>{{ item.category }}</span>
                <span>{{ item.campus }}</span>
                <span>{{ item.condition }}</span>
                <span>{{ item.publishedAt }}</span>
              </div>

              <div v-if="item.tags.length" class="tag-row">
                <el-tag v-for="tag in item.tags.slice(0, 3)" :key="tag" effect="plain">{{ tag }}</el-tag>
              </div>

              <div class="goods-card__actions">
                <span class="subtle-text">卖家：{{ item.seller }}</span>
                <span class="card-link">查看详情</span>
              </div>
            </div>
          </article>
        </section>
      </div>

      <div v-else class="market-empty">
        <div class="market-empty__icon">🛍️</div>
        <h2>暂时没有找到符合条件的宝贝</h2>
        <p>可以试着减少筛选条件，或者直接查看下方“猜你喜欢”的热门商品。</p>
        <div class="market-empty__actions">
          <el-button @click="resetFilters">清空筛选</el-button>
          <el-button type="primary" @click="loadGoods">重新加载</el-button>
        </div>
      </div>
    </section>

    <section v-if="recommendedGoods.length" class="page-section recommend-section">
      <div class="recommend-section__header">
        <div>
          <h2>猜你喜欢</h2>
          <p>按收藏量优先推荐的热门商品，帮助你更快发现校内高关注闲置。</p>
        </div>
        <el-tag effect="plain" type="danger">热门推荐</el-tag>
      </div>

      <section class="grid-cards market-grid">
        <article
          v-for="item in recommendedGoods"
          :key="`recommend-${item.id}`"
          class="goods-card goods-card--compact interactive-card"
          tabindex="0"
          @click="goDetail(item.id)"
          @keyup.enter="goDetail(item.id)"
        >
          <div class="goods-card__cover mock-cover goods-card__cover--compact" :style="{ background: getGoodsCardBackground(item) }">
            <span class="goods-card__cover-badge">{{ item.category }}</span>
            <span class="goods-card__cover-mark">{{ item.title.slice(0, 2) }}</span>
          </div>
          <div class="goods-card__body goods-card__body--compact">
            <h3>{{ item.title }}</h3>
            <div class="goods-card__compact-row">
              <span class="price-text price-text--small">{{ formatPrice(item.price) }}</span>
              <span class="subtle-text">收藏 {{ item.favoriteCount }}</span>
            </div>
          </div>
        </article>
      </section>
    </section>

    <aside class="market-float" aria-label="快捷入口">
      <RouterLink class="market-float__item" to="/user/posts">
        <strong>发布</strong>
        <span>闲置</span>
      </RouterLink>
      <RouterLink class="market-float__item" to="/user/notifications">
        <strong>消息</strong>
        <span>通知</span>
      </RouterLink>
      <RouterLink class="market-float__item" to="/user/appointments">
        <strong>预约</strong>
        <span>查看</span>
      </RouterLink>
      <RouterLink class="market-float__item" :to="userStore.isLoggedIn ? '/user/dashboard' : '/login'">
        <strong>{{ userStore.isLoggedIn ? '我的' : '登录' }}</strong>
        <span>{{ userStore.isLoggedIn ? '中心' : '账号' }}</span>
      </RouterLink>
    </aside>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getGoodsList, type GoodsItem } from '@/api/marketplace';
import { useUserStore } from '@/stores/user';
import { formatPrice } from '@/utils/format';
import { buildCoverBackground } from '@/utils/media';
import { getGoodsStatusMeta } from '@/utils/status';

type SortMode = 'comprehensive' | 'latest' | 'favorite' | 'price-asc' | 'price-desc';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const list = ref<GoodsItem[]>([]);
const loading = ref(false);
const errorMessage = ref('');
const keyword = ref('');
const category = ref('');
const campus = ref('');
const status = ref('');
const condition = ref('');
const minPrice = ref('');
const maxPrice = ref('');
const sortMode = ref<SortMode>('comprehensive');

const sortOptions: Array<{ label: string; value: SortMode }> = [
  { label: '综合', value: 'comprehensive' },
  { label: '最新发布', value: 'latest' },
  { label: '收藏优先', value: 'favorite' },
  { label: '价格升序', value: 'price-asc' },
  { label: '价格降序', value: 'price-desc' },
];

const getGoodsCardBackground = (item: GoodsItem) => buildCoverBackground(item.coverStyle, item.coverImageUrl || item.imageUrls[0]);

const toTime = (value: string) => {
  const time = new Date(value).getTime();
  return Number.isNaN(time) ? 0 : time;
};

const loadGoods = async () => {
  loading.value = true;
  errorMessage.value = '';
  try {
    list.value = await getGoodsList();
  } catch (error) {
    console.error('加载商品列表失败', error);
    errorMessage.value = '商品列表接口暂时不可用，请稍后重试。';
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
  await router.replace({ path: '/goods', query: nextQuery });
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
  category.value = '';
  campus.value = '';
  status.value = '';
  condition.value = '';
  minPrice.value = '';
  maxPrice.value = '';
  sortMode.value = 'comprehensive';
  syncKeywordQuery();
};

const goDetail = (id: number) => router.push(`/goods/${id}`);
const hasFilters = computed(() => Boolean(
  keyword.value.trim()
  || category.value
  || campus.value
  || status.value
  || condition.value
  || minPrice.value
  || maxPrice.value
  || sortMode.value !== 'comprehensive',
));
const categories = computed(() => [...new Set(list.value.map((item) => item.category).filter(Boolean))]);
const campuses = computed(() => [...new Set(list.value.map((item) => item.campus).filter(Boolean))]);
const conditions = computed(() => [...new Set(list.value.map((item) => item.condition).filter(Boolean))]);
const statuses = computed(() => [...new Set(list.value.map((item) => item.status))].map((value) => ({
  value,
  label: getGoodsStatusMeta(value).text,
})));
const activeSortLabel = computed(() => sortOptions.find((item) => item.value === sortMode.value)?.label ?? '综合');
const onSaleCount = computed(() => list.value.filter((item) => item.status === 'on_sale').length);
const hotKeywords = computed(() => {
  const bucket = new Set<string>();
  categories.value.slice(0, 4).forEach((item) => bucket.add(item));
  list.value
    .slice()
    .sort((a, b) => b.favoriteCount - a.favoriteCount)
    .slice(0, 6)
    .forEach((item) => {
      const title = item.title.trim();
      if (title) {
        bucket.add(title.length > 8 ? title.slice(0, 8) : title);
      }
    });
  return [...bucket].slice(0, 10);
});

const filteredList = computed(() => {
  const minimumPrice = minPrice.value === '' ? null : Number(minPrice.value);
  const maximumPrice = maxPrice.value === '' ? null : Number(maxPrice.value);

  const filtered = list.value.filter((item) => {
    const mergedText = `${item.title}${item.intro}${item.description}${item.tags.join('')}`.toLowerCase();
    const hitKeyword = !keyword.value || mergedText.includes(keyword.value.toLowerCase());
    const hitCategory = !category.value || item.category === category.value;
    const hitCampus = !campus.value || item.campus === campus.value;
    const hitStatus = !status.value || item.status === status.value;
    const hitCondition = !condition.value || item.condition === condition.value;
    const hitMinPrice = minimumPrice === null || Number.isNaN(minimumPrice) || item.price >= minimumPrice;
    const hitMaxPrice = maximumPrice === null || Number.isNaN(maximumPrice) || item.price <= maximumPrice;
    return hitKeyword && hitCategory && hitCampus && hitStatus && hitCondition && hitMinPrice && hitMaxPrice;
  });

  return filtered.slice().sort((a, b) => {
    switch (sortMode.value) {
      case 'latest':
        return toTime(b.publishedAt) - toTime(a.publishedAt);
      case 'favorite':
        return b.favoriteCount - a.favoriteCount || toTime(b.publishedAt) - toTime(a.publishedAt);
      case 'price-asc':
        return a.price - b.price;
      case 'price-desc':
        return b.price - a.price;
      case 'comprehensive':
      default:
        return (b.favoriteCount - a.favoriteCount) || (toTime(b.publishedAt) - toTime(a.publishedAt));
    }
  });
});

const recommendedGoods = computed(() => list.value
  .slice()
  .sort((a, b) => (b.favoriteCount - a.favoriteCount) || (toTime(b.publishedAt) - toTime(a.publishedAt)))
  .slice(0, 10));

watch(
  () => route.query.keyword,
  (value) => {
    keyword.value = typeof value === 'string' ? value : '';
  },
  { immediate: true },
);

onMounted(loadGoods);
</script>

<style scoped lang="scss">
.goods-market {
  position: relative;
  padding-bottom: 24px;
}

.market-hero {
  position: relative;
  overflow: hidden;
  border-radius: 32px;
  padding: 30px 34px;
  background: linear-gradient(135deg, #ffe35c 0%, #ffd640 48%, #fff4bc 100%);
  box-shadow: 0 22px 48px rgba(212, 175, 41, 0.22);
}

.market-hero::after {
  content: '';
  position: absolute;
  right: -60px;
  top: -60px;
  width: 240px;
  height: 240px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.56) 0%, rgba(255, 255, 255, 0) 72%);
}

.market-hero__content {
  position: relative;
  z-index: 1;
}

.market-brand {
  display: flex;
  align-items: center;
  gap: 18px;
  margin-bottom: 26px;
}

.market-brand__badge {
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

.market-brand h1 {
  margin: 0;
  color: #2f2b1c;
  font-size: 34px;
}

.market-brand p {
  margin: 10px 0 0;
  color: rgba(53, 47, 29, 0.8);
  font-size: 15px;
}

.market-search {
  display: flex;
  align-items: center;
  gap: 14px;
}

.market-search__input :deep(.el-input__wrapper) {
  min-height: 56px;
  border-radius: 999px;
  box-shadow: 0 0 0 2px rgba(39, 43, 57, 0.86) inset, 0 14px 26px rgba(99, 86, 19, 0.12);
  background: rgba(255, 255, 255, 0.96);
}

.market-search__button {
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

.market-search__button:hover {
  transform: translateY(-1px);
  box-shadow: 0 12px 24px rgba(57, 52, 30, 0.18);
}

.market-hotwords {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  margin-top: 18px;
}

.market-hotwords__label {
  color: rgba(47, 43, 28, 0.7);
  font-size: 14px;
}

.market-hotwords__item {
  border: none;
  background: rgba(255, 255, 255, 0.4);
  color: #3b3621;
  border-radius: 999px;
  padding: 8px 14px;
  cursor: pointer;
  transition: background 0.18s ease, transform 0.18s ease;
}

.market-hotwords__item:hover {
  background: rgba(255, 255, 255, 0.7);
  transform: translateY(-1px);
}

.market-stats {
  margin-top: 24px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.market-stat-card {
  padding: 16px 18px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.36);
  border: 1px solid rgba(255, 255, 255, 0.46);
  backdrop-filter: blur(8px);
}

.market-stat-card strong {
  display: block;
  color: #292516;
  font-size: 28px;
}

.market-stat-card span {
  display: block;
  margin-top: 8px;
  color: rgba(53, 47, 29, 0.78);
  font-size: 13px;
}

.market-notice {
  padding: 18px 22px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.market-notice strong {
  display: block;
  color: var(--qh-text-primary);
}

.market-notice p {
  margin: 6px 0 0;
  color: var(--qh-text-secondary);
}

.market-notice__meta {
  color: var(--qh-primary-deep);
  white-space: nowrap;
}

.market-toolbar {
  padding: 22px;
}

.market-toolbar__top,
.market-toolbar__bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
}

.market-toolbar__bottom {
  margin-top: 18px;
}

.sort-tabs,
.category-tabs,
.quick-filters {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.category-tabs {
  margin-top: 18px;
}

.quick-filters {
  margin-top: 18px;
}

.sort-tab,
.category-tab,
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
.category-tab.is-active,
.quick-filter.is-active,
.sort-tab:hover,
.category-tab:hover,
.quick-filter:hover {
  color: #5a4600;
  border-color: rgba(255, 209, 61, 0.7);
  background: rgba(255, 232, 140, 0.52);
}

.toolbar-side-filters {
  display: grid;
  grid-template-columns: repeat(3, minmax(120px, 160px));
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

.goods-board {
  padding: 24px;
}

.goods-board__header {
  margin-bottom: 22px;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.goods-board__header h2,
.recommend-section__header h2,
.market-empty h2 {
  margin: 0;
  color: var(--qh-text-primary);
}

.goods-board__header p,
.recommend-section__header p,
.market-empty p {
  margin: 10px 0 0;
  color: var(--qh-text-secondary);
}

.market-grid {
  grid-template-columns: repeat(5, minmax(0, 1fr));
}

.goods-card {
  overflow: hidden;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid rgba(146, 185, 220, 0.18);
  box-shadow: 0 14px 34px rgba(122, 173, 225, 0.1);
}

.goods-card__cover {
  position: relative;
  min-height: 210px;
  justify-content: space-between;
  flex-direction: column;
}

.goods-card__cover--compact {
  min-height: 180px;
}

.goods-card__cover-badge {
  align-self: flex-start;
  padding: 8px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.24);
  font-size: 13px;
  color: #fff;
  backdrop-filter: blur(8px);
}

.goods-card__cover-mark {
  font-size: 28px;
  font-weight: 800;
}

.goods-card__body {
  padding: 16px;
}

.goods-card__body--compact {
  padding: 14px 16px 16px;
}

.goods-card__title-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
}

.goods-card__title-row h3,
.goods-card__body--compact h3 {
  margin: 0;
  color: var(--qh-text-primary);
  line-height: 1.5;
}

.goods-card__status-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  margin-top: 12px;
}

.goods-card__intro {
  margin: 12px 0;
  min-height: 44px;
  line-height: 1.7;
  color: var(--qh-text-secondary);
}

.goods-card__meta {
  font-size: 12px;
}

.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 14px;
}

.goods-card__actions,
.goods-card__compact-row {
  margin-top: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.price-text--small {
  font-size: 22px;
}

.loading-card {
  padding: 16px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.7);
}

.loading-card__inner {
  padding: 2px;
}

.state-panel {
  padding: 12px;
}

.interactive-card {
  cursor: pointer;
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}

.interactive-card:hover,
.interactive-card:focus-visible {
  transform: translateY(-4px);
  box-shadow: 0 20px 40px rgba(122, 173, 225, 0.18);
  outline: none;
}

.card-link {
  color: var(--qh-primary-deep);
  white-space: nowrap;
  font-weight: 600;
}

.market-empty {
  min-height: 420px;
  display: grid;
  place-items: center;
  text-align: center;
  padding: 32px 20px;
}

.market-empty__icon {
  width: 110px;
  height: 110px;
  margin: 0 auto 18px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  font-size: 48px;
  background: radial-gradient(circle, rgba(255, 239, 169, 0.95) 0%, rgba(255, 248, 222, 0.64) 62%, rgba(255, 255, 255, 0) 100%);
}

.market-empty__actions {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  gap: 12px;
}

.recommend-section__header {
  margin-bottom: 20px;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.market-float {
  position: fixed;
  right: 18px;
  top: 50%;
  transform: translateY(-50%);
  z-index: 8;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.market-float__item {
  width: 72px;
  padding: 12px 8px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.95);
  border: 1px solid rgba(146, 185, 220, 0.18);
  box-shadow: 0 14px 30px rgba(122, 173, 225, 0.14);
  text-decoration: none;
  text-align: center;
}

.market-float__item strong {
  display: block;
  color: var(--qh-text-primary);
}

.market-float__item span {
  display: block;
  margin-top: 6px;
  color: var(--qh-text-secondary);
  font-size: 12px;
}

@media (max-width: 1440px) {
  .market-grid {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }
}

@media (max-width: 1200px) {
  .market-toolbar__top,
  .market-toolbar__bottom,
  .goods-board__header,
  .recommend-section__header,
  .market-notice {
    flex-direction: column;
    align-items: flex-start;
  }

  .toolbar-side-filters {
    width: 100%;
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .market-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .market-float {
    display: none;
  }
}

@media (max-width: 768px) {
  .goods-market {
    padding-bottom: 12px;
  }

  .market-hero {
    padding: 22px 18px;
    border-radius: 24px;
  }

  .market-brand {
    flex-direction: column;
    align-items: flex-start;
  }

  .market-brand h1 {
    font-size: 26px;
  }

  .market-search {
    flex-direction: column;
    align-items: stretch;
  }

  .market-stats {
    grid-template-columns: 1fr;
  }

  .toolbar-side-filters {
    grid-template-columns: 1fr;
  }

  .filter-actions,
  .market-empty__actions {
    width: 100%;
    display: grid;
    grid-template-columns: 1fr;
  }

  .market-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 560px) {
  .goods-board,
  .market-toolbar {
    padding: 18px;
  }

  .market-grid {
    grid-template-columns: 1fr;
  }
}
</style>



