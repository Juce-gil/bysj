<template>
  <div class="home-market">
    <section class="home-hero page-section qh-panel">
      <div class="home-hero__content">
        <span class="home-hero__badge">科成校园闲置交换平台</span>
        <h1>在科成，把闲置交给刚好需要的人。</h1>
        <p>
          首页已统一为黄黑搜索电商风格，保留商品、求购、公告、轮播与真实首页数据联调，适合作为毕业设计展示首页。
        </p>

        <div class="home-hero__actions">
          <RouterLink class="hero-action hero-action--primary" to="/goods">立即逛商品</RouterLink>
          <RouterLink class="hero-action" to="/wanted">查看求购</RouterLink>
          <RouterLink class="hero-action hero-action--dark" to="/announcements">浏览公告</RouterLink>
        </div>

        <div v-if="homeData.stats.length" class="hero-stats">
          <article v-for="item in homeData.stats" :key="item.label" class="hero-stat">
            <strong>{{ item.value }}</strong>
            <span>{{ item.label }}</span>
          </article>
        </div>
      </div>

      <div class="home-hero__aside qh-panel--subtle">
        <div class="quote-card">
          <span>今日推荐</span>
          <strong>让每一次转手，都发生在更安心的校园环境里。</strong>
          <p>支持收藏优先推荐、预约线下见面交易、公告通知与求购信息联动。</p>
        </div>

        <div class="hero-mini-grid">
          <article class="hero-mini-card">
            <span>热门商品</span>
            <strong>{{ homeData.featuredGoods.length }}</strong>
          </article>
          <article class="hero-mini-card">
            <span>热门求购</span>
            <strong>{{ homeData.hotWanted.length }}</strong>
          </article>
          <article class="hero-mini-card">
            <span>最新公告</span>
            <strong>{{ homeData.latestAnnouncements.length }}</strong>
          </article>
          <article class="hero-mini-card">
            <span>轮播推荐</span>
            <strong>{{ homeData.banners.length }}</strong>
          </article>
        </div>
      </div>
    </section>

    <section class="home-notice qh-panel--subtle page-section">
      <div>
        <strong>📢 首页推荐持续更新中</strong>
        <p>顶部风格已统一为黄黑搜索风格，首页重点展示热门商品、近期求购和最新公告。</p>
      </div>
      <div class="home-notice__actions">
        <span v-if="hasContent">当前已加载首页数据</span>
        <el-button text :loading="loading" @click="loadHomeData">刷新首页</el-button>
      </div>
    </section>

    <section class="page-section qh-panel banner-panel">
      <div class="section-head">
        <div>
          <h2>轮播推荐</h2>
          <p>支持跳转到商品详情、公告详情或自定义链接。</p>
        </div>
        <el-tag effect="plain" type="warning">首页焦点位</el-tag>
      </div>

      <el-skeleton v-if="loading && !homeData.banners.length" :rows="6" animated />
      <el-carousel v-else-if="homeData.banners.length" height="300px" indicator-position="outside" class="banner-carousel">
        <el-carousel-item v-for="item in homeData.banners" :key="item.id">
          <button type="button" class="banner-slide" :style="getBannerStyle(item)" @click="handleBannerClick(item)">
            <div class="banner-slide__overlay">
              <span class="banner-slide__badge">{{ getBannerLabel(item.jumpType) }}</span>
              <h3>{{ item.title }}</h3>
              <p>{{ getBannerDescription(item) }}</p>
              <span class="banner-slide__action">点击进入</span>
            </div>
          </button>
        </el-carousel-item>
      </el-carousel>
      <div v-else class="home-empty-state">
        <div class="home-empty-state__icon">🎯</div>
        <h3>暂未配置首页轮播</h3>
        <p>等后台配置轮播图后，这里会展示商品推荐、公告推荐和自定义跳转入口。</p>
      </div>
    </section>

    <section v-if="errorMessage && !hasContent" class="page-section qh-panel state-panel">
      <el-result icon="warning" title="首页数据加载失败" :sub-title="errorMessage">
        <template #extra>
          <el-button type="primary" @click="loadHomeData">重新加载</el-button>
        </template>
      </el-result>
    </section>
    <el-alert v-else-if="errorMessage" class="page-section" type="warning" show-icon :closable="false" :title="errorMessage" />

    <section class="page-section">
      <div class="section-head">
        <div>
          <h2>热门商品</h2>
          <p>按收藏量优先展示的高热度商品，优先使用真实上传图片作为卡片封面。</p>
        </div>
        <RouterLink class="section-link" to="/goods">前往商品广场</RouterLink>
      </div>

      <div v-if="homeData.featuredGoods.length" class="market-grid">
        <article
          v-for="item in homeData.featuredGoods"
          :key="item.id"
          class="market-card interactive-card"
          tabindex="0"
          @click="goToGoods(item.id)"
          @keyup.enter="goToGoods(item.id)"
        >
          <div class="market-card__cover mock-cover" :style="{ background: getGoodsCardBackground(item) }">
            <span class="market-card__badge">{{ item.category }}</span>
            <span class="market-card__mark">{{ item.title.slice(0, 2) }}</span>
          </div>
          <div class="market-card__body">
            <div class="market-card__title-row">
              <h3>{{ item.title }}</h3>
              <span class="price-text price-text--sm">{{ formatPrice(item.price) }}</span>
            </div>
            <div class="market-card__status-row">
              <el-tag size="small" effect="light" :type="getGoodsStatusMeta(item.status).type">
                {{ getGoodsStatusMeta(item.status).text }}
              </el-tag>
              <span class="subtle-text">收藏 {{ item.favoriteCount }}</span>
            </div>
            <p>{{ item.intro }}</p>
            <div class="card-meta">
              <span>{{ item.campus }}</span>
              <span>{{ item.condition }}</span>
              <span>{{ item.seller }}</span>
            </div>
          </div>
        </article>
      </div>
      <EmptyHint v-else title="暂无热门商品" description="等商品数据加载完成后，这里会展示按收藏量排序的商品。" />
    </section>

    <section class="home-columns page-section">
      <SectionCard title="求购大厅" subtitle="最近活跃的求购信息，帮助你快速找到交易需求。">
        <div v-if="homeData.hotWanted.length" class="wanted-list">
          <article
            v-for="item in homeData.hotWanted"
            :key="item.id"
            class="wanted-item qh-panel--subtle interactive-card"
            tabindex="0"
            @click="goToWanted(item.id)"
            @keyup.enter="goToWanted(item.id)"
          >
            <div class="wanted-item__head">
              <h3>{{ item.title }}</h3>
              <span class="wanted-item__budget">{{ item.budget }}</span>
            </div>
            <p>{{ item.intro }}</p>
            <div class="card-meta">
              <span>{{ item.category }}</span>
              <span>{{ item.campus }}</span>
              <span>{{ getWantedStatusMeta(item.status).text }}</span>
            </div>
          </article>
        </div>
        <EmptyHint v-else title="暂无求购信息" description="求购数据加载后，这里会展示热门求购内容。" />
      </SectionCard>

      <SectionCard title="公告中心" subtitle="最新公告、活动与提示会优先展示在这里。">
        <div v-if="homeData.latestAnnouncements.length" class="announcement-list">
          <article
            v-for="item in homeData.latestAnnouncements"
            :key="item.id"
            class="announcement-item qh-panel--subtle interactive-card"
            tabindex="0"
            @click="goToAnnouncement(item.id)"
            @keyup.enter="goToAnnouncement(item.id)"
          >
            <div class="announcement-item__head">
              <h3>{{ item.title }}</h3>
              <el-tag size="small" effect="light" :type="getAnnouncementLevelTagType(item.level)">{{ item.level }}</el-tag>
            </div>
            <p>{{ item.summary }}</p>
            <div class="announcement-item__foot">
              <span class="subtle-text">{{ item.publishedAt }}</span>
              <span class="section-link section-link--inline">查看详情</span>
            </div>
          </article>
        </div>
        <EmptyHint v-else title="暂无公告" description="公告加载完成后，这里会展示最新校园通知。" />
      </SectionCard>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { RouterLink, useRouter } from 'vue-router';
