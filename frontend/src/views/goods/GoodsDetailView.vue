<template>
  <div class="goods-detail-market">
    <section v-if="loading" class="detail-loading qh-panel page-section">
      <el-skeleton animated :rows="12" />
    </section>

    <div v-else-if="detail" class="detail-page">
      <el-alert
        v-if="appointmentSuccessMessage"
        class="page-section"
        type="success"
        show-icon
        :closable="true"
        :title="appointmentSuccessMessage"
        @close="appointmentSuccessMessage = ''"
      />

      <section class="detail-hero page-section">
        <div class="detail-hero__cover qh-panel">
          <div class="mock-cover detail-hero__cover-inner" :style="{ background: detailCoverBackground }">
            <span class="detail-hero__cover-badge">{{ detail.category }}</span>
            <span class="detail-hero__cover-mark">{{ detail.title.slice(0, 2) }}</span>
          </div>
          <div v-if="detailGalleryImages.length > 1" class="detail-hero__thumbs">
            <button
              v-for="(image, index) in detailGalleryImages"
              :key="`${detail.id}-${index}`"
              type="button"
              class="detail-hero__thumb"
              :class="{ 'is-active': index === currentImageIndex }"
              @click="selectDetailImage(index)"
            >
              <span class="detail-hero__thumb-image" :style="{ background: buildCoverBackground(DETAIL_IMAGE_FALLBACK, image) }" />
            </button>
          </div>
        </div>

        <div class="detail-hero__content">
          <div class="detail-hero__top">
            <div>
              <span class="detail-hero__eyebrow">商品详情</span>
              <h1>{{ detail.title }}</h1>
              <p class="detail-hero__intro">{{ detail.intro }}</p>
            </div>
            <div class="detail-hero__links">
              <RouterLink class="hero-link" to="/goods">返回商品广场</RouterLink>
              <RouterLink class="hero-link" to="/user/appointments">我的预约</RouterLink>
            </div>
          </div>

          <div class="detail-hero__status-row">
            <el-tag effect="light" :type="statusMeta.type">{{ statusMeta.text }}</el-tag>
            <span class="subtle-text">卖家：{{ detail.seller }}</span>
            <span class="subtle-text">发布时间：{{ detail.publishedAt }}</span>
          </div>

          <div class="detail-hero__price-row">
            <span class="price-text">{{ formatPrice(detail.price) }}</span>
            <span v-if="detail.originalPrice > 0" class="detail-hero__original-price">原价 {{ formatPrice(detail.originalPrice) }}</span>
          </div>

          <div class="card-meta detail-hero__meta">
            <span>{{ detail.category }}</span>
            <span>{{ detail.campus }}</span>
            <span>{{ detail.condition }}</span>
            <span>收藏 {{ detail.favoriteCount }}</span>
          </div>

          <div v-if="detail.tags.length" class="tag-row detail-hero__tags">
            <el-tag v-for="tag in detail.tags" :key="tag" effect="plain">{{ tag }}</el-tag>
          </div>

          <div class="detail-hero__actions">
            <el-button type="primary" :disabled="!canAppointment" :loading="appointmentSubmitting" @click="openAppointmentDialog">
              {{ appointmentButtonText }}
            </el-button>
            <el-button :disabled="!userStore.isLoggedIn" :loading="favoriteSubmitting" @click="handleFavorite">
              {{ detail.favorited ? '取消收藏' : '收藏商品' }}（{{ detail.favoriteCount }}）
            </el-button>
          </div>

          <div class="detail-hero__description qh-panel--subtle">
            <h3>商品描述</h3>
            <p>{{ detail.description }}</p>
          </div>
        </div>
      </section>

      <section class="detail-summary page-section">
        <article class="summary-card qh-panel">
          <span>商品状态</span>
          <strong>{{ statusMeta.text }}</strong>
          <p>当前商品状态会直接影响预约按钮是否可点击。</p>
        </article>
        <article class="summary-card qh-panel">
          <span>卖家信息</span>
          <strong>{{ detail.seller }}</strong>
          <p>建议优先在校内公共区域完成面交与验货。</p>
        </article>
        <article class="summary-card qh-panel">
          <span>收藏热度</span>
          <strong>{{ detail.favoriteCount }}</strong>
          <p>热门商品按收藏量排序展示，符合你当前平台规则。</p>
        </article>
      </section>

      <section class="detail-grid page-section">
        <SectionCard title="卖家信息" subtitle="当前商品详情已接入收藏、预约与评论联调能力。">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="卖家昵称">{{ detail.seller }}</el-descriptions-item>
            <el-descriptions-item label="所属校区">{{ detail.campus }}</el-descriptions-item>
            <el-descriptions-item label="商品状态">{{ statusMeta.text }}</el-descriptions-item>
            <el-descriptions-item label="交易建议">优先选择校内公开区域面交，建议现场验货。</el-descriptions-item>
          </el-descriptions>
        </SectionCard>

        <SectionCard title="预约提醒" subtitle="预约成功后，联系方式会在预约流程内展示给双方。">
          <el-alert
            :type="detail.status === 'on_sale' ? 'info' : 'warning'"
            show-icon
            :closable="false"
            :title="detail.status === 'on_sale' ? '当前商品可直接发起预约，卖家确认后即可查看预约记录。' : `当前商品状态为 ${statusMeta.text}，暂时不能发起新的预约。`"
          />
          <div class="tips-list">
            <p>1. 请填写清晰的意向交易时间和地点，方便卖家快速确认。</p>
            <p>2. 若商品已被预约或已售出，预约按钮会自动禁用。</p>
            <p>3. 建议优先选择校内公开区域见面，现场验货后再完成交易。</p>
          </div>
        </SectionCard>
      </section>

      <section class="page-section">
        <SectionCard title="评论交流" subtitle="当前商品详情已接通评论列表与发表评论接口。">
          <div class="comment-form qh-panel--subtle">
            <el-input v-model="commentText" type="textarea" :rows="4" maxlength="200" show-word-limit placeholder="登录后可发布评论，最多 200 字。" />
            <div class="comment-actions">
              <el-button type="primary" :disabled="!userStore.isLoggedIn" :loading="commentSubmitting" @click="submitComment">发表评论</el-button>
            </div>
          </div>

          <div v-if="comments.length" class="comment-list">
            <article v-for="item in comments" :key="item.id" class="comment-item qh-panel--subtle">
              <div class="comment-item__head">
                <strong>{{ item.userName }}</strong>
                <span>{{ item.createTime }}</span>
              </div>
              <p>{{ item.content }}</p>
            </article>
          </div>
          <EmptyHint v-else title="还没有评论" description="成为第一个留言的人，和卖家确认更多商品细节。" />
        </SectionCard>
      </section>

      <el-dialog v-model="appointmentVisible" title="发起预约" width="520px" @closed="resetAppointmentForm">
        <div class="appointment-form">
          <el-date-picker v-model="appointmentForm.intendedTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="请选择意向交易时间" style="width: 100%;" />
          <el-input v-model="appointmentForm.intendedLocation" maxlength="100" placeholder="请输入意向交易地点" />
          <el-input v-model="appointmentForm.remark" type="textarea" :rows="4" maxlength="200" show-word-limit placeholder="可填写补充说明，如希望验机、面交时间段等。" />
        </div>
        <template #footer>
          <div class="dialog-actions">
            <el-button @click="appointmentVisible = false">取消</el-button>
            <el-button type="primary" :loading="appointmentSubmitting" @click="submitAppointment">提交预约</el-button>
          </div>
        </template>
      </el-dialog>
    </div>

    <section v-else-if="errorMessage" class="page-section qh-panel state-panel">
      <el-result icon="warning" title="商品详情加载失败" :sub-title="errorMessage">
        <template #extra>
          <el-button type="primary" @click="loadData">重新加载</el-button>
          <RouterLink class="hero-link" to="/goods">返回商品广场</RouterLink>
        </template>
      </el-result>
    </section>

    <EmptyHint v-else title="商品不存在" description="请检查商品链接，或返回商品广场浏览其他闲置。" />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { RouterLink, useRoute } from 'vue-router';
