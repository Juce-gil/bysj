<template>
  <div class="profile-page">
    <section class="grid-cards grid-cards--4 page-section">
      <article v-for="item in stats" :key="item.label" class="stat-card qh-panel">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
        <p>{{ item.tip }}</p>
      </article>
    </section>

    <SectionCard title="账号设置" subtitle="统一维护个人资料、联系方式与个性签名，展示真实接口读写后的用户信息状态。">
      <template #extra>
        <el-button text :loading="loading" @click="loadProfile">重新加载</el-button>
      </template>

      <el-alert
        type="success"
        show-icon
        :closable="false"
        title="当前页面已联调 /api/users/me 查询接口与 PUT /api/users/me 更新接口，可直接维护电话、QQ 和个性签名。"
      />

      <div class="profile-summary-bar qh-panel--subtle">
        <article>
          <span>当前角色</span>
          <strong>{{ userStore.role === 'admin' ? '管理员' : '普通用户' }}</strong>
          <p>展示当前登录账号的身份状态。</p>
        </article>
        <article>
          <span>编辑状态</span>
          <strong>{{ hasChanges ? '待保存' : '已同步' }}</strong>
          <p>{{ hasChanges ? '你有尚未保存的资料修改。' : '当前表单内容已与服务器同步。' }}</p>
        </article>
        <article>
          <span>所属学校</span>
          <strong>{{ form.school || '未设置学校' }}</strong>
          <p>学校字段来自账号资料，可用于前后台统一展示。</p>
        </article>
      </div>

      <el-skeleton v-if="loading" :rows="8" animated class="profile-skeleton" />
      <div v-else-if="errorMessage" class="state-panel qh-panel--subtle">
        <el-result icon="warning" title="资料加载失败" :sub-title="errorMessage">
          <template #extra>
            <el-button type="primary" @click="loadProfile">重新加载</el-button>
          </template>
        </el-result>
      </div>
      <div v-else class="profile-grid">
        <div class="profile-form qh-panel--subtle">
          <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="form.name" maxlength="50" show-word-limit placeholder="请输入姓名" />
            </el-form-item>
            <el-form-item label="学校">
              <el-input :model-value="form.school" disabled />
            </el-form-item>
            <el-form-item label="电话" prop="phone">
              <el-input v-model="form.phone" maxlength="20" placeholder="请输入联系电话" />
            </el-form-item>
            <el-form-item label="QQ" prop="qq">
              <el-input v-model="form.qq" maxlength="20" placeholder="可选，用于补充 QQ 联系方式" />
            </el-form-item>
            <el-form-item label="个性签名" prop="slogan">
              <el-input
                v-model="form.slogan"
                type="textarea"
                :rows="4"
                maxlength="120"
                show-word-limit
                placeholder="写一句简短介绍，方便别人更快认识你"
              />
            </el-form-item>
            <div class="form-actions">
              <el-button :disabled="saving" @click="resetForm">恢复上次加载</el-button>
              <el-button type="primary" :loading="saving" :disabled="!hasChanges" @click="handleSave">保存修改</el-button>
            </div>
          </el-form>
        </div>

        <div class="profile-side">
          <div class="profile-card qh-panel--subtle">
            <span class="profile-card__label">资料预览</span>
            <strong>{{ form.name || '未填写姓名' }}</strong>
            <p>{{ form.school || '未设置学校' }} · {{ userStore.role === 'admin' ? '管理员' : '普通用户' }}</p>
            <small>{{ form.slogan || '还没有填写个性签名。' }}</small>
          </div>

          <div class="profile-summary qh-panel--subtle">
            <h3>资料摘要</h3>
            <el-descriptions :column="1" border>
              <el-descriptions-item label="显示名称">{{ form.name || '未填写' }}</el-descriptions-item>
              <el-descriptions-item label="角色">{{ userStore.role === 'admin' ? '管理员' : '普通用户' }}</el-descriptions-item>
              <el-descriptions-item label="学校">{{ form.school || '未设置学校' }}</el-descriptions-item>
              <el-descriptions-item label="电话">{{ form.phone || '未填写' }}</el-descriptions-item>
              <el-descriptions-item label="QQ">{{ form.qq || '未填写' }}</el-descriptions-item>
            </el-descriptions>
          </div>

          <div class="profile-tips qh-panel--subtle">
            <h3>接口说明</h3>
            <ul>
              <li><strong>读取资料：</strong>进入页面时调用 <code>/api/users/me</code> 获取当前账号数据。</li>
              <li><strong>更新资料：</strong>点击保存后调用 <code>PUT /api/users/me</code> 提交修改内容。</li>
              <li><strong>联系方式：</strong>电话和 QQ 可用于商品交易与求购沟通展示。</li>
            </ul>
          </div>
        </div>
      </div>
    </SectionCard>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';
import SectionCard from '@/components/SectionCard.vue';
import { useUserStore } from '@/stores/user';
import { extractErrorMessage } from '@/utils/error';

const userStore = useUserStore();
const formRef = ref<FormInstance>();
const loading = ref(false);
const saving = ref(false);
const errorMessage = ref('');
const form = reactive({ name: '', school: '', phone: '', qq: '', slogan: '' });
const snapshot = reactive({ name: '', school: '', phone: '', qq: '', slogan: '' });

const normalize = (value: string) => value.trim();

