<template>
  <div class="admin-goods-page">
    <section class="grid-cards grid-cards--4">
      <article v-for="item in stats" :key="item.label" class="stat-card qh-panel">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
        <p>{{ item.tip }}</p>
      </article>
    </section>

    <section class="page-section">
      <SectionCard title="筛选查询" subtitle="支持按关键字、状态和分类筛选管理员商品列表。">
        <el-form :inline="true" class="filter-form" @submit.prevent>
          <el-form-item label="关键字" class="filter-form__item filter-form__item--keyword">
            <el-input
              v-model="filters.keyword"
              clearable
              placeholder="搜索商品标题、卖家或分类"
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="状态" class="filter-form__item filter-form__item--select">
            <el-select v-model="filters.status" clearable placeholder="全部状态">
              <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="分类" class="filter-form__item filter-form__item--select">
            <el-select v-model="filters.category" clearable placeholder="全部分类">
              <el-option v-for="item in categoryOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
          <div class="filter-actions">
            <el-button @click="handleReset">重置</el-button>
            <el-button type="primary" :loading="loading" @click="handleSearch">查询</el-button>
          </div>
        </el-form>
      </SectionCard>
    </section>

    <section class="page-section">
      <SectionCard title="商品列表" subtitle="支持查看详情、管理员下架、重新上架和删除。">
        <template #extra>
          <div class="card-actions">
            <el-button text :loading="loading" @click="loadGoods">刷新</el-button>
          </div>
        </template>

        <el-alert
          type="info"
          show-icon
          :closable="false"
          title="当前页面已接入真实管理员商品接口，操作后会自动刷新当前列表。"
        />

        <el-skeleton v-if="loading && !tableData.length && !errorMessage" :rows="8" animated class="table-skeleton" />

        <div v-else-if="errorMessage" class="state-panel qh-panel--subtle">
          <el-result icon="warning" title="商品列表加载失败" :sub-title="errorMessage">
            <template #extra>
              <el-button type="primary" @click="loadGoods">重新加载</el-button>
            </template>
          </el-result>
        </div>

        <template v-else-if="tableData.length">
          <div v-loading="loading" class="table-wrapper">
            <el-table :data="tableData" row-key="id" stripe class="goods-table">
              <el-table-column label="商品信息" min-width="280">
                <template #default="scope">
                  <div class="goods-cell">
                    <strong>{{ scope.row.title }}</strong>
                    <p>{{ scope.row.category }} · 收藏 {{ scope.row.favoriteCount }}</p>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="sellerName" label="卖家" min-width="120" />
              <el-table-column label="价格" min-width="120">
                <template #default="scope">{{ formatPrice(scope.row.price) }}</template>
              </el-table-column>
              <el-table-column label="状态" min-width="120">
                <template #default="scope">
                  <el-tag effect="light" :type="getGoodsStatusMeta(scope.row.status).type">
                    {{ getGoodsStatusMeta(scope.row.status).text }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="发布时间" min-width="180">
                <template #default="scope">{{ formatDate(scope.row.publishedAt) }}</template>
              </el-table-column>
              <el-table-column label="操作" min-width="320" fixed="right">
                <template #default="scope">
                  <div class="table-actions">
                    <el-button link type="primary" @click="handleOpenDetail(scope.row)">查看详情</el-button>
                    <RouterLink class="table-link" :to="`/goods/${scope.row.id}`">前台详情</RouterLink>
                    <el-button
                      v-if="canOffShelf(scope.row)"
                      link
                      type="warning"
                      :loading="isActionLoading('off-shelf', scope.row.id)"
                      @click="handleOffShelf(scope.row)"
                    >
                      下架
                    </el-button>
                    <el-button
                      v-else-if="canRelist(scope.row)"
                      link
                      type="success"
                      :loading="isActionLoading('relist', scope.row.id)"
                      @click="handleRelist(scope.row)"
                    >
                      重新上架
                    </el-button>
                    <el-button
                      link
                      type="danger"
                      :disabled="!canDelete(scope.row)"
                      :loading="isActionLoading('delete', scope.row.id)"
                      @click="handleDelete(scope.row)"
                    >
                      删除
                    </el-button>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <div class="pagination-bar">
            <el-pagination
              v-model:current-page="pagination.pageNum"
              v-model:page-size="pagination.pageSize"
              background
              layout="total, sizes, prev, pager, next"
              :page-sizes="[10, 20, 30, 50]"
              :total="pagination.total"
              @current-change="loadGoods"
              @size-change="handlePageSizeChange"
            />
          </div>
        </template>

        <div v-else class="empty-wrapper">
          <EmptyHint title="暂无商品数据" description="当前筛选条件下没有匹配的商品记录。">
            <template #actions>
              <el-button type="primary" @click="handleReset">重置筛选</el-button>
              <el-button @click="loadGoods">刷新列表</el-button>
            </template>
          </EmptyHint>
        </div>
      </SectionCard>
    </section>

    <el-drawer v-model="detailVisible" title="商品详情" size="480px" destroy-on-close>
      <div v-if="detailLoading" class="detail-loading">
        <el-skeleton :rows="8" animated />
      </div>
      <div v-else-if="detailData" class="detail-panel">
        <div class="detail-head">
          <h2>{{ detailData.title }}</h2>
          <div class="detail-tags">
            <el-tag effect="light" :type="getGoodsStatusMeta(detailData.status).type">
              {{ getGoodsStatusMeta(detailData.status).text }}
            </el-tag>
            <el-tag effect="plain">{{ detailData.category }}</el-tag>
            <el-tag effect="plain">收藏 {{ detailData.favoriteCount }}</el-tag>
          </div>
          <p class="detail-price">
            <strong>{{ formatPrice(detailData.price) }}</strong>
            <span v-if="detailData.originalPrice > 0">原价 {{ formatPrice(detailData.originalPrice) }}</span>
          </p>
          <p class="detail-meta">卖家：{{ detailData.sellerName }} · 校区：{{ detailData.campus }} · 成色：{{ detailData.condition }}</p>
          <p class="detail-meta">发布时间：{{ formatDate(detailData.publishedAt) }}</p>
        </div>

        <div class="detail-actions qh-panel--subtle">
          <el-button
            v-if="canOffShelf(detailData)"
            type="warning"
            :loading="isActionLoading('off-shelf', detailData.id)"
            @click="handleOffShelf(detailData)"
          >
            下架商品
          </el-button>
          <el-button
            v-else-if="canRelist(detailData)"
            type="success"
            :loading="isActionLoading('relist', detailData.id)"
            @click="handleRelist(detailData)"
          >
            重新上架
          </el-button>
          <el-button
            type="danger"
            plain
            :disabled="!canDelete(detailData)"
            :loading="isActionLoading('delete', detailData.id)"
            @click="handleDelete(detailData)"
          >
            删除商品
          </el-button>
        </div>

        <section class="detail-section">
          <span class="detail-section__label">一句话简介</span>
          <p>{{ detailData.intro }}</p>
        </section>

        <section class="detail-section">
          <span class="detail-section__label">详细描述</span>
          <div class="detail-content qh-panel--subtle">{{ detailData.description }}</div>
        </section>

        <section class="detail-section">
          <span class="detail-section__label">标签</span>
          <div v-if="detailData.tags.length" class="detail-tags">
            <el-tag v-for="tag in detailData.tags" :key="tag" effect="plain">{{ tag }}</el-tag>
          </div>
          <p v-else class="detail-empty">暂无标签</p>
        </section>
      </div>
      <div v-else class="detail-empty-wrapper">
        <EmptyHint title="暂无详情数据" description="请重新选择商品后再试。" />
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { RouterLink } from 'vue-router';
import EmptyHint from '@/components/EmptyHint.vue';
import SectionCard from '@/components/SectionCard.vue';
import {
  deleteAdminGoods,
  getAdminGoodsDetail,
  getAdminGoodsPage,
  offShelfAdminGoods,
  relistAdminGoods,
  type AdminGoodsDetail,
  type AdminGoodsItem,
} from '@/api/adminGoods';
import { getCategories } from '@/api/marketplace';
import { formatDate, formatPrice } from '@/utils/format';
import { getGoodsStatusMeta } from '@/utils/status';

const filters = reactive({
  keyword: '',
  status: '',
  category: '',
});

const statusOptions = [
  { label: '在售中', value: 'on_sale' },
  { label: '已预约', value: 'reserved' },
  { label: '已售出', value: 'sold' },
  { label: '已下架', value: 'off_shelf' },
];

const categoryOptions = ref<string[]>([]);
const tableData = ref<AdminGoodsItem[]>([]);
const loading = ref(false);
const errorMessage = ref('');
const detailVisible = ref(false);
const detailLoading = ref(false);
const detailData = ref<AdminGoodsDetail | null>(null);
const currentDetailId = ref<number | null>(null);
const currentActionKey = ref('');

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
});

