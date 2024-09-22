const state = {
    user: {},
    isLoggedIn: false,
    refreshIntervalId: null
}

const getters = {
    isLoggedIn(state) {
        return state.isLoggedIn;
    },
    user(state) {
        return state.user;
    }
}

const mutations = {
    setIsLoggedIn(state, payload) {
        state.isLoggedIn = payload.isLoggedIn;
    },
    setUser(state, payload) {
        state.user = payload.user;
    },
    setRefreshIntervalId(state, payload) {
        state.refreshIntervalId = payload.refreshIntervalId;
    }
}

const actions = {
    async refreshAuth(context) {
        if (context.state.refreshIntervalId) {
            clearInterval(context.state.refreshIntervalId);
        }
        try {
            const response = await fetch("/bff/api/profile");
            const user = await response.json();
            if (user.username) {
                context.commit("setUser", { user });
                context.commit("setIsLoggedIn", { isLoggedIn: true });
            }
            if (user.exp) {
                const now = new Date().getTime();
                const delay = ((user.exp * 1000) - now) * 0.8;
                if (delay > 2000) {
                    const refreshIntervalId = setInterval(() => {
                        context.dispatch("refreshAuth");
                    }, delay);
                    context.commit("setRefreshIntervalId", { refreshIntervalId });
                }
            }
        } catch (error) {
            console.error("Failed to refresh auth:", error);
        }
    }
}

export default {
    namespaced: true,
    state,
    getters,
    mutations,
    actions
}