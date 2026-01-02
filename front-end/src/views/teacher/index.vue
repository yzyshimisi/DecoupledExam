<template>
  <div>
    <div class="p-4">
      <router-view />
    </div>
  </div>
</template>

<script setup lang="ts">
// 退出登录逻辑
const logout = () => {
  localStorage.removeItem("token");
  localStorage.removeItem("userType");
  // 这里可以触发store的登录状态更新
  // router.push('/login')
}
</script>