<template>
<div class="navbar bg-base-100 shadow-md">
  <div class="flex-1">
    <a class="btn btn-ghost text-xl">基于教考分离的考试系统</a>
  </div>
  <div class="flex-none">
    <ul v-if="userType=='1'" class="menu menu-horizontal px-1 text-base">
      <li><a href="/teacher/question">题库管理</a></li>
      <li><a>试卷管理</a></li>
    </ul>
  </div>
  <div v-if="userType" class="dropdown dropdown-end">
    <div tabindex="0" role="button" class="btn btn-ghost btn-circle avatar">
      <div class="w-10 rounded-full">
        <img
            alt="Tailwind CSS Navbar component"
            src="https://img.daisyui.com/images/stock/photo-1534528741775-53994a69daeb.webp" />
      </div>
    </div>
    <ul
        tabindex="0"
        class="menu menu-sm dropdown-content bg-base-100 rounded-box z-[1] mt-3 w-52 p-2 shadow">
      <li>
        <a class="justify-between">
          Profile
          <span class="badge">New</span>
        </a>
      </li>
      <li><a>Settings</a></li>
      <li><a @click="logout">Logout</a></li>
    </ul>
  </div>
</div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { useMainStore } from "../../stores";
import router from "../../routers";

const userType = ref<string>(localStorage.getItem("userType"))

const logout = () => {
  localStorage.removeItem("token")
  localStorage.removeItem("userType")
  useMainStore().useLoginStore().setLogin(false)
  router.push('/login').then(()=>{window.location.reload();})
}
</script>
