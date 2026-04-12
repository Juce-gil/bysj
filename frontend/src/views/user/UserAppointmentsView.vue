<template>
  <div class="appointments-page">
    <section class="grid-cards grid-cards--4">
      <article v-for="item in stats" :key="item.label" class="stat-card qh-panel">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
        <p>{{ item.tip }}</p>
      </article>
    </section>

    <SectionCard title="我的预约" subtitle="已联调预约列表接口，并支持取消未完成预约。">
      <template #extra>
        <div class="toolbar">
          <el-select v-model="statusFilter" clearable placeholder="全部状态" class="toolbar-select">
            <el-option label="全部状态" value="" />
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
          <el-button text :loading="loading" @click="loadAppointments">刷新</el-button>
        </div>
      </template>

      <div class="appointments-summary qh-panel--subtle">
        <article>
          <span>当前筛选</span>
          <strong>{{ currentFilterSummary }}</strong>
          <p>切换状态可快速查看待确认、已完成或已取消记录。</p>
        </article>
        <article>
          <span>可取消预约</span>
          <strong>{{ cancellableCount }}</strong>
          <p>待确认或已同意状态支持用户主动取消。</p>
        </article>
        <article>
          <span>展示结果</span>
          <strong>{{ filteredAppointments.length }}</strong>
          <p>{{ appointments.length ? '当前列表已接入真实预约接口。' : '暂时还没有产生预约记录。' }}</p>
        </article>
      </div>

      <el-skeleton v-if="loading" :rows="8" animated />
      <div v-else-if="errorMessage" class="state-panel qh-panel--subtle">
        <el-result icon="warning" title="预约列表加载失败" :sub-title="errorMessage">
          <template #extra>
            <el-button type="primary" @click="loadAppointments">重新加载</el-button>
          </template>
        </el-result>
      </div>
      <div v-else-if="filteredAppointments.length" class="appointment-list">
        <article v-for="item in filteredAppointments" :key="item.id" class="appointment-card qh-panel--subtle">
          <div class="appointment-card__head">
            <div>
              <h3>{{ item.goodsTitle }}</h3>
              <p>{{ resolveRoleText(item) }} · 卖家：{{ item.sellerName }} · 买家：{{ item.buyerName }}</p>
            </div>
            <el-tag effect="light" :type="getAppointmentStatusMeta(item.status).type">{{ getAppointmentStatusMeta(item.status).text }}</el-tag>
          </div>

          <div class="appointment-grid">
            <div>
              <span>预约时间</span>
              <strong>{{ item.intendedTime }}</strong>
            </div>
            <div>
              <span>面交地点</span>
              <strong>{{ item.intendedLocation || '待协商' }}</strong>
            </div>
            <div>
              <span>备注信息</span>
              <strong>{{ item.remark || '暂无备注' }}</strong>
            </div>
            <div>
              <span>关联商品</span>
              <RouterLink :to="`/goods/${item.goodsId}`">查看商品详情</RouterLink>
            </div>
          </div>

          <div class="appointment-card__actions">
            <el-button
              v-if="canCancel(item.status)"
              type="danger"
              plain
              :loading="isCancelling(item.id)"
              @click="handleCancel(item)"
            >
              取消预约
            </el-button>
            <el-button v-else text disabled>当前状态不可取消</el-button>
          </div>
        </article>
      </div>
      <EmptyHint
        v-else
        :title="appointments.length ? '没有符合条件的预约' : '你还没有预约记录'"
        :description="appointments.length ? '可以切换状态筛选查看其他预约记录。' : '去商品详情页发起一次预约后，这里会展示时间、地点与处理状态。'"
      />
    </SectionCard>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { RouterLink } from 'vue-router';
import SectionCard from '@/components/SectionCard.vue';
import EmptyHint from '@/components/EmptyHint.vue';
import { cancelAppointment, getMyAppointments, type AppointmentItem } from '@/api/marketplace';
import { useUserStore } from '@/stores/user';
import { getAppointmentStatusMeta } from '@/utils/status';

const userStore = useUserStore();
const appointments = ref<AppointmentItem[]>([]);
const loading = ref(false);
const errorMessage = ref('');
const statusFilter = ref('');
const cancellingIds = ref<number[]>([]);

const stats = computed(() => {
  const total = appointments.value.length;
  const pending = appointments.value.filter((item) => item.status === 'pending').length;
  const completed = appointments.value.filter((item) => item.status === 'completed').length;
  const cancelled = appointments.value.filter((item) => item.status === 'cancelled').length;
  return [
    { label: '预约总数', value: String(total).padStart(2, '0'), tip: '已接入我的预约接口' },
    { label: '待确认', value: String(pending).padStart(2, '0'), tip: '等待对方确认时间与地点' },
    { label: '已完成', value: String(completed).padStart(2, '0'), tip: '可作为历史成交记录展示' },
    { label: '已取消', value: String(cancelled).padStart(2, '0'), tip: '会保留取消后的记录状态' },
  ];
});