import EmptyHint from '@/components/EmptyHint.vue';
import SectionCard from '@/components/SectionCard.vue';
import { getHomeData, type BannerItem, type HomeData } from '@/api/marketplace';
import { formatPrice } from '@/utils/format';
import { buildCoverBackground } from '@/utils/media';
import { getAnnouncementLevelTagType, getGoodsStatusMeta, getWantedStatusMeta } from '@/utils/status';

const router = useRouter();
const loading = ref(false);
const errorMessage = ref('');
const homeData = reactive<HomeData>({
  stats: [],
  featuredGoods: [],
  hotWanted: [],
  latestAnnouncements: [],
  banners: [],
});

const bannerPalette = [
  'linear-gradient(135deg, #2d2411 0%, #8d6114 42%, #ffd351 100%)',
  'linear-gradient(135deg, #4b3108 0%, #aa731c 45%, #ffe081 100%)',
  'linear-gradient(135deg, #1e2832 0%, #55626d 38%, #f6c945 100%)',
  'linear-gradient(135deg, #623d0b 0%, #b37b1d 50%, #fff2b3 100%)',
];

const hasContent = computed(() => Boolean(
  homeData.stats.length
  || homeData.featuredGoods.length
  || homeData.hotWanted.length
  || homeData.latestAnnouncements.length
  || homeData.banners.length,
));

