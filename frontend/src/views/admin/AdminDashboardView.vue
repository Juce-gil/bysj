<template>
  <div class="admin-dashboard">
    <section class="grid-cards grid-cards--4">
      <article v-for="item in stats" :key="item.label" class="stat-card qh-panel">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
        <p>{{ item.tip }}</p>
      </article>
    </section>

    <el-alert
      class="page-section"
      type="warning"
      show-icon
      :closable="false"
      title="控制台数据来自真实接口联调，若显示异常请检查后端服务、权限与登录状态。"
    />

    <section v-if="errorMessage" class="page-section qh-panel state-panel">
      <el-result icon="warning" title="控制台加载失败" :sub-title="errorMessage">
        <template #extra>
          <el-button type="primary" @click="loadDashboard">重新加载</el-button>
        </template>
      </el-result>
    </section>

    <section class="page-section">
      <div class="operations-grid">
        <RouterLink v-for="item in shortcuts" :key="item.title" :to="item.to" class="operation-card qh-panel">
          <span class="operation-card__badge">{{ item.badge }}</span>
          <strong>{{ item.title }}</strong>
          <p>{{ item.description }}</p>
        </RouterLink>
      </div>
    </section>

    <section class="page-section chart-grid">
      <SectionCard title="运营趋势图表" subtitle="用图表形式查看商品、求购与公告的结构分布">
        <template #extra>
          <span class="panel-tag">ECharts</span>
        </template>
        <div v-if="loading" class="chart-skeletons">
          <el-skeleton v-for="item in 2" :key="item" :rows="8" animated />
        </div>
        <div v-else class="chart-card-grid">
          <article class="chart-card qh-panel--subtle">
            <header class="chart-card__header">
              <div>
                <strong>商品状态分布</strong>
                <p>观察当前商品池在不同交易阶段的数量变化</p>
              </div>
              <span class="chart-card__badge">BAR</span>
            </header>
            <div ref="goodsStatusChartRef" class="chart-card__canvas"></div>
          </article>

          <article class="chart-card qh-panel--subtle">
            <header class="chart-card__header">
              <div>
                <strong>平台模块占比</strong>
                <p>汇总商品、求购与公告的整体数据规模</p>
              </div>
              <span class="chart-card__badge">PIE</span>
            </header>
            <div ref="moduleOverviewChartRef" class="chart-card__canvas"></div>
          </article>
        </div>
      </SectionCard>

      <SectionCard title="公告结构图表" subtitle="帮助管理员快速判断当前公告内容类型分布">
        <template #extra>
          <span class="panel-tag panel-tag--dark">Dashboard</span>
        </template>
        <div v-if="loading" class="chart-skeletons chart-skeletons--single">
          <el-skeleton :rows="8" animated />
        </div>
        <div v-else class="single-chart-card qh-panel--subtle">
          <header class="chart-card__header">
            <div>
              <strong>公告级别分布</strong>
              <p>统计通知、活动、提示三类公告的当前数量</p>
            </div>
            <span class="chart-card__badge">DONUT</span>
          </header>
          <div ref="announcementLevelChartRef" class="chart-card__canvas chart-card__canvas--large"></div>
        </div>
      </SectionCard>
    </section>

    <section class="page-section grid-layout">
      <SectionCard title="当前会话" subtitle="展示管理员身份信息与当前账号概览">
        <template #extra>
          <el-button text :loading="loading" @click="loadDashboard">刷新</el-button>
        </template>
        <el-skeleton v-if="loading" :rows="6" animated />
        <template v-else>
          <div class="session-banner qh-panel--subtle">
            <div>
              <span class="session-banner__label">当前账号</span>
              <strong>{{ profile?.name || '未命名' }}</strong>
            </div>
            <p>你可以从这里快速确认管理员身份、学校信息与联系方式。</p>
          </div>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="昵称">{{ profile?.name || '未命名' }}</el-descriptions-item>
            <el-descriptions-item label="角色">{{ profile?.role === 'admin' ? '管理员' : '普通用户' }}</el-descriptions-item>
            <el-descriptions-item label="学校">{{ profile?.school || '未知' }}</el-descriptions-item>
            <el-descriptions-item label="电话">{{ profile?.phone || '未填写' }}</el-descriptions-item>
            <el-descriptions-item label="QQ">{{ profile?.qq || '未填写' }}</el-descriptions-item>
            <el-descriptions-item label="签名">{{ profile?.slogan || '这个人很懒，还没有留下签名' }}</el-descriptions-item>
          </el-descriptions>
        </template>
      </SectionCard>

      <SectionCard title="运营概览" subtitle="按接口聚合商品、求购与公告的实时数据">
        <div v-if="loading" class="todo-board">
          <el-skeleton v-for="item in 3" :key="item" :rows="2" animated />
        </div>
        <div v-else class="todo-board">
          <div class="todo-item qh-panel--subtle">
            <strong>商品数据</strong>
            <p>在售 {{ goodsOnSale }} 件，已预约 {{ goodsReserved }} 件，已售出 {{ goodsSold }} 件</p>
            <span>接口：/api/goods</span>
          </div>
          <div class="todo-item qh-panel--subtle">
            <strong>求购数据</strong>
            <p>求购中 {{ wantedBuying }} 条，已关闭 {{ wantedClosed }} 条</p>
            <span>接口：/api/wanted</span>
          </div>
          <div class="todo-item qh-panel--subtle">
            <strong>公告动态</strong>
            <p>共计 {{ announcements.length }} 条，最近更新《{{ latestAnnouncementTitle }}》</p>
            <span>接口：/api/announcements</span>
          </div>
        </div>
      </SectionCard>
    </section>

    <section class="page-section">
      <SectionCard title="公告速览" subtitle="快速查看最近公告与重点数据">
        <div class="announcement-summary qh-panel--subtle">
          <article>
            <span>最新公告</span>
            <strong>{{ latestAnnouncementTitle }}</strong>
          </article>
          <article>
            <span>活跃求购</span>
            <strong>{{ wantedBuying }}</strong>
          </article>
          <article>
            <span>在售商品</span>
            <strong>{{ goodsOnSale }}</strong>
          </article>
        </div>

        <el-table v-if="!loading" :data="announcements.slice(0, 5)" stripe>
          <el-table-column prop="title" label="标题" min-width="220" />
          <el-table-column prop="level" label="级别" min-width="100">
            <template #default="scope">
              <el-tag effect="plain" :type="getAnnouncementLevelTagType(scope.row.level)">{{ scope.row.level }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="publishedAt" label="发布时间" min-width="180" />
        </el-table>
        <el-skeleton v-else :rows="6" animated />
      </SectionCard>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { RouterLink } from 'vue-router';
import * as echarts from 'echarts/core';
import type { EChartsType } from 'echarts/core';
import { BarChart, PieChart } from 'echarts/charts';
import { CanvasRenderer } from 'echarts/renderers';
import { GridComponent, LegendComponent, TooltipComponent } from 'echarts/components';
import SectionCard from '@/components/SectionCard.vue';
import { getAnnouncementList, getGoodsList, getWantedList, type AnnouncementItem, type GoodsItem, type WantedItem } from '@/api/marketplace';
import { fetchCurrentUser } from '@/api/auth';
import type { StoredProfile } from '@/utils/storage';
import { getAnnouncementLevelTagType } from '@/utils/status';

echarts.use([BarChart, PieChart, GridComponent, LegendComponent, TooltipComponent, CanvasRenderer]);

const loading = ref(false);
const errorMessage = ref('');
const profile = ref<StoredProfile | null>(null);
const goods = ref<GoodsItem[]>([]);
const wanted = ref<WantedItem[]>([]);
const announcements = ref<AnnouncementItem[]>([]);

const goodsStatusChartRef = ref<HTMLDivElement | null>(null);
const moduleOverviewChartRef = ref<HTMLDivElement | null>(null);
const announcementLevelChartRef = ref<HTMLDivElement | null>(null);

let goodsStatusChart: EChartsType | null = null;
let moduleOverviewChart: EChartsType | null = null;
let announcementLevelChart: EChartsType | null = null;

const goodsOnSale = computed(() => goods.value.filter((item) => item.status === 'on_sale').length);
const goodsReserved = computed(() => goods.value.filter((item) => item.status === 'reserved').length);
const goodsSold = computed(() => goods.value.filter((item) => item.status === 'sold').length);
const goodsOffShelf = computed(() => goods.value.filter((item) => item.status === 'off_shelf').length);
const wantedBuying = computed(() => wanted.value.filter((item) => item.status === 'buying').length);
const wantedClosed = computed(() => wanted.value.filter((item) => item.status === 'closed').length);
const latestAnnouncementTitle = computed(() => announcements.value[0]?.title || '暂无公告');

const announcementLevelStats = computed(() => {
  const counts = {
    通知: 0,
    活动: 0,
    提示: 0,
  };
  announcements.value.forEach((item) => {
    if (item.level in counts) {
      counts[item.level as keyof typeof counts] += 1;
    }
  });
  return counts;
});

const stats = computed(() => [
  { label: '商品总数', value: String(goods.value.length).padStart(2, '0'), tip: '当前已接入的商品数据' },
  { label: '在售商品', value: String(goodsOnSale.value).padStart(2, '0'), tip: '正在前台展示的商品' },
  { label: '求购中', value: String(wantedBuying.value).padStart(2, '0'), tip: '当前有效的求购需求' },
  { label: '公告总数', value: String(announcements.value.length).padStart(2, '0'), tip: '后台可管理的公告数量' },
]);

const shortcuts = [
  { title: '商品管理', badge: '01', description: '查看商品状态、上下架与删除操作。', to: '/admin/goods' },
  { title: '用户管理', badge: '02', description: '检索账号信息并切换启停用状态。', to: '/admin/users' },
  { title: '公告管理', badge: '03', description: '维护公告内容、发布状态与置顶信息。', to: '/admin/announcements' },
  { title: '返回前台', badge: '04', description: '回到校园跳蚤市场首页继续浏览。', to: '/' },
];

const ensureChart = (chartRef: HTMLDivElement | null, chart: EChartsType | null) => {
  if (!chartRef) return null;
  if (chart) return chart;
  return echarts.init(chartRef);
};

const renderCharts = () => {
  if (loading.value) return;

  goodsStatusChart = ensureChart(goodsStatusChartRef.value, goodsStatusChart);
  moduleOverviewChart = ensureChart(moduleOverviewChartRef.value, moduleOverviewChart);
  announcementLevelChart = ensureChart(announcementLevelChartRef.value, announcementLevelChart);

  goodsStatusChart?.setOption({
    animationDuration: 500,
    tooltip: { trigger: 'axis' },
    grid: { left: 36, right: 16, top: 28, bottom: 30 },
    xAxis: {
      type: 'category',
      axisTick: { show: false },
      axisLine: { lineStyle: { color: '#d6c37c' } },
      data: ['在售', '已预约', '已售出', '已下架'],
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: 'rgba(160, 132, 35, 0.12)' } },
    },
    series: [
      {
        type: 'bar',
        barWidth: 26,
        itemStyle: {
          borderRadius: [10, 10, 0, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#202733' },
            { offset: 1, color: '#ffd85b' },
          ]),
        },
        data: [goodsOnSale.value, goodsReserved.value, goodsSold.value, goodsOffShelf.value],
      },
    ],
  });

  moduleOverviewChart?.setOption({
    animationDuration: 500,
    tooltip: { trigger: 'item' },
    legend: { bottom: 0, icon: 'circle' },
    series: [
      {
        type: 'pie',
        radius: ['42%', '72%'],
        center: ['50%', '45%'],
        itemStyle: {
          borderRadius: 12,
          borderColor: '#fff8e4',
          borderWidth: 3,
        },
        label: { formatter: '{b}\n{c}' },
        data: [
          { value: goods.value.length, name: '商品', itemStyle: { color: '#202733' } },
          { value: wanted.value.length, name: '求购', itemStyle: { color: '#d8a21d' } },
          { value: announcements.value.length, name: '公告', itemStyle: { color: '#86a788' } },
        ],
      },
    ],
  });

  announcementLevelChart?.setOption({
    animationDuration: 500,
    tooltip: { trigger: 'item' },
    legend: { bottom: 0, icon: 'circle' },
    series: [
      {
        type: 'pie',
        radius: ['38%', '70%'],
        center: ['50%', '45%'],
        roseType: 'area',
        itemStyle: {
          borderColor: '#fff7db',
          borderWidth: 3,
        },
        label: { formatter: '{b}\n{c}' },
        data: [
          { value: announcementLevelStats.value.通知, name: '通知', itemStyle: { color: '#64748b' } },
          { value: announcementLevelStats.value.活动, name: '活动', itemStyle: { color: '#86a788' } },
          { value: announcementLevelStats.value.提示, name: '提示', itemStyle: { color: '#e0a100' } },
        ],
      },
    ],
  });
};

