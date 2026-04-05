<template>
  <div class="wanted-market">
    <section class="wanted-hero qh-panel">
      <div>
        <span class="wanted-hero__eyebrow">求购大厅</span>
        <h1>看看同学们正在找什么</h1>
        <p>支持按关键词、分类、校区和状态筛选，帮助卖家更快找到真实需求。</p>
      </div>
      <div class="wanted-hero__stats">
        <article>
          <strong>{{ list.length }}</strong>
          <span>全部需求</span>
        </article>
        <article>
          <strong>{{ activeCount }}</strong>
          <span>求购中</span>
        </article>
        <article>
          <strong>{{ recommendedWanted.length }}</strong>
          <span>推荐展示</span>
        </article>
      </div>
    </section>

    <section class="wanted-toolbar qh-panel page-section">
      <div class="toolbar-row">
        <el-input
          v-model="keyword"
          clearable
          placeholder="搜索求购标题、描述或关键词"
          class="toolbar-input"
          @keyup.enter="handleSearch"
        />
        <el-select v-model="campus" clearable placeholder="全部校区" class="toolbar-select">
          <el-option label="全部校区" value="" />
          <el-option v-for="item in campuses" :key="item" :label="item" :value="item" />
        </el-select>
        <el-input v-model="budgetKeyword" clearable placeholder="预算关键词，例如 50" class="toolbar-input" />
      </div>

      <div class="toolbar-row toolbar-row--wrap">
        <div class="filter-group">
          <button type="button" class="filter-chip" :class="{ 'is-active': !category }" @click="category = ''">
            全部分类
          </button>
          <button
            v-for="item in categories"
            :key="item"
            type="button"
            class="filter-chip"
            :class="{ 'is-active': category === item }"
            @click="category = item"
          >
            {{ item }}
          </button>
        </div>
        <div class="filter-group">
          <button type="button" class="filter-chip" :class="{ 'is-active': !status }" @click="status = ''">
            全部状态
          </button>
          <button
            v-for="item in statuses"
            :key="item.value"
            type="button"
            class="filter-chip"
            :class="{ 'is-active': status === item.value }"
            @click="status = item.value"
          >
            {{ item.label }}
          </button>
        </div>
      </div>

      <div class="toolbar-row toolbar-row--between">
        <span class="toolbar-summary">
          {{ hasFilters ? ('筛选后找到 ' + filteredList.length + ' 条求购信息') : '当前展示全部求购信息，可继续按分类、校区和状态筛选。' }}
        </span>
        <div class="toolbar-actions">
          <el-button @click="resetFilters">重置筛选</el-button>
          <el-button type="primary" plain :loading="loading" @click="loadWanted">刷新列表</el-button>
        </div>
      </div>
    </section>

    <section v-if="loading" class="page-section">
      <el-skeleton :rows="10" animated />
    </section>

    <section v-else-if="errorMessage" class="page-section state-panel qh-panel">
      <el-result icon="warning" title="求购列表加载失败" :sub-title="errorMessage">
        <template #extra>
          <el-button type="primary" @click="loadWanted">重新加载</el-button>
        </template>
      </el-result>
    </section>

    <section v-else class="page-section">
      <div v-if="filteredList.length" class="wanted-grid">
        <article
          v-for="item in filteredList"
          :key="item.id"
          class="wanted-card qh-panel interactive-card"
          tabindex="0"
          @click="goDetail(item.id)"
          @keyup.enter="goDetail(item.id)"
        >
          <div class="wanted-card__cover mock-cover" :style="{ background: getWantedCardBackground(item) }">
            <span class="wanted-card__badge">{{ item.category }}</span>
            <span class="wanted-card__mark">{{ item.title.slice(0, 2) }}</span>
          </div>
          <div class="wanted-card__body">
            <div class="wanted-card__head">
              <h3>{{ item.title }}</h3>
              <span class="wanted-budget">{{ item.budget }}</span>
            </div>
            <div class="wanted-card__status">
              <el-tag size="small" effect="light" :type="getWantedStatusMeta(item.status).type">
                {{ getWantedStatusMeta(item.status).text }}
              </el-tag>
              <span class="subtle-text">截止 {{ item.deadline || '待补充' }}</span>
            </div>
            <p>{{ item.intro }}</p>
            <div class="card-meta">
              <span>{{ item.campus }}</span>
              <span>发布者 {{ item.publisher }}</span>
            </div>
            <div v-if="item.tags.length" class="tag-row">
              <el-tag v-for="tag in item.tags.slice(0, 3)" :key="tag" effect="plain">{{ tag }}</el-tag>
            </div>
          </div>
        </article>
      </div>
      <EmptyHint
        v-else
        title="暂时没有符合条件的求购信息"
        description="可以清空筛选后重试，或稍后再来看看新的需求。"
      >
        <template #action>
          <el-button type="primary" @click="resetFilters">清空筛选</el-button>
        </template>
      </EmptyHint>
    </section>

    <section v-if="recommendedWanted.length" class="page-section">
      <div class="recommend-head">
        <div>
          <h2>优先推荐</h2>
          <p>优先展示仍在求购中的热门需求。</p>
        </div>
        <el-tag effect="plain" type="danger">近期需求</el-tag>
      </div>
      <div class="wanted-grid wanted-grid--compact">
        <article
          v-for="item in recommendedWanted"
          :key="'recommend-' + item.id"
          class="wanted-card qh-panel interactive-card"
          tabindex="0"
          @click="goDetail(item.id)"
          @keyup.enter="goDetail(item.id)"
        >
          <div class="wanted-card__cover wanted-card__cover--compact mock-cover" :style="{ background: getWantedCardBackground(item) }">
            <span class="wanted-card__badge">{{ item.category }}</span>
            <span class="wanted-card__mark">{{ item.title.slice(0, 2) }}</span>
          </div>
          <div class="wanted-card__body wanted-card__body--compact">
            <h3>{{ item.title }}</h3>
            <div class="wanted-card__status">
              <span class="wanted-budget wanted-budget--small">{{ item.budget }}</span>
              <span class="subtle-text">{{ getWantedStatusMeta(item.status).text }}</span>
            </div>
          </div>
        </article>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import EmptyHint from '@/components/EmptyHint.vue';