import EmptyHint from '@/components/EmptyHint.vue';
import SectionCard from '@/components/SectionCard.vue';
import {
  createAppointment,
  createComment,
  getComments,
  getGoodsDetail,
  toggleGoodsFavorite,
  type CommentItem,
  type GoodsItem,
} from '@/api/marketplace';
import { useUserStore } from '@/stores/user';
import { formatPrice } from '@/utils/format';
import { buildCoverBackground } from '@/utils/media';
import { getGoodsStatusMeta } from '@/utils/status';

const route = useRoute();
const userStore = useUserStore();
const loading = ref(false);
const errorMessage = ref('');
const detail = ref<GoodsItem | null>(null);
const comments = ref<CommentItem[]>([]);
const commentText = ref('');
const commentSubmitting = ref(false);
const favoriteSubmitting = ref(false);
const appointmentVisible = ref(false);
const appointmentSubmitting = ref(false);
const appointmentSuccessMessage = ref('');
const appointmentForm = reactive({
  intendedTime: '',
  intendedLocation: '',
  remark: '',
});

const DETAIL_IMAGE_FALLBACK = 'linear-gradient(135deg, #ffe58f 0%, #ffb703 100%)';
const currentImageIndex = ref(0);
const statusMeta = computed(() => getGoodsStatusMeta(detail.value?.status || ''));
const detailGalleryImages = computed(() => {
  if (!detail.value) return [] as string[];
  const images = detail.value.imageUrls?.filter(Boolean) ?? [];
  if (images.length) return images;
  return detail.value.coverImageUrl ? [detail.value.coverImageUrl] : [];
});
const activeImageUrl = computed(() => detailGalleryImages.value[currentImageIndex.value] || detail.value?.coverImageUrl || '');
const detailCoverBackground = computed(() => buildCoverBackground(detail.value?.coverStyle || DETAIL_IMAGE_FALLBACK, activeImageUrl.value));
const canAppointment = computed(() => detail.value?.status === 'on_sale');
const appointmentButtonText = computed(() => {
  if (!detail.value) return '发起预约';
  return detail.value.status === 'on_sale' ? '发起预约' : `当前${statusMeta.value.text}`;
});

