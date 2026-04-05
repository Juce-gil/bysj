<template>
  <div class="admin-announcement-page">
    <section class="grid-cards grid-cards--4">
      <article v-for="item in stats" :key="item.label" class="stat-card qh-panel">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
        <p>{{ item.tip }}</p>
      </article>
    </section>

    <section class="page-section">
      <SectionCard title="查询筛选" subtitle="支持按关键字和发布状态检索，列表数据来自管理员公告真实接口。">
        <el-form :inline="true" class="filter-form" @submit.prevent>
          <el-form-item label="关键字" class="filter-form__item filter-form__item--keyword">
            <el-input
              v-model="filters.keyword"
              clearable
              placeholder="搜索公告标题、摘要或正文关键字"
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="状态" class="filter-form__item filter-form__item--status">
            <el-select v-model="filters.status" placeholder="全部状态">
              <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
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
      <SectionCard title="公告列表" subtitle="支持查看详情、新增、编辑、发布、下线和删除，操作完成后自动刷新列表。">
        <template #extra>
          <div class="card-actions">
            <el-button text :loading="loading" @click="loadAnnouncements">刷新</el-button>
            <el-button type="primary" @click="handleOpenCreate">新增公告</el-button>
          </div>
        </template>

        <el-alert
          type="info"
          show-icon
          :closable="false"
          title="当前页面已接入管理员公告列表、详情、新增、编辑、发布、下线与删除接口，可直接进行真联调。"
        />

        <el-skeleton v-if="loading && !tableData.length && !errorMessage" :rows="8" animated class="table-skeleton" />
        <div v-else-if="errorMessage" class="state-panel qh-panel--subtle">
          <el-result icon="warning" title="公告列表加载失败" :sub-title="errorMessage">
            <template #extra>
              <el-button type="primary" @click="loadAnnouncements">重新加载</el-button>
            </template>
          </el-result>
        </div>
        <template v-else-if="tableData.length">
          <div v-loading="loading" class="table-wrapper">
            <el-table :data="tableData" row-key="id" stripe class="announcement-table">
              <el-table-column label="公告信息" min-width="320">
                <template #default="scope">
                  <div class="announcement-cell">
                    <div class="announcement-cell__header">
                      <strong>{{ scope.row.title }}</strong>
                      <el-tag v-if="scope.row.top" effect="light" type="danger">置顶</el-tag>
                    </div>
                    <p>{{ scope.row.summary || '暂无摘要' }}</p>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="level" label="类型" min-width="110">
                <template #default="scope">
                  <el-tag effect="plain" :type="getAnnouncementLevelTagType(scope.row.level)">{{ scope.row.level }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="状态" min-width="120">
                <template #default="scope">
                  <el-tag effect="light" :type="getPublishStatusMeta(scope.row.published).type">
                    {{ getPublishStatusMeta(scope.row.published).text }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="发布时间" min-width="180">
                <template #default="scope">{{ formatPublishTime(scope.row.publishedAt) }}</template>
              </el-table-column>
              <el-table-column label="操作" min-width="300" fixed="right">
                <template #default="scope">
                  <div class="table-actions">
                    <el-button link type="primary" @click="handleOpenDetail(scope.row)">查看</el-button>
                    <el-button link @click="handleOpenEdit(scope.row)">编辑</el-button>
                    <el-button
                      link
                      type="success"
                      :disabled="scope.row.published"
                      :loading="isActionLoading('publish', scope.row.id)"
                      @click="handlePublish(scope.row)"
                    >
                      发布
                    </el-button>
                    <el-button
                      link
                      type="warning"
                      :disabled="!scope.row.published"
                      :loading="isActionLoading('offline', scope.row.id)"
                      @click="handleOffline(scope.row)"
                    >
                      下线
                    </el-button>
                    <el-button
                      link
                      type="danger"
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
              background
              layout="total, sizes, prev, pager, next, jumper"
              :current-page="pagination.pageNum"
              :page-size="pagination.pageSize"
              :page-sizes="pageSizeOptions"
              :total="pagination.total"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
        </template>
        <div v-else class="empty-wrapper">
          <EmptyHint
            :title="hasFilters ? '没有符合条件的公告' : '当前暂无公告数据'"
            :description="hasFilters ? '可以尝试清空关键字或切换状态后重新查询。' : '公告列表接口暂未返回数据时，这里会展示统一空态。'"
          />
        </div>
      </SectionCard>
    </section>

    <el-drawer v-model="detailVisible" title="公告详情" size="560px" destroy-on-close>
      <div v-loading="detailLoading" class="detail-panel">
        <template v-if="detailData && !detailLoading">
          <div class="detail-head">
            <div class="detail-tags">
              <el-tag effect="plain" :type="getAnnouncementLevelTagType(detailData.level)">{{ detailData.level }}</el-tag>
              <el-tag effect="light" :type="getPublishStatusMeta(detailData.published).type">
                {{ getPublishStatusMeta(detailData.published).text }}
              </el-tag>
              <el-tag v-if="detailData.top" effect="light" type="danger">置顶</el-tag>
            </div>
            <h2>{{ detailData.title }}</h2>
            <p class="detail-time">发布时间：{{ formatPublishTime(detailData.publishedAt) }}</p>
          </div>
          <div class="detail-section">
            <span class="detail-section__label">摘要</span>
            <p>{{ detailData.summary || '暂无摘要' }}</p>
          </div>
          <div class="detail-section">
            <span class="detail-section__label">正文</span>
            <div class="detail-content qh-panel--subtle">{{ detailData.content || '暂无正文内容' }}</div>
          </div>
        </template>
        <el-empty v-else-if="detailErrorMessage" description="公告详情加载失败，请稍后重试。">
          <el-button type="primary" @click="retryLoadDetail">重新加载</el-button>
        </el-empty>
      </div>
    </el-drawer>

    <el-dialog
      v-model="formVisible"
      :title="dialogTitle"
      width="720px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <div v-loading="dialogLoading" class="form-panel">
        <el-form ref="formRef" :model="formModel" :rules="formRules" label-position="top" class="announcement-form">
          <el-form-item label="公告标题" prop="title">
            <el-input v-model="formModel.title" maxlength="100" show-word-limit placeholder="请输入公告标题" />
          </el-form-item>
          <el-form-item label="公告摘要" prop="summary">
            <el-input
              v-model="formModel.summary"
              type="textarea"
              :rows="3"
              maxlength="255"
              show-word-limit
              placeholder="请输入公告摘要，可留空"
            />
          </el-form-item>
          <div class="form-grid">
            <el-form-item label="公告类型" prop="level">
              <el-select
                v-model="formModel.level"
                filterable
                allow-create
                default-first-option
                placeholder="请选择或输入公告类型"
              >
                <el-option v-for="item in levelOptions" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
            <el-form-item label="附加设置">
              <div class="switch-group qh-panel--subtle">
                <div class="switch-item">
                  <div>
                    <strong>置顶公告</strong>
                    <p>置顶后会在前台列表中优先展示</p>
                  </div>
                  <el-switch v-model="formModel.top" />
                </div>
                <div class="switch-item">
                  <div>
                    <strong>立即发布</strong>
                    <p>关闭时保存为未发布状态，可稍后手动发布</p>
                  </div>
                  <el-switch v-model="formModel.published" />
                </div>
              </div>
            </el-form-item>
          </div>
          <el-form-item label="公告正文" prop="content">
            <el-input
              v-model="formModel.content"
              type="textarea"
              :rows="10"
              maxlength="10000"
              show-word-limit
              placeholder="请输入公告正文内容"
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="formVisible = false">取消</el-button>
          <el-button type="primary" :loading="saveLoading" @click="handleSubmitForm">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus';
import EmptyHint from '@/components/EmptyHint.vue';
import SectionCard from '@/components/SectionCard.vue';
import {
  createAdminAnnouncement,
  deleteAdminAnnouncement,
  getAdminAnnouncementDetail,
  getAdminAnnouncementPage,
  offlineAdminAnnouncement,
  publishAdminAnnouncement,
  updateAdminAnnouncement,
  type AdminAnnouncementDetail,
  type AdminAnnouncementItem,
  type AdminAnnouncementPayload,
} from '@/api/adminAnnouncement';
import { formatDate } from '@/utils/format';
import { getAnnouncementLevelTagType } from '@/utils/status';

type StatusFilterValue = '' | 'published' | 'unpublished';
type DialogMode = 'create' | 'edit';
type ActionType = 'publish' | 'offline' | 'delete';

interface AnnouncementFormModel {
  title: string;
  summary: string;
  content: string;
  level: string;
  top: boolean;
  published: boolean;
}

const statusOptions: Array<{ label: string; value: StatusFilterValue }> = [
  { label: '全部状态', value: '' },
  { label: '已发布', value: 'published' },
  { label: '未发布', value: 'unpublished' },
];

const levelOptions = ['通知', '活动', '提示'];
const pageSizeOptions = [10, 20, 50];

const createDefaultForm = (): AnnouncementFormModel => ({
  title: '',
  summary: '',
  content: '',
  level: '通知',
  top: false,
  published: false,
});

const tableData = ref<AdminAnnouncementItem[]>([]);
const loading = ref(false);
const errorMessage = ref('');
const filters = reactive<{ keyword: string; status: StatusFilterValue }>({
  keyword: '',
  status: '',
});
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
});