const stats = computed(() => {
  const currentPageReserved = tableData.value.filter((item) => item.status === 'reserved').length;
  const currentPageOffShelf = tableData.value.filter((item) => item.status === 'off_shelf').length;
  const currentPageOnSale = tableData.value.filter((item) => item.status === 'on_sale').length;
  return [
    { label: '商品总数', value: String(pagination.total).padStart(2, '0'), tip: '管理员可见的商品总记录数' },
    { label: '本页在售', value: String(currentPageOnSale).padStart(2, '0'), tip: '当前页仍处于在售状态的商品数' },
    { label: '本页预约', value: String(currentPageReserved).padStart(2, '0'), tip: '当前页已被预约的商品数' },
    { label: '本页下架', value: String(currentPageOffShelf).padStart(2, '0'), tip: '当前页处于下架状态的商品数' },
  ];
});

type GoodsActionTarget = Pick<AdminGoodsItem, 'id' | 'title' | 'status'>;

const loadCategories = async () => {
  try {
    const categories = await getCategories();
    categoryOptions.value = categories.map((item) => item.name);
  } catch (error) {
    console.error('加载商品分类失败', error);
  }
};

const loadGoods = async () => {
  loading.value = true;
  errorMessage.value = '';
  try {
    const result = await getAdminGoodsPage({
      keyword: filters.keyword || undefined,
      status: filters.status || undefined,
      category: filters.category || undefined,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
    });
    tableData.value = result.records;
    pagination.total = result.total;
    pagination.pageNum = result.pageNum;
    pagination.pageSize = result.pageSize;
  } catch (error) {
    console.error('加载管理员商品列表失败', error);
    errorMessage.value = '管理员商品列表加载失败，请稍后重试。';
  } finally {
    loading.value = false;
  }
};