const getGoodsCardBackground = (item: HomeData['featuredGoods'][number]) =>
  buildCoverBackground(item.coverStyle, item.coverImageUrl || item.imageUrls[0]);

const getBannerStyle = (item: BannerItem) => ({
  background: buildCoverBackground(bannerPalette[item.id % bannerPalette.length], item.imageUrl),
});

const getBannerLabel = (jumpType: string) => {
  if (jumpType === 'goods') return '商品推荐';
  if (jumpType === 'announcement') return '公告推荐';
  return '自定义链接';
};

const getBannerDescription = (item: BannerItem) => {
  if (item.jumpType === 'goods') return '点击可直接跳转到对应商品详情页，查看价格、状态与收藏信息。';
  if (item.jumpType === 'announcement') return '点击可查看公告详情页，了解最新通知、活动与提示内容。';
  return item.jumpTarget ? `当前将跳转到：${item.jumpTarget}` : '点击查看配置的自定义跳转内容。';
};

const goToGoods = (id: number) => router.push(`/goods/${id}`);
const goToWanted = (id: number) => router.push(`/wanted/${id}`);
const goToAnnouncement = (id: number) => router.push(`/announcements/${id}`);

const handleBannerClick = (item: BannerItem) => {
  if (!item.jumpTarget) {
    ElMessage.info('当前轮播图暂未配置跳转目标');
    return;
  }

  if (/^https?:\/\//.test(item.jumpTarget)) {
    window.open(item.jumpTarget, '_blank', 'noopener');
    return;
  }

  router.push(item.jumpTarget);
};

const loadHomeData = async () => {
  loading.value = true;
  errorMessage.value = '';
  try {
    const result = await getHomeData();
    Object.assign(homeData, result);
  } catch (error) {
    console.error('加载首页数据失败', error);
    errorMessage.value = '首页数据加载失败，请稍后重试。';
  } finally {
    loading.value = false;
  }
};

onMounted(loadHomeData);
</script>

<style scoped lang="scss">
.home-market {
  padding-bottom: 24px;
}

.home-hero {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) 360px;
  gap: 22px;
  padding: 28px;
  background: linear-gradient(135deg, rgba(255, 235, 160, 0.96) 0%, rgba(255, 247, 204, 0.96) 100%);
}

.home-hero__badge {
  display: inline-flex;
  padding: 8px 14px;
  border-radius: 999px;
  background: #1f2329;
  color: #ffe08a;
  font-size: 13px;
}

.home-hero h1 {
  margin: 18px 0 12px;
  font-size: clamp(32px, 4vw, 46px);
  color: #1f2329;
}

.home-hero p {
  margin: 0;
  line-height: 1.85;
  color: #5f6368;
}

.home-hero__actions,
.hero-stats,
.hero-mini-grid,
.home-notice,
.home-notice__actions,
.section-head,
.market-card__title-row,
.market-card__status-row,
.wanted-item__head,
.announcement-item__head,
.announcement-item__foot {
  display: flex;
  gap: 12px;
}

.home-hero__actions,
.hero-stats,
.hero-mini-grid {
  margin-top: 22px;
  flex-wrap: wrap;
}

.hero-action {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 116px;
  padding: 12px 18px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.86);
  color: #1f2329;
  font-weight: 600;
}

.hero-action--primary {
  background: #111827;
  color: #ffe08a;
}

