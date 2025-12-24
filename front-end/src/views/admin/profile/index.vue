<template>
  <div>
    <h1 class="text-2xl font-bold mb-6">个人信息管理</h1>
    
    <div class="bg-base-100 p-6 rounded-lg shadow">
      <div class="flex flex-col items-center mb-8">
        <div class="avatar mb-4">
          <div class="w-24 h-24 rounded-full ring-2 ring-primary relative">
            <img :src="userProfile.avatar || 'https://via.placeholder.com/150' " alt="头像" />
            <div class="absolute -bottom-2 -right-2">
              <label class="btn btn-xs btn-circle btn-primary flex items-center justify-center">
                <input 
                  type="file" 
                  class="hidden" 
                  accept="image/*"
                  @change="handleFileUpload"
                  :disabled="!isEditing"
                />
                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z" />
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 13a3 3 0 11-6 0 3 3 0 016 0z" />
                </svg>
              </label>
            </div>
          </div>
        </div>
        <h2 class="text-xl font-semibold">{{ userProfile.realName || userProfile.username }}</h2>
        <p class="text-base-content/70">{{ userProfile.username }}</p>
      </div>
      
      <form @submit.prevent="updateProfile">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div class="form-control">
            <label class="label">
              <span class="label-text">用户名</span>
            </label>
            <input 
              type="text" 
              placeholder="用户名" 
              class="input input-bordered" 
              v-model="profileForm.username"
              :disabled="!isEditing"
            />
          </div>
          
          <div class="form-control">
            <label class="label">
              <span class="label-text">真实姓名</span>
            </label>
            <input 
              type="text" 
              placeholder="真实姓名" 
              class="input input-bordered" 
              v-model="profileForm.realName"
              :disabled="!isEditing"
            />
          </div>
          
          <div class="form-control">
            <label class="label">
              <span class="label-text">手机号</span>
            </label>
            <input 
              type="text" 
              placeholder="手机号" 
              class="input input-bordered" 
              v-model="profileForm.phone"
              :disabled="!isEditing"
            />
          </div>
          
          <div class="form-control">
            <label class="label">
              <span class="label-text">用户类型</span>
            </label>
            <input 
              type="text" 
              :value="getUserTypeText(userProfile.userType)"
              class="input input-bordered" 
              disabled
            />
          </div>
        </div>
        
        <div class="flex justify-end space-x-4 mt-8">
          <button 
            v-if="!isEditing" 
            type="button" 
            @click="toggleEdit" 
            class="btn btn-primary"
          >
            编辑信息
          </button>
          
          <button 
            v-if="isEditing" 
            type="submit" 
            class="btn btn-primary"
            :class="{ 'loading': isSaving }"
          >
            <span v-if="!isSaving">保存更改</span>
            <span v-else>保存中...</span>
          </button>
          
          <button 
            v-if="isEditing" 
            type="button" 
            @click="cancelEdit" 
            class="btn btn-ghost"
          >
            取消
          </button>
        </div>
      </form>
    </div>
    
    <div class="bg-base-100 p-6 rounded-lg shadow mt-6">
      <h2 class="text-xl font-semibold mb-4">安全设置</h2>
      <div class="flex flex-col space-y-4">
        <button @click="showChangePasswordModal = true" class="btn btn-outline">修改密码</button>
      </div>
    </div>
    
    <!-- 修改密码模态框 -->
    <div class="modal" v-if="showChangePasswordModal" @click="showChangePasswordModal = false">
      <div class="modal-box" @click.stop>
        <h3 class="font-bold text-lg mb-4">修改密码</h3>
        
        <form @submit.prevent="changePassword">
          <div class="form-control mb-4">
            <label class="label">
              <span class="label-text">当前密码</span>
            </label>
            <input 
              type="password" 
              placeholder="输入当前密码" 
              class="input input-bordered" 
              v-model="passwordForm.oldPassword"
              required
            />
          </div>
          
          <div class="form-control mb-4">
            <label class="label">
              <span class="label-text">新密码</span>
            </label>
            <input 
              type="password" 
              placeholder="输入新密码" 
              class="input input-bordered" 
              v-model="passwordForm.newPassword"
              required
              minlength="6"
            />
          </div>
          
          <div class="form-control mb-6">
            <label class="label">
              <span class="label-text">确认新密码</span>
            </label>
            <input 
              type="password" 
              placeholder="再次输入新密码" 
              class="input input-bordered" 
              v-model="passwordForm.confirmNewPassword"
              required
              :class="{ 'input-error': passwordError }"
            />
            <label class="label" v-if="passwordError">
              <span class="label-text-alt text-error">{{ passwordError }}</span>
            </label>
          </div>
          
          <div class="modal-action">
            <button 
              type="submit" 
              class="btn btn-primary"
              :class="{ 'loading': isChangingPassword }"
              :disabled="!isPasswordFormValid"
            >
              <span v-if="!isChangingPassword">确认修改</span>
              <span v-else>修改中...</span>
            </button>
            <button 
              type="button" 
              class="btn" 
              @click="showChangePasswordModal = false"
            >
              取消
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { updateUserInfoAPI, changePasswordAPI, uploadAvatarAPI, uploadFaceImageAPI } from '../../../apis/Server/userAPI';

interface UserProfile {
  id: number;
  username: string;
  realName: string;
  phone: string;
  userType: number; // 0: 管理员, 1: 教师, 2: 学生
  avatar?: string;
}

const userProfile = ref<UserProfile>({
  id: 0,
  username: '',
  realName: '',
  phone: '',
  userType: 0,
  avatar: ''
});

