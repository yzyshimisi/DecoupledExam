<template>
  <div class="min-h-screen bg-base-200 flex items-center justify-center p-6">
    <div class="card w-full max-w-md bg-base-100 shadow-2xl">
      <div class="card-body">
        <!-- 头部 -->
        <div class="text-center mb-8">
          <h2 class="text-3xl font-bold text-base-content">注册教师账号</h2>
          <p class="text-base-content/70 mt-2">请填写教师账号信息</p>
        </div>

        <!-- 教师注册表单 -->
        <form @submit.prevent="handleTeacherRegister">
          <!-- 用户名输入 -->
          <div class="form-control mb-4">
            <label class="label">
              <span class="label-text text-base-content font-medium">用户名</span>
            </label>
            <input
              v-model="teacherForm.username"
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
              v-model="teacherForm.realName"
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
              v-model="teacherForm.phone"
              type="text"
              placeholder="请输入电话号码"
              class="input input-bordered w-full focus:ring-2 focus:ring-primary/50"
              :class="{ 'input-error': errors.phone }"
            />
            <label class="label" v-if="errors.phone">
              <span class="label-text-alt text-error">{{ errors.phone }}</span>
            </label>
          </div>

          <!-- 邮箱输入 -->
          <div class="form-control mb-4">
            <label class="label">
              <span class="label-text text-base-content font-medium">邮箱</span>
            </label>
            <input
                v-model="teacherForm.email"
                type="email"
                placeholder="请输入邮箱地址"
                class="input input-bordered w-full focus:ring-2 focus:ring-primary/50"
                :class="{ 'input-error': errors.email }"
            />
            <label class="label" v-if="errors.email">
              <span class="label-text-alt text-error">{{ errors.email }}</span>
            </label>
          </div>

          <!-- 密码输入 -->
          <div class="form-control mb-4">
            <label class="label">
              <span class="label-text text-base-content font-medium">密码</span>
            </label>
            <input
              v-model="teacherForm.password"
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
              v-model="teacherForm.confirmPassword"
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
              <span v-if="!isLoading">注册教师账号</span>
              <span v-else>注册中...</span>
            </button>
          </div>
        </form>

        <!-- 返回管理员主页按钮 -->
        <div class="text-center">
          <p class="text-base-content/70">
            <router-link to="/admin" class="btn btn-ghost link link-primary font-semibold hover:link-hover">返回管理员主页</router-link>
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRequest } from "vue-hooks-plus";
import teacherRegisterAPI from "../../../apis/Server/teacherRegisterAPI";
import router from "../../../routers";

// 教师注册表单数据
interface TeacherRegisterForm {
  username: string;
  password: string;
  realName: string;
  userType: number;
  phone: string;
  confirmPassword: string;
}

const teacherForm = reactive<TeacherRegisterForm>({
  username: '',
  password: '',
  realName: '',
  userType: 1, // 固定为教师用户类型
  phone: '',
  email: '',
  confirmPassword: ''
})

// 错误信息
const errors = reactive({
  username: '',
  password: '',
  realName: '',
  phone: '',
  email: '',
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
  errors.email = ''
  errors.confirmPassword = ''

  if (!teacherForm.username.trim()) {
    errors.username = '请输入用户名'
    isValid = false
  }

  if (!teacherForm.realName.trim()) {
    errors.realName = '请输入真实姓名'
    isValid = false
  }

  if (!teacherForm.phone.trim()) {
    errors.phone = '请输入电话号码'
    isValid = false
  } else if (!/^1[3-9]\d{9}$/.test(teacherForm.phone)) {
    // 验证手机号格式
    errors.phone = '请输入正确的电话号码格式'
    isValid = false
  }

  if (!teacherForm.password.trim()) {
    errors.password = '请输入密码'
    isValid = false
  } else if (teacherForm.password.length < 6) {
    errors.password = '密码长度至少6位'
    isValid = false
  }

  if (!teacherForm.confirmPassword.trim()) {
    errors.confirmPassword = '请确认密码'
    isValid = false
  } else if (teacherForm.password !== teacherForm.confirmPassword) {
    errors.confirmPassword = '两次输入的密码不一致'
    isValid = false
  }

  return isValid
}

// 处理教师注册
const handleTeacherRegister = () => {
  if (!validateForm()) return

  isLoading.value = true

  // 从 localStorage 获取 token
  const token = localStorage.getItem('token')

  if (!token) {
    alert('请先登录');
    router.push('/login');
    isLoading.value = false;
    return;
  }

  useRequest(() => teacherRegisterAPI({
    username: teacherForm.username,
    password: teacherForm.password,
    realName: teacherForm.realName,
    userType: teacherForm.userType, // 固定为 1（教师）
    phone: teacherForm.phone,
    email: teacherForm.email
  }, token), {
    onSuccess(res) {
      if (res['code'] == 200) {
        // 注册成功，弹窗提示并跳转回教师注册页面
        alert('教师账号注册成功');
        // 跳转回当前页面以清空表单
        router.push('/admin/teacher/register');
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