const loadDetail = async () => {
  detail.value = await getGoodsDetail(Number(route.params.id));
  currentImageIndex.value = 0;
};

const selectDetailImage = (index: number) => {
  currentImageIndex.value = index;
};

const loadComments = async () => {
  comments.value = await getComments(Number(route.params.id));
};

const loadData = async () => {
  loading.value = true;
  errorMessage.value = '';
  detail.value = null;
  comments.value = [];
  try {
    await Promise.all([loadDetail(), loadComments()]);
  } catch (error) {
    console.error('加载商品详情失败', error);
    errorMessage.value = '商品详情接口暂时不可用，请稍后重试。';
  } finally {
    loading.value = false;
  }
};

const resetAppointmentForm = () => {
  appointmentForm.intendedTime = '';
  appointmentForm.intendedLocation = '';
  appointmentForm.remark = '';
};

const handleFavorite = async () => {
  if (!detail.value) return;
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再收藏商品');
    return;
  }

  favoriteSubmitting.value = true;
  try {
    const favorited = await toggleGoodsFavorite(detail.value.id);
    detail.value.favorited = favorited;
    detail.value.favoriteCount = Math.max(0, detail.value.favoriteCount + (favorited ? 1 : -1));
    ElMessage.success(favorited ? '已加入收藏' : '已取消收藏');
  } finally {
    favoriteSubmitting.value = false;
  }
};

const submitComment = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再发表评论');
    return;
  }

  if (!commentText.value.trim()) {
    ElMessage.warning('请输入评论内容');
    return;
  }

  commentSubmitting.value = true;
  try {
    const result = await createComment(Number(route.params.id), commentText.value.trim());
    comments.value = [...comments.value, result];
    commentText.value = '';
    ElMessage.success('评论成功');
  } finally {
    commentSubmitting.value = false;
  }
};

const openAppointmentDialog = () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再发起预约');
    return;
  }
  if (detail.value?.status !== 'on_sale') {
    ElMessage.warning('当前商品暂时无法预约');
    return;
  }
  appointmentVisible.value = true;
};

