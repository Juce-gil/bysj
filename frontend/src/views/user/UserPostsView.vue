<template>
  <div class="posts-page">
    <section class="overview-banner qh-panel">
      <div class="overview-banner__main">
        <span class="overview-badge">MY POSTS CENTER</span>
        <h1>我的发布</h1>
        <p>集中查看商品与求购发布记录，让用户端与管理端形成前后台联动的完整展示链路。</p>
      </div>
      <div class="overview-banner__actions">
        <el-button type="primary" @click="openGoodsCreate">发布商品</el-button>
        <el-button @click="openWantedCreate">发布求购</el-button>
        <el-button text :loading="refreshing" @click="refreshAll">刷新全部</el-button>
      </div>
    </section>

    <section class="stats grid-cards grid-cards--4">
      <article class="stat-card qh-panel"><span>我的商品</span><strong>{{ goodsList.length }}</strong><p>当前已发布商品记录数</p></article>
      <article class="stat-card qh-panel"><span>在售商品</span><strong>{{ goodsList.filter((item) => canOffShelfGoods(item.status)).length }}</strong><p>支持继续展示与交易沟通</p></article>
      <article class="stat-card qh-panel"><span>我的求购</span><strong>{{ wantedList.length }}</strong><p>当前已发布求购记录数</p></article>
      <article class="stat-card qh-panel"><span>开放求购</span><strong>{{ wantedList.filter((item) => canCloseWanted(item.status)).length }}</strong><p>仍在持续征集的求购需求</p></article>
    </section>

    <section class="board qh-panel">
      <div class="board__head">
        <div>
          <h3>发布管理工作台</h3>
          <p>统一查看与维护商品、求购内容，支持筛选、编辑、删除和真实接口提交。</p>
        </div>
        <div class="board__tags">
          <el-tag effect="plain">默认校区：{{ defaultCampus }}</el-tag>
          <el-tag effect="plain" type="success">真实接口</el-tag>
        </div>
      </div>

      <div class="board-summary qh-panel--subtle">
        <article>
          <span>当前标签页</span>
          <strong>{{ activeTab === 'goods' ? '商品管理' : '求购管理' }}</strong>
          <p>{{ activeTab === 'goods' ? `当前匹配 ${filteredGoods.length} 条商品` : `当前匹配 ${filteredWanted.length} 条求购` }}</p>
        </article>
        <article>
          <span>筛选关键字</span>
          <strong>{{ activeTab === 'goods' ? (goodsKeyword || '未设置') : (wantedKeyword || '未设置') }}</strong>
          <p>可结合状态筛选快速定位已发布内容</p>
        </article>
        <article>
          <span>快捷操作</span>
          <strong>{{ activeTab === 'goods' ? '商品发布 / 编辑' : '求购发布 / 编辑' }}</strong>
          <p>支持图片上传、详情跳转与状态管理</p>
        </article>
      </div>

      <el-tabs v-model="activeTab" class="tabs">
        <el-tab-pane :label="`我的商品（${goodsList.length}）`" name="goods">
          <div class="toolbar qh-panel--subtle">
            <el-input v-model="goodsKeyword" clearable class="toolbar__input" placeholder="搜索标题、简介、分类、标签" />
            <el-select v-model="goodsStatusFilter" clearable class="toolbar__select" placeholder="全部状态">
              <el-option label="全部状态" value="" />
              <el-option v-for="item in goodsStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
            <el-button type="primary" plain @click="openGoodsCreate">发布商品</el-button>
            <el-button :loading="goodsLoading" @click="loadMyGoods">刷新</el-button>
          </div>
          <el-skeleton v-if="goodsLoading" :rows="6" animated />
          <el-result v-else-if="goodsError" icon="warning" title="商品列表加载失败" :sub-title="goodsError" />
          <el-empty v-else-if="!filteredGoods.length" description="暂无符合条件的商品发布" />
          <div v-else class="table-shell">
            <el-table :data="filteredGoods" stripe>
              <el-table-column label="商品" min-width="280">
                <template #default="{ row }"><div class="title-cell"><strong>{{ row.title }}</strong><p>{{ row.intro || '暂无简介' }}</p></div></template>
              </el-table-column>
              <el-table-column label="价格" min-width="120"><template #default="{ row }">{{ formatPrice(row.price) }}</template></el-table-column>
              <el-table-column prop="category" label="分类" min-width="120" />
              <el-table-column prop="campus" label="校区" min-width="120" />
              <el-table-column label="状态" min-width="120"><template #default="{ row }"><el-tag size="small" effect="light" :type="getGoodsStatusMeta(row.status).type">{{ getGoodsStatusMeta(row.status).text }}</el-tag></template></el-table-column>
              <el-table-column label="操作" min-width="320" fixed="right">
                <template #default="{ row }">
                  <el-button link type="primary" @click="goGoodsDetail(row.id)">详情</el-button>
                  <el-button link type="primary" @click="startEditGoods(row)">编辑</el-button>
                  <el-button v-if="canOffShelfGoods(row.status)" link type="warning" :loading="goodsAct(row.id, 'off')" @click="offGoods(row)">下架</el-button>
                  <el-button v-else-if="canRelistGoods(row.status)" link type="success" :loading="goodsAct(row.id, 'relist')" @click="relistGoods(row)">重新上架</el-button>
                  <el-button link type="danger" :loading="goodsAct(row.id, 'delete')" @click="deleteGoods(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <el-tab-pane :label="`我的求购（${wantedList.length}）`" name="wanted">
          <div class="toolbar qh-panel--subtle">
            <el-input v-model="wantedKeyword" clearable class="toolbar__input" placeholder="搜索标题、简介、分类、标签" />
            <el-select v-model="wantedStatusFilter" clearable class="toolbar__select" placeholder="全部状态">
              <el-option label="全部状态" value="" />
              <el-option v-for="item in wantedStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
            <el-button type="primary" plain @click="openWantedCreate">发布求购</el-button>
            <el-button :loading="wantedLoading" @click="loadMyWanted">刷新</el-button>
          </div>
          <el-skeleton v-if="wantedLoading" :rows="6" animated />
          <el-result v-else-if="wantedError" icon="warning" title="求购列表加载失败" :sub-title="wantedError" />
          <el-empty v-else-if="!filteredWanted.length" description="暂无符合条件的求购发布" />
          <div v-else class="table-shell">
            <el-table :data="filteredWanted" stripe>
              <el-table-column label="求购" min-width="280"><template #default="{ row }"><div class="title-cell"><strong>{{ row.title }}</strong><p>{{ row.intro || '暂无简介' }}</p></div></template></el-table-column>
              <el-table-column prop="budget" label="预算" min-width="140" />
              <el-table-column prop="category" label="分类" min-width="120" />
              <el-table-column prop="campus" label="校区" min-width="120" />
              <el-table-column label="状态" min-width="120"><template #default="{ row }"><el-tag size="small" effect="light" :type="getWantedStatusMeta(row.status).type">{{ getWantedStatusMeta(row.status).text }}</el-tag></template></el-table-column>
              <el-table-column label="操作" min-width="320" fixed="right">
                <template #default="{ row }">
                  <el-button link type="primary" @click="goWantedDetail(row.id)">详情</el-button>
                  <el-button link type="primary" @click="startEditWanted(row)">编辑</el-button>
                  <el-button v-if="canCloseWanted(row.status)" link type="warning" :loading="wantedAct(row.id, 'close')" @click="closeWanted(row)">关闭</el-button>
                  <el-button v-else-if="canReopenWanted(row.status)" link type="success" :loading="wantedAct(row.id, 'reopen')" @click="reopenWanted(row)">重新开放</el-button>
                  <el-button link type="danger" :loading="wantedAct(row.id, 'delete')" @click="deleteWantedItem(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
      </el-tabs>
    </section>

    <el-dialog v-model="goodsDialog" :title="goodsEditingId ? '编辑商品' : '发布商品'" width="860px" destroy-on-close>
      <el-form label-position="top" class="dialog-form">
        <div class="form-grid form-grid--2">
          <el-form-item label="标题"><el-input v-model="goodsForm.title" maxlength="100" show-word-limit /></el-form-item>
          <el-form-item label="分类"><el-select v-model="goodsForm.category" filterable allow-create default-first-option><el-option v-for="item in categoryOptions" :key="item" :label="item" :value="item" /></el-select></el-form-item>
        </div>
        <div class="form-grid form-grid--3">
          <el-form-item label="售价"><el-input-number v-model="goodsForm.price" :min="0.01" :precision="2" class="full-width" /></el-form-item>
          <el-form-item label="原价"><el-input-number v-model="goodsForm.originalPrice" :min="0" :precision="2" class="full-width" /></el-form-item>
          <el-form-item label="成色"><el-select v-model="goodsForm.condition" filterable allow-create default-first-option><el-option v-for="item in conditionOptions" :key="item" :label="item" :value="item" /></el-select></el-form-item>
        </div>
        <div class="form-grid form-grid--2">
          <el-form-item label="校区"><el-select v-model="goodsForm.campus" filterable allow-create default-first-option><el-option v-for="item in campusOptions" :key="item" :label="item" :value="item" /></el-select></el-form-item>
          <el-form-item label="标签"><el-input v-model="goodsForm.tagsText" placeholder="多个标签可用空格或中文逗号分隔" /></el-form-item>
        </div>
        <el-form-item label="简介"><el-input v-model="goodsForm.intro" maxlength="120" show-word-limit /></el-form-item>
        <el-form-item label="详细描述"><el-input v-model="goodsForm.description" type="textarea" :rows="5" maxlength="1000" show-word-limit /></el-form-item>
        <el-form-item label="商品图片">
          <el-upload v-model:file-list="goodsFiles" list-type="picture-card" accept="image/*" :auto-upload="false" :limit="MAX_IMAGE_COUNT" :before-upload="beforeImageUpload" :on-change="onGoodsUploadChange" :on-remove="onGoodsUploadRemove" :on-preview="onPreview" :on-exceed="onExceed">
            <div class="upload-trigger"><strong>+</strong><span>上传图片</span></div>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="closeGoodsDialog">取消</el-button><el-button type="primary" :loading="goodsSubmitting" @click="submitGoods">{{ goodsEditingId ? '保存商品' : '立即发布' }}</el-button></template>
    </el-dialog>

    <el-dialog v-model="wantedDialog" :title="wantedEditingId ? '编辑求购' : '发布求购'" width="860px" destroy-on-close>
      <el-form label-position="top" class="dialog-form">
        <div class="form-grid form-grid--2">
          <el-form-item label="标题"><el-input v-model="wantedForm.title" maxlength="100" show-word-limit /></el-form-item>
          <el-form-item label="分类"><el-select v-model="wantedForm.category" filterable allow-create default-first-option><el-option v-for="item in categoryOptions" :key="item" :label="item" :value="item" /></el-select></el-form-item>
        </div>
        <div class="form-grid form-grid--2">
          <el-form-item label="预算"><el-input v-model="wantedForm.budget" maxlength="30" show-word-limit /></el-form-item>
          <el-form-item label="截止日期"><el-date-picker v-model="wantedForm.deadline" type="date" value-format="YYYY-MM-DD" class="full-width" /></el-form-item>
        </div>
        <div class="form-grid form-grid--2">
          <el-form-item label="校区"><el-select v-model="wantedForm.campus" filterable allow-create default-first-option><el-option v-for="item in campusOptions" :key="item" :label="item" :value="item" /></el-select></el-form-item>
          <el-form-item label="标签"><el-input v-model="wantedForm.tagsText" placeholder="多个标签可用空格或中文逗号分隔" /></el-form-item>
        </div>
        <el-form-item label="简介"><el-input v-model="wantedForm.intro" maxlength="120" show-word-limit /></el-form-item>
        <el-form-item label="详细描述"><el-input v-model="wantedForm.description" type="textarea" :rows="5" maxlength="1000" show-word-limit /></el-form-item>
        <el-form-item label="求购图片（可选）">
          <el-upload v-model:file-list="wantedFiles" list-type="picture-card" accept="image/*" :auto-upload="false" :limit="MAX_IMAGE_COUNT" :before-upload="beforeImageUpload" :on-change="onWantedUploadChange" :on-remove="onWantedUploadRemove" :on-preview="onPreview" :on-exceed="onExceed">
            <div class="upload-trigger"><strong>+</strong><span>上传图片</span></div>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="closeWantedDialog">取消</el-button><el-button type="primary" :loading="wantedSubmitting" @click="submitWanted">{{ wantedEditingId ? '保存求购' : '立即发布' }}</el-button></template>
    </el-dialog>

    <el-dialog v-model="previewVisible" title="图片预览" width="720px"><img v-if="previewUrl" :src="previewUrl" alt="preview" class="preview-image" /></el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox, type UploadFile, type UploadFiles, type UploadProps, type UploadRawFile, type UploadUserFile } from 'element-plus';
