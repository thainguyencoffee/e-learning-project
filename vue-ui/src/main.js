import { createApp } from 'vue'
import 'bootstrap/dist/css/bootstrap.css'
import 'bootstrap/dist/js/bootstrap.js'
import './style.css'
import App from './App.vue'
import router from "./router/index.js";
import store from "./store/index.js";
import '@fortawesome/fontawesome-free/css/all.css';

const app = createApp(App);

app
    .use(router)
    .use(store)
    .mount('#app')