const submitAppointment = async () => {
  if (!detail.value) return;
  if (!appointmentForm.intendedTime || !appointmentForm.intendedLocation.trim()) {
    ElMessage.warning('请完善预约时间与地点');
    return;
  }

  appointmentSubmitting.value = true;
  try {
    await createAppointment({
      goodsId: detail.value.id,
      intendedTime: appointmentForm.intendedTime,
      intendedLocation: appointmentForm.intendedLocation.trim(),
      remark: appointmentForm.remark.trim(),
    });
    appointmentVisible.value = false;
    resetAppointmentForm();
    await loadDetail();
    appointmentSuccessMessage.value = '预约已提交成功，可在“我的预约”中查看当前处理状态。';
    ElMessage.success('预约已提交，可在“我的预约”中查看处理状态');
  } finally {
    appointmentSubmitting.value = false;
  }
};

onMounted(loadData);
</script>

<style scoped lang="scss">
.goods-detail-market {
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
  min-height: 460px;
  justify-content: space-between;
  flex-direction: column;
  font-size: 56px;
}

.detail-hero__thumbs {
  margin-top: 14px;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(72px, 1fr));
  gap: 12px;
}

.detail-hero__thumb {
  padding: 0;
  border: 2px solid transparent;
  border-radius: 18px;
  background: transparent;
  cursor: pointer;
  transition: transform 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease;
}

.detail-hero__thumb:hover,
.detail-hero__thumb.is-active {
  transform: translateY(-2px);
  border-color: rgba(255, 183, 3, 0.92);
  box-shadow: 0 12px 24px rgba(255, 183, 3, 0.18);
}

.detail-hero__thumb-image {
  display: block;
  width: 100%;
  aspect-ratio: 1 / 1;
  border-radius: 16px;
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

.detail-hero__top,
.detail-hero__status-row,
.detail-hero__price-row,
.detail-hero__actions,
.comment-item__head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
}

.detail-hero__top,
.comment-item__head {
  align-items: flex-start;
}

.detail-hero__content > * {
  position: relative;
  z-index: 1;
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
.tips-list p,
.comment-item p {
  margin: 0;
  line-height: 1.85;
  color: #584d2f;
}

.detail-hero__links,
.detail-hero__tags,
.comment-actions,
.dialog-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.hero-link {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 10px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.78);
  color: #463606;
}

.detail-hero__status-row {
  flex-wrap: wrap;
  margin-top: 18px;
}

.detail-hero__price-row {
  align-items: baseline;
  margin-top: 18px;
}

.detail-hero__original-price {
  color: #7a6a37;
  text-decoration: line-through;
}

.detail-hero__meta,
.detail-hero__tags,
.detail-hero__actions,
.detail-hero__description {
  margin-top: 18px;
}

.detail-hero__description {
  padding: 18px;
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
  margin: 14px 0 10px;
  font-size: 32px;
  color: var(--qh-primary-deep);
}

.summary-card p {
  margin: 0;
  color: var(--qh-text-secondary);
  line-height: 1.8;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.tips-list {
  display: grid;
  gap: 12px;
  margin-top: 18px;
}

.comment-form {
  padding: 18px;
}

.comment-actions {
  margin-top: 14px;
  justify-content: flex-end;
}

.comment-list {
  display: grid;
  gap: 16px;
  margin-top: 18px;
}

.comment-item {
  padding: 18px;
}

.comment-item__head strong {
  color: var(--qh-text-primary);
}

.comment-item__head span {
  color: var(--qh-text-secondary);
  font-size: 13px;
}

.appointment-form {
  display: grid;
  gap: 14px;
}

.dialog-actions {
  justify-content: flex-end;
}

@media (max-width: 1080px) {
  .detail-hero,
  .detail-grid,
  .detail-summary {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .detail-hero__cover-inner {
    min-height: 320px;
  }

  .detail-hero__thumbs {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }

  .detail-hero__content {
    padding: 22px 18px;
  }

  .detail-hero__content h1 {
    font-size: 30px;
  }

  .detail-hero__top,
  .detail-hero__status-row,
  .detail-hero__price-row,
  .detail-hero__actions,
  .comment-item__head {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>