const hasChanges = computed(
  () =>
    normalize(form.name) !== snapshot.name ||
    normalize(form.phone) !== snapshot.phone ||
    normalize(form.qq) !== snapshot.qq ||
    normalize(form.slogan) !== snapshot.slogan,
);

const stats = computed(() => [
  { label: '账号角色', value: userStore.role === 'admin' ? 'AD' : 'US', tip: '支持前后台角色切换展示' },
  { label: '所属学校', value: form.school || '未设置', tip: '展示当前账号绑定学校' },
  { label: '同步状态', value: hasChanges.value ? 'EDIT' : 'SYNC', tip: hasChanges.value ? '存在待保存修改' : '表单内容已同步' },
  { label: '联系方式', value: form.phone ? '已填写' : '待完善', tip: '电话和 QQ 可用于交易联系' },
]);

const rules: FormRules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 50, message: '姓名长度需在 2 到 50 个字符之间', trigger: 'blur' },
  ],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^[0-9+\-() ]{6,20}$/, message: '联系电话格式不正确', trigger: 'blur' },
  ],
  qq: [{ pattern: /^$|^[1-9][0-9]{4,19}$/, message: '请输入正确的 QQ 号码', trigger: 'blur' }],
  slogan: [{ max: 120, message: '个性签名不能超过 120 个字符', trigger: 'blur' }],
};

const syncSnapshot = () => {
  snapshot.name = normalize(form.name);
  snapshot.school = form.school;
  snapshot.phone = normalize(form.phone);
  snapshot.qq = normalize(form.qq);
  snapshot.slogan = normalize(form.slogan);
};

const fillForm = () => {
  form.name = userStore.profile?.name || '';
  form.school = userStore.profile?.school || '未设置学校';
  form.phone = userStore.profile?.phone || '';
  form.qq = userStore.profile?.qq || '';
  form.slogan = userStore.profile?.slogan || '';
  syncSnapshot();
};

const loadProfile = async () => {
  loading.value = true;
  errorMessage.value = '';
  try {
    await userStore.refreshProfile();
    fillForm();
    formRef.value?.clearValidate();
  } catch (error) {
    console.error('加载用户资料失败', error);
    errorMessage.value = extractErrorMessage(error, '用户资料暂时无法加载，请稍后重试。');
  } finally {
    loading.value = false;
  }
};

const resetForm = () => {
  form.name = snapshot.name;
  form.school = snapshot.school;
  form.phone = snapshot.phone;
  form.qq = snapshot.qq;
  form.slogan = snapshot.slogan;
  formRef.value?.clearValidate();
};

const handleSave = async () => {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) {
    return;
  }

  if (!hasChanges.value) {
    ElMessage.info('当前没有需要保存的修改');
    return;
  }

  saving.value = true;
  try {
    await userStore.updateProfile({
      name: normalize(form.name),
      phone: normalize(form.phone),
      qq: normalize(form.qq),
      slogan: normalize(form.slogan),
    });
    fillForm();
    formRef.value?.clearValidate();
    ElMessage.success('个人资料已更新');
  } catch (error) {
    console.error('保存用户资料失败', error);
    ElMessage.error(extractErrorMessage(error, '保存失败，请稍后重试'));
  } finally {
    saving.value = false;
  }
};

onMounted(loadProfile);
</script>

<style scoped lang="scss">
.stat-card { padding: 22px; }
.stat-card span { color: var(--qh-text-secondary); }
.stat-card strong { display: block; margin: 14px 0 10px; font-size: 30px; color: var(--qh-primary-deep); }
.stat-card p { margin: 0; color: var(--qh-text-secondary); }
.profile-summary-bar {
  margin: 18px 0 0;
  padding: 16px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}
.profile-summary-bar article {
  padding: 16px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.78);
}
.profile-summary-bar span,
.profile-card__label { display: block; color: var(--qh-text-secondary); }
.profile-summary-bar strong,
.profile-card strong { display: block; margin-top: 10px; color: var(--qh-text-primary); font-size: 22px; }
.profile-summary-bar p,
.profile-card p,
.profile-card small { margin: 10px 0 0; color: var(--qh-text-secondary); line-height: 1.7; display: block; }
.profile-skeleton { margin-top: 18px; }
.profile-grid { margin-top: 18px; display: grid; grid-template-columns: 1.1fr 0.9fr; gap: 18px; }
.profile-form { padding: 20px; }
.form-actions { display: flex; gap: 12px; justify-content: flex-end; }
.profile-side { display: grid; gap: 18px; }
.profile-card,
.profile-summary,
.profile-tips { padding: 20px; }
.profile-summary h3,
.profile-tips h3 { margin: 0 0 16px; color: var(--qh-text-primary); }
.profile-tips ul { margin: 0; padding-left: 18px; line-height: 1.9; color: var(--qh-text-secondary); }
.profile-tips code {
  padding: 2px 6px;
  border-radius: 8px;
  background: rgba(216, 236, 255, 0.52);
  color: var(--qh-text-primary);
}
.state-panel { margin-top: 18px; padding: 12px; }

@media (max-width: 1100px) {
  .profile-summary-bar { grid-template-columns: 1fr; }
}

@media (max-width: 960px) {
  .profile-grid { grid-template-columns: 1fr; }
  .form-actions { flex-wrap: wrap; }
}
</style>
