<template>
  <div class="max-w-4xl mx-auto p-6">
    <div class="flex justify-between items-center mb-8">
      <h1 class="text-3xl font-bold">加入课程</h1>
      <button @click="goBack" class="btn btn-outline">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
        </svg>
        返回主页
      </button>
    </div>

    <div class="bg-base-100 p-8 rounded-xl shadow-lg">
      <form @submit.prevent="joinCourse" class="space-y-6">
        <div class="form-control">
          <label class="label">
            <span class="label-text font-medium">输入邀请码</span>
          </label>
          <input 
            type="text" 
            placeholder="请输入课程邀请码" 
            class="input input-bordered w-full" 
            v-model="inviteCode"
            required
          />
        </div>

        <div class="flex justify-end">
          <button 
            type="submit" 
            class="btn btn-primary"
            :class="{ 'loading': isLoading }"
            :disabled="!inviteCode.trim() || isLoading"
          >
            <span v-if="!isLoading">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
              </svg>
              加入课程
            </span>
            <span v-else>加入中...</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { joinCourseAPI } from '../../../apis/Server/userAPI';
import router from '../../../routers';
import { jwtDecode } from 'jwt-decode';

const inviteCode = ref('');
const isLoading = ref(false);

const joinCourse = async () => {
  if (!inviteCode.value.trim()) return;
  
  isLoading.value = true;
  
  try {
    // 获取token
    const token = localStorage.getItem('token');
    if (!token) {
      alert('请先登录');
      return;
    }
    
    // 调用加入课程接口
    const response = await joinCourseAPI(inviteCode.value, token);
    
    if (response.code === 200) {
      alert('成功加入课程！');
      // 跳转到课程列表页面
      router.push('/student/courses');
    } else {
      alert(`加入课程失败: ${response.message}`);
    }
  } catch (error) {
    console.error('加入课程失败:', error);
    alert('加入课程失败，请重试');
  } finally {
    isLoading.value = false;
  }
};

const goBack = () => {
  router.push('/student');
};
</script>