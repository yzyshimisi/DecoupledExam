<template>
<div class="navbar bg-base-100 shadow-md">
  <div class="flex-1">
    <a class="btn btn-ghost text-xl">基于教考分离的考试系统</a>
  </div>
  <div class="flex-none">
    <ul v-if="userType=='1'" class="menu menu-horizontal px-1 text-base">
      <li><router-link to="/teacher/courses">课程管理</router-link></li>
      <li><router-link to="/teacher/question">题库管理</router-link></li>
      <li><router-link to="/teacher/exam-paper">试卷管理</router-link></li>
      <li><router-link to="/exam">考试管理</router-link></li>
    </ul>
    <ul v-else-if="userType=='0'" class="menu menu-horizontal px-1 text-base">
      <li><router-link to="/admin/subject">学科管理</router-link></li>
      <li><a href="/admin/teachers">教师管理</a></li>
      <li><a href="/exam">考试管理</a></li>
      <li><router-link to="/teacher/question">题库管理</router-link></li>
      <li><router-link to="/teacher/exam-paper">试卷管理</router-link></li>
    </ul>
    <ul v-else-if="userType=='2'" class="menu menu-horizontal px-1 text-base">
      <li><a href="/exam">我的考试</a></li>
      <li><a href="/student/notifications">考试通知</a></li>
      <li><a href="/student/courses/join">加入课程</a></li>
    </ul>
    <ul v-if="userType=='0'" class="menu menu-horizontal px-1 text-base">
      <li><router-link to="/admin/logs">日志管理</router-link></li>
    </ul>
  </div>
  <div v-if="userType" class="dropdown dropdown-end">
    <div tabindex="0" role="button" class="btn btn-ghost btn-circle avatar">
      <div class="w-10 rounded-full">
        <img
            alt="用户头像"
            :src="userAvatar || defaultAvatar" />
      </div>
    </div>
    <ul
        tabindex="0"
        class="menu menu-sm dropdown-content bg-base-100 rounded-box z-[1] mt-3 w-52 p-2 shadow">
      <li>
        <a class="justify-between">
          个人资料
          <span class="badge">New</span>
        </a>
        <a @click="routeToHome">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 10.5L12 4l9 6.5V20a1 1 0 01-1 1h-4v-6H8v6H4a1 1 0 01-1-1V10.5z" />
          </svg>
          <span>主页</span>
        </a>
      </li>
      <li>
        <router-link to="/profile">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
          </svg>
          <span>个人资料</span>
        </router-link>
      </li>
      <li>
        <a>
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11.049 2.927c.3-.921 1.603-.921 1.902 0a1.724 1.724 0 002.461 1.049c.88-.51 1.985.264 1.648 1.22a1.724 1.724 0 001.316 2.145c.966.327 1.43 1.77.725 2.482a1.724 1.724 0 00-.347 1.921c.36.826-.414 1.93-1.37 1.62a1.724 1.724 0 00-1.89.63c-.47.663-1.538.663-2.008 0a1.724 1.724 0 00-1.89-.63c-.956.31-1.73-.794-1.37-1.62.312-.715-.005-1.574-.347-1.921-.705-.712-.241-2.155.725-2.482a1.724 1.724 0 001.316-2.145c-.337-.956.768-1.73 1.648-1.22.87.503 2.066-.128 2.461-1.049z" />
          </svg>
          <span>设置</span>
        </a>
      </li>
      <li>
        <a @click="logout">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a2 2 0 01-2 2H5a2 2 0 01-2-2V7a2 2 0 012-2h6a2 2 0 012 2v1" />
          </svg>
          <span>退出登录</span>
        </a>
      </li>
    </ul>
  </div>
</div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useMainStore } from "../../stores";
import router from "../../routers";
import { getAvatarAPI, getUserInfoByIdAPI } from "../../apis/Server/userAPI";
import { jwtDecode } from 'jwt-decode';

const userType = ref<string>(localStorage.getItem("userType") || "");
const userAvatar = ref<string | null>(null);
const defaultAvatar = 'https://daisyui.com/images/stock/photo-1534528741775-53994a69daeb.jpg';

const loadUserAvatar = async () => {
  const token = localStorage.getItem('token');
  if (!token) return;
  
  try {
    // 从token中解析用户ID来获取用户信息
    const decoded: any = jwtDecode(token);
    const userId = decoded.id; // 根据您提供的JWT，用户ID存储在id字段中
    
    // 获取用户信息，其中包含头像URL
    const response: any = await getUserInfoByIdAPI(userId, token);
    if (response && response.code === 200 && response.data) {
      const userData = response.data;
      
      // 如果用户有头像URL，直接使用
      if (userData.avatarUrl) {
        // 拼接完整的图片URL（假设后端返回的是相对路径）
        userAvatar.value = `http://localhost:80${userData.avatarUrl}`;
      } else {
        // 如果没有头像，尝试获取头像
        const avatarResponse: any = await getAvatarAPI(token);
        if (avatarResponse && avatarResponse.code === 200 && avatarResponse.data) {
          // 如果返回的是URL字符串，直接使用
          if (typeof avatarResponse.data === 'string') {
            userAvatar.value = `http://localhost:80${avatarResponse.data}`;
          } else {
            // 如果返回的是二进制数据，创建blob URL
            const blob = new Blob([avatarResponse.data], { type: avatarResponse.headers['content-type'] || 'image/jpeg' });
            userAvatar.value = URL.createObjectURL(blob);
          }
        } else {
          userAvatar.value = defaultAvatar;
        }
      }
    } else {
      userAvatar.value = defaultAvatar;
    }
  } catch (error) {
    userAvatar.value = defaultAvatar;
  }
};

onMounted(() => {
  loadUserAvatar();
});

const routeToHome = () => {
  if(userType.value=='1'){
    router.push('/teacher')
  }else if(userType.value=='2'){
    router.push('/student')
  }else if(userType.value=='0'){
    router.push('/admin')
  }
}

const logout = () => {
  localStorage.removeItem("token")
  localStorage.removeItem("userType")
  useMainStore().useLoginStore().setLogin(false)
  router.push('/login').then(()=>{window.location.reload();})
}
</script>