const resizeCharts = () => {
  goodsStatusChart?.resize();
  moduleOverviewChart?.resize();
  announcementLevelChart?.resize();
};

const disposeCharts = () => {
  goodsStatusChart?.dispose();
  moduleOverviewChart?.dispose();
  announcementLevelChart?.dispose();
  goodsStatusChart = null;
  moduleOverviewChart = null;
  announcementLevelChart = null;
};

const loadDashboard = async () => {
  loading.value = true;
  errorMessage.value = '';
  try {
    const [currentUser, goodsList, wantedList, announcementList] = await Promise.all([
      fetchCurrentUser(),
      getGoodsList(),
      getWantedList(),
      getAnnouncementList(),
    ]);
    profile.value = currentUser;
    goods.value = goodsList;
    wanted.value = wantedList;
    announcements.value = announcementList;
  } catch (error) {
    console.error('管理员控制台加载失败', error);
    errorMessage.value = '控制台数据加载失败，请稍后重试。';
  } finally {
    loading.value = false;
  }
};

watch(
  [loading, goods, wanted, announcements],
  async ([isLoading]) => {
    if (isLoading) return;
    await nextTick();
    renderCharts();
  },
  { deep: true },
);

onMounted(() => {
  loadDashboard();
  window.addEventListener('resize', resizeCharts);
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts);
  disposeCharts();
});
</script>

