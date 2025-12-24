<template>
  <div class="min-h-screen bg-base-200">
    <!-- 顶部导航栏 -->
    <div class="navbar bg-base-100 shadow-md">
      <div class="flex-1">
        <a class="btn btn-ghost text-xl">管理员面板</a>
      </div>
      <div class="flex-none">
        <div class="dropdown dropdown-end">
          <div tabindex="0" role="button" class="btn btn-ghost btn-circle avatar">
            <div class="w-10 rounded-full">
              <img alt="头像" src="https://daisyui.com/images/stock/photo-1534528741775-53994a69daeb.jpg" />
            </div>
          </div>
          <ul tabindex="0" class="mt-3 z-[1] p-2 shadow menu menu-sm dropdown-content bg-base-100 rounded-box w-52">
            <li>
              <router-link to="/admin/profile">
                <span>个人资料</span>
              </router-link>
            </li>
            <li>
              <a @click="logout">退出登录</a>
            </li>
          </ul>
        </div>
      </div>
    </div>

    <!-- 主内容区域 -->
    <div class="flex">
      <!-- 侧边栏 -->
      <div class="drawer-side z-0 w-64 bg-base-100 min-h-screen">
        <div class="menu p-4 text-base-content">
          <h2 class="text-lg font-semibold mb-4">功能导航</h2>
          
          <ul class="space-y-2">
            <li>
              <router-link 
                to="/admin" 
                class="flex items-center p-2 rounded-lg hover:bg-base-300"
                active-class="bg-primary text-white"
              >
                <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2H5a2 2 0 00-2-2z"></path>
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 5a2 2 0 012-2h4a2 2 0 012 2v6H8V5z"></path>
                </svg>
                <span>仪表盘</span>
              </router-link>
            </li>
            <li>
              <router-link 
                to="/admin/simulate-login" 
                class="flex items-center p-2 rounded-lg hover:bg-base-300"
                active-class="bg-primary text-white"
              >
                <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path>
                </svg>
                <span>模拟用户登录</span>
              </router-link>
            </li>
            <li>
              <details class="group">
                <summary class="flex items-center p-2 rounded-lg hover:bg-base-300 cursor-pointer">
                  <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197m13.5-9a2.5 2.5 0 11-5 0 2.5 2.5 0 015 0z"></path>
                  </svg>
                  <span>用户管理</span>
                </summary>
                <ul class="p-2 ml-6 space-y-1">
                  <li>
                    <router-link 
                      to="/admin/user-management/students" 
                      class="flex items-center p-2 rounded-lg hover:bg-base-300"
                      active-class="bg-primary text-white"
                    >
                      <span>学生用户管理</span>
                    </router-link>
                  </li>
                  <li>
                    <router-link 
                      to="/admin/user-management/teachers" 
                      class="flex items-center p-2 rounded-lg hover:bg-base-300"
                      active-class="bg-primary text-white"
                    >
                      <span>教师用户管理</span>
                    </router-link>
                  </li>
                </ul>
              </details>
            </li>
            <li>
              <router-link 
                to="/admin/profile" 
                class="flex items-center p-2 rounded-lg hover:bg-base-300"
                active-class="bg-primary text-white"
              >
                <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path>
                </svg>
                <span>个人信息管理</span>
              </router-link>
            </li>
          </ul>
        </div>
      </div>

      <!-- 主内容区域 -->
      <div class="flex-1 p-6">
        <div class="bg-base-100 rounded-lg shadow min-h-[calc(100vh-140px)]">
          <router-view />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import { useMainStore } from '../../stores';
import { storeToRefs } from 'pinia';
import router from '../../routers';

const logout = () => {
  // 清除登录状态
  const store = useMainStore();
  const { useLoginStore } = store;
  useLoginStore().setLogin(false);
  
  // 清除本地存储的token
  localStorage.removeItem('token');
  
  // 跳转到登录页
  router.push('/login');
};

onMounted(() => {
  // 检查用户是否已登录
  const store = useMainStore();
  const { useLoginStore } = store;
  const { loginSession } = storeToRefs(useLoginStore());

  if (!loginSession.value) {
    router.push('/login');
  }
});
</script>

<style scoped>
.router-link-active {
  background-color: var(--fallback-p, oklch(0.78039 0.19907 274.37));
  color: white;
}
</style>