<template>
  <div>
    <h1 class="text-2xl font-bold mb-6">管理员仪表盘</h1>
    
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
      <div class="card bg-primary text-primary-content">
        <div class="card-body">
          <h2 class="card-title">总用户数</h2>
          <p class="text-3xl">152</p>
          <div class="text-sm opacity-80">较上月 +12%</div>
        </div>
      </div>
      
      <div class="card bg-secondary text-secondary-content">
        <div class="card-body">
          <h2 class="card-title">学生总数</h2>
          <p class="text-3xl">128</p>
          <div class="text-sm opacity-80">活跃: 110</div>
        </div>
      </div>
      
      <div class="card bg-accent text-accent-content">
        <div class="card-body">
          <h2 class="card-title">教师总数</h2>
          <p class="text-3xl">24</p>
          <div class="text-sm opacity-80">在线: 18</div>
        </div>
      </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
      <div class="bg-base-100 p-6 rounded-lg shadow">
        <h2 class="text-xl font-semibold mb-4">快速操作</h2>
        <div class="grid grid-cols-2 gap-4">
          <router-link to="/admin/user-management/students" class="btn btn-outline">
            <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z"></path>
            </svg>
            管理学生
          </router-link>
          <router-link to="/admin/user-management/teachers" class="btn btn-outline">
            <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path>
            </svg>
            管理教师
          </router-link>
          <router-link to="/admin/simulate-login" class="btn btn-outline">
            <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4"></path>
            </svg>
            模拟登录
          </router-link>
          <router-link to="/admin/profile" class="btn btn-outline">
            <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path>
            </svg>
            个人资料
          </router-link>
        </div>
      </div>
      
      <div class="bg-base-100 p-6 rounded-lg shadow">
        <h2 class="text-xl font-semibold mb-4">系统状态</h2>
        <div class="space-y-4">
          <div class="flex items-center justify-between">
            <span>服务器状态</span>
            <span class="badge badge-success">正常</span>
          </div>
          <div class="flex items-center justify-between">
            <span>数据库连接</span>
            <span class="badge badge-success">正常</span>
          </div>
          <div class="flex items-center justify-between">
            <span>API响应时间</span>
            <span class="badge badge-success">85ms</span>
          </div>
          <div class="flex items-center justify-between">
            <span>当前在线用户</span>
            <span class="badge badge-primary">23</span>
          </div>
        </div>
      </div>
    </div>

    <div class="bg-base-100 p-6 rounded-lg shadow">
      <h2 class="text-xl font-semibold mb-4">最近活动</h2>
      <div class="overflow-x-auto">
        <table class="table">
          <thead>
            <tr>
              <th>用户</th>
              <th>操作</th>
              <th>时间</th>
              <th>IP地址</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td>admin</td>
              <td>登录系统</td>
              <td>2025-12-22 16:30</td>
              <td>127.0.0.1</td>
            </tr>
            <tr>
              <td>teacher1</td>
              <td>发布新课程</td>
              <td>2025-12-22 15:45</td>
              <td>192.168.1.100</td>
            </tr>
            <tr>
              <td>student1</td>
              <td>提交作业</td>
              <td>2025-12-22 14:20</td>
              <td>10.0.0.50</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
// 仪表盘逻辑
</script>