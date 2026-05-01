import { createApp } from 'vue';
import App from './App.vue';
import { pinia } from './stores';
import router from './router';
import { initGlobalErrorHandler } from './utils/errorHandler';
import 'element-plus/es/components/message/style/css';
import 'element-plus/es/components/message-box/style/css';
import './assets/styles/index.scss';

// 初始化全局错误处理
initGlobalErrorHandler();

const app = createApp(App);

app.use(pinia);
app.use(router);

app.mount('#app');
