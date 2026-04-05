<template>
  <div class="auth-page register-page">
    <section class="auth-form qh-panel">
      <div class="page-heading">
        <h1>创建新账号</h1>
        <p>当前已接入基础注册接口，注册后可直接登录体验用户端流程。</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent>
        <el-form-item label="用户名" prop="username"><el-input v-model="form.username" placeholder="建议使用学号或常用昵称" /></el-form-item>
        <el-form-item label="昵称" prop="nickname"><el-input v-model="form.nickname" placeholder="请输入展示昵称" /></el-form-item>
        <el-form-item label="密码" prop="password"><el-input v-model="form.password" type="password" show-password placeholder="请输入密码" /></el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword"><el-input v-model="form.confirmPassword" type="password" show-password placeholder="请再次输入密码" /></el-form-item>
        <el-form-item><el-button type="primary" class="submit-button" @click="handleSubmit">完成注册</el-button></el-form-item>
      </el-form>
      <p class="subtle-text">已有账号？<RouterLink to="/login">去登录</RouterLink></p>
    </section>

    <section class="auth-intro qh-panel">
      <p class="eyebrow">注册说明</p>
      <h2>为后续完整认证流程预留位置</h2>
      <div class="tip-list">
        <div class="tip-item qh-panel--subtle"><strong>账号体系</strong><p>后续优先对接：注册、登录、找回密码、角色区分。</p></div>
        <div class="tip-item qh-panel--subtle"><strong>用户资料</strong><p>后续优先对接：头像、学院、联系方式、信用分。</p></div>
        <div class="tip-item qh-panel--subtle"><strong>校园认证</strong><p>后续可增加学号、邮箱或统一身份认证绑定流程。</p></div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import { RouterLink, useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';

const router = useRouter();
const userStore = useUserStore();
const formRef = ref<FormInstance>();
const form = reactive({ username: '', nickname: '', password: '', confirmPassword: '' });
const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: (_rule, value, callback) => { if (value !== form.password) return callback(new Error('两次输入的密码不一致')); callback(); }, trigger: 'blur' },
  ],
};
const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) return;
  const result = await userStore.register({ ...form });
  ElMessage.success(result.message);
  router.push('/login');
};
</script>

<style scoped lang="scss">
.register-page { display: grid; grid-template-columns: 0.95fr 1.05fr; gap: 24px; }
.auth-form, .auth-intro { padding: 32px; }
.tip-list { display: grid; gap: 16px; margin-top: 24px; }
.tip-item { padding: 20px; }
.tip-item strong { display: block; margin-bottom: 8px; color: var(--qh-text-primary); }
.tip-item p { margin: 0; color: var(--qh-text-secondary); line-height: 1.7; }
.eyebrow { margin: 0; color: var(--qh-primary-deep); letter-spacing: 0.08em; font-size: 13px; }
.submit-button { width: 100%; }
@media (max-width: 960px) { .register-page { grid-template-columns: 1fr; } }
</style>

