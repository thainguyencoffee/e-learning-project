import {createRouter, createWebHistory} from "vue-router";
import HomePage from "../views/HomePage.vue";

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {path: '/', component: HomePage},
    ],
    scrollBehavior(_,
                   _1,
                   savedPosition) {
        if (savedPosition) {
            return savedPosition
        }
        return {top: 0, left: 0}
    }
})

export default router