const detailVisible = ref(false);
const detailLoading = ref(false);
const detailErrorMessage = ref('');
const detailData = ref<AdminAnnouncementDetail | null>(null);
const currentDetailId = ref<number | null>(null);

const formVisible = ref(false);
const dialogLoading = ref(false);
const saveLoading = ref(false);
const dialogMode = ref<DialogMode>('create');
const editingId = ref<number | null>(null);
const formRef = ref<FormInstance>();
const formModel = reactive<AnnouncementFormModel>(createDefaultForm());

const currentActionKey = ref('');

const formRules: FormRules<AnnouncementFormModel> = {
  title: [
    { required: true, message: '请输入公告标题', trigger: 'blur' },
    { min: 1, max: 100, message: '标题长度需在 1 到 100 个字符之间', trigger: 'blur' },
  ],
  summary: [
    { max: 255, message: '摘要不能超过 255 个字符', trigger: 'blur' },
  ],
  level: [
    { required: true, message: '请选择或输入公告类型', trigger: 'change' },
    { min: 1, max: 30, message: '类型不能超过 30 个字符', trigger: 'change' },
  ],
  content: [
    { required: true, message: '请输入公告正文', trigger: 'blur' },
    { min: 1, max: 10000, message: '正文长度需在 1 到 10000 个字符之间', trigger: 'blur' },
  ],
};

