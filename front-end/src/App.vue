<template>
  <div data-theme="cupcake" class="flex flex-col justify-center items-center">
    <Navbar></Navbar>
    <div class="min-h-screen w-screen">
      <router-view />
    </div>
    <div class="w-screen flex flex-col items-center bg-base-200">
<!--      <Footer></Footer>-->
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { Navbar, Footer } from './components';
import { useMainStore } from "./stores";

const isLogin = ref<boolean>(useMainStore().useLoginStore().loginSession)
</script>

<style scoped>
</style>