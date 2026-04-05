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
      title="当前后台控制台基于真实前台接口聚合数据构建；审核、举报、用户列表等专属管理接口仍待后端补齐。"
    />

    <section v-if="errorMessage" class="page-section qh-panel state-panel">
      <el-result icon="warning" title="控制台数据加载失败" :sub-title="errorMessage">
        <template #extra>
          <el-button type="primary" @click="loadDashboard">重新加载</el-button>
        </template>
      </el-result>
    </section>

    <section class="page-section grid-layout">
      <SectionCard title="当前管理员会话" subtitle="已联调当前用户资料接口，可直接展示后台操作者信息。">
        <template #extra>
          <el-button text :loading="loading" @click="loadDashboard">刷新</el-button>
        </template>
        <el-skeleton v-if="loading" :rows="6" animated />
        <el-descriptions v-else :column="1" border>
          <el-descriptions-item label="管理员">{{ profile?.name || '未加载' }}</el-descriptions-item>
          <el-descriptions-item label="角色">{{ profile?.role === 'admin' ? '管理员' : '普通用户' }}</el-descriptions-item>
          <el-descriptions-item label="学校">{{ profile?.school || '科成' }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ profile?.phone || '暂未提供' }}</el-descriptions-item>
          <el-descriptions-item label="QQ">{{ profile?.qq || '暂未提供' }}</el-descriptions-item>
          <el-descriptions-item label="签名">{{ profile?.slogan || '维护好每一份校园交易信任。' }}</el-descriptions-item>
        </el-descriptions>
      </SectionCard>
      <SectionCard title="内容概览" subtitle="使用真实商品、求购和公告接口聚合出后台首屏摘要。">
        <div v-if="loading" class="todo-board">
          <el-skeleton v-for="item in 3" :key="item" :rows="2" animated />
        </div>
        <div v-else class="todo-board">
          <div class="todo-item qh-panel--subtle">
            <strong>商品状态分布</strong>
            <p>在售 {{ goodsOnSale }} 件，已预约 {{ goodsReserved }} 件，已售出 {{ goodsSold }} 件。</p>
            <span>接口来源：/api/goods</span>
          </div>
          <div class="todo-item qh-panel--subtle">
            <strong>求购活跃度</strong>
            <p>求购中 {{ wantedBuying }} 条，已关闭 {{ wantedClosed }} 条。</p>
            <span>接口来源：/api/wanted</span>
          </div>
          <div class="todo-item qh-panel--subtle">
            <strong>公告发布情况</strong>
            <p>当前共有 {{ announcements.length }} 条公告，最近一条为 {{ latestAnnouncementTitle }}。</p>
            <span>接口来源：/api/announcements</span>
          </div>
        </div>
      </SectionCard>
    </section>

    <section class="page-section">
      <SectionCard title="最近公告" subtitle="沿用真实公告接口，先作为后台运营信息占位。">
        <el-table v-if="!loading" :data="announcements.slice(0, 5)" stripe>
          <el-table-column prop="title" label="标题" min-width="220" />
          <el-table-column prop="level" label="类型" min-width="100">
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
import { computed, onMounted, ref } from 'vue';
import SectionCard from '@/components/SectionCard.vue';
import { getAnnouncementList, getGoodsList, getWantedList, type AnnouncementItem, type GoodsItem, type WantedItem } from '@/api/marketplace';
import { fetchCurrentUser } from '@/api/auth';
import type { StoredProfile } from '@/utils/storage';
import { getAnnouncementLevelTagType } from '@/utils/status';

const loading = ref(false);
const errorMessage = ref('');
const profile = ref<StoredProfile | null>(null);
const goods = ref<GoodsItem[]>([]);
const wanted = ref<WantedItem[]>([]);
const announcements = ref<AnnouncementItem[]>([]);

const goodsOnSale = computed(() => goods.value.filter((item) => item.status === 'on_sale').length);
const goodsReserved = computed(() => goods.value.filter((item) => item.status === 'reserved').length);
const goodsSold = computed(() => goods.value.filter((item) => item.status === 'sold').length);
const wantedBuying = computed(() => wanted.value.filter((item) => item.status === 'buying').length);
const wantedClosed = computed(() => wanted.value.filter((item) => item.status === 'closed').length);
const latestAnnouncementTitle = computed(() => announcements.value[0]?.title || '暂无公告');

const stats = computed(() => [
  { label: '商品总量', value: String(goods.value.length).padStart(2, '0'), tip: '来源：商品列表接口' },
  { label: '在售商品', value: String(goodsOnSale.value).padStart(2, '0'), tip: '当前仍可交易的商品数' },
  { label: '活跃求购', value: String(wantedBuying.value).padStart(2, '0'), tip: '来源：求购列表接口' },
  { label: '公告总数', value: String(announcements.value.length).padStart(2, '0'), tip: '来源：公告列表接口' },
]);

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
    console.error('加载后台控制台失败', error);
    errorMessage.value = '控制台聚合数据加载失败，请稍后重试。';
  } finally {
    loading.value = false;
  }
};

onMounted(loadDashboard);
</script>

<style scoped lang="scss">
.stat-card { padding: 22px; }
.stat-card span { color: var(--qh-text-secondary); }
.stat-card strong { display: block; margin: 14px 0 10px; font-size: 30px; color: var(--qh-primary-deep); }
.stat-card p { margin: 0; color: var(--qh-text-secondary); }
.grid-layout { display: grid; grid-template-columns: 1.1fr 0.9fr; gap: 24px; }
.todo-board { display: grid; gap: 16px; }
.todo-item { padding: 18px; }
.todo-item strong { display: block; margin-bottom: 8px; color: var(--qh-text-primary); }
.todo-item p { margin: 0; line-height: 1.8; color: var(--qh-text-secondary); }
.todo-item span { display: inline-block; margin-top: 12px; color: var(--qh-primary-deep); font-weight: 600; }
.state-panel { padding: 12px; }
@media (max-width: 960px) { .grid-layout { grid-template-columns: 1fr; } }
</style>
