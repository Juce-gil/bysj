<template>
  <div class="admin-user-page">
    <section class="grid-cards grid-cards--4">
      <article v-for="item in stats" :key="item.label" class="stat-card qh-panel">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
        <p>{{ item.tip }}</p>
      </article>
    </section>

    <section class="page-section">
      <SectionCard title="筛选用户" subtitle="按关键词、角色与状态快速定位平台账号">
        <div class="filter-overview qh-panel--subtle">
          <article>
            <span>用户总数</span>
            <strong>{{ String(pagination.total).padStart(2, '0') }}</strong>
            <p>当前筛选范围内的账号量</p>
          </article>
          <article>
            <span>筛选条件</span>
            <strong>{{ currentFilterSummary }}</strong>
            <p>支持关键词、角色与状态组合检索</p>
          </article>
          <article>
            <span>账号状态</span>
            <strong>启用 {{ activeCount }}</strong>
            <p>禁用 {{ disabledCount }} · 管理员 {{ adminCount }}</p>
          </article>
        </div>

        <el-form :inline="true" class="filter-form" @submit.prevent>
          <el-form-item label="关键词" class="filter-form__item filter-form__item--keyword">
            <el-input
              v-model="filters.keyword"
              clearable
              placeholder="搜索昵称、账号或学号"
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="角色" class="filter-form__item filter-form__item--select">
            <el-select v-model="filters.role" placeholder="全部角色">
              <el-option v-for="item in roleOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态" class="filter-form__item filter-form__item--select">
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
      <SectionCard title="用户列表" subtitle="支持分页查看用户信息并执行启停用操作">
        <template #extra>
          <el-button text :loading="loading" @click="loadUsers">刷新</el-button>
        </template>

        <div class="table-headline qh-panel--subtle">
          <div>
            <strong>账号列表</strong>
            <p>{{ listSummary }}</p>
          </div>
          <div class="table-headline__chips">
            <span class="info-chip">当前 {{ tableData.length }} 条</span>
            <span class="info-chip">每页 {{ pagination.pageSize }} 条</span>
            <span v-if="hasFilters" class="info-chip info-chip--dark">已启用筛选</span>
          </div>
        </div>

        <el-alert
          type="info"
          show-icon
          :closable="false"
          title="接口已联调：GET /api/admin/users 与 PUT /api/admin/users/{id}/status，可直接完成用户状态管理。"
        />

        <el-skeleton v-if="loading && !tableData.length && !errorMessage" :rows="8" animated class="table-skeleton" />
        <div v-else-if="errorMessage" class="state-panel qh-panel--subtle">
          <el-result icon="warning" title="加载失败" :sub-title="errorMessage">
            <template #extra>
              <el-button type="primary" @click="loadUsers">重新获取</el-button>
            </template>
          </el-result>
        </div>
        <template v-else-if="tableData.length">
          <div v-loading="loading" class="table-wrapper">
            <el-table :data="tableData" row-key="id" stripe class="user-table">
              <el-table-column label="用户信息" min-width="300">
                <template #default="scope">
                  <div class="user-cell">
                    <div class="user-avatar">{{ getDisplayInitial(scope.row.displayName) }}</div>
                    <div class="user-cell__content">
                      <strong>{{ scope.row.displayName }}</strong>
                      <span>@{{ scope.row.username }}</span>
                      <p>{{ scope.row.school || '未知' }}</p>
                    </div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="studentNo" label="学号" min-width="130">
                <template #default="scope">{{ displayValue(scope.row.studentNo) }}</template>
              </el-table-column>
              <el-table-column prop="phone" label="手机号" min-width="140">
                <template #default="scope">{{ displayValue(scope.row.phone) }}</template>
              </el-table-column>
              <el-table-column label="角色" min-width="110">
                <template #default="scope">
                  <el-tag effect="plain" :type="getRoleMeta(scope.row.role).type">{{ getRoleMeta(scope.row.role).text }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="状态" min-width="110">
                <template #default="scope">
                  <el-tag effect="light" :type="getStatusMeta(scope.row.disabled).type">{{ getStatusMeta(scope.row.disabled).text }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="注册时间" min-width="170">
                <template #default="scope">{{ formatRegisterTime(scope.row.registerTime) }}</template>
              </el-table-column>
              <el-table-column label="操作" min-width="150" fixed="right">
                <template #default="scope">
                  <template v-if="isAdminRole(scope.row.role)">
                    <el-tag effect="plain" type="warning">管理员账号</el-tag>
                  </template>
                  <template v-else>
                    <el-button
                      link
                      :type="scope.row.disabled ? 'success' : 'danger'"
                      :loading="pendingUserIds.includes(scope.row.id)"
                      @click="handleToggleStatus(scope.row)"
                    >
                      {{ scope.row.disabled ? '恢复' : '禁用' }}
                    </el-button>
                  </template>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <div class="pagination-bar">
            <el-pagination
              background
              layout="total, sizes, prev, pager, next"
              :total="pagination.total"
              :page-size="pagination.pageSize"
              :current-page="pagination.pageNum"
              :page-sizes="pageSizeOptions"
              @current-change="handleCurrentChange"
              @size-change="handleSizeChange"
            />
          </div>
        </template>
        <div v-else class="empty-wrapper">
          <EmptyHint
            title="暂无用户数据"
            :description="hasFilters ? '未找到符合当前筛选条件的用户' : '当前还没有可展示的用户数据'"
          />
        </div>
      </SectionCard>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import SectionCard from '@/components/SectionCard.vue';
import EmptyHint from '@/components/EmptyHint.vue';
import { getAdminUserPage, updateAdminUserStatus, type AdminUserItem } from '@/api/adminUser';
import { formatDate } from '@/utils/format';

const roleOptions = [
  { label: '全部角色', value: '' },
  { label: '管理员', value: 'admin' },
  { label: '普通用户', value: 'user' },
];

type StatusFilterValue = '' | 'active' | 'disabled';

const statusOptions: Array<{ label: string; value: StatusFilterValue }> = [
  { label: '全部状态', value: '' },
  { label: '启用', value: 'active' },
  { label: '禁用', value: 'disabled' },
];

const pageSizeOptions = [10, 20, 50];
const tableData = ref<AdminUserItem[]>([]);
const loading = ref(false);
const errorMessage = ref('');
const pendingUserIds = ref<number[]>([]);
const filters = reactive<{ keyword: string; role: string; status: StatusFilterValue }>({
  keyword: '',
  role: '',
  status: '',
});
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
});