const handleSearch = async () => {
  pagination.pageNum = 1;
  await loadGoods();
};

const handleReset = async () => {
  filters.keyword = '';
  filters.status = '';
  filters.category = '';
  pagination.pageNum = 1;
  await loadGoods();
};

const handlePageSizeChange = async (pageSize: number) => {
  pagination.pageSize = pageSize;
  pagination.pageNum = 1;
  await loadGoods();
};

const handleOpenDetail = async (item: AdminGoodsItem) => {
  detailVisible.value = true;
  detailLoading.value = true;
  currentDetailId.value = item.id;
  try {
    detailData.value = await getAdminGoodsDetail(item.id);
  } catch (error) {
    console.error('加载管理员商品详情失败', error);
    detailData.value = null;
  } finally {
    detailLoading.value = false;
  }
};

const canOffShelf = (item: GoodsActionTarget) => item.status === 'on_sale';
const canRelist = (item: GoodsActionTarget) => item.status === 'off_shelf';
const canDelete = (item: GoodsActionTarget) => item.status !== 'reserved';
const isActionLoading = (action: string, id: number) => currentActionKey.value === `${action}-${id}`;

const syncDetailIfMatched = (detail: AdminGoodsDetail) => {
  if (currentDetailId.value === detail.id) {
    detailData.value = detail;
  }
};

const handleOffShelf = async (item: GoodsActionTarget) => {
  if (!canOffShelf(item)) {
    ElMessage.warning('当前商品状态不支持下架。');
    return;
  }
  try {
    await ElMessageBox.confirm(`确认下架商品《${item.title}》吗？`, '下架确认', {
      type: 'warning',
      confirmButtonText: '确认下架',
      cancelButtonText: '取消',
    });
  } catch {
    return;
  }

  currentActionKey.value = `off-shelf-${item.id}`;
  try {
    const detail = await offShelfAdminGoods(item.id);
    syncDetailIfMatched(detail);
    ElMessage.success('商品已下架');
    await loadGoods();
  } catch (error) {
    console.error('管理员下架商品失败', error);
  } finally {
    currentActionKey.value = '';
  }
};

const handleRelist = async (item: GoodsActionTarget) => {
  if (!canRelist(item)) {
    ElMessage.warning('当前商品状态不支持重新上架。');
    return;
  }
  try {
    await ElMessageBox.confirm(`确认重新上架商品《${item.title}》吗？`, '重新上架确认', {
      type: 'warning',
      confirmButtonText: '确认上架',
      cancelButtonText: '取消',
    });
  } catch {
    return;
  }

  currentActionKey.value = `relist-${item.id}`;
  try {
    const detail = await relistAdminGoods(item.id);
    syncDetailIfMatched(detail);
    ElMessage.success('商品已重新上架');
    await loadGoods();
  } catch (error) {
    console.error('管理员重新上架商品失败', error);
  } finally {
    currentActionKey.value = '';
  }
};

