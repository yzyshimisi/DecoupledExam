<template>
  <div
      v-if="teacher"
      class="w-full max-w-sm bg-white rounded-lg shadow-md overflow-hidden">
    <!-- 头像区域 -->
    <div class="flex justify-center pt-6 pb-4">
      <div class="w-24 h-24 rounded-full bg-gray-200 flex items-center justify-center">
        <span class="text-3xl text-gray-500">{{ teacher.realName.charAt(0) }}</span>
      </div>
    </div>

    <!-- 信息区域 -->
    <div class="px-6 pb-6">
      <h2 class="text-xl font-bold text-center text-gray-800 mb-2">{{ teacher.realName }}</h2>

      <!-- 联系方式 -->
      <div class="border-t pt-4">
        <div class="flex items-center mb-2">
          <svg class="w-4 h-4 text-gray-400 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"></path>
          </svg>
          <span class="text-sm text-gray-600">{{ teacher.email }}</span>
        </div>

        <div class="flex items-center">
          <svg class="w-4 h-4 text-gray-400 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z"></path>
          </svg>
          <span class="text-sm text-gray-600">{{ teacher.phone }}</span>
        </div>
      </div>

      <!-- 状态信息 -->
      <div class="mt-4 text-center">
        <span class="inline-block px-3 py-1 text-xs font-semibold rounded-full bg-gray-100 text-gray-800">
          状态: {{ getStatusText(teacher.status) }}
        </span>
      </div>

      <!-- 请求出卷按钮 -->
      <div class="mt-6">
        <button
            @click="emit('select', teacher)"
            class="w-full bg-blue-500 hover:bg-blue-600 text-white font-medium py-2 px-4 rounded-md transition duration-300 ease-in-out transform hover:scale-105"
        >
          请求出卷
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
interface User {
  userId: number;
  username: string;
  password: string | null;
  realName: string;
  avatarUrl: string | null;
  userType: number;
  faceImg: string | null;
  phone: string;
  email: string;
  status: string;
  createTime: number;
}

const props = defineProps<{
  teacher: User;
}>();

const emit = defineEmits<{
  (e: 'select', teacherId : any): void;
}>();

const getStatusText = (status: string): string => {
  return status === '0' ? '正常' : '禁用'
}
</script>

<style scoped>
/* Tailwindcss 会处理样式，这里可以添加自定义样式 */
</style>
