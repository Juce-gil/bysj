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
      <SectionCard title="查询筛选" subtitle="已接入真实管理员用户分页接口，支持关键词、角色、状态与分页联调。">
        <el-form :inline="true" class="filter-form" @submit.prevent>
          <el-form-item label="关键词" class="filter-form__item filter-form__item--keyword">
            <el-input
              v-model="filters.keyword"
              clearable
              placeholder="搜索用户名、昵称、学号或手机号"
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
      <SectionCard title="用户列表" subtitle="支持管理员查看用户资料、注册时间，并执行禁用或恢复操作。">
        <template #extra>
          <el-button text :loading="loading" @click="loadUsers">刷新</el-button>
        </template>

        <el-alert
          type="info"
          show-icon
          :closable="false"
          title="当前页面已接入 GET /api/admin/users 与 PUT /api/admin/users/{id}/status；为避免误操作，管理员账号不显示禁用按钮。"
        />

        <el-skeleton v-if="loading && !tableData.length && !errorMessage" :rows="8" animated class="table-skeleton" />
        <div v-else-if="errorMessage" class="state-panel qh-panel--subtle">
          <el-result icon="warning" title="用户列表加载失败" :sub-title="errorMessage">
            <template #extra>
              <el-button type="primary" @click="loadUsers">重新加载</el-button>
            </template>
          </el-result>
        </div>
        <template v-else-if="tableData.length">
          <div v-loading="loading" class="table-wrapper">
            <el-table :data="tableData" row-key="id" stripe class="user-table">
              <el-table-column label="用户信息" min-width="260">
                <template #default="scope">
                  <div class="user-cell">
                    <strong>{{ scope.row.displayName }}</strong>
                    <span>@{{ scope.row.username }}</span>
                    <p>{{ scope.row.school || '科成' }}</p>
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
            :description="hasFilters ? '当前筛选条件下没有匹配用户，请调整搜索条件后重试。' : '当前还没有可展示的用户数据。'"
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
  { label: '正常', value: 'active' },
  { label: '已禁用', value: 'disabled' },
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
  { label: '筛选结果', value: String(pagination.total).padStart(2, '0'), tip: '来自后台真实分页总数' },
  { label: '当前页条数', value: String(tableData.value.length).padStart(2, '0'), tip: '本页已加载的用户数量' },
  { label: '本页管理员', value: String(adminCount.value).padStart(2, '0'), tip: '管理员账号默认不提供禁用按钮' },
  { label: '正常用户', value: String(activeCount.value).padStart(2, '0'), tip: `当前页已禁用 ${String(disabledCount.value).padStart(2, '0')} 人` },
]);

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
  ? { text: '已禁用', type: 'danger' as const }
  : { text: '正常', type: 'success' as const });

const displayValue = (value: string) => value || '—';
const formatRegisterTime = (value: string) => value ? formatDate(value) : '暂无';

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
    console.error('加载管理员用户列表失败', error);
    tableData.value = [];
    pagination.total = 0;
    errorMessage.value = '管理员用户接口暂时不可用，请确认已使用管理员账号登录并稍后重试。';
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

  await ElMessageBox.confirm(
    `确认要${actionText}用户“${item.displayName}”吗？`,
    `${actionText}确认`,
    {
      type: 'warning',
      confirmButtonText: '确认',
      cancelButtonText: '取消',
    },
  );

  pendingUserIds.value = [...pendingUserIds.value, item.id];
  try {
    await updateAdminUserStatus(item.id, nextDisabled);
    ElMessage.success(`已${actionText}用户`);
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
.stat-card { padding: 22px; }
.stat-card span { color: var(--qh-text-secondary); }
.stat-card strong { display: block; margin: 14px 0 10px; font-size: 30px; color: var(--qh-primary-deep); }
.stat-card p { margin: 0; color: var(--qh-text-secondary); }
.filter-form { display: grid; grid-template-columns: minmax(0, 1.8fr) 180px 180px auto; gap: 16px; align-items: end; }
.filter-form :deep(.el-form-item) { margin-bottom: 0; }
.filter-form__item { min-width: 0; }
.filter-form__item :deep(.el-input),
.filter-form__item :deep(.el-select) { width: 100%; }
.filter-actions { display: flex; justify-content: flex-end; gap: 12px; }
.table-skeleton { margin-top: 18px; }
.state-panel { margin-top: 18px; padding: 12px; }
.table-wrapper { margin-top: 18px; }
.user-cell { display: grid; gap: 6px; }
.user-cell strong { color: var(--qh-text-primary); line-height: 1.5; }
.user-cell span { color: var(--qh-primary-deep); font-size: 13px; }
.user-cell p { margin: 0; color: var(--qh-text-secondary); line-height: 1.6; }
.pagination-bar { margin-top: 20px; display: flex; justify-content: flex-end; }
.empty-wrapper { margin-top: 18px; }

@media (max-width: 1200px) {
  .filter-form { grid-template-columns: minmax(0, 1fr) 180px 180px; }
  .filter-actions { grid-column: 1 / -1; justify-content: flex-start; }
}

@media (max-width: 768px) {
  .filter-form { grid-template-columns: 1fr; }
  .filter-actions { justify-content: flex-start; }
  .pagination-bar { justify-content: flex-start; overflow-x: auto; }
}
</style>