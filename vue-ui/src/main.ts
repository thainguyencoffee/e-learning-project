import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import router from "./router";
import store from "./store";
import 'bootstrap/dist/css/bootstrap.css'
import 'bootstrap/dist/js/bootstrap.js'

const app = createApp(App);
app
    .use(router)
    .use(store)
    .mount('#app')