const hasFilters = computed(() => Boolean(filters.keyword.trim()) || Boolean(filters.status));
const publishedCount = computed(() => tableData.value.filter((item) => item.published).length);
const unpublishedCount = computed(() => tableData.value.filter((item) => !item.published).length);
const topCount = computed(() => tableData.value.filter((item) => item.top).length);
const stats = computed(() => [
  { label: '筛选结果', value: String(pagination.total).padStart(2, '0'), tip: '来自后端真实分页总数' },
  { label: '当前页条数', value: String(tableData.value.length).padStart(2, '0'), tip: '本页已加载的公告数量' },
  { label: '本页已发布', value: String(publishedCount.value).padStart(2, '0'), tip: `未发布 ${String(unpublishedCount.value).padStart(2, '0')} 条` },
  { label: '本页置顶', value: String(topCount.value).padStart(2, '0'), tip: '便于快速核对重点公告' },
]);
const dialogTitle = computed(() => (dialogMode.value === 'create' ? '新增公告' : '编辑公告'));

const resolvePublishedParam = (status: StatusFilterValue) => {
  if (status === 'published') {
    return true;
  }
  if (status === 'unpublished') {
    return false;
  }
  return undefined;
};

const resetFormModel = () => {
  Object.assign(formModel, createDefaultForm());
};

const fillFormModel = (detail: AdminAnnouncementDetail) => {
  Object.assign(formModel, {
    title: detail.title,
    summary: detail.summary,
    content: detail.content,
    level: detail.level || '通知',
    top: detail.top,
    published: detail.published,
  });
};

const buildPayload = (): AdminAnnouncementPayload => ({
  title: formModel.title.trim(),
  summary: formModel.summary.trim(),
  content: formModel.content.trim(),
  level: (formModel.level || '通知').trim(),
  top: formModel.top,
  published: formModel.published,
});

const getPublishStatusMeta = (published: boolean) => (published
  ? { text: '已发布', type: 'success' as const }
  : { text: '未发布', type: 'info' as const });

const formatPublishTime = (value: string) => (value ? formatDate(value) : '暂无');
const getActionKey = (type: ActionType, id: number) => `${type}-${id}`;
const isActionLoading = (type: ActionType, id: number) => currentActionKey.value === getActionKey(type, id);

