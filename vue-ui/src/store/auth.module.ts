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
    }
}

const actions = {
    authenticate(context) {
        fetch('/user')
            .then(response => response.json())
            .then(user => {
                if (user.username) {
                    context.commit('setUser', {user});
                    context.commit('setIsLoggedIn', {isLoggedIn: true});
                }else {
                    context.commit('setUser', {});
                    context.commit('setIsLoggedIn', {isLoggedIn: false});
                }
            })
    },
}

export default {
    namespaced: true,
    state,
    getters,
    mutations,
    actions
}