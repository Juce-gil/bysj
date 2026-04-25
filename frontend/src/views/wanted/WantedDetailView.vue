<template>
  <div class="wanted-detail-market">
    <section v-if="loading" class="detail-loading qh-panel page-section">
      <el-skeleton animated :rows="10" />
    </section>

    <div v-else-if="detail" class="detail-page">
      <section class="detail-hero page-section">
        <div class="detail-hero__cover qh-panel">
          <div class="mock-cover detail-hero__cover-inner" :style="{ background: detailCoverBackground }">
            <span class="detail-hero__cover-badge">{{ detail.category }}</span>
            <span class="detail-hero__cover-mark">{{ detail.title.slice(0, 2) }}</span>
          </div>
        </div>

        <div class="detail-hero__content">
          <div class="detail-hero__top">
            <div>
              <span class="detail-hero__eyebrow">求购详情</span>
              <h1>{{ detail.title }}</h1>
              <p class="detail-hero__intro">{{ detail.intro }}</p>
            </div>
            <div class="detail-hero__links">
              <RouterLink class="hero-link" to="/wanted">返回求购大厅</RouterLink>
              <RouterLink class="hero-link" to="/">回到首页</RouterLink>
            </div>
          </div>

          <div class="detail-hero__status-row">
            <el-tag effect="light" :type="statusMeta.type">{{ statusMeta.text }}</el-tag>
            <span class="subtle-text">发布者：{{ detail.publisher }}</span>
            <span class="subtle-text">截止日期：{{ detail.deadline || '待补充' }}</span>
          </div>

          <div class="detail-hero__budget-row">
            <span class="detail-budget">{{ detail.budget }}</span>
            <span class="subtle-text">校区：{{ detail.campus }}</span>
          </div>

          <div class="card-meta detail-hero__meta">
            <span>{{ detail.category }}</span>
            <span>{{ detail.campus }}</span>
            <span>发布者 {{ detail.publisher }}</span>
          </div>

          <div v-if="detail.tags.length" class="tag-row detail-hero__tags">
            <el-tag v-for="tag in detail.tags" :key="tag" effect="plain">{{ tag }}</el-tag>
          </div>

          <div class="detail-hero__description qh-panel--subtle">
            <h3>需求描述</h3>
            <p>{{ detail.description }}</p>
          </div>
        </div>
      </section>

      <section class="detail-summary page-section">
        <article class="summary-card qh-panel">
          <span>当前状态</span>
          <strong>{{ statusMeta.text }}</strong>
          <p>求购大厅会优先展示仍在求购中的需求信息。</p>
        </article>
        <article class="summary-card qh-panel">
          <span>预算信息</span>
          <strong>{{ detail.budget }}</strong>
          <p>预算字段已接入详情页和列表页统一展示。</p>
        </article>
        <article class="summary-card qh-panel">
          <span>联系权限</span>
          <strong>{{ detail.contactVisible ? '已可查看' : '登录后可见' }}</strong>
          <p>联系方式由后端根据登录状态控制返回。</p>
        </article>
      </section>

      <section class="detail-grid page-section">
        <SectionCard title="需求信息" subtitle="已与后端联调求购详情与联系方式显示规则。">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="预算区间">{{ detail.budget }}</el-descriptions-item>
            <el-descriptions-item label="需求分类">{{ detail.category }}</el-descriptions-item>
            <el-descriptions-item label="交易地点">{{ detail.campus }}</el-descriptions-item>
            <el-descriptions-item label="需求状态">{{ statusMeta.text }}</el-descriptions-item>
          </el-descriptions>
        </SectionCard>

        <SectionCard title="联系方式" subtitle="仅已登录用户可见真实联系方式。">
          <template v-if="detail.contactVisible">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="手机号">{{ detail.phone || '暂未提供' }}</el-descriptions-item>
              <el-descriptions-item label="QQ">{{ detail.qq || '暂未提供' }}</el-descriptions-item>
              <el-descriptions-item label="联系说明">建议先通过站内备注确认需求，再线下面交。</el-descriptions-item>
            </el-descriptions>
          </template>
          <EmptyHint v-else title="登录后可查看联系方式" description="为保护隐私，求购联系方式仅对已登录用户可见。" />
        </SectionCard>
      </section>
    </div>

    <section v-else-if="errorMessage" class="page-section qh-panel state-panel">
      <el-result icon="warning" title="求购详情加载失败" :sub-title="errorMessage">
        <template #extra>
          <el-button type="primary" @click="loadDetail">重新加载</el-button>
          <RouterLink class="hero-link" to="/wanted">返回求购大厅</RouterLink>
        </template>
      </el-result>
    </section>

    <EmptyHint v-else title="求购信息不存在" description="请检查路由参数，或返回求购大厅查看其他需求。" />
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { RouterLink, useRoute } from 'vue-router';
import EmptyHint from '@/components/EmptyHint.vue';
import SectionCard from '@/components/SectionCard.vue';
import { getWantedDetail, type WantedItem } from '@/api/marketplace';
import { buildCoverBackground } from '@/utils/media';
import { getWantedStatusMeta } from '@/utils/status';

