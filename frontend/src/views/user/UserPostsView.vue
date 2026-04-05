<template>
  <div class="posts-page">
    <section class="hero qh-panel">
      <div>
        <span class="hero__eyebrow">MY POSTS CENTER</span>
        <h1>我的商品 & 我的求购</h1>
        <p>支持搜索、状态筛选、编辑、上下架/关闭、删除、详情跳转，以及多图预览与真实接口提交。</p>
      </div>
      <div class="hero__actions">
        <el-button type="warning" @click="openGoodsCreate">发布商品</el-button>
        <el-button @click="openWantedCreate">发布求购</el-button>
        <el-button text :loading="refreshing" @click="refreshAll">刷新全部</el-button>
      </div>
    </section>

    <section class="stats grid-cards grid-cards--4">
      <article class="stat-card qh-panel"><span>我的商品</span><strong>{{ goodsList.length }}</strong></article>
      <article class="stat-card qh-panel"><span>在售商品</span><strong>{{ goodsList.filter((i) => canOffShelfGoods(i.status)).length }}</strong></article>
      <article class="stat-card qh-panel"><span>我的求购</span><strong>{{ wantedList.length }}</strong></article>
      <article class="stat-card qh-panel"><span>开放求购</span><strong>{{ wantedList.filter((i) => canCloseWanted(i.status)).length }}</strong></article>
    </section>

    <section class="board qh-panel">
      <div class="board__head">
        <div>
          <h3>发布管理</h3>
          <p>黄黑风格统一工作台，覆盖商品与求购的完整管理能力。</p>
        </div>
        <div class="board__tags">
          <el-tag effect="dark" type="warning">{{ defaultCampus }}</el-tag>
          <el-tag effect="plain" type="info">真实接口</el-tag>
        </div>
      </div>

      <el-tabs v-model="activeTab" class="tabs">
        <el-tab-pane :label="`我的商品（${goodsList.length}）`" name="goods">
          <div class="toolbar qh-panel--subtle">
            <el-input v-model="goodsKeyword" clearable class="toolbar__input" placeholder="搜索标题、简介、分类、标签" />
            <el-select v-model="goodsStatusFilter" clearable class="toolbar__select" placeholder="全部状态">
              <el-option label="全部状态" value="" />
              <el-option v-for="item in goodsStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
            <el-button type="warning" plain @click="openGoodsCreate">发布商品</el-button>
            <el-button :loading="goodsLoading" @click="loadMyGoods">刷新</el-button>
          </div>
          <el-skeleton v-if="goodsLoading" :rows="6" animated />
          <el-result v-else-if="goodsError" icon="warning" title="商品列表加载失败" :sub-title="goodsError" />
          <el-empty v-else-if="!filteredGoods.length" description="没有符合条件的商品" />
          <el-table v-else :data="filteredGoods" stripe class="posts-table">
            <el-table-column label="商品" min-width="280"><template #default="{row}"><div class="title-cell"><strong>{{ row.title }}</strong><p>{{ row.intro || '暂无简介' }}</p></div></template></el-table-column>
            <el-table-column label="价格" min-width="120"><template #default="{row}">{{ formatPrice(row.price) }}</template></el-table-column>
            <el-table-column prop="category" label="分类" min-width="120" />
            <el-table-column prop="campus" label="校区" min-width="120" />
            <el-table-column label="状态" min-width="120"><template #default="{row}"><el-tag size="small" effect="light" :type="getGoodsStatusMeta(row.status).type">{{ getGoodsStatusMeta(row.status).text }}</el-tag></template></el-table-column>
            <el-table-column label="操作" min-width="320" fixed="right"><template #default="{row}"><el-button link type="primary" @click="goGoodsDetail(row.id)">详情</el-button><el-button link type="primary" @click="startEditGoods(row)">编辑</el-button><el-button v-if="canOffShelfGoods(row.status)" link type="warning" :loading="goodsAct(row.id,'off')" @click="offGoods(row)">下架</el-button><el-button v-else-if="canRelistGoods(row.status)" link type="success" :loading="goodsAct(row.id,'relist')" @click="relistGoods(row)">重新上架</el-button><el-button link type="danger" :loading="goodsAct(row.id,'delete')" @click="deleteGoods(row)">删除</el-button></template></el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane :label="`我的求购（${wantedList.length}）`" name="wanted">
          <div class="toolbar qh-panel--subtle">
            <el-input v-model="wantedKeyword" clearable class="toolbar__input" placeholder="搜索标题、简介、分类、标签" />
            <el-select v-model="wantedStatusFilter" clearable class="toolbar__select" placeholder="全部状态">
              <el-option label="全部状态" value="" />
              <el-option v-for="item in wantedStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
            <el-button type="warning" plain @click="openWantedCreate">发布求购</el-button>
            <el-button :loading="wantedLoading" @click="loadMyWanted">刷新</el-button>
          </div>
          <el-skeleton v-if="wantedLoading" :rows="6" animated />
          <el-result v-else-if="wantedError" icon="warning" title="求购列表加载失败" :sub-title="wantedError" />
          <el-empty v-else-if="!filteredWanted.length" description="没有符合条件的求购" />
          <el-table v-else :data="filteredWanted" stripe class="posts-table">
            <el-table-column label="求购" min-width="280"><template #default="{row}"><div class="title-cell"><strong>{{ row.title }}</strong><p>{{ row.intro || '暂无简介' }}</p></div></template></el-table-column>
            <el-table-column prop="budget" label="预算" min-width="140" />
            <el-table-column prop="category" label="分类" min-width="120" />
            <el-table-column prop="campus" label="校区" min-width="120" />
            <el-table-column label="状态" min-width="120"><template #default="{row}"><el-tag size="small" effect="light" :type="getWantedStatusMeta(row.status).type">{{ getWantedStatusMeta(row.status).text }}</el-tag></template></el-table-column>
            <el-table-column label="操作" min-width="320" fixed="right"><template #default="{row}"><el-button link type="primary" @click="goWantedDetail(row.id)">详情</el-button><el-button link type="primary" @click="startEditWanted(row)">编辑</el-button><el-button v-if="canCloseWanted(row.status)" link type="warning" :loading="wantedAct(row.id,'close')" @click="closeWanted(row)">关闭</el-button><el-button v-else-if="canReopenWanted(row.status)" link type="success" :loading="wantedAct(row.id,'reopen')" @click="reopenWanted(row)">重新开放</el-button><el-button link type="danger" :loading="wantedAct(row.id,'delete')" @click="deleteWantedItem(row)">删除</el-button></template></el-table-column>
          </el-table>
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
          <el-form-item label="标签"><el-input v-model="goodsForm.tagsText" placeholder="多个标签用逗号、顿号或换行分隔" /></el-form-item>
        </div>
        <el-form-item label="简介"><el-input v-model="goodsForm.intro" maxlength="120" show-word-limit /></el-form-item>
        <el-form-item label="详细描述"><el-input v-model="goodsForm.description" type="textarea" :rows="5" maxlength="1000" show-word-limit /></el-form-item>
        <el-form-item label="商品图片">
          <el-upload v-model:file-list="goodsFiles" list-type="picture-card" accept="image/*" :auto-upload="false" :limit="MAX_IMAGE_COUNT" :before-upload="beforeImageUpload" :on-change="onGoodsUploadChange" :on-remove="onGoodsUploadRemove" :on-preview="onPreview" :on-exceed="onExceed">
            <div class="upload-trigger"><strong>+</strong><span>上传图片</span></div>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="closeGoodsDialog">取消</el-button><el-button type="warning" :loading="goodsSubmitting" @click="submitGoods">{{ goodsEditingId ? '保存商品' : '立即发布' }}</el-button></template>
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
          <el-form-item label="标签"><el-input v-model="wantedForm.tagsText" placeholder="多个标签用逗号、顿号或换行分隔" /></el-form-item>
        </div>
        <el-form-item label="简介"><el-input v-model="wantedForm.intro" maxlength="120" show-word-limit /></el-form-item>
        <el-form-item label="详细描述"><el-input v-model="wantedForm.description" type="textarea" :rows="5" maxlength="1000" show-word-limit /></el-form-item>
        <el-form-item label="求购图片（可选）">
          <el-upload v-model:file-list="wantedFiles" list-type="picture-card" accept="image/*" :auto-upload="false" :limit="MAX_IMAGE_COUNT" :before-upload="beforeImageUpload" :on-change="onWantedUploadChange" :on-remove="onWantedUploadRemove" :on-preview="onPreview" :on-exceed="onExceed">
            <div class="upload-trigger"><strong>+</strong><span>上传图片</span></div>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="closeWantedDialog">取消</el-button><el-button type="warning" :loading="wantedSubmitting" @click="submitWanted">{{ wantedEditingId ? '保存求购' : '立即发布' }}</el-button></template>
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
const MAX_IMAGE_COUNT = 6, GOODS_OFF = new Set(['off_shelf','offsale','removed']), GOODS_LOCK = new Set(['sold','finished']), WANTED_CLOSED = new Set(['closed','cancelled','expired']), WANTED_DONE = new Set(['finished','completed','matched']);
const router = useRouter(), userStore = useUserStore();
const activeTab = ref<TabName>('goods'), refreshing = ref(false), goodsLoading = ref(false), wantedLoading = ref(false), goodsSubmitting = ref(false), wantedSubmitting = ref(false), goodsError = ref(''), wantedError = ref(''), goodsKeyword = ref(''), wantedKeyword = ref(''), goodsStatusFilter = ref(''), wantedStatusFilter = ref(''), goodsDialog = ref(false), wantedDialog = ref(false), previewVisible = ref(false), previewUrl = ref(''), goodsEditingId = ref<number | null>(null), wantedEditingId = ref<number | null>(null), goodsActionId = ref<number | null>(null), goodsActionType = ref(''), wantedActionId = ref<number | null>(null), wantedActionType = ref('');
const categories = ref<CategoryItem[]>([]), goodsList = ref<GoodsItem[]>([]), wantedList = ref<WantedItem[]>([]), goodsFiles = ref<UploadUserFile[]>([]), wantedFiles = ref<UploadUserFile[]>([]);
const defaultCampus = computed(() => userStore.profile?.school || '科成');
const makeGoodsForm = (): GoodsForm => ({ title: '', price: undefined, originalPrice: undefined, category: '', campus: defaultCampus.value, condition: '全新', intro: '', description: '', tagsText: '' });
const makeWantedForm = (): WantedForm => ({ title: '', budget: '', category: '', campus: defaultCampus.value, deadline: '', intro: '', description: '', tagsText: '' });
const goodsForm = reactive<GoodsForm>(makeGoodsForm()), wantedForm = reactive<WantedForm>(makeWantedForm());
const previewMap = new Map<number, string>();
const categoryOptions = computed(() => Array.from(new Set(['数码','书籍','生活用品','服饰','运动','其他', ...categories.value.map((i) => i.name), ...goodsList.value.map((i) => i.category), ...wantedList.value.map((i) => i.category)].filter(Boolean))));
const campusOptions = computed(() => Array.from(new Set([defaultCampus.value, '科成', ...goodsList.value.map((i) => i.campus), ...wantedList.value.map((i) => i.campus)].filter(Boolean))));
const conditionOptions = computed(() => Array.from(new Set(['全新','九成新','八成新','七成新','正常使用', ...goodsList.value.map((i) => i.condition)].filter(Boolean))));
const goodsStatusOptions = computed(() => Array.from(new Set(goodsList.value.map((i) => i.status).filter(Boolean))).map((value) => ({ value, label: getGoodsStatusMeta(value).text })));
const wantedStatusOptions = computed(() => Array.from(new Set(wantedList.value.map((i) => i.status).filter(Boolean))).map((value) => ({ value, label: getWantedStatusMeta(value).text })));
const filteredGoods = computed(() => goodsList.value.filter((item) => hit([item.title,item.intro,item.description,item.category,item.tags.join(' ')], goodsKeyword.value) && (!goodsStatusFilter.value || item.status === goodsStatusFilter.value)));
const filteredWanted = computed(() => wantedList.value.filter((item) => hit([item.title,item.intro,item.description,item.category,item.tags.join(' ')], wantedKeyword.value) && (!wantedStatusFilter.value || item.status === wantedStatusFilter.value)));
watch(categoryOptions, (v) => { if (!goodsEditingId.value && !goodsForm.category && v.length) goodsForm.category = v[0]; if (!wantedEditingId.value && !wantedForm.category && v.length) wantedForm.category = v[0]; }, { immediate: true });
watch(conditionOptions, (v) => { if (!goodsEditingId.value && !goodsForm.condition && v.length) goodsForm.condition = v[0]; }, { immediate: true });
watch(defaultCampus, (v) => { if (!goodsEditingId.value && !goodsForm.campus) goodsForm.campus = v; if (!wantedEditingId.value && !wantedForm.campus) wantedForm.campus = v; }, { immediate: true });
const hit = (parts: string[], keyword: string) => !keyword.trim() || parts.join(' ').toLowerCase().includes(keyword.trim().toLowerCase());
const statusKey = (value: string) => value.trim().toLowerCase();
const canOffShelfGoods = (status: string) => !GOODS_OFF.has(statusKey(status)) && !GOODS_LOCK.has(statusKey(status));
const canRelistGoods = (status: string) => GOODS_OFF.has(statusKey(status));
const canCloseWanted = (status: string) => !WANTED_CLOSED.has(statusKey(status)) && !WANTED_DONE.has(statusKey(status));
const canReopenWanted = (status: string) => WANTED_CLOSED.has(statusKey(status));
const err = (e: unknown, f: string) => typeof e === 'object' && e ? ((e as { response?: { data?: { message?: string } }; message?: string }).response?.data?.message || (e as { message?: string }).message || f) : f;
const tags = (s: string) => Array.from(new Set(s.split(/[，,、\n\s]+/).map((i) => i.trim()).filter(Boolean))).slice(0, 8);
const uidNum = (v: number | string | undefined) => { const n = Number(v); return Number.isFinite(n) ? n : null; };
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
const submitGoods = async () => { const msg = !goodsForm.title.trim() ? '请输入商品标题' : goodsForm.price == null || Number(goodsForm.price) <= 0 ? '请输入正确的商品售价' : !goodsForm.category.trim() ? '请选择商品分类' : !goodsForm.campus.trim() ? '请选择校区' : !goodsForm.condition.trim() ? '请选择商品成色' : !goodsForm.intro.trim() ? '请输入商品简介' : !goodsForm.description.trim() ? '请输入商品描述' : !goodsFiles.value.length ? '请至少上传一张商品图片' : ''; if (msg) return ElMessage.warning(msg); goodsSubmitting.value = true; try { const payload: CreateGoodsPayload = { title: goodsForm.title.trim(), price: Number(goodsForm.price), originalPrice: goodsForm.originalPrice == null ? null : Number(goodsForm.originalPrice), category: goodsForm.category.trim(), campus: goodsForm.campus.trim(), condition: goodsForm.condition.trim(), intro: goodsForm.intro.trim(), description: goodsForm.description.trim(), tags: tags(goodsForm.tagsText), imageUrls: await resolveUrls(goodsFiles.value) }; goodsEditingId.value != null ? await updateGoodsPost(goodsEditingId.value, payload) : await createGoodsPost(payload); ElMessage.success(goodsEditingId.value != null ? '商品已更新' : '商品已发布'); closeGoodsDialog(); await loadMyGoods(); } catch (e) { ElMessage.error(err(e, '商品保存失败')); } finally { goodsSubmitting.value = false; } };
const submitWanted = async () => { const msg = !wantedForm.title.trim() ? '请输入求购标题' : !wantedForm.budget.trim() ? '请输入预算说明' : !wantedForm.category.trim() ? '请选择求购分类' : !wantedForm.campus.trim() ? '请选择校区' : !wantedForm.deadline.trim() ? '请选择截止日期' : !wantedForm.intro.trim() ? '请输入求购简介' : !wantedForm.description.trim() ? '请输入求购描述' : ''; if (msg) return ElMessage.warning(msg); wantedSubmitting.value = true; try { const payload: CreateWantedPayload = { title: wantedForm.title.trim(), budget: wantedForm.budget.trim(), category: wantedForm.category.trim(), campus: wantedForm.campus.trim(), deadline: wantedForm.deadline.trim(), intro: wantedForm.intro.trim(), description: wantedForm.description.trim(), tags: tags(wantedForm.tagsText), imageUrls: await resolveUrls(wantedFiles.value) }; wantedEditingId.value != null ? await updateWantedPost(wantedEditingId.value, payload) : await createWantedPost(payload); ElMessage.success(wantedEditingId.value != null ? '求购已更新' : '求购已发布'); closeWantedDialog(); await loadMyWanted(); } catch (e) { ElMessage.error(err(e, '求购保存失败')); } finally { wantedSubmitting.value = false; } };
const loadCategories = async () => { try { categories.value = (await getCategories()).sort((a, b) => a.sortNum - b.sortNum); } catch { categories.value = []; } };
const loadMyGoods = async () => { goodsLoading.value = true; goodsError.value = ''; try { goodsList.value = await getMyGoodsList(); } catch (e) { goodsError.value = err(e, '请稍后重试'); } finally { goodsLoading.value = false; } };
const loadMyWanted = async () => { wantedLoading.value = true; wantedError.value = ''; try { wantedList.value = await getMyWantedList(); } catch (e) { wantedError.value = err(e, '请稍后重试'); } finally { wantedLoading.value = false; } };
const refreshAll = async () => { refreshing.value = true; await Promise.allSettled([loadCategories(), loadMyGoods(), loadMyWanted()]); refreshing.value = false; };
const goGoodsDetail = (id: number) => router.push({ name: 'goods-detail', params: { id } });
const goWantedDetail = (id: number) => router.push({ name: 'wanted-detail', params: { id } });
const goodsAct = (id: number, type: string) => goodsActionId.value === id && goodsActionType.value === type;
const wantedAct = (id: number, type: string) => wantedActionId.value === id && wantedActionType.value === type;
const offGoods = async (item: GoodsItem) => { goodsActionId.value = item.id; goodsActionType.value = 'off'; try { await offShelfGoodsPost(item.id); ElMessage.success('商品已下架'); await loadMyGoods(); } catch (e) { ElMessage.error(err(e, '商品下架失败')); } finally { goodsActionId.value = null; goodsActionType.value = ''; } };
const relistGoods = async (item: GoodsItem) => { goodsActionId.value = item.id; goodsActionType.value = 'relist'; try { await relistGoodsPost(item.id); ElMessage.success('商品已重新上架'); await loadMyGoods(); } catch (e) { ElMessage.error(err(e, '商品重新上架失败')); } finally { goodsActionId.value = null; goodsActionType.value = ''; } };
const deleteGoods = async (item: GoodsItem) => { try { await ElMessageBox.confirm(`确认删除商品“${item.title}”吗？删除后不可恢复。`, '删除商品', { type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消' }); } catch { return; } goodsActionId.value = item.id; goodsActionType.value = 'delete'; try { await deleteGoodsPost(item.id); if (goodsEditingId.value === item.id) closeGoodsDialog(); ElMessage.success('商品已删除'); await loadMyGoods(); } catch (e) { ElMessage.error(err(e, '商品删除失败')); } finally { goodsActionId.value = null; goodsActionType.value = ''; } };
const closeWanted = async (item: WantedItem) => { wantedActionId.value = item.id; wantedActionType.value = 'close'; try { await closeWantedPost(item.id); ElMessage.success('求购已关闭'); await loadMyWanted(); } catch (e) { ElMessage.error(err(e, '求购关闭失败')); } finally { wantedActionId.value = null; wantedActionType.value = ''; } };
const reopenWanted = async (item: WantedItem) => { wantedActionId.value = item.id; wantedActionType.value = 'reopen'; try { await reopenWantedPost(item.id); ElMessage.success('求购已重新开放'); await loadMyWanted(); } catch (e) { ElMessage.error(err(e, '求购重新开放失败')); } finally { wantedActionId.value = null; wantedActionType.value = ''; } };
const deleteWantedItem = async (item: WantedItem) => { try { await ElMessageBox.confirm(`确认删除求购“${item.title}”吗？删除后不可恢复。`, '删除求购', { type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消' }); } catch { return; } wantedActionId.value = item.id; wantedActionType.value = 'delete'; try { await deleteWantedPost(item.id); if (wantedEditingId.value === item.id) closeWantedDialog(); ElMessage.success('求购已删除'); await loadMyWanted(); } catch (e) { ElMessage.error(err(e, '求购删除失败')); } finally { wantedActionId.value = null; wantedActionType.value = ''; } };
onMounted(refreshAll); onBeforeUnmount(() => { Array.from(previewMap.values()).forEach((url) => URL.revokeObjectURL(url)); previewMap.clear(); });
</script>

<style scoped lang="scss">
.posts-page{display:grid;gap:24px}.hero{display:flex;justify-content:space-between;gap:20px;padding:28px 30px;background:linear-gradient(135deg,#111 0%,#2c2c2c 55%,#f4b400 100%);border:1px solid rgba(255,210,102,.24)}.hero__eyebrow{display:inline-block;margin-bottom:10px;letter-spacing:.16em;font-size:12px;color:#ffe08a}.hero h1{margin:0;color:#fff;font-size:clamp(28px,3vw,38px)}.hero p{margin:12px 0 0;color:rgba(255,245,214,.88);line-height:1.8}.hero__actions{display:flex;flex-direction:column;align-items:flex-end;gap:12px}.stats .stat-card{padding:22px;border-color:rgba(245,180,0,.18)}.stat-card span{color:#8a6c25}.stat-card strong{display:block;margin-top:12px;font-size:30px;color:#111}.board{padding:24px}.board__head{display:flex;justify-content:space-between;gap:16px;align-items:flex-start;margin-bottom:18px}.board__head h3{margin:0;color:#111}.board__head p{margin:8px 0 0;color:#6b7280}.board__tags{display:flex;gap:10px;flex-wrap:wrap}.tabs :deep(.el-tabs__item.is-active){color:#111;font-weight:700}.tabs :deep(.el-tabs__active-bar){background:#f4b400}.toolbar{display:flex;flex-wrap:wrap;gap:12px;align-items:center;padding:14px;margin-bottom:14px;border:1px solid rgba(245,180,0,.14)}.toolbar__input{flex:1 1 320px}.toolbar__select{width:180px}.posts-table :deep(.el-table__header th){background:rgba(255,248,224,.95);color:#111}.title-cell strong{color:#111}.title-cell p{margin:6px 0 0;color:#6b7280;line-height:1.6}.dialog-form{display:grid;gap:12px}.form-grid{display:grid;gap:14px}.form-grid--2{grid-template-columns:repeat(2,minmax(0,1fr))}.form-grid--3{grid-template-columns:repeat(3,minmax(0,1fr))}.full-width,:deep(.full-width .el-input-number),:deep(.el-select),:deep(.el-date-editor){width:100%}.upload-trigger{display:flex;flex-direction:column;align-items:center;gap:4px;color:#8e5f05}.upload-trigger strong{font-size:24px;line-height:1}.preview-image{display:block;width:100%;max-height:72vh;object-fit:contain}@media (max-width:960px){.hero,.hero__actions,.board__head{flex-direction:column;align-items:flex-start}.form-grid--2,.form-grid--3{grid-template-columns:1fr}}
</style>
