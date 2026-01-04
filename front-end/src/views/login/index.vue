<template>
  <div class="flex items-center justify-center p-6">
    <div class="card w-full max-w-md bg-white shadow-xl border-4 border-base-200">
      <div class="card-body">
        <!-- 头部 -->
        <div class="text-center mb-8">
          <h2 class="text-3xl font-bold text-base-content">欢迎回来</h2>
          <p class="text-base-content/70 mt-2">请登录您的账户以继续</p>
        </div>

        <!-- 登录表单 -->
        <form @submit.prevent="handleLogin">
          <!-- 用户名输入 -->
          <div class="form-control mb-4">
            <label class="label">
              <span class="label-text text-base-content font-medium">用户名</span>
            </label>
            <input
                v-model="loginForm.username"
                type="text"
                placeholder="请输入用户名"
                class="input input-bordered w-full focus:ring-2 focus:ring-primary/50"
                :class="{ 'input-error': errors.username }"
            />
            <label class="label" v-if="errors.username">
              <span class="label-text-alt text-error">{{ errors.username }}</span>
            </label>
          </div>

          <!-- 密码输入 -->
          <div class="form-control mb-6">
            <label class="label">
              <span class="label-text text-base-content font-medium">密码</span>
            </label>
            <input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                class="input input-bordered w-full focus:ring-2 focus:ring-primary/50"
                :class="{ 'input-error': errors.password }"
            />
            <label class="label" v-if="errors.password">
              <span class="label-text-alt text-error">{{ errors.password }}</span>
            </label>
          </div>

          <!-- 忘记密码链接 (已移除“记住我”) -->
          <div class="flex justify-end mb-8">
            <a href="#" class="link link-primary text-sm hover:link-hover">忘记密码？</a>
          </div>

          <!-- 登录按钮 -->
          <div class="form-control mb-6">
            <button
                type="submit"
                class="btn btn-primary w-full text-lg font-semibold"
                :class="{ 'loading': isLoading }"
            >
              <span v-if="!isLoading">登 录</span>
              <span v-else>登录中...</span>
            </button>
          </div>
        </form>

        <!-- 分割线 -->
        <div class="divider text-base-content/50">或使用以下方式登录</div>

        <!-- 人脸登录按钮 -->
        <div class="text-center">
          <button
              type="button"
              class="btn btn-primary bg-cyan-500 text-white w-1/2 mb-4 hover:bg-cyan-600 transition-colors"
              onclick="cameraRecorderDia.showModal()"
          >
            <svg class="w-7 h-7 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/>
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/>
            </svg>
            人脸识别登录
          </button>
        </div>

        <!-- 社交登录 -->
        <div class="flex gap-4 justify-center mb-8">
          <button class="btn btn-circle btn-outline hover:btn-primary">
            <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
              <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
              <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
              <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
              <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
            </svg>
          </button>
          <button class="btn btn-circle btn-outline hover:btn-primary">
            <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
              <path d="M12 2.163c3.204 0 3.584.012 4.85.07 3.252.148 4.771 1.691 4.919 4.919.058 1.265.069 1.645.069 4.849 0 3.205-.012 3.584-.069 4.849-.149 3.225-1.664 4.771-4.919 4.919-1.266.058-1.644.07-4.85.07-3.204 0-3.584-.012-4.849-.07-3.26-.149-4.771-1.699-4.919-4.92-.058-1.265-.07-1.644-.07-4.849 0-3.204.013-3.583.07-4.849.149-3.227 1.664-4.771 4.919-4.919 1.266-.057 1.645-.069 4.849-.069zm0-2.163c-3.259 0-3.667.014-4.947.072-4.358.2-6.78 2.618-6.98 6.98-.059 1.281-.073 1.689-.073 4.948 0 3.259.014 3.668.072 4.948.2 4.358 2.618 6.78 6.98 6.98 1.281.058 1.689.072 4.948.072 3.259 0 3.668-.014 4.948-.072 4.354-.2 6.782-2.618 6.979-6.98.059-1.28.073-1.689.073-4.948 0-3.259-.014-3.667-.072-4.947-.196-4.354-2.617-6.78-6.979-6.98-1.281-.059-1.69-.073-4.949-.073zM5.838 12a6.162 6.162 0 1112.324 0 6.162 6.162 0 01-12.324 0zM12 16a4 4 0 110-8 4 4 0 010 8zm4.965-10.405a1.44 1.44 0 112.881.001 1.44 1.44 0 01-2.881-.001z"/>
            </svg>
          </button>
          <button class="btn btn-circle btn-outline hover:btn-primary">
            <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
              <path d="M12 2C6.477 2 2 6.477 2 12c0 4.991 3.657 9.128 8.438 9.879V14.89h-2.54V12h2.54V9.797c0-2.506 1.492-3.89 3.777-3.89 1.094 0 2.238.195 2.238.195v2.46h-1.26c-1.243 0-1.63.771-1.63 1.562V12h2.773l-.443 2.89h-2.33v6.989C18.343 21.129 22 16.99 22 12c0-5.523-4.477-10-10-10z"/>
            </svg>
          </button>
        </div>

        <!-- 注册链接 -->
        <div class="text-center">
          <p class="text-base-content/70">
            还没有账户？
            <router-link to="/register" class="link link-primary font-semibold hover:link-hover">立即注册</router-link>
          </p>
        </div>
      </div>
    </div>
  </div>
  <CameraRecorder
      :isLoading="isLoading"
      @loginFace="loginFace"
  />
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRequest } from "vue-hooks-plus";
import { loginAPI, loginFaceAPI } from "../../apis"
import { useMainStore } from "../../stores";
import router from "../../routers";
import { jwtDecode } from "jwt-decode";
import { CameraRecorder } from "../../components"
import { ElNotification } from 'element-plus'

