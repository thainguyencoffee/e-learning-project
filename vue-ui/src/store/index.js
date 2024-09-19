import { createStore } from 'vuex';

import auth from "./auth.module.js";

const store = createStore({
    modules: {
        auth
    }
})


export default store;