const profileForm = reactive({
  username: '',
  realName: '',
  phone: ''
});

const isEditing = ref(false);
const isSaving = ref(false);
const showChangePasswordModal = ref(false);
const isChangingPassword = ref(false);

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmNewPassword: ''
});

const passwordError = ref('');

// 获取用户类型文本
const getUserTypeText = (userType: number): string => {
  switch(userType) {
    case 0: return '管理员';
    case 1: return '教师';
    case 2: return '学生';
    default: return '未知';
  }
};

// 初始化用户信息
onMounted(() => {
  loadUserProfile();
});

const loadUserProfile = () => {
  // 从localStorage获取token
  const token = localStorage.getItem('token');
  
  if (!token) {
    // 这里应该跳转到登录页，但为了演示，我们使用模拟数据
    userProfile.value = {
      id: 1,
      username: 'admin',
      realName: '管理员',
      phone: '13800138000',
      userType: 0,
      avatar: ''
    };
    
    // 同步到表单
    profileForm.username = userProfile.value.username;
    profileForm.realName = userProfile.value.realName;
    profileForm.phone = userProfile.value.phone;
    return;
  }
  
  // 在实际项目中，这里应该调用API获取用户信息
  // 为了演示目的，我们使用模拟数据
  userProfile.value = {
    id: 1,
    username: 'admin',
    realName: '管理员',
    phone: '13800138000',
    userType: 0,
    avatar: ''
  };
  
  // 同步到表单
  profileForm.username = userProfile.value.username;
  profileForm.realName = userProfile.value.realName;
  profileForm.phone = userProfile.value.phone;
};

const toggleEdit = () => {
  isEditing.value = true;
};

const cancelEdit = () => {
  // 恢复原始值
  profileForm.username = userProfile.value.username;
  profileForm.realName = userProfile.value.realName;
  profileForm.phone = userProfile.value.phone;
  isEditing.value = false;
};

const updateProfile = async () => {
  if (!validateProfileForm()) return;
  
  isSaving.value = true;
  
  try {
    const token = localStorage.getItem('token');
    if (!token) {
      alert('请先登录');
      return;
    }
    
    await updateUserInfoAPI({
      username: profileForm.username,
      realName: profileForm.realName,
      phone: profileForm.phone
    }, token);
    
    // 更新用户信息
    userProfile.value.username = profileForm.username;
    userProfile.value.realName = profileForm.realName;
    userProfile.value.phone = profileForm.phone;
    
    isEditing.value = false;
    alert('个人信息更新成功');
  } catch (error) {
    console.error('更新个人信息失败:', error);
    alert('更新失败，请重试');
  } finally {
    isSaving.value = false;
  }
};

const validateProfileForm = (): boolean => {
  if (!profileForm.username.trim()) {
    alert('请输入用户名');
    return false;
  }
  
  if (!profileForm.phone.trim()) {
    alert('请输入手机号');
    return false;
  }
  
  // 验证手机号格式
  const phoneRegex = /^1[3-9]\d{9}$/;
  if (!phoneRegex.test(profileForm.phone)) {
    alert('请输入正确的手机号格式');
    return false;
  }
  
  return true;
};

const handleFileUpload = async (event: Event) => {
  const target = event.target as HTMLInputElement;
  const file = target.files?.[0];
  
  if (!file) return;
  
  // 验证文件类型
  const validTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif'];
  if (!validTypes.includes(file.type)) {
    alert('请选择有效的图片文件(jpg, jpeg, png, gif)');
    return;
  }
  
  // 验证文件大小 (例如：不超过5MB)
  if (file.size > 5 * 1024 * 1024) {
    alert('图片大小不能超过5MB');
    return;
  }
  
  try {
    const token = localStorage.getItem('token');
    if (!token) {
      alert('请先登录');
      return;
    }
    
    await uploadAvatarAPI(file, token);
    alert('头像上传成功');
    
    // 更新头像预览
    const reader = new FileReader();
    reader.onload = (e) => {
      userProfile.value.avatar = e.target?.result as string;
    };
    reader.readAsDataURL(file);
  } catch (error) {
    console.error('上传头像失败:', error);
    alert('上传失败，请重试');
  }
};

// 密码相关计算属性
const isPasswordFormValid = () => {
  return (
    passwordForm.oldPassword &&
    passwordForm.newPassword &&
    passwordForm.confirmNewPassword &&
    passwordForm.newPassword === passwordForm.confirmNewPassword &&
    passwordForm.newPassword.length >= 6
  );
};

const changePassword = async () => {
  if (!isPasswordFormValid()) {
    passwordError.value = '请确认密码输入正确';
    return;
  }
  
  isChangingPassword.value = true;
  
  try {
    const token = localStorage.getItem('token');
    if (!token) {
      alert('请先登录');
      return;
    }
    
    await changePasswordAPI({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    }, token);
    
    alert('密码修改成功');
    showChangePasswordModal.value = false;
    
    // 重置表单
    passwordForm.oldPassword = '';
    passwordForm.newPassword = '';
    passwordForm.confirmNewPassword = '';
    passwordError.value = '';
  } catch (error) {
    console.error('修改密码失败:', error);
    alert('密码修改失败，请重试');
  } finally {
    isChangingPassword.value = false;
  }
};
</script>

<style scoped>
.modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-box {
  background: white;
  border-radius: 0.5rem;
  padding: 1.5rem;
  max-width: 24rem;
  width: 90%;
  max-height: 90vh;
  overflow-y: auto;
}
</style>