<style scoped lang="scss">
.stat-card {
  padding: 24px;
  display: grid;
  align-content: end;
  gap: 8px;
}

.stat-card span {
  font-size: 13px;
  letter-spacing: 0.04em;
  color: var(--qh-text-secondary);
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

.operations-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 18px;
}

.operation-card {
  display: grid;
  gap: 14px;
  padding: 26px;
  text-decoration: none;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
  border: 1px solid rgba(180, 141, 30, 0.1);
}

.operation-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 22px 40px rgba(128, 94, 15, 0.12);
}

.operation-card__badge {
  width: fit-content;
  padding: 8px 14px;
  border-radius: 999px;
  background: rgba(32, 39, 51, 0.92);
  color: #ffe27a;
  font-weight: 700;
}

.operation-card strong {
  font-size: 22px;
  color: var(--qh-text-primary);
  line-height: 1.35;
}

.operation-card p {
  margin: 0;
  line-height: 1.8;
  color: var(--qh-text-secondary);
}

.chart-grid {
  display: grid;
  grid-template-columns: 1.3fr 0.9fr;
  gap: 24px;
}

.chart-skeletons {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.chart-skeletons--single {
  grid-template-columns: 1fr;
}

.chart-card-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.chart-card,
.single-chart-card {
  padding: 20px;
  border-radius: 24px;
  border: 1px solid rgba(168, 137, 34, 0.1);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.52);
}

