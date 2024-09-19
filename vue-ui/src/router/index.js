import {createRouter, createWebHistory} from "vue-router";
import HomePage from "../views/HomePage.vue";
import ProfilePage from "../views/ProfilePage.vue";

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {path: '/', name: 'home', component: HomePage},
        {path: '/profile', name: 'profile', component: ProfilePage}
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