<template>
  <div class="favorites-page">
    <section class="grid-cards grid-cards--4 page-section">
      <article v-for="item in stats" :key="item.label" class="stat-card qh-panel">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
        <p>{{ item.tip }}</p>
      </article>
    </section>

    <SectionCard title="我的收藏" subtitle="这里会汇总你收藏过的商品，并优先展示真实商品封面。">
      <template #extra>
        <div class="toolbar">
          <el-input v-model="keyword" clearable placeholder="搜索收藏商品" class="toolbar-input" />
          <el-button text :loading="loading" @click="loadFavorites">刷新</el-button>
        </div>
      </template>

      <el-skeleton v-if="loading" :rows="8" animated />
      <div v-else-if="errorMessage" class="state-panel qh-panel--subtle">
        <el-result icon="warning" title="收藏列表加载失败" :sub-title="errorMessage">
          <template #extra>
            <el-button type="primary" @click="loadFavorites">重新加载</el-button>
          </template>
        </el-result>
      </div>
      <div v-else-if="filteredFavorites.length" class="favorites-grid">
        <article v-for="item in filteredFavorites" :key="item.id" class="favorite-card qh-panel--subtle">
          <div class="mock-cover favorite-card__cover" :style="{ background: getGoodsCardBackground(item) }">{{ item.title.slice(0, 2) }}</div>
          <div class="favorite-card__body">
            <div class="favorite-card__head">
              <h3>{{ item.title }}</h3>
              <el-tag size="small" effect="light" :type="getGoodsStatusMeta(item.status).type">{{ getGoodsStatusMeta(item.status).text }}</el-tag>
            </div>
            <p>{{ item.intro }}</p>
            <div class="card-meta">
              <span>{{ item.category }}</span>
              <span>{{ item.campus }}</span>
              <span>{{ item.condition }}</span>
            </div>
            <div class="favorite-card__bottom">
              <span class="price-text">{{ formatPrice(item.price) }}</span>
              <div class="actions">
                <RouterLink :to="`/goods/${item.id}`">查看详情</RouterLink>
                <el-button link type="primary" :loading="isPending(item.id)" @click="removeFavorite(item)">取消收藏</el-button>
              </div>
            </div>
          </div>
        </article>
      </div>
      <EmptyHint
        v-else
        :title="favorites.length ? '没有匹配的收藏商品' : '你还没有收藏商品'"
        :description="favorites.length ? '可以换个关键词再试，或者清空搜索条件。' : '去商品详情页点一下收藏，喜欢的物品就会汇总到这里。'"
      />
    </SectionCard>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { RouterLink } from 'vue-router';
import SectionCard from '@/components/SectionCard.vue';
import EmptyHint from '@/components/EmptyHint.vue';
import { getMyFavorites, toggleGoodsFavorite, type GoodsItem } from '@/api/marketplace';
import { formatPrice } from '@/utils/format';
import { buildCoverBackground } from '@/utils/media';
import { getGoodsStatusMeta } from '@/utils/status';

const favorites = ref<GoodsItem[]>([]);
const loading = ref(false);
const errorMessage = ref('');
const keyword = ref('');
const pendingIds = ref<number[]>([]);

const stats = computed(() => {
  const total = favorites.value.length;
  const onSale = favorites.value.filter((item) => item.status === 'on_sale').length;
  const reserved = favorites.value.filter((item) => item.status === 'reserved').length;
  const sold = favorites.value.filter((item) => item.status === 'sold').length;
  return [
    { label: '收藏总数', value: String(total).padStart(2, '0'), tip: '已接入真实收藏列表' },
    { label: '在售商品', value: String(onSale).padStart(2, '0'), tip: '可继续发起预约或评论' },
    { label: '已预约', value: String(reserved).padStart(2, '0'), tip: '说明卖家已收到预约意向' },
    { label: '已售出', value: String(sold).padStart(2, '0'), tip: '仍可作为历史收藏留档' },
  ];
});

const filteredFavorites = computed(() => favorites.value.filter((item) => `${item.title}${item.intro}${item.description}`.toLowerCase().includes(keyword.value.toLowerCase())));
const getGoodsCardBackground = (item: GoodsItem) => buildCoverBackground(item.coverStyle, item.coverImageUrl || item.imageUrls[0]);
const isPending = (id: number) => pendingIds.value.includes(id);

const loadFavorites = async () => {
  loading.value = true;
  errorMessage.value = '';
  try {
    favorites.value = await getMyFavorites();
  } catch (error) {
    console.error('加载收藏列表失败', error);
    errorMessage.value = '收藏列表接口暂时不可用，请稍后重试。';
  } finally {
    loading.value = false;
  }
};

const removeFavorite = async (item: GoodsItem) => {
  if (isPending(item.id)) {
    return;
  }

  pendingIds.value = [...pendingIds.value, item.id];
  try {
    const favorited = await toggleGoodsFavorite(item.id);
    if (!favorited) {
      favorites.value = favorites.value.filter((favorite) => favorite.id !== item.id);
      ElMessage.success('已取消收藏');
      return;
    }

    await loadFavorites();
    ElMessage.warning('收藏状态已发生变化，已为你刷新列表');
  } finally {
    pendingIds.value = pendingIds.value.filter((id) => id !== item.id);
  }
};

onMounted(loadFavorites);
</script>

<style scoped lang="scss">
.stat-card { padding: 22px; }
.stat-card span { color: var(--qh-text-secondary); }
.stat-card strong { display: block; margin: 14px 0 10px; font-size: 30px; color: var(--qh-primary-deep); }
.stat-card p { margin: 0; color: var(--qh-text-secondary); }
.toolbar { display: flex; gap: 12px; align-items: center; }
.toolbar-input { width: 260px; }
.favorites-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 18px; }
.favorite-card { overflow: hidden; }
.favorite-card__cover { min-height: 180px; }
.favorite-card__body { padding: 18px; }
.favorite-card__head { display: flex; justify-content: space-between; gap: 12px; align-items: flex-start; }
.favorite-card__head h3 { margin: 0; color: var(--qh-text-primary); }
.favorite-card p { margin: 14px 0; line-height: 1.8; color: var(--qh-text-secondary); }
.favorite-card__bottom { margin-top: 18px; display: flex; justify-content: space-between; align-items: center; gap: 12px; }
.actions { display: flex; gap: 12px; align-items: center; }
.state-panel { padding: 12px; }
@media (max-width: 960px) {
  .toolbar { width: 100%; flex-wrap: wrap; justify-content: flex-end; }
  .toolbar-input,
  .favorites-grid { width: 100%; }
  .favorites-grid { grid-template-columns: 1fr; }
}
</style>