const loadAnnouncements = async () => {
  loading.value = true;
  errorMessage.value = '';

  try {
    const page = await getAdminAnnouncementPage({
      keyword: filters.keyword.trim() || undefined,
      published: resolvePublishedParam(filters.status),
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
    });

    tableData.value = page.records;
    pagination.total = page.total;
    pagination.pageNum = page.pageNum;
    pagination.pageSize = page.pageSize;
  } catch (error) {
    console.error('加载管理员公告列表失败', error);
    tableData.value = [];
    pagination.total = 0;
    errorMessage.value = '管理员公告接口暂时不可用，请确认已使用管理员账号登录后重试。';
  } finally {
    loading.value = false;
  }
};

const loadAnnouncementDetail = async (id: number) => {
  currentDetailId.value = id;
  detailLoading.value = true;
  detailErrorMessage.value = '';

  try {
    detailData.value = await getAdminAnnouncementDetail(id);
  } catch (error) {
    console.error('加载公告详情失败', error);
    detailData.value = null;
    detailErrorMessage.value = '公告详情加载失败';
  } finally {
    detailLoading.value = false;
  }
};

const retryLoadDetail = () => {
  if (currentDetailId.value !== null) {
    loadAnnouncementDetail(currentDetailId.value);
  }
};

const handleSearch = () => {
  pagination.pageNum = 1;
  loadAnnouncements();
};

const handleReset = () => {
  filters.keyword = '';
  filters.status = '';
  pagination.pageNum = 1;
  loadAnnouncements();
};

const handleCurrentChange = (pageNum: number) => {
  pagination.pageNum = pageNum;
  loadAnnouncements();
};

const handleSizeChange = (pageSize: number) => {
  pagination.pageSize = pageSize;
  pagination.pageNum = 1;
  loadAnnouncements();
};

const handleOpenDetail = async (item: AdminAnnouncementItem) => {
  detailVisible.value = true;
  detailData.value = null;
  await loadAnnouncementDetail(item.id);
};

const handleOpenCreate = async () => {
  dialogMode.value = 'create';
  editingId.value = null;
  dialogLoading.value = false;
  resetFormModel();
  formVisible.value = true;
  await nextTick();
  formRef.value?.clearValidate();
};

const handleOpenEdit = async (item: AdminAnnouncementItem) => {
  dialogMode.value = 'edit';
  editingId.value = item.id;
  resetFormModel();
  formVisible.value = true;
  dialogLoading.value = true;

  try {
    const detail = await getAdminAnnouncementDetail(item.id);
    fillFormModel(detail);
    await nextTick();
    formRef.value?.clearValidate();
  } catch (error) {
    console.error('加载公告编辑数据失败', error);
    formVisible.value = false;
    ElMessage.error('公告详情加载失败，暂时无法编辑。');
  } finally {
    dialogLoading.value = false;
  }
};

const syncDetailIfMatched = (detail: AdminAnnouncementDetail) => {
  if (currentDetailId.value === detail.id) {
    detailData.value = detail;
  }
};

const handleSubmitForm = async () => {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) {
    return;
  }

  saveLoading.value = true;

  try {
    const payload = buildPayload();
    const result = dialogMode.value === 'create'
      ? await createAdminAnnouncement(payload)
      : await updateAdminAnnouncement(editingId.value as number, payload);

    syncDetailIfMatched(result);
    ElMessage.success(dialogMode.value === 'create' ? '公告新增成功' : '公告更新成功');
    formVisible.value = false;
    await loadAnnouncements();
  } catch (error) {
    console.error('保存公告失败', error);
  } finally {
    saveLoading.value = false;
  }
};

const handlePublish = async (item: AdminAnnouncementItem) => {
  try {
    await ElMessageBox.confirm(`确认发布公告《${item.title}》吗？`, '发布确认', {
      type: 'warning',
      confirmButtonText: '确认发布',
      cancelButtonText: '取消',
    });
  } catch {
    return;
  }

  currentActionKey.value = getActionKey('publish', item.id);
  try {
    const detail = await publishAdminAnnouncement(item.id);
    syncDetailIfMatched(detail);
    ElMessage.success('公告发布成功');
    await loadAnnouncements();
  } catch (error) {
    console.error('发布公告失败', error);
  } finally {
    currentActionKey.value = '';
  }
};