import { closeWantedPost, createGoodsPost, createWantedPost, deleteGoodsPost, deleteWantedPost, getCategories, getMyGoodsList, getMyWantedList, offShelfGoodsPost, relistGoodsPost, reopenWantedPost, updateGoodsPost, updateWantedPost, uploadGoodsImages, type CategoryItem, type CreateGoodsPayload, type CreateWantedPayload, type GoodsItem, type WantedItem } from '@/api/marketplace';
import { useUserStore } from '@/stores/user';
import { formatPrice } from '@/utils/format';
import { getGoodsStatusMeta, getWantedStatusMeta } from '@/utils/status';

type TabName = 'goods' | 'wanted';
type GoodsForm = { title: string; price: number | undefined; originalPrice: number | undefined; category: string; campus: string; condition: string; intro: string; description: string; tagsText: string };
type WantedForm = { title: string; budget: string; category: string; campus: string; deadline: string; intro: string; description: string; tagsText: string };

const MAX_IMAGE_COUNT = 6, GOODS_OFF = new Set(['off_shelf', 'offsale', 'removed']), GOODS_LOCK = new Set(['sold', 'finished']), WANTED_CLOSED = new Set(['closed', 'cancelled', 'expired']), WANTED_DONE = new Set(['finished', 'completed', 'matched']);
const router = useRouter(), userStore = useUserStore();
const activeTab = ref<TabName>('goods'), refreshing = ref(false), goodsLoading = ref(false), wantedLoading = ref(false), goodsSubmitting = ref(false), wantedSubmitting = ref(false), goodsError = ref(''), wantedError = ref(''), goodsKeyword = ref(''), wantedKeyword = ref(''), goodsStatusFilter = ref(''), wantedStatusFilter = ref(''), goodsDialog = ref(false), wantedDialog = ref(false), previewVisible = ref(false), previewUrl = ref(''), goodsEditingId = ref<number | null>(null), wantedEditingId = ref<number | null>(null), goodsActionId = ref<number | null>(null), goodsActionType = ref(''), wantedActionId = ref<number | null>(null), wantedActionType = ref('');
const categories = ref<CategoryItem[]>([]), goodsList = ref<GoodsItem[]>([]), wantedList = ref<WantedItem[]>([]), goodsFiles = ref<UploadUserFile[]>([]), wantedFiles = ref<UploadUserFile[]>([]);
const defaultCampus = computed(() => userStore.profile?.school || '科成');
const makeGoodsForm = (): GoodsForm => ({ title: '', price: undefined, originalPrice: undefined, category: '', campus: defaultCampus.value, condition: '全新', intro: '', description: '', tagsText: '' });
const makeWantedForm = (): WantedForm => ({ title: '', budget: '', category: '', campus: defaultCampus.value, deadline: '', intro: '', description: '', tagsText: '' });
const goodsForm = reactive<GoodsForm>(makeGoodsForm()), wantedForm = reactive<WantedForm>(makeWantedForm());
const previewMap = new Map<number, string>();
const categoryOptions = computed(() => Array.from(new Set(['数码', '书籍', '生活用品', '服饰', '运动', '其他', ...categories.value.map((item) => item.name), ...goodsList.value.map((item) => item.category), ...wantedList.value.map((item) => item.category)].filter(Boolean))));
const campusOptions = computed(() => Array.from(new Set([defaultCampus.value, '科成', ...goodsList.value.map((item) => item.campus), ...wantedList.value.map((item) => item.campus)].filter(Boolean))));
const conditionOptions = computed(() => Array.from(new Set(['全新', '九成新', '八成新', '七成新', '正常使用', ...goodsList.value.map((item) => item.condition)].filter(Boolean))));
const goodsStatusOptions = computed(() => Array.from(new Set(goodsList.value.map((item) => item.status).filter(Boolean))).map((value) => ({ value, label: getGoodsStatusMeta(value).text })));
const wantedStatusOptions = computed(() => Array.from(new Set(wantedList.value.map((item) => item.status).filter(Boolean))).map((value) => ({ value, label: getWantedStatusMeta(value).text })));
const hit = (parts: string[], keyword: string) => !keyword.trim() || parts.join(' ').toLowerCase().includes(keyword.trim().toLowerCase());
const filteredGoods = computed(() => goodsList.value.filter((item) => hit([item.title, item.intro, item.description, item.category, item.tags.join(' ')], goodsKeyword.value) && (!goodsStatusFilter.value || item.status === goodsStatusFilter.value)));
const filteredWanted = computed(() => wantedList.value.filter((item) => hit([item.title, item.intro, item.description, item.category, item.tags.join(' ')], wantedKeyword.value) && (!wantedStatusFilter.value || item.status === wantedStatusFilter.value)));
watch(categoryOptions, (value) => { if (!goodsEditingId.value && !goodsForm.category && value.length) goodsForm.category = value[0]; if (!wantedEditingId.value && !wantedForm.category && value.length) wantedForm.category = value[0]; }, { immediate: true });
watch(conditionOptions, (value) => { if (!goodsEditingId.value && !goodsForm.condition && value.length) goodsForm.condition = value[0]; }, { immediate: true });
watch(defaultCampus, (value) => { if (!goodsEditingId.value && !goodsForm.campus) goodsForm.campus = value; if (!wantedEditingId.value && !wantedForm.campus) wantedForm.campus = value; }, { immediate: true });
const statusKey = (value: string) => value.trim().toLowerCase();
const canOffShelfGoods = (status: string) => !GOODS_OFF.has(statusKey(status)) && !GOODS_LOCK.has(statusKey(status));
const canRelistGoods = (status: string) => GOODS_OFF.has(statusKey(status));
const canCloseWanted = (status: string) => !WANTED_CLOSED.has(statusKey(status)) && !WANTED_DONE.has(statusKey(status));
const canReopenWanted = (status: string) => WANTED_CLOSED.has(statusKey(status));
const err = (error: unknown, fallback: string) => typeof error === 'object' && error ? ((error as { response?: { data?: { message?: string } }; message?: string }).response?.data?.message || (error as { message?: string }).message || fallback) : fallback;
const tags = (source: string) => Array.from(new Set(source.split(/[，,、\n\s]+/).map((item) => item.trim()).filter(Boolean))).slice(0, 8);
const uidNum = (value: number | string | undefined) => { const next = Number(value); return Number.isFinite(next) ? next : null; };
const ensureUrl = (uid: number, raw?: UploadRawFile) => { if (!raw) return ''; const current = previewMap.get(uid); if (current) return current; const next = URL.createObjectURL(raw); previewMap.set(uid, next); return next; };
const releaseUrl = (uid: number | string | undefined) => { const key = uidNum(uid); if (key == null) return; const current = previewMap.get(key); if (current) { URL.revokeObjectURL(current); previewMap.delete(key); } };
const normFile = (file: UploadFile | UploadUserFile, index: number): UploadUserFile => { const uid = uidNum(file.uid) ?? (Date.now() + index); const raw = file.raw as UploadRawFile | undefined; const url = typeof file.url === 'string' && file.url ? file.url : ensureUrl(uid, raw); return { ...file, uid, name: String(file.name || `图片${index + 1}`), status: file.status ?? 'success', url: String(url || ''), raw }; };
const normList = (files: Array<UploadFile | UploadUserFile>) => files.slice(0, MAX_IMAGE_COUNT).map(normFile);
const remoteList = (urls: string[]) => urls.filter(Boolean).slice(0, MAX_IMAGE_COUNT).map((url, index) => ({ uid: Date.now() + index, name: `图片${index + 1}`, status: 'success' as const, url: String(url) }));
const clearList = (list: UploadUserFile[]) => list.forEach((item) => releaseUrl(item.uid));
const goodsUrls = (item: GoodsItem) => item.imageUrls.length ? item.imageUrls : (item.coverImageUrl ? [item.coverImageUrl] : []);
const wantedUrls = (item: WantedItem) => item.imageUrls.length ? item.imageUrls : (item.coverImageUrl ? [item.coverImageUrl] : []);
const beforeImageUpload = (raw: UploadRawFile) => { const ok = raw.type.startsWith('image/'); if (!ok) ElMessage.warning('只能上传图片文件'); return ok; };
const onExceed = () => ElMessage.warning(`最多上传 ${MAX_IMAGE_COUNT} 张图片`);
const onPreview: UploadProps['onPreview'] = (file) => { previewUrl.value = String(file.url || ''); previewVisible.value = Boolean(previewUrl.value); };
const onGoodsUploadChange: UploadProps['onChange'] = (_file, files) => { goodsFiles.value = normList(files as UploadFiles); };
const onWantedUploadChange: UploadProps['onChange'] = (_file, files) => { wantedFiles.value = normList(files as UploadFiles); };
const onGoodsUploadRemove: UploadProps['onRemove'] = (file, files) => { releaseUrl(file.uid); goodsFiles.value = normList(files as UploadFiles); return true; };
const onWantedUploadRemove: UploadProps['onRemove'] = (file, files) => { releaseUrl(file.uid); wantedFiles.value = normList(files as UploadFiles); return true; };
const closeGoodsDialog = () => { goodsDialog.value = false; goodsEditingId.value = null; Object.assign(goodsForm, makeGoodsForm()); clearList(goodsFiles.value); goodsFiles.value = []; };
const closeWantedDialog = () => { wantedDialog.value = false; wantedEditingId.value = null; Object.assign(wantedForm, makeWantedForm()); clearList(wantedFiles.value); wantedFiles.value = []; };
const openGoodsCreate = () => { activeTab.value = 'goods'; closeGoodsDialog(); goodsDialog.value = true; };
const openWantedCreate = () => { activeTab.value = 'wanted'; closeWantedDialog(); wantedDialog.value = true; };
const startEditGoods = (item: GoodsItem) => { goodsEditingId.value = item.id; Object.assign(goodsForm, { title: item.title, price: item.price, originalPrice: item.originalPrice || undefined, category: item.category, campus: item.campus || defaultCampus.value, condition: item.condition || conditionOptions.value[0] || '全新', intro: item.intro, description: item.description, tagsText: item.tags.join('，') }); clearList(goodsFiles.value); goodsFiles.value = remoteList(goodsUrls(item)); goodsDialog.value = true; };
const startEditWanted = (item: WantedItem) => { wantedEditingId.value = item.id; Object.assign(wantedForm, { title: item.title, budget: item.budget, category: item.category, campus: item.campus || defaultCampus.value, deadline: item.deadline ? item.deadline.slice(0, 10) : '', intro: item.intro, description: item.description, tagsText: item.tags.join('，') }); clearList(wantedFiles.value); wantedFiles.value = remoteList(wantedUrls(item)); wantedDialog.value = true; };
const resolveUrls = async (list: UploadUserFile[]) => { const remote = list.filter((item) => !item.raw && typeof item.url === 'string' && item.url && !item.url.startsWith('blob:')).map((item) => String(item.url)); const local = list.filter((item) => item.raw).map((item) => item.raw as UploadRawFile).filter(Boolean) as File[]; if (!local.length) return remote.slice(0, MAX_IMAGE_COUNT); const uploaded = await uploadGoodsImages(local); return [...remote, ...uploaded.map((item) => item.url)].slice(0, MAX_IMAGE_COUNT); };
const submitGoods = async () => { const message = !goodsForm.title.trim() ? '请输入商品标题' : goodsForm.price == null || Number(goodsForm.price) <= 0 ? '请输入正确的商品售价' : !goodsForm.category.trim() ? '请选择商品分类' : !goodsForm.campus.trim() ? '请选择校区' : !goodsForm.condition.trim() ? '请选择商品成色' : !goodsForm.intro.trim() ? '请输入商品简介' : !goodsForm.description.trim() ? '请输入商品详细描述' : !goodsFiles.value.length ? '请至少上传一张商品图片' : ''; if (message) return ElMessage.warning(message); goodsSubmitting.value = true; try { const payload: CreateGoodsPayload = { title: goodsForm.title.trim(), price: Number(goodsForm.price), originalPrice: goodsForm.originalPrice == null ? null : Number(goodsForm.originalPrice), category: goodsForm.category.trim(), campus: goodsForm.campus.trim(), condition: goodsForm.condition.trim(), intro: goodsForm.intro.trim(), description: goodsForm.description.trim(), tags: tags(goodsForm.tagsText), imageUrls: await resolveUrls(goodsFiles.value) }; goodsEditingId.value != null ? await updateGoodsPost(goodsEditingId.value, payload) : await createGoodsPost(payload); ElMessage.success(goodsEditingId.value != null ? '商品已更新' : '商品已发布'); closeGoodsDialog(); await loadMyGoods(); } catch (error) { ElMessage.error(err(error, '商品保存失败')); } finally { goodsSubmitting.value = false; } };
const submitWanted = async () => { const message = !wantedForm.title.trim() ? '请输入求购标题' : !wantedForm.budget.trim() ? '请输入预算说明' : !wantedForm.category.trim() ? '请选择求购分类' : !wantedForm.campus.trim() ? '请选择校区' : !wantedForm.deadline.trim() ? '请选择截止日期' : !wantedForm.intro.trim() ? '请输入求购简介' : !wantedForm.description.trim() ? '请输入求购详细描述' : ''; if (message) return ElMessage.warning(message); wantedSubmitting.value = true; try { const payload: CreateWantedPayload = { title: wantedForm.title.trim(), budget: wantedForm.budget.trim(), category: wantedForm.category.trim(), campus: wantedForm.campus.trim(), deadline: wantedForm.deadline.trim(), intro: wantedForm.intro.trim(), description: wantedForm.description.trim(), tags: tags(wantedForm.tagsText), imageUrls: await resolveUrls(wantedFiles.value) }; wantedEditingId.value != null ? await updateWantedPost(wantedEditingId.value, payload) : await createWantedPost(payload); ElMessage.success(wantedEditingId.value != null ? '求购已更新' : '求购已发布'); closeWantedDialog(); await loadMyWanted(); } catch (error) { ElMessage.error(err(error, '求购保存失败')); } finally { wantedSubmitting.value = false; } };
const loadCategories = async () => { try { categories.value = (await getCategories()).sort((a, b) => a.sortNum - b.sortNum); } catch { categories.value = []; } };
const loadMyGoods = async () => { goodsLoading.value = true; goodsError.value = ''; try { goodsList.value = await getMyGoodsList(); } catch (error) { goodsError.value = err(error, '请稍后重试'); } finally { goodsLoading.value = false; } };
const loadMyWanted = async () => { wantedLoading.value = true; wantedError.value = ''; try { wantedList.value = await getMyWantedList(); } catch (error) { wantedError.value = err(error, '请稍后重试'); } finally { wantedLoading.value = false; } };
const refreshAll = async () => { refreshing.value = true; await Promise.allSettled([loadCategories(), loadMyGoods(), loadMyWanted()]); refreshing.value = false; };
const goGoodsDetail = (id: number) => router.push({ name: 'goods-detail', params: { id } });
const goWantedDetail = (id: number) => router.push({ name: 'wanted-detail', params: { id } });
const goodsAct = (id: number, type: string) => goodsActionId.value === id && goodsActionType.value === type;
const wantedAct = (id: number, type: string) => wantedActionId.value === id && wantedActionType.value === type;
const offGoods = async (item: GoodsItem) => { goodsActionId.value = item.id; goodsActionType.value = 'off'; try { await offShelfGoodsPost(item.id); ElMessage.success('商品已下架'); await loadMyGoods(); } catch (error) { ElMessage.error(err(error, '商品下架失败')); } finally { goodsActionId.value = null; goodsActionType.value = ''; } };
const relistGoods = async (item: GoodsItem) => { goodsActionId.value = item.id; goodsActionType.value = 'relist'; try { await relistGoodsPost(item.id); ElMessage.success('商品已重新上架'); await loadMyGoods(); } catch (error) { ElMessage.error(err(error, '商品重新上架失败')); } finally { goodsActionId.value = null; goodsActionType.value = ''; } };
const deleteGoods = async (item: GoodsItem) => { try { await ElMessageBox.confirm(`确认删除商品“${item.title}”吗？删除后不可恢复。`, '删除商品', { type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消' }); } catch { return; } goodsActionId.value = item.id; goodsActionType.value = 'delete'; try { await deleteGoodsPost(item.id); if (goodsEditingId.value === item.id) closeGoodsDialog(); ElMessage.success('商品已删除'); await loadMyGoods(); } catch (error) { ElMessage.error(err(error, '商品删除失败')); } finally { goodsActionId.value = null; goodsActionType.value = ''; } };
const closeWanted = async (item: WantedItem) => { wantedActionId.value = item.id; wantedActionType.value = 'close'; try { await closeWantedPost(item.id); ElMessage.success('求购已关闭'); await loadMyWanted(); } catch (error) { ElMessage.error(err(error, '求购关闭失败')); } finally { wantedActionId.value = null; wantedActionType.value = ''; } };
const reopenWanted = async (item: WantedItem) => { wantedActionId.value = item.id; wantedActionType.value = 'reopen'; try { await reopenWantedPost(item.id); ElMessage.success('求购已重新开放'); await loadMyWanted(); } catch (error) { ElMessage.error(err(error, '求购重新开放失败')); } finally { wantedActionId.value = null; wantedActionType.value = ''; } };
const deleteWantedItem = async (item: WantedItem) => { try { await ElMessageBox.confirm(`确认删除求购“${item.title}”吗？删除后不可恢复。`, '删除求购', { type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消' }); } catch { return; } wantedActionId.value = item.id; wantedActionType.value = 'delete'; try { await deleteWantedPost(item.id); if (wantedEditingId.value === item.id) closeWantedDialog(); ElMessage.success('求购已删除'); await loadMyWanted(); } catch (error) { ElMessage.error(err(error, '求购删除失败')); } finally { wantedActionId.value = null; wantedActionType.value = ''; } };
onMounted(refreshAll); onBeforeUnmount(() => { Array.from(previewMap.values()).forEach((url) => URL.revokeObjectURL(url)); previewMap.clear(); });
</script>

<style scoped lang="scss">
.posts-page { display: grid; gap: 24px; }
.overview-banner { display: flex; justify-content: space-between; gap: 20px; padding: 28px 30px; background: linear-gradient(180deg, rgba(255, 255, 255, 0.98) 0%, rgba(236, 246, 255, 0.94) 100%); }
.overview-badge { display: inline-flex; align-items: center; min-height: 34px; padding: 0 14px; border-radius: 999px; background: rgba(36, 65, 95, 0.94); color: #fff; font-size: 12px; letter-spacing: .12em; }
.overview-banner h1 { margin: 16px 0 0; color: var(--qh-text-primary); font-size: clamp(28px, 3vw, 40px); }
.overview-banner p { margin: 12px 0 0; color: var(--qh-text-secondary); line-height: 1.9; max-width: 760px; }
.overview-banner__actions { display: flex; flex-direction: column; align-items: flex-end; gap: 12px; }
.stats .stat-card { padding: 22px; }
.stat-card span { color: var(--qh-text-secondary); }
.stat-card strong { display: block; margin-top: 12px; font-size: 30px; color: var(--qh-text-primary); }
.stat-card p { margin: 10px 0 0; color: var(--qh-text-secondary); line-height: 1.7; }
.board { padding: 24px; }
.board__head { display: flex; justify-content: space-between; gap: 16px; align-items: flex-start; margin-bottom: 18px; }
.board__head h3 { margin: 0; color: var(--qh-text-primary); }
.board__head p { margin: 8px 0 0; color: var(--qh-text-secondary); line-height: 1.8; }
.board__tags { display: flex; gap: 10px; flex-wrap: wrap; }
.board-summary { margin-bottom: 18px; padding: 16px; display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 16px; }
.board-summary article { padding: 16px; border-radius: 18px; background: rgba(255, 255, 255, 0.78); }
.board-summary span { display: block; color: var(--qh-text-secondary); }
.board-summary strong { display: block; margin-top: 10px; font-size: 22px; color: var(--qh-text-primary); }
.board-summary p { margin: 10px 0 0; color: var(--qh-text-secondary); line-height: 1.7; }
.tabs :deep(.el-tabs__item.is-active) { color: var(--qh-text-primary); font-weight: 700; }
.tabs :deep(.el-tabs__active-bar) { background: var(--qh-primary-deep); }
.toolbar { display: flex; flex-wrap: wrap; gap: 12px; align-items: center; padding: 14px; margin-bottom: 14px; }
.toolbar__input { flex: 1 1 320px; }
.toolbar__select { width: 180px; }
.table-shell { padding: 16px; border-radius: 22px; background: rgba(236, 246, 255, 0.46); }
.title-cell strong { color: var(--qh-text-primary); }
.title-cell p { margin: 6px 0 0; color: var(--qh-text-secondary); line-height: 1.6; }
.dialog-form { display: grid; gap: 12px; }
.form-grid { display: grid; gap: 14px; }
.form-grid--2 { grid-template-columns: repeat(2, minmax(0, 1fr)); }
.form-grid--3 { grid-template-columns: repeat(3, minmax(0, 1fr)); }
.full-width, :deep(.full-width .el-input-number), :deep(.el-select), :deep(.el-date-editor) { width: 100%; }
.upload-trigger { display: flex; flex-direction: column; align-items: center; gap: 4px; color: var(--qh-primary-deep); }
.upload-trigger strong { font-size: 24px; line-height: 1; }
.preview-image { display: block; width: 100%; max-height: 72vh; object-fit: contain; }
@media (max-width: 1100px) { .board-summary { grid-template-columns: 1fr; } }
@media (max-width: 960px) { .overview-banner, .overview-banner__actions, .board__head { flex-direction: column; align-items: flex-start; } .form-grid--2, .form-grid--3 { grid-template-columns: 1fr; } }
</style>