.hero-action--dark {
  background: #2d2411;
  color: #fff0ba;
}

.hero-stat,
.hero-mini-card,
.quote-card,
.market-card,
.wanted-item,
.announcement-item {
  border-radius: 24px;
}

.hero-stat,
.hero-mini-card {
  min-width: 120px;
  padding: 18px;
  background: rgba(255, 255, 255, 0.78);
}

.hero-stat strong,
.hero-mini-card strong {
  display: block;
  font-size: 30px;
  color: #1f2329;
}

.hero-stat span,
.hero-mini-card span,
.quote-card span {
  color: #7a6527;
}

.home-hero__aside {
  padding: 20px;
}

.quote-card {
  padding: 20px;
  background: rgba(255, 255, 255, 0.82);
}

.quote-card strong {
  display: block;
  margin: 14px 0 12px;
  font-size: 24px;
  line-height: 1.45;
  color: #1f2329;
}

.home-notice {
  justify-content: space-between;
  align-items: center;
  padding: 18px 22px;
}

.banner-panel {
  padding: 22px;
}

.section-head {
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 18px;
}

.section-head h2 {
  margin: 0 0 8px;
  color: #1f2329;
}

.section-head p {
  margin: 0;
  color: #6b7280;
}

.section-link {
  color: #a16207;
  font-weight: 600;
}

.section-link--inline {
  font-size: 13px;
}

.banner-slide {
  width: 100%;
  height: 100%;
  padding: 0;
  border: none;
  border-radius: 26px;
  overflow: hidden;
  cursor: pointer;
}

.banner-slide__overlay {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  align-items: flex-start;
  gap: 10px;
  padding: 28px;
  background: linear-gradient(180deg, rgba(8, 12, 20, 0.08) 0%, rgba(8, 12, 20, 0.62) 100%);
  color: #fff;
}

.banner-slide__badge,
.market-card__badge {
  display: inline-flex;
  align-items: center;
  padding: 8px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
  backdrop-filter: blur(8px);
}

.banner-slide__overlay h3 {
  margin: 0;
  font-size: 28px;
}

.banner-slide__overlay p {
  margin: 0;
  max-width: 620px;
  line-height: 1.8;
  color: rgba(255, 255, 255, 0.88);
}

.banner-slide__action {
  font-weight: 700;
}

.home-empty-state {
  padding: 32px 20px 20px;
  text-align: center;
}

.home-empty-state__icon {
  font-size: 42px;
}

.market-grid,
.home-columns {
  display: grid;
  gap: 18px;
}

.market-grid {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.market-card {
  overflow: hidden;
  border: 1px solid rgba(255, 183, 3, 0.18);
  background: rgba(255, 255, 255, 0.98);
}

.market-card__cover {
  min-height: 210px;
  display: flex;
  justify-content: space-between;
  flex-direction: column;
}

.market-card__mark {
  font-size: 30px;
  font-weight: 800;
  color: #fff;
}

.market-card__body {
  padding: 18px;
}

.market-card__title-row,
.market-card__status-row,
.announcement-item__foot {
  justify-content: space-between;
  align-items: center;
}

.market-card__title-row h3,
.wanted-item__head h3,
.announcement-item__head h3 {
  margin: 0;
  color: #1f2329;
}

.market-card p,
.wanted-item p,
.announcement-item p {
  margin: 12px 0 0;
  line-height: 1.8;
  color: #6b7280;
}

.market-card__status-row,
.card-meta,
.announcement-item__foot {
  margin-top: 12px;
}

.home-columns {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.wanted-list,
.announcement-list {
  display: grid;
  gap: 14px;
}

.wanted-item,
.announcement-item {
  padding: 18px;
}

.wanted-item__budget {
  font-weight: 700;
  color: #1f2329;
}

.interactive-card {
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.interactive-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 18px 36px rgba(15, 23, 42, 0.08);
}

@media (max-width: 1080px) {
  .home-hero,
  .home-columns,
  .market-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .home-notice,
  .home-notice__actions,
  .section-head,
  .market-card__title-row,
  .market-card__status-row,
  .wanted-item__head,
  .announcement-item__head,
  .announcement-item__foot {
    flex-direction: column;
    align-items: flex-start;
  }

  .banner-slide__overlay h3 {
    font-size: 22px;
  }
}
</style>