const handleOffline = async (item: AdminAnnouncementItem) => {
  try {
    await ElMessageBox.confirm(`确认下线公告《${item.title}》吗？`, '下线确认', {
      type: 'warning',
      confirmButtonText: '确认下线',
      cancelButtonText: '取消',
    });
  } catch {
    return;
  }

  currentActionKey.value = getActionKey('offline', item.id);
  try {
    const detail = await offlineAdminAnnouncement(item.id);
    syncDetailIfMatched(detail);
    ElMessage.success('公告已下线');
    await loadAnnouncements();
  } catch (error) {
    console.error('下线公告失败', error);
  } finally {
    currentActionKey.value = '';
  }
};

const handleDelete = async (item: AdminAnnouncementItem) => {
  try {
    await ElMessageBox.confirm(`删除后不可恢复，确认删除公告《${item.title}》吗？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
    });
  } catch {
    return;
  }

  currentActionKey.value = getActionKey('delete', item.id);
  try {
    await deleteAdminAnnouncement(item.id);
    if (tableData.value.length === 1 && pagination.pageNum > 1) {
      pagination.pageNum -= 1;
    }
    if (currentDetailId.value === item.id) {
      detailVisible.value = false;
      detailData.value = null;
      currentDetailId.value = null;
    }
    ElMessage.success('公告删除成功');
    await loadAnnouncements();
  } catch (error) {
    console.error('删除公告失败', error);
  } finally {
    currentActionKey.value = '';
  }
};

onMounted(loadAnnouncements);
</script>

<style scoped lang="scss">
.stat-card { padding: 22px; }
.stat-card span { color: var(--qh-text-secondary); }
.stat-card strong { display: block; margin: 14px 0 10px; font-size: 30px; color: var(--qh-primary-deep); }
.stat-card p { margin: 0; color: var(--qh-text-secondary); }
.filter-form { display: grid; grid-template-columns: minmax(0, 1.8fr) 220px auto; gap: 16px; align-items: end; }
.filter-form :deep(.el-form-item) { margin-bottom: 0; }
.filter-form__item { min-width: 0; }
.filter-form__item :deep(.el-input),
.filter-form__item :deep(.el-select) { width: 100%; }
.filter-actions,
.card-actions,
.dialog-footer,
.table-actions { display: flex; gap: 12px; }
.filter-actions { justify-content: flex-end; }
.card-actions { align-items: center; }
.table-skeleton { margin-top: 18px; }
.state-panel { margin-top: 18px; padding: 12px; }
.table-wrapper { margin-top: 18px; }
.announcement-cell { display: grid; gap: 8px; }
.announcement-cell__header { display: flex; align-items: center; gap: 10px; }
.announcement-cell strong { color: var(--qh-text-primary); line-height: 1.5; }
.announcement-cell p {
  margin: 0;
  color: var(--qh-text-secondary);
  line-height: 1.7;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.table-actions { flex-wrap: wrap; gap: 0 4px; }
.pagination-bar { margin-top: 20px; display: flex; justify-content: flex-end; }
.empty-wrapper { margin-top: 18px; }
.detail-panel { min-height: 240px; }
.detail-head { display: grid; gap: 12px; }
.detail-head h2 { margin: 0; color: var(--qh-text-primary); line-height: 1.5; }
.detail-tags { display: flex; flex-wrap: wrap; gap: 8px; }
.detail-time { margin: 0; color: var(--qh-text-secondary); }
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
.form-panel { min-height: 220px; }
.announcement-form { margin-top: 4px; }
.form-grid { display: grid; grid-template-columns: minmax(0, 220px) minmax(0, 1fr); gap: 18px; }
.switch-group {
  height: 100%;
  padding: 14px 16px;
  border-radius: 18px;
  display: grid;
  gap: 14px;
}
.switch-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}
.switch-item strong { color: var(--qh-text-primary); }
.switch-item p { margin: 6px 0 0; color: var(--qh-text-secondary); line-height: 1.6; }
.dialog-footer { justify-content: flex-end; }

@media (max-width: 1200px) {
  .filter-form { grid-template-columns: minmax(0, 1fr) 220px; }
  .filter-actions { grid-column: 1 / -1; justify-content: flex-start; }
}

@media (max-width: 900px) {
  .form-grid { grid-template-columns: 1fr; }
}

@media (max-width: 768px) {
  .filter-form { grid-template-columns: 1fr; }
  .filter-actions,
  .card-actions,
  .pagination-bar { justify-content: flex-start; }
  .pagination-bar { overflow-x: auto; }
}
</style>
