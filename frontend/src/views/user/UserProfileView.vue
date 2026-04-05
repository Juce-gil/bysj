<template>
  <SectionCard title="账号设置" subtitle="已打通当前用户资料查询与保存接口，支持资料回填、编辑与保存。">
    <template #extra>
      <el-button text :loading="loading" @click="loadProfile">同步最新资料</el-button>
    </template>

    <el-alert
      type="success"
      show-icon
      :closable="false"
      title="当前已接入 /api/users/me 查询接口与 PUT /api/users/me 保存接口，修改昵称、手机号、QQ、个性签名后可直接保存。"
    />

    <el-skeleton v-if="loading" :rows="8" animated class="profile-skeleton" />
    <div v-else-if="errorMessage" class="state-panel qh-panel--subtle">
      <el-result icon="warning" title="账号资料加载失败" :sub-title="errorMessage">
        <template #extra>
          <el-button type="primary" @click="loadProfile">重新加载</el-button>
        </template>
      </el-result>
    </div>
    <div v-else class="profile-grid">
      <div class="profile-form qh-panel--subtle">
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
          <el-form-item label="昵称" prop="name">
            <el-input v-model="form.name" maxlength="50" show-word-limit placeholder="请输入昵称" />
          </el-form-item>
          <el-form-item label="学校">
            <el-input :model-value="form.school" disabled />
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="form.phone" maxlength="20" placeholder="请输入手机号" />
          </el-form-item>
          <el-form-item label="QQ" prop="qq">
            <el-input v-model="form.qq" maxlength="20" placeholder="请输入 QQ（可选）" />
          </el-form-item>
          <el-form-item label="个性签名" prop="slogan">
            <el-input v-model="form.slogan" type="textarea" :rows="4" maxlength="120" show-word-limit placeholder="写一句介绍自己的话（可选）" />
          </el-form-item>
          <div class="form-actions">
            <el-button :disabled="saving" @click="resetForm">恢复上次保存</el-button>
            <el-button type="primary" :loading="saving" :disabled="!hasChanges" @click="handleSave">保存设置</el-button>
          </div>
        </el-form>
      </div>

      <div class="profile-side">
        <div class="profile-summary qh-panel--subtle">
          <h3>账号概览</h3>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="当前昵称">{{ form.name || '未设置' }}</el-descriptions-item>
            <el-descriptions-item label="角色">{{ userStore.role === 'admin' ? '管理员' : '普通用户' }}</el-descriptions-item>
            <el-descriptions-item label="学校">{{ form.school || '科成' }}</el-descriptions-item>
            <el-descriptions-item label="手机号">{{ form.phone || '暂未提供' }}</el-descriptions-item>
            <el-descriptions-item label="QQ">{{ form.qq || '暂未提供' }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="profile-tips qh-panel--subtle">
          <h3>接口状态</h3>
          <ul>
            <li><strong>已接入：</strong>当前用户信息查询 /api/users/me</li>
            <li><strong>已接入：</strong>当前用户资料保存 PUT /api/users/me</li>
            <li><strong>后续可扩展：</strong>头像上传与联系方式可见性配置</li>
          </ul>
        </div>
      </div>
    </div>
  </SectionCard>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';
import SectionCard from '@/components/SectionCard.vue';
import { useUserStore } from '@/stores/user';

const userStore = useUserStore();
const formRef = ref<FormInstance>();
const loading = ref(false);
const saving = ref(false);
const errorMessage = ref('');
const form = reactive({ name: '', school: '', phone: '', qq: '', slogan: '' });
const snapshot = reactive({ name: '', school: '', phone: '', qq: '', slogan: '' });

const normalize = (value: string) => value.trim();

const hasChanges = computed(() => (
  normalize(form.name) !== snapshot.name
  || normalize(form.phone) !== snapshot.phone
  || normalize(form.qq) !== snapshot.qq
  || normalize(form.slogan) !== snapshot.slogan
));

const rules: FormRules = {
  name: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 50, message: '昵称长度为 2 到 50 个字符', trigger: 'blur' },
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^[0-9+\-() ]{6,20}$/, message: '请输入正确的手机号格式', trigger: 'blur' },
  ],
  qq: [
    { pattern: /^$|^[1-9][0-9]{4,19}$/, message: '请输入正确的 QQ 号码', trigger: 'blur' },
  ],
  slogan: [
    { max: 120, message: '个性签名不能超过 120 个字符', trigger: 'blur' },
  ],
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
  form.school = userStore.profile?.school || '科成';
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
    console.error('加载账号资料失败', error);
    errorMessage.value = '账号资料接口暂时不可用，请稍后重试。';
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
    ElMessage.info('当前没有需要保存的改动');
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
    ElMessage.success('账号资料已保存');
  } catch (error) {
    console.error('保存账号资料失败', error);
  } finally {
    saving.value = false;
  }
};

onMounted(loadProfile);
</script>

<style scoped lang="scss">
.profile-skeleton { margin-top: 18px; }
.profile-grid { margin-top: 18px; display: grid; grid-template-columns: 1.1fr 0.9fr; gap: 18px; }
.profile-form { padding: 20px; }
.form-actions { display: flex; gap: 12px; justify-content: flex-end; }
.profile-side { display: grid; gap: 18px; }
.profile-summary, .profile-tips { padding: 20px; }
.profile-summary h3, .profile-tips h3 { margin: 0 0 16px; color: var(--qh-text-primary); }
.profile-tips ul { margin: 0; padding-left: 18px; line-height: 1.9; color: var(--qh-text-secondary); }
.state-panel { margin-top: 18px; padding: 12px; }
@media (max-width: 960px) {
  .profile-grid { grid-template-columns: 1fr; }
  .form-actions { flex-wrap: wrap; }
}
</style>