<template>
  <nav class="navbar navbar-expand-lg bg-body-tertiary">
    <div class="container-fluid">
      <router-link class="navbar-brand" :to="{name: 'home'}">Home</router-link>
      <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNavAltMarkup"
              aria-controls="navbarNavAltMarkup" aria-expanded="false" aria-label="Toggle navigation">
        <span class="navbar-toggler-icon"></span>
      </button>
      <div class="collapse navbar-collapse" id="navbarNavDropdown">
        <ul class="navbar-nav">
          <li class="nav-item">
            <a class="nav-link active" aria-current="page" href="#">Home</a>
          </li>
          <li class="nav-item">
            <a class="nav-link" href="#">Features</a>
          </li>
          <li class="nav-item" v-if="!isLoggedIn">
            <a class="nav-link" @click="login" role="button">Login</a>
          </li>
          <li class="nav-item dropdown" v-if="isLoggedIn">
            <a class="nav-link dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown" aria-expanded="false">
              {{user.firstName}} {{user.lastName}}
            </a>
            <ul class="dropdown-menu">
              <li><router-link class="dropdown-item" :to="{name: 'profile'}">Profile</router-link></li>
              <li><button class="dropdown-item" @click="logout(cookies.get('XSRF-TOKEN'))">Logout</button></li>
              <li><a class="dropdown-item" href="#">Something else here</a></li>
            </ul>
          </li>
        </ul>
      </div>
    </div>
  </nav>
</template>

<script setup>
import {useStore} from "vuex";
import {computed, onMounted, ref} from "vue";
import {useRoute} from "vue-router";
import {useCookies} from "vue3-cookies";

const store = useStore()
const {cookies} = useCookies()

const isLoggedIn = computed(() => store.getters["auth/isLoggedIn"])
const user = computed(() => store.getters["auth/user"])

onMounted(() => {
  store.dispatch("auth/authenticate");
});

function login() {
  window.open("/oauth2/authorization/keycloak", "_self");
}

function logout(xsrfToken) {
  if (!xsrfToken) {
    console.error("XSRF-TOKEN is missing.");
    return;
  }
  fetch("/bff/logout", {
    method: "POST",
    headers: {
      "X-XSRF-TOKEN": xsrfToken,
      "X-POST-LOGOUT-SUCCESS-URI": `${import.meta.env.VITE_REVERSE_PROXY}${import.meta.env.BASE_URL}`
    }
  }).then(response => {
    const location = response.headers.get("Location")
    if (location) {
      window.location.href = location
    }
  })
}
</script>

<style scoped>
</style>
