<template>
  <div class="announcement-detail-market">
    <section v-if="loading" class="detail-loading qh-panel page-section">
      <el-skeleton animated :rows="8" />
    </section>

    <section v-else-if="detail" class="detail-page">
      <section class="detail-hero page-section">
        <div class="detail-hero__content qh-panel">
          <div class="detail-hero__top">
            <div>
              <span class="detail-hero__eyebrow">{{ detail.level }}</span>
              <h1>{{ detail.title }}</h1>
              <p class="detail-hero__summary">{{ detail.summary || '这是一条平台公告，点击进入查看完整内容。' }}</p>
            </div>
            <div class="detail-hero__links">
              <RouterLink class="hero-link" to="/announcements">返回公告中心</RouterLink>
              <RouterLink class="hero-link" to="/">回到首页</RouterLink>
            </div>
          </div>

          <div class="detail-hero__meta-row">
            <el-tag effect="light" :type="getAnnouncementLevelTagType(detail.level)">{{ detail.level }}</el-tag>
            <span class="subtle-text">发布时间：{{ detail.publishedAt || '暂无发布时间' }}</span>
          </div>

          <div class="detail-hero__content-box qh-panel--subtle">
            <h3>公告正文</h3>
            <p>{{ detail.content || '暂无公告正文内容。' }}</p>
          </div>
        </div>
      </section>

      <section class="detail-summary page-section">
        <article class="summary-card qh-panel">
          <span>公告类型</span>
          <strong>{{ detail.level }}</strong>
          <p>支持在公告中心按类型筛选，并统一显示标签颜色。</p>
        </article>
        <article class="summary-card qh-panel">
          <span>发布时间</span>
          <strong>{{ detail.publishedAt || '待补充' }}</strong>
          <p>公告中心与首页会优先展示最新的公告内容。</p>
        </article>
        <article class="summary-card qh-panel">
          <span>查看建议</span>
          <strong>优先阅读</strong>
          <p>如涉及活动、通知或安全提醒，建议第一时间查看完整公告。</p>
        </article>
      </section>
    </section>

    <section v-else-if="errorMessage" class="page-section qh-panel state-panel">
      <el-result icon="warning" title="公告详情加载失败" :sub-title="errorMessage">
        <template #extra>
          <el-button type="primary" @click="loadDetail">重新加载</el-button>
          <RouterLink class="hero-link" to="/announcements">返回公告中心</RouterLink>
        </template>
      </el-result>
    </section>

    <EmptyHint v-else title="公告不存在" description="请检查路由参数，或返回公告中心查看其他内容。" />
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { RouterLink, useRoute } from 'vue-router';
import EmptyHint from '@/components/EmptyHint.vue';
import { getAnnouncementDetail, type AnnouncementItem } from '@/api/marketplace';
import { getAnnouncementLevelTagType } from '@/utils/status';

const route = useRoute();
const loading = ref(false);
const errorMessage = ref('');
const detail = ref<AnnouncementItem | null>(null);

const loadDetail = async () => {
  loading.value = true;
  errorMessage.value = '';
  detail.value = null;
  try {
    detail.value = await getAnnouncementDetail(Number(route.params.id));
  } catch (error) {
    console.error('加载公告详情失败', error);
    errorMessage.value = '公告详情接口暂时不可用，请稍后重试。';
  } finally {
    loading.value = false;
  }
};

watch(() => route.params.id, () => {
  loadDetail();
}, { immediate: true });
</script>

<style scoped lang="scss">
.announcement-detail-market {
  padding-bottom: 24px;
}

.detail-loading,
.state-panel {
  padding: 22px;
}

.detail-hero__content {
  position: relative;
  overflow: hidden;
  padding: 34px;
  border-radius: 32px;
  background: linear-gradient(135deg, #ffe35b 0%, #ffd73d 52%, #fff0aa 100%);
  box-shadow: 0 24px 52px rgba(213, 172, 41, 0.2);
}

.detail-hero__content::after {
  content: '';
  position: absolute;
  right: -70px;
  top: -70px;
  width: 240px;
  height: 240px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.42) 0%, rgba(255, 255, 255, 0) 72%);
}

.detail-hero__content > * {
  position: relative;
  z-index: 1;
}

.detail-hero__top,
.detail-hero__meta-row {
  display: flex;
  justify-content: space-between;
  gap: 16px;
}

.detail-hero__top {
  align-items: flex-start;
}

.detail-hero__eyebrow {
  display: inline-flex;
  padding: 8px 14px;
  border-radius: 999px;
  background: rgba(31, 37, 48, 0.9);
  color: #ffe89a;
  font-size: 13px;
}

.detail-hero__content h1 {
  margin: 16px 0 14px;
  font-size: 40px;
  line-height: 1.25;
  color: #262111;
}

.detail-hero__summary,
.detail-hero__content-box p,
.summary-card p {
  margin: 0;
  line-height: 1.9;
  color: rgba(44, 37, 19, 0.82);
}

.detail-hero__links {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.hero-link {
  background: rgba(255, 255, 255, 0.44);
  color: #2a2515;
  border-radius: 999px;
  padding: 10px 16px;
  text-decoration: none;
}

.detail-hero__meta-row,
.detail-hero__content-box {
  margin-top: 20px;
}

.detail-hero__meta-row {
  align-items: center;
  flex-wrap: wrap;
}

.detail-hero__content-box {
  padding: 20px 22px;
}

.detail-hero__content-box h3 {
  margin: 0 0 12px;
  color: var(--qh-text-primary);
}

.detail-hero__content-box p {
  white-space: pre-wrap;
}

.detail-summary {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
}

.summary-card {
  padding: 22px;
}

.summary-card span {
  color: var(--qh-text-secondary);
}

.summary-card strong {
  display: block;
  margin: 12px 0 10px;
  font-size: 24px;
  color: #5e4500;
}

@media (max-width: 1100px) {
  .detail-summary {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .detail-hero__content {
    padding: 22px 18px;
  }

  .detail-hero__content h1 {
    font-size: 30px;
  }

  .detail-hero__top,
  .detail-hero__meta-row {
    flex-direction: column;
    align-items: flex-start;
  }

  .hero-link {
    width: 100%;
  }
}
</style>