import { getWantedList, type WantedItem } from '@/api/marketplace';
import { buildCoverBackground } from '@/utils/media';
import { getWantedStatusMeta } from '@/utils/status';

type SortStatus = '' | 'buying' | 'finished' | 'closed';

const route = useRoute();
const router = useRouter();
const list = ref<WantedItem[]>([]);
const loading = ref(false);
const errorMessage = ref('');
const keyword = ref('');
const category = ref('');
const campus = ref('');
const status = ref<SortStatus>('');
const budgetKeyword = ref('');

const categories = computed(() => Array.from(new Set(list.value.map((item) => item.category))).filter(Boolean));
const campuses = computed(() => Array.from(new Set(list.value.map((item) => item.campus))).filter(Boolean));
const statuses = computed<Array<{ value: SortStatus; label: string }>>(() => ['buying', 'finished', 'closed'].map((value) => ({ value: value as SortStatus, label: getWantedStatusMeta(value).text })));
const activeCount = computed(() => list.value.filter((item) => item.status === 'buying').length);
const hasFilters = computed(() => Boolean(keyword.value || category.value || campus.value || status.value || budgetKeyword.value));
const recommendedWanted = computed(() => list.value.filter((item) => item.status === 'buying').slice(0, 4));
const getWantedCardBackground = (item: WantedItem) => buildCoverBackground(item.coverStyle, item.coverImageUrl || item.imageUrls[0]);

const filteredList = computed(() => {
  const normalizedKeyword = keyword.value.trim().toLowerCase();
  const normalizedBudgetKeyword = budgetKeyword.value.trim().toLowerCase();
  return list.value.filter((item) => {
    const mergedText = `${item.title}${item.intro}${item.description}${item.tags.join('')}`.toLowerCase();
    const hitKeyword = !normalizedKeyword || mergedText.includes(normalizedKeyword);
    const hitCategory = !category.value || item.category === category.value;
    const hitCampus = !campus.value || item.campus === campus.value;
    const hitStatus = !status.value || item.status === status.value;
    const hitBudget = !normalizedBudgetKeyword || item.budget.toLowerCase().includes(normalizedBudgetKeyword);
    return hitKeyword && hitCategory && hitCampus && hitStatus && hitBudget;
  });
});

const loadWanted = async () => {
  loading.value = true;
  errorMessage.value = '';
  try {
    list.value = await getWantedList();
  } catch (error) {
    console.error('加载求购列表失败', error);
    errorMessage.value = '求购列表接口暂时不可用，请稍后重试。';
  } finally {
    loading.value = false;
  }
};