const route = useRoute();
const loading = ref(false);
const errorMessage = ref('');
const detail = ref<WantedItem | null>(null);
const statusMeta = computed(() => getWantedStatusMeta(detail.value?.status || ''));
const detailCoverBackground = computed(() => buildCoverBackground(
  detail.value?.coverStyle || 'linear-gradient(135deg, #ffd666 0%, #ffb703 100%)',
  detail.value?.coverImageUrl || detail.value?.imageUrls?.[0],
));

const loadDetail = async () => {
  loading.value = true;
  errorMessage.value = '';
  detail.value = null;
  try {
    detail.value = await getWantedDetail(Number(route.params.id));
  } catch (error) {
    console.error('加载求购详情失败', error);
    errorMessage.value = '求购详情接口暂时不可用，请稍后重试。';
  } finally {
    loading.value = false;
  }
};

watch(() => route.params.id, () => {
  loadDetail();
}, { immediate: true });
</script>

<style scoped lang="scss">
.wanted-detail-market {
  padding-bottom: 24px;
}

.detail-loading,
.state-panel {
  padding: 22px;
}

.detail-hero {
  display: grid;
  grid-template-columns: 0.88fr 1.12fr;
  gap: 24px;
  align-items: stretch;
}

.detail-hero__cover,
.detail-hero__content {
  border-radius: 30px;
}

.detail-hero__cover {
  overflow: hidden;
  padding: 18px;
}

.detail-hero__cover-inner {
  min-height: 420px;
  justify-content: space-between;
  flex-direction: column;
  font-size: 56px;
}

.detail-hero__cover-badge {
  align-self: flex-start;
  padding: 8px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.24);
  color: #fff;
  font-size: 14px;
}

.detail-hero__cover-mark {
  font-weight: 800;
}

.detail-hero__content {
  position: relative;
  overflow: hidden;
  padding: 32px;
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
.detail-hero__status-row,
.detail-hero__budget-row {
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

.detail-hero__intro,
.detail-hero__description p,
.summary-card p {
  margin: 0;
  line-height: 1.85;
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

.detail-hero__status-row,
.detail-hero__budget-row,
.detail-hero__meta,
.detail-hero__tags,
.detail-hero__description {
  margin-top: 18px;
}

.detail-hero__status-row,
.detail-hero__budget-row {
  align-items: center;
  flex-wrap: wrap;
}

.detail-budget {
  padding: 10px 18px;
  border-radius: 999px;
  background: rgba(31, 37, 48, 0.88);
  color: #ffe89a;
  font-weight: 700;
}

.detail-hero__description {
  padding: 18px 20px;
}

.detail-hero__description h3 {
  margin: 0 0 12px;
  color: var(--qh-text-primary);
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
  font-size: 28px;
  color: #5e4500;
}

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

@media (max-width: 1100px) {
  .detail-hero,
  .detail-grid,
  .detail-summary {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .detail-hero__cover-inner {
    min-height: 300px;
  }

  .detail-hero__content {
    padding: 22px 18px;
  }

  .detail-hero__content h1 {
    font-size: 30px;
  }

  .detail-hero__top,
  .detail-hero__status-row,
  .detail-hero__budget-row {
    flex-direction: column;
    align-items: flex-start;
  }

  .hero-link {
    width: 100%;
  }
}
</style>
