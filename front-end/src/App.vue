<template>
  <div data-theme="cupcake" class="flex flex-col justify-center items-center">
    <Navbar v-if="!hideNavbar"></Navbar>
    <div class="min-h-screen w-screen">
      <router-view />
    </div>
    <div class="w-screen flex flex-col items-center bg-base-200">
      <!--      <Footer></Footer>-->
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from "vue";
import { Navbar, Footer } from './components';
import { useMainStore } from "./stores";
import {useRoute} from "vue-router";

const isLogin = ref<boolean>(useMainStore().useLoginStore().loginSession)

const route = useRoute()

const hideNavbar = computed(() => {

  return route.path.startsWith('/student/exam-page/')
})

</script>

<style scoped>
</style>