const hasFilters = computed(() => Boolean(filters.keyword.trim()) || Boolean(filters.role) || Boolean(filters.status));
const adminCount = computed(() => tableData.value.filter((item) => isAdminRole(item.role)).length);
const disabledCount = computed(() => tableData.value.filter((item) => item.disabled).length);
const activeCount = computed(() => tableData.value.filter((item) => !item.disabled).length);
const stats = computed(() => [
  { label: '用户总数', value: String(pagination.total).padStart(2, '0'), tip: '当前筛选后的总用户数' },
  { label: '本页数量', value: String(tableData.value.length).padStart(2, '0'), tip: '当前页展示的账号数量' },
  { label: '管理员', value: String(adminCount.value).padStart(2, '0'), tip: '当前页中的管理员账号数' },
  { label: '启用中', value: String(activeCount.value).padStart(2, '0'), tip: `禁用用户 ${String(disabledCount.value).padStart(2, '0')} 人` },
]);

const currentFilterSummary = computed(() => {
  const parts: string[] = [];
  if (filters.keyword.trim()) parts.push(`关键词“${filters.keyword.trim()}”`);
  if (filters.role) parts.push(`角色 ${filters.role === 'admin' ? '管理员' : '普通用户'}`);
  if (filters.status) parts.push(`状态 ${filters.status === 'active' ? '启用' : '禁用'}`);
  return parts.join(' · ') || '全部用户';
});

const listSummary = computed(() => (hasFilters.value
  ? `当前按 ${currentFilterSummary.value} 共筛选到 ${pagination.total} 位用户`
  : `当前共有 ${pagination.total} 位用户，支持按关键词、角色和状态快速检索`));

const resolveDisabledParam = (status: StatusFilterValue) => {
  if (status === 'disabled') {
    return true;
  }
  if (status === 'active') {
    return false;
  }
  return undefined;
};

const isAdminRole = (role: string) => role.trim().toLowerCase() === 'admin';

const getRoleMeta = (role: string) => {
  if (isAdminRole(role)) {
    return { text: '管理员', type: 'danger' as const };
  }
  return { text: '普通用户', type: 'info' as const };
};

const getStatusMeta = (disabled: boolean) => (disabled
  ? { text: '禁用', type: 'danger' as const }
  : { text: '启用', type: 'success' as const });

const displayValue = (value: string) => value || '-';
const formatRegisterTime = (value: string) => (value ? formatDate(value) : '未知');
const getDisplayInitial = (value: string) => (value?.trim()?.charAt(0) || '?').toUpperCase();