// 登录表单数据
const loginForm = reactive({
  username: '',
  password: ''
})

// 错误信息
const errors = reactive({
  username: '',
  password: ''
})

// 加载状态
const isLoading = ref(false)

// 表单验证
const validateForm = (): boolean => {
  let isValid = true

  // 重置错误
  errors.username = ''
  errors.password = ''

  if (!loginForm.username.trim()) {
    errors.username = '请输入用户名'
    isValid = false
  }

  if (!loginForm.password.trim()) {
    errors.password = '请输入密码'
    isValid = false
  } else if (loginForm.password.length < 6) {
    errors.password = '密码长度至少6位'
    isValid = false
  }

  return isValid
}

// 处理登录
const handleLogin = () => {

  if (!validateForm()) return

  isLoading.value = true

  useRequest(()=>loginAPI(loginForm),{
    onSuccess(res){
      console.log(res)
      if(res['code']==200){
        let token = res['data']

        if(token==null || token=='') return

        const decoded = jwtDecode(token);
        localStorage.setItem('token', token)
        localStorage.setItem('userType', decoded['userType'])
        useMainStore().useLoginStore().setLogin(true)
        router.push('/admin/teacher/register')
        switch (decoded['userType']){
          case 0:
            router.push('/admin').then(()=>{window.location.reload();})
            break
          case 1:
            router.push('/teacher').then(()=>{window.location.reload();})
            break
          case 2:
            router.push('/student').then(()=>{window.location.reload();})
            break
        }
      }else{
        ElNotification({title: 'Warning', message: res['msg'], type: 'warning',})
      }
    },

    onError(err){
      ElNotification({title: 'Error', message: err['msg'], type: 'error',})
    },

    onFinally(){
      isLoading.value = false
    }
  })
}

const loginFace = (videoBase64:string) => {
  useRequest(()=>loginFaceAPI({"video":videoBase64}), {

    onBefore(){
      isLoading.value = true
    },

    onSuccess(res){
      if(res['code']==200){

        cameraRecorderDia.close()

        let token = res['data']

        if(token==null || token=='') return

        const decoded = jwtDecode(token);
        localStorage.setItem('token', token)
        localStorage.setItem('userType', decoded['userType'])
        useMainStore().useLoginStore().setLogin(true)
        router.push('/admin/teacher/register')
        switch (decoded['userType']){
          case 0:
            router.push('/admin').then(()=>{window.location.reload();})
            break
          case 1:
            router.push('/teacher').then(()=>{window.location.reload();})
            break
          case 2:
            router.push('/student').then(()=>{window.location.reload();})
            break
        }
      }else{
        ElNotification({title: 'Warning', message: res['msg'], type: 'warning',})
      }
    },

    onFinally(){
      isLoading.value = false
    }
  })
}
</script>

<style scoped>
/* 可以添加一些自定义动画或过渡效果 */
.card {
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.card:hover {
  transform: translateY(-4px);
  box-shadow: 0 20px 25px -5px rgb(0 0 0 / 0.3), 0 8px 10px -6px rgb(0 0 0 / 0.3);
}

.btn {
  transition: all 0.2s ease;
}
</style>
