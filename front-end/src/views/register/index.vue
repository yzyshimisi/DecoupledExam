<template>
  <div class="min-h-screen bg-base-200 flex items-center justify-center p-6">
    <div class="card w-full max-w-md bg-base-100 shadow-2xl">
      <div class="card-body">
        <!-- 头部 -->
        <div class="text-center mb-8">
          <h2 class="text-3xl font-bold text-base-content">创建账户</h2>
          <p class="text-base-content/70 mt-2">请填写以下信息以注册新账户</p>
        </div>

        <!-- 注册表单 -->
        <form @submit.prevent="handleRegister">
          <!-- 用户名输入 -->
          <div class="form-control mb-4">
            <label class="label">
              <span class="label-text text-base-content font-medium">用户名</span>
            </label>
            <input
              v-model="registerForm.username"
              type="text"
              placeholder="请输入用户名"
              class="input input-bordered w-full focus:ring-2 focus:ring-primary/50"
              :class="{ 'input-error': errors.username }"
            />
            <label class="label" v-if="errors.username">
              <span class="label-text-alt text-error">{{ errors.username }}</span>
            </label>
          </div>

          <!-- 真实姓名输入 -->
          <div class="form-control mb-4">
            <label class="label">
              <span class="label-text text-base-content font-medium">真实姓名</span>
            </label>
            <input
              v-model="registerForm.realName"
              type="text"
              placeholder="请输入真实姓名"
              class="input input-bordered w-full focus:ring-2 focus:ring-primary/50"
              :class="{ 'input-error': errors.realName }"
            />
            <label class="label" v-if="errors.realName">
              <span class="label-text-alt text-error">{{ errors.realName }}</span>
            </label>
          </div>

          <!-- 电话输入 -->
          <div class="form-control mb-4">
            <label class="label">
              <span class="label-text text-base-content font-medium">电话</span>
            </label>
            <input
              v-model="registerForm.phone"
              type="text"
              placeholder="请输入电话号码"
              class="input input-bordered w-full focus:ring-2 focus:ring-primary/50"
              :class="{ 'input-error': errors.phone }"
            />
            <label class="label" v-if="errors.phone">
              <span class="label-text-alt text-error">{{ errors.phone }}</span>
            </label>
          </div>

          <!-- 密码输入 -->
          <div class="form-control mb-4">
            <label class="label">
              <span class="label-text text-base-content font-medium">密码</span>
            </label>
            <input
              v-model="registerForm.password"
              type="password"
              placeholder="请输入密码"
              class="input input-bordered w-full focus:ring-2 focus:ring-primary/50"
              :class="{ 'input-error': errors.password }"
            />
            <label class="label" v-if="errors.password">
              <span class="label-text-alt text-error">{{ errors.password }}</span>
            </label>
          </div>

          <!-- 确认密码输入 -->
          <div class="form-control mb-6">
            <label class="label">
              <span class="label-text text-base-content font-medium">确认密码</span>
            </label>
            <input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              class="input input-bordered w-full focus:ring-2 focus:ring-primary/50"
              :class="{ 'input-error': errors.confirmPassword }"
            />
            <label class="label" v-if="errors.confirmPassword">
              <span class="label-text-alt text-error">{{ errors.confirmPassword }}</span>
            </label>
          </div>

          <!-- 注册按钮 -->
          <div class="form-control mb-6">
            <button
              type="submit"
              class="btn btn-primary w-full text-lg font-semibold"
              :class="{ 'loading': isLoading }"
            >
              <span v-if="!isLoading">注 册</span>
              <span v-else>注册中...</span>
            </button>
          </div>
        </form>

        <!-- 登录链接 -->
        <div class="text-center">
          <p class="text-base-content/70">
            已有账户？
            <router-link to="/login" class="link link-primary font-semibold hover:link-hover">立即登录</router-link>
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRequest } from "vue-hooks-plus";
  import registerAPI from "../../apis/Server/registerAPI";
import router from "../../routers";

// 注册表单数据
interface RegisterForm {
  username: string;
  password: string;
  realName: string;
  phone: string;
  confirmPassword: string;
}

const registerForm = reactive<RegisterForm>({
  username: '',
  password: '',
  realName: '',
  phone: '',
  confirmPassword: ''
})

// 错误信息
const errors = reactive({
  username: '',
  password: '',
  realName: '',
  phone: '',
  confirmPassword: ''
})

// 加载状态
const isLoading = ref(false)

// 表单验证
const validateForm = (): boolean => {
  let isValid = true

  // 重置错误
  errors.username = ''
  errors.password = ''
  errors.realName = ''
  errors.phone = ''
  errors.confirmPassword = ''

  if (!registerForm.username.trim()) {
    errors.username = '请输入用户名'
    isValid = false
  }

  if (!registerForm.realName.trim()) {
    errors.realName = '请输入真实姓名'
    isValid = false
  }

  if (!registerForm.phone.trim()) {
    errors.phone = '请输入电话号码'
    isValid = false
  } else if (!/^1[3-9]\d{9}$/.test(registerForm.phone)) {
    // 验证手机号格式
    errors.phone = '请输入正确的电话号码格式'
    isValid = false
  }

  if (!registerForm.password.trim()) {
    errors.password = '请输入密码'
    isValid = false
  } else if (registerForm.password.length < 6) {
    errors.password = '密码长度至少6位'
    isValid = false
  }

  if (!registerForm.confirmPassword.trim()) {
    errors.confirmPassword = '请确认密码'
    isValid = false
  } else if (registerForm.password !== registerForm.confirmPassword) {
    errors.confirmPassword = '两次输入的密码不一致'
    isValid = false
  }

  return isValid
}

// 处理注册
const handleRegister = () => {
  if (!validateForm()) return

  isLoading.value = true

  useRequest(() => registerAPI({
    username: registerForm.username,
    password: registerForm.password,
    realName: registerForm.realName,
    phone: registerForm.phone
  }), {
    onSuccess(res) {
      if (res['code'] == 200) {
        // 注册成功，跳转到登录页
        alert('注册成功，请登录');
        router.push('/login');
      } else {
        alert(res['message'] || '注册失败');
      }
    },
    onError(err) {
      console.error('注册错误:', err);
      alert('注册失败，请重试');
    },
    onFinally() {
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
  box-shadow: 0 20px 25px -5px rgb(0 0 0 / 0.1), 0 8px 10px -6px rgb(0 0 0 / 0.1);
}

.btn {
  transition: all 0.2s ease;
}
</style>