.chart-card__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.chart-card__header strong {
  color: var(--qh-text-primary);
  font-size: 18px;
}

.chart-card__header p {
  margin: 8px 0 0;
  color: var(--qh-text-secondary);
  line-height: 1.7;
  max-width: 28rem;
}

.chart-card__badge,
.panel-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 64px;
  min-height: 30px;
  padding: 0 12px;
  border-radius: 999px;
  background: rgba(255, 237, 171, 0.96);
  color: #76560f;
  font-weight: 700;
  font-size: 12px;
}

.panel-tag--dark {
  background: rgba(32, 39, 51, 0.92);
  color: #ffe27a;
}

.chart-card__canvas {
  height: 320px;
  margin-top: 12px;
}

.chart-card__canvas--large {
  height: 360px;
}

.grid-layout {
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  gap: 24px;
}

.session-banner {
  margin-bottom: 18px;
  padding: 20px 22px;
  display: grid;
  gap: 10px;
}

.session-banner__label {
  display: block;
  color: var(--qh-text-secondary);
  font-size: 13px;
}

.session-banner strong {
  font-size: 24px;
  color: var(--qh-text-primary);
}

.session-banner p {
  margin: 0;
  line-height: 1.8;
  color: var(--qh-text-secondary);
}

.todo-board {
  display: grid;
  gap: 16px;
}

.todo-item {
  padding: 20px;
  border-radius: 20px;
}

.todo-item strong {
  display: block;
  margin-bottom: 8px;
  color: var(--qh-text-primary);
}

.todo-item p {
  margin: 0;
  line-height: 1.8;
  color: var(--qh-text-secondary);
}

.todo-item span {
  display: inline-block;
  margin-top: 12px;
  color: var(--qh-primary-deep);
  font-weight: 600;
}

.announcement-summary {
  margin-bottom: 18px;
  padding: 18px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.announcement-summary article {
  padding: 16px 18px;
  min-height: 106px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.72);
}

.announcement-summary span {
  display: block;
  color: var(--qh-text-secondary);
}

.announcement-summary strong {
  display: block;
  margin-top: 10px;
  color: var(--qh-text-primary);
  font-size: 20px;
  line-height: 1.5;
}

.state-panel {
  padding: 12px;
  border-radius: 24px;
}

@media (max-width: 1280px) {
  .chart-grid,
  .grid-layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 1100px) {
  .operations-grid,
  .chart-card-grid,
  .chart-skeletons {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .operations-grid,
  .announcement-summary,
  .chart-card-grid,
  .chart-skeletons {
    grid-template-columns: 1fr;
  }

  .chart-card__canvas,
  .chart-card__canvas--large {
    height: 280px;
  }
}
</style>