const resetFilters = () => {
  keyword.value = '';
  category.value = '';
  campus.value = '';
  status.value = '';
  budgetKeyword.value = '';
};

const handleSearch = () => {
  // 保留输入框回车行为，筛选由计算属性实时生效
};

const goDetail = (id: number) => {
  router.push(`/wanted/${id}`);
};

watch(
  () => route.query.keyword,
  (value) => {
    keyword.value = typeof value === 'string' ? value : '';
  },
  { immediate: true },
);

onMounted(loadWanted);
</script>

<style scoped lang="scss">
.wanted-market { display: flex; flex-direction: column; gap: 24px; }
.wanted-hero { display: flex; justify-content: space-between; gap: 24px; padding: 28px; }
.wanted-hero__eyebrow { display: inline-flex; font-size: 12px; letter-spacing: 0.22em; text-transform: uppercase; color: var(--qh-primary-deep); }
.wanted-hero h1 { margin: 12px 0 10px; color: var(--qh-text-primary); }
.wanted-hero p { margin: 0; line-height: 1.8; color: var(--qh-text-secondary); max-width: 700px; }
.wanted-hero__stats { display: flex; gap: 14px; align-items: stretch; }
.wanted-hero__stats article { min-width: 120px; padding: 18px; border-radius: 20px; background: rgba(236, 246, 255, 0.9); border: 1px solid var(--qh-border); }
.wanted-hero__stats strong { display: block; font-size: 28px; color: var(--qh-primary-deep); }
.wanted-hero__stats span { color: var(--qh-text-secondary); }
.wanted-toolbar { padding: 20px; display: grid; gap: 14px; }
.toolbar-row { display: flex; gap: 12px; align-items: center; }
.toolbar-row--wrap { flex-wrap: wrap; }
.toolbar-row--between { justify-content: space-between; }
.toolbar-input { width: 320px; }
.toolbar-select { width: 180px; }
.filter-group { display: flex; flex-wrap: wrap; gap: 10px; }
.filter-chip { padding: 8px 14px; border-radius: 999px; border: 1px solid var(--qh-border); background: #fff; color: var(--qh-text-regular); cursor: pointer; }
.filter-chip.is-active { background: var(--qh-primary-deep); color: #fff; border-color: var(--qh-primary-deep); }
.toolbar-summary { color: var(--qh-text-secondary); }
.toolbar-actions { display: flex; gap: 12px; }
.state-panel { padding: 12px; }
.wanted-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 18px; }
.wanted-grid--compact { grid-template-columns: repeat(4, minmax(0, 1fr)); }
.wanted-card { overflow: hidden; cursor: pointer; }
.wanted-card__cover { min-height: 210px; justify-content: space-between; flex-direction: column; }
.wanted-card__cover--compact { min-height: 160px; }
.wanted-card__badge { align-self: flex-start; padding: 8px 12px; border-radius: 999px; background: rgba(255, 255, 255, 0.24); color: #fff; font-size: 13px; }
.wanted-card__mark { font-size: 28px; font-weight: 800; }
.wanted-card__body { padding: 18px; }
.wanted-card__body--compact { padding: 16px; }
.wanted-card__head,
.wanted-card__status { display: flex; justify-content: space-between; gap: 12px; align-items: center; }
.wanted-card__head h3 { margin: 0; color: var(--qh-text-primary); }
.wanted-card p { margin: 14px 0; line-height: 1.8; color: var(--qh-text-secondary); }
.wanted-budget { font-size: 20px; font-weight: 700; color: var(--qh-primary-deep); }
.wanted-budget--small { font-size: 16px; }
.tag-row { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 12px; }
.recommend-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.recommend-head h2 { margin: 0; color: var(--qh-text-primary); }
.recommend-head p { margin: 10px 0 0; color: var(--qh-text-secondary); }

@media (max-width: 1080px) {
  .wanted-grid,
  .wanted-grid--compact { grid-template-columns: 1fr 1fr; }
}

@media (max-width: 768px) {
  .wanted-hero,
  .toolbar-row,
  .toolbar-row--between,
  .wanted-card__head,
  .wanted-card__status,
  .recommend-head {
    flex-direction: column;
    align-items: flex-start;
  }

  .toolbar-input,
  .toolbar-select { width: 100%; }

  .wanted-grid,
  .wanted-grid--compact { grid-template-columns: 1fr; }
}
</style>
