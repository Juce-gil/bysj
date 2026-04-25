import { createApp } from 'vue';
import App from './App.vue';
import { pinia } from './stores';
import router from './router';
import 'element-plus/es/components/message/style/css';
import 'element-plus/es/components/message-box/style/css';
import './assets/styles/index.scss';

const app = createApp(App);

app.use(pinia);
app.use(router);

app.mount('#app');