const handleDelete = async (item: GoodsActionTarget) => {
  if (!canDelete(item)) {
    ElMessage.warning('已预约商品暂不支持直接删除。');
    return;
  }
  try {
    await ElMessageBox.confirm(`删除后不可恢复，确认删除商品《${item.title}》吗？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
    });
  } catch {
    return;
  }

  currentActionKey.value = `delete-${item.id}`;
  try {
    await deleteAdminGoods(item.id);
    if (tableData.value.length === 1 && pagination.pageNum > 1) {
      pagination.pageNum -= 1;
    }
    if (currentDetailId.value === item.id) {
      detailVisible.value = false;
      detailData.value = null;
      currentDetailId.value = null;
    }
    ElMessage.success('商品删除成功');
    await loadGoods();
  } catch (error) {
    console.error('管理员删除商品失败', error);
  } finally {
    currentActionKey.value = '';
  }
};

onMounted(async () => {
  await Promise.allSettled([loadCategories(), loadGoods()]);
});
</script>

<style scoped lang="scss">
.stat-card { padding: 22px; }
.stat-card span { color: var(--qh-text-secondary); }
.stat-card strong { display: block; margin: 14px 0 10px; font-size: 30px; color: var(--qh-primary-deep); }
.stat-card p { margin: 0; color: var(--qh-text-secondary); }
.filter-form { display: grid; grid-template-columns: minmax(0, 1.8fr) 180px 180px auto; gap: 16px; align-items: end; }
.filter-form :deep(.el-form-item) { margin-bottom: 0; }
.filter-form__item { min-width: 0; }
.filter-form__item :deep(.el-input),
.filter-form__item :deep(.el-select) { width: 100%; }
.filter-actions,
.card-actions,
.table-actions,
.detail-tags,
.detail-actions { display: flex; gap: 12px; }
.filter-actions { justify-content: flex-end; }
.card-actions { align-items: center; }
.table-skeleton { margin-top: 18px; }
.state-panel { margin-top: 18px; padding: 12px; }
.table-wrapper { margin-top: 18px; }
.goods-cell { display: grid; gap: 8px; }
.goods-cell strong { color: var(--qh-text-primary); line-height: 1.5; }
.goods-cell p,
.detail-meta,
.detail-empty { margin: 0; color: var(--qh-text-secondary); font-size: 13px; line-height: 1.7; }
.table-actions { flex-wrap: wrap; gap: 0 4px; }
.table-link { color: var(--qh-primary-deep); }
.pagination-bar { margin-top: 20px; display: flex; justify-content: flex-end; }
.empty-wrapper { margin-top: 18px; }
.detail-panel { min-height: 260px; }
.detail-head { display: grid; gap: 12px; }
.detail-tags { flex-wrap: wrap; }
.detail-head h2 { margin: 0; color: var(--qh-text-primary); line-height: 1.5; }
.detail-price { display: flex; flex-wrap: wrap; align-items: baseline; gap: 12px; margin: 0; }
.detail-price strong { font-size: 28px; color: var(--qh-primary-deep); }
.detail-price span { color: var(--qh-text-secondary); }
.detail-actions {
  margin-top: 20px;
  padding: 16px;
  border-radius: 18px;
  flex-wrap: wrap;
}
.detail-section { margin-top: 24px; display: grid; gap: 10px; }
.detail-section__label { font-size: 13px; color: var(--qh-text-secondary); }
.detail-section p { margin: 0; line-height: 1.8; color: var(--qh-text-regular); }
.detail-content {
  padding: 16px;
  line-height: 1.9;
  color: var(--qh-text-regular);
  white-space: pre-wrap;
  border-radius: 16px;
}
.detail-empty-wrapper,
.detail-loading { padding: 8px 0; }

@media (max-width: 1200px) {
  .filter-form { grid-template-columns: minmax(0, 1fr) 180px 180px; }
  .filter-actions { grid-column: 1 / -1; justify-content: flex-start; }
}

@media (max-width: 768px) {
  .filter-form { grid-template-columns: 1fr; }
  .filter-actions,
  .pagination-bar { justify-content: flex-start; }
  .pagination-bar { overflow-x: auto; }
}
</style>