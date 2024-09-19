<template>
  <div class="container">
    <h3 class="text-center">Hi {{user.fullName}}! Welcome to <span class="fw-bold">profile page</span></h3>
  </div>
</template>

<script setup>
import {useStore} from "vuex";
import {computed, onMounted} from "vue";

const store = useStore()

const user = computed(() => store.getters["auth/user"])
onMounted(() => {
  store.dispatch("auth/refreshAuth")
})

</script>

<style scoped>

</style>