const loadUsers = async () => {
  loading.value = true;
  errorMessage.value = '';

  try {
    const page = await getAdminUserPage({
      keyword: filters.keyword.trim() || undefined,
      role: filters.role || undefined,
      disabled: resolveDisabledParam(filters.status),
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
    });

    tableData.value = page.records;
    pagination.total = page.total;
    pagination.pageNum = page.pageNum;
    pagination.pageSize = page.pageSize;
  } catch (error) {
    console.error('管理员用户列表加载失败', error);
    tableData.value = [];
    pagination.total = 0;
    errorMessage.value = '用户列表加载失败，请稍后重试。';
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  pagination.pageNum = 1;
  loadUsers();
};

const handleReset = () => {
  filters.keyword = '';
  filters.role = '';
  filters.status = '';
  pagination.pageNum = 1;
  loadUsers();
};

const handleCurrentChange = (pageNum: number) => {
  pagination.pageNum = pageNum;
  loadUsers();
};

const handleSizeChange = (pageSize: number) => {
  pagination.pageSize = pageSize;
  pagination.pageNum = 1;
  loadUsers();
};

const handleToggleStatus = async (item: AdminUserItem) => {
  const nextDisabled = !item.disabled;
  const actionText = nextDisabled ? '禁用' : '恢复';

  try {
    await ElMessageBox.confirm(
      `确认${actionText}用户“${item.displayName}”吗？`,
      `${actionText}用户`,
      {
        type: 'warning',
        confirmButtonText: '确认',
        cancelButtonText: '取消',
      },
    );
  } catch {
    return;
  }

  pendingUserIds.value = [...pendingUserIds.value, item.id];
  try {
    await updateAdminUserStatus(item.id, nextDisabled);
    ElMessage.success(`${actionText}成功`);
    await loadUsers();
  } catch (error) {
    console.error(`${actionText}用户失败`, error);
  } finally {
    pendingUserIds.value = pendingUserIds.value.filter((id) => id !== item.id);
  }
};

onMounted(loadUsers);
</script>

<style scoped lang="scss">
.stat-card {
  padding: 24px;
  display: grid;
  align-content: end;
  gap: 8px;
}

.stat-card span {
  color: var(--qh-text-secondary);
  font-size: 13px;
  letter-spacing: 0.04em;
}

.stat-card strong {
  display: block;
  margin: 6px 0 2px;
  font-size: 34px;
  line-height: 1;
  color: var(--qh-primary-deep);
}

.stat-card p {
  margin: 0;
  color: var(--qh-text-secondary);
  line-height: 1.75;
}

.filter-overview {
  margin-bottom: 20px;
  padding: 18px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.filter-overview article {
  padding: 16px 18px;
  min-height: 108px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.78);
}

.filter-overview span {
  display: block;
  color: var(--qh-text-secondary);
}

.filter-overview strong {
  display: block;
  margin-top: 10px;
  font-size: 21px;
  color: var(--qh-text-primary);
  line-height: 1.5;
}

.filter-overview p {
  margin: 8px 0 0;
  line-height: 1.7;
  color: var(--qh-text-secondary);
}

.filter-form {
  display: grid;
  grid-template-columns: minmax(0, 1.8fr) 180px 180px auto;
  gap: 16px;
  align-items: end;
}

.filter-form :deep(.el-form-item) {
  margin-bottom: 0;
}

.filter-form__item {
  min-width: 0;
}

.filter-form__item :deep(.el-input),
.filter-form__item :deep(.el-select) {
  width: 100%;
}

.filter-actions,
.table-headline__chips {
  display: flex;
  gap: 12px;
}

.filter-actions {
  justify-content: flex-end;
  align-self: stretch;
}

.table-headline {
  margin-bottom: 18px;
  padding: 20px 22px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
}

.table-headline strong {
  color: var(--qh-text-primary);
}

.table-headline p {
  margin: 8px 0 0;
  color: var(--qh-text-secondary);
  line-height: 1.8;
}

.info-chip {
  display: inline-flex;
  align-items: center;
  min-height: 38px;
  padding: 0 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.84);
  color: var(--qh-text-primary);
  font-weight: 600;
}

.info-chip--dark {
  background: rgba(32, 39, 51, 0.94);
  color: #ffe27a;
}

.table-skeleton {
  margin-top: 18px;
}

.state-panel {
  margin-top: 18px;
  padding: 12px;
}

.table-wrapper {
  margin-top: 18px;
  overflow: hidden;
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-avatar {
  width: 50px;
  height: 50px;
  border-radius: 18px;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, rgba(255, 221, 119, 0.95), rgba(255, 248, 209, 0.95));
  color: var(--qh-text-primary);
  font-weight: 800;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.5);
}

.user-cell__content {
  display: grid;
  gap: 6px;
}

.user-cell strong {
  color: var(--qh-text-primary);
  line-height: 1.5;
  font-size: 15px;
}

.user-cell span {
  color: var(--qh-primary-deep);
  font-size: 13px;
}

.user-cell p {
  margin: 0;
  color: var(--qh-text-secondary);
  line-height: 1.6;
}

.pagination-bar {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.empty-wrapper {
  margin-top: 18px;
}

@media (max-width: 1200px) {
  .filter-overview {
    grid-template-columns: 1fr;
  }

  .filter-form {
    grid-template-columns: minmax(0, 1fr) 180px 180px;
  }

  .filter-actions {
    grid-column: 1 / -1;
    justify-content: flex-start;
  }

  .table-headline {
    flex-direction: column;
    align-items: flex-start;
  }
}

@media (max-width: 768px) {
  .filter-form {
    grid-template-columns: 1fr;
  }

  .filter-actions {
    justify-content: flex-start;
  }

  .pagination-bar {
    justify-content: flex-start;
    overflow-x: auto;
  }

  .table-headline__chips {
    flex-wrap: wrap;
  }
}
</style>
