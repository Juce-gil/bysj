<template>
  <div class="auth-page">
    <section class="auth-intro qh-panel">
      <p class="eyebrow">欢迎回来</p>
      <h1>登录校园跳蚤市场</h1>
      <p class="desc">在科成继续一次高效、安心的闲置交换，也让每件物品都能找到下一位主人。</p>
      <ul>
        <li>支持进入前台、用户中心与后台管理三套视图</li>
        <li>当前已接入真实后端登录接口，可直接联调</li>
        <li>可使用 admin / 123456 体验管理员，或 zhangsan / 123456 体验普通用户</li>
      </ul>
    </section>

    <section class="auth-form qh-panel">
      <div class="page-heading">
        <h2>账号登录</h2>
        <p>登录后可访问收藏、预约、资料设置与后台骨架页面。</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名或学号，例如：admin 或 zhangsan" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="submit-button" @click="handleSubmit">立即登录</el-button>
          <el-button text @click="fillUserDemo">填入普通用户示例</el-button>
          <el-button text @click="fillAdminDemo">填入管理员示例</el-button>
        </el-form-item>
      </el-form>
      <p class="subtle-text">还没有账号？<RouterLink to="/register">去注册</RouterLink></p>
    </section>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import { RouterLink, useRoute, useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();
const formRef = ref<FormInstance>();
const form = reactive({ username: '', password: '' });
const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
};
const fillUserDemo = () => { form.username = 'zhangsan'; form.password = '123456'; };
const fillAdminDemo = () => { form.username = 'admin'; form.password = '123456'; };
const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) return;
  const result = await userStore.login({ ...form });
  ElMessage.success(`欢迎回来，${result.profile.name}`);
  const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '';
  if (redirect) return router.push(redirect);
  router.push(result.profile.role === 'admin' ? '/admin/dashboard' : '/user/dashboard');
};
</script>

<style scoped lang="scss">
.auth-page { display: grid; grid-template-columns: 1.05fr 0.95fr; gap: 24px; align-items: stretch; min-height: calc(100vh - 220px); }
.auth-intro, .auth-form { padding: 32px; }
.auth-intro { background: linear-gradient(135deg, rgba(241, 249, 255, 0.96) 0%, rgba(218, 237, 255, 0.92) 100%); }
.auth-intro h1 { margin: 10px 0 16px; font-size: 36px; color: var(--qh-text-primary); }
.auth-intro .desc { color: var(--qh-text-secondary); line-height: 1.8; }
.auth-intro ul { margin: 24px 0 0; padding-left: 18px; color: var(--qh-text-regular); line-height: 1.9; }
.auth-form { display: flex; flex-direction: column; justify-content: center; }
.eyebrow { margin: 0; color: var(--qh-primary-deep); letter-spacing: 0.08em; font-size: 13px; }
.submit-button { width: 100%; margin-right: 12px; }
@media (max-width: 960px) { .auth-page { grid-template-columns: 1fr; min-height: auto; } }
</style>