const statusOptions = computed(() =>
  [...new Set(appointments.value.map((item) => item.status))].map((value) => ({
    value,
    label: getAppointmentStatusMeta(value).text,
  })),
);
const filteredAppointments = computed(() => appointments.value.filter((item) => !statusFilter.value || item.status === statusFilter.value));
const canCancel = (status: string) => status === 'pending' || status === 'agreed';
const isCancelling = (id: number) => cancellingIds.value.includes(id);
const cancellableCount = computed(() => appointments.value.filter((item) => canCancel(item.status)).length);
const currentFilterSummary = computed(() => (statusFilter.value ? getAppointmentStatusMeta(statusFilter.value).text : '显示全部预约'));

const resolveRoleText = (item: AppointmentItem) => (item.buyerId === userStore.profile?.id ? '我是买家' : '我是卖家');

const loadAppointments = async () => {
  loading.value = true;
  errorMessage.value = '';
  try {
    appointments.value = await getMyAppointments();
  } catch (error) {
    console.error('加载预约列表失败', error);
    errorMessage.value = '预约列表接口暂时不可用，请稍后重试。';
  } finally {
    loading.value = false;
  }
};

const handleCancel = async (item: AppointmentItem) => {
  try {
    await ElMessageBox.confirm(`确认取消《${item.goodsTitle}》的预约吗？`, '取消预约确认', {
      type: 'warning',
      confirmButtonText: '确认取消',
      cancelButtonText: '再想想',
    });
  } catch {
    return;
  }

  cancellingIds.value = [...cancellingIds.value, item.id];
  try {
    const result = await cancelAppointment(item.id);
    const target = appointments.value.find((appointment) => appointment.id === item.id);
    if (target) {
      Object.assign(target, result);
    }
    ElMessage.success('预约已取消');
  } finally {
    cancellingIds.value = cancellingIds.value.filter((id) => id !== item.id);
  }
};

onMounted(loadAppointments);
</script>

<style scoped lang="scss">
.stat-card { padding: 22px; }
.stat-card span { color: var(--qh-text-secondary); }
.stat-card strong { display: block; margin: 14px 0 10px; font-size: 30px; color: var(--qh-primary-deep); }
.stat-card p { margin: 0; color: var(--qh-text-secondary); }

.toolbar {
  display: flex;
  gap: 12px;
  align-items: center;
}

.toolbar-select {
  width: 180px;
}

.appointments-summary {
  margin-bottom: 18px;
  padding: 16px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.appointments-summary article {
  padding: 16px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.78);
}

.appointments-summary span {
  display: block;
  color: var(--qh-text-secondary);
}

.appointments-summary strong {
  display: block;
  margin-top: 10px;
  color: var(--qh-text-primary);
  font-size: 22px;
}

.appointments-summary p {
  margin: 10px 0 0;
  color: var(--qh-text-secondary);
  line-height: 1.7;
}

.appointment-list {
  display: grid;
  gap: 18px;
}

.appointment-card {
  padding: 20px;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.appointment-card:hover {
  transform: translateY(-2px);
}

.appointment-card__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.appointment-card__head h3 {
  margin: 0 0 8px;
  color: var(--qh-text-primary);
}

.appointment-card__head p {
  margin: 0;
  color: var(--qh-text-secondary);
  line-height: 1.7;
}

.appointment-grid {
  margin-top: 18px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.appointment-grid div {
  padding: 14px 16px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.7);
}

.appointment-grid span {
  display: block;
  font-size: 12px;
  color: var(--qh-text-secondary);
  margin-bottom: 6px;
}

.appointment-grid strong,
.appointment-grid a {
  color: var(--qh-text-primary);
  font-weight: 600;
  text-decoration: none;
}

.appointment-card__actions {
  margin-top: 18px;
  display: flex;
  justify-content: flex-end;
}

.state-panel {
  padding: 12px;
}

@media (max-width: 1100px) {
  .appointments-summary {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 960px) {
  .toolbar {
    width: 100%;
    flex-wrap: wrap;
    justify-content: flex-end;
  }

  .toolbar-select,
  .appointment-grid {
    width: 100%;
    grid-template-columns: 1fr;
  }

  .appointment-card__head {
    flex-direction: column;
  }
}
</style>
