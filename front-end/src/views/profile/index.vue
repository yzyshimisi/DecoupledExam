<template>
  <div class="max-w-4xl mx-auto p-6">
    <div class="flex justify-between items-center mb-8">
      <h1 class="text-3xl font-bold">个人信息管理</h1>
      <button @click="goToUserDashboard" class="btn btn-outline">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
        </svg>
        返回主页面
      </button>
    </div>
    
    <div class="bg-base-100 p-8 rounded-xl shadow-lg mb-8">
      <div class="flex flex-col items-center mb-8">
        <!-- 头像区域 -->
        <div class="avatar mb-6 relative group">
          <div class="w-32 h-32 rounded-full ring-4 ring-primary relative overflow-hidden">
            <img 
              :src="userAvatar || defaultAvatar" 
              alt="头像" 
              class="w-full h-full object-cover"
            />
            <div class="absolute inset-0 bg-black bg-opacity-50 flex items-center justify-center rounded-full opacity-0 group-hover:opacity-100 transition-opacity">
              <label class="btn btn-circle btn-primary flex items-center justify-center cursor-pointer">
                <input 
                  type="file" 
                  class="hidden" 
                  accept="image/*"
                  @change="handleAvatarUpload"
                />
                <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z" />
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 13a3 3 0 11-6 0 3 3 0 016 0z" />
                </svg>
              </label>
            </div>
          </div>
        </div>
        
        <!-- 用户名显示 -->
        <h2 class="text-2xl font-semibold">{{ userProfile.realName || userProfile.username }}</h2>
        <p class="text-base-content/70 mt-2">{{ userProfile.username }}</p>
      </div>
    </div>
    
    <!-- 基本信息模块 -->
    <div class="bg-base-100 p-8 rounded-xl shadow-lg mb-8">
      <h2 class="text-2xl font-semibold mb-6 flex items-center">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
        </svg>
        基本信息
      </h2>
      
      <form @submit.prevent="updateProfile">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div class="form-control">
            <label class="label">
              <span class="label-text font-medium">用户角色</span>
            </label>
            <input 
              type="text" 
              :value="getUserTypeText(userProfile.userType)"
              class="input input-bordered input-disabled" 
              disabled
            />
          </div>
          
          <div class="form-control">
            <label class="label">
              <span class="label-text font-medium">用户名</span>
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
              <span class="label-text font-medium">真实姓名</span>
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
              <span class="label-text font-medium">手机号</span>
            </label>
            <input 
              type="text" 
              placeholder="手机号" 
              class="input input-bordered" 
              v-model="profileForm.phone"
              :disabled="!isEditing"
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
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
            </svg>
            编辑信息
          </button>
          
          <button 
            v-if="isEditing" 
            type="submit" 
            class="btn btn-primary"
            :class="{ 'loading': isSaving }"
          >
            <span v-if="!isSaving">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
              </svg>
              保存更改
            </span>
            <span v-else>保存中...</span>
          </button>
          
          <button 
            v-if="isEditing" 
            type="button" 
            @click="cancelEdit" 
            class="btn btn-ghost"
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
            取消
          </button>
        </div>
      </form>
    </div>
    
    <!-- 安全信息模块 -->
    <div class="bg-base-100 p-8 rounded-xl shadow-lg">
      <h2 class="text-2xl font-semibold mb-6 flex items-center">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
        </svg>
        安全设置
      </h2>
      
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
        <button @click="toggleChangePassword" class="btn btn-outline flex items-center justify-center">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 7a2 2 0 012 2m4 0a6 6 0 01-7.743 5.743L11 17H9v2H7v2H4a1 1 0 01-1-1v-2.586a1 1 0 01.293-.707l5.964-5.964A6 6 0 1121 9z" />
          </svg>
          {{ showChangePasswordForm ? '取消修改密码' : '修改密码' }}
        </button>
        
        <button @click="toggleUploadFaceForm" class="btn btn-outline flex items-center justify-center">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14.828 14.828a4 4 0 01-5.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          {{ showUploadFaceForm ? '取消上传人脸' : '上传人脸信息' }}
        </button>
      </div>
      
      <!-- 修改密码表单 -->
      <div v-if="showChangePasswordForm" class="bg-base-200 p-6 rounded-lg mb-6">
        <h3 class="text-xl font-semibold mb-4 flex items-center">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 7a2 2 0 012 2m4 0a6 6 0 01-7.743 5.743L11 17H9v2H7v2H4a1 1 0 01-1-1v-2.586a1 1 0 01.293-.707l5.964-5.964A6 6 0 1121 9z" />
          </svg>
          修改密码
        </h3>
        
        <form @submit.prevent="changePassword">
          <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div class="form-control">
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
            
            <div class="form-control">
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
            
            <div class="form-control">
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
          </div>
          
          <div class="flex justify-end space-x-4 mt-4">
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
              class="btn btn-ghost" 
              @click="cancelChangePassword"
            >
              取消
            </button>
          </div>
        </form>
      </div>
      
      <!-- 上传人脸信息表单 -->
      <div v-if="showUploadFaceForm" class="bg-base-200 p-6 rounded-lg">
        <h3 class="text-xl font-semibold mb-4 flex items-center">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14.828 14.828a4 4 0 01-5.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          上传人脸信息
        </h3>
        
        <p class="mb-4 text-base-content/80">人脸信息将用于实名认证，仅用于身份验证，不会在页面中显示。</p>
        
        <form @submit.prevent="uploadFaceImage">
          <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div class="form-control">
              <label class="label">
                <span class="label-text">选择人脸图片</span>
              </label>
              <input 
                type="file" 
                accept="image/*" 
                class="file-input file-input-bordered w-full" 
                @change="handleFaceImageUpload"
                required
              />
            </div>
            
            <div class="flex items-end">
              <button 
                type="submit" 
                class="btn btn-primary w-full"
                :class="{ 'loading': isUploadingFace }"
                :disabled="!selectedFaceFile"
              >
                <span v-if="!isUploadingFace">上传</span>
                <span v-else>上传中...</span>
              </button>
            </div>
            
            <div class="flex items-end">
              <button 
                type="button" 
                class="btn btn-ghost w-full" 
                @click="cancelUploadFace"
              >
                取消
              </button>
            </div>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { 
  getUserInfoByIdAPI, 
  updateUserInfoAPI, 
  changePasswordAPI, 
  uploadAvatarAPI, 
  uploadFaceImageAPI
} from '../../apis/Server/userAPI';
import { jwtDecode } from 'jwt-decode';
import router from '../../routers';

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
const showChangePasswordForm = ref(false);
const showUploadFaceForm = ref(false);
const isChangingPassword = ref(false);
const isUploadingFace = ref(false);
const selectedFaceFile = ref<File | null>(null);

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmNewPassword: ''
});

const passwordError = ref('');
const userAvatar = ref<string | null>(null);
const defaultAvatar = 'https://daisyui.com/images/stock/photo-1534528741775-53994a69daeb.jpg';

// 获取用户类型文本
const getUserTypeText = (userType: number): string => {
  switch(userType) {
    case 0: return '管理员';
    case 1: return '教师';
    case 2: return '学生';
    default: return '未知';
  }
};

// 根据用户类型导航到对应的主页面
const goToUserDashboard = () => {
  switch(userProfile.value.userType) {
    case 0: // 管理员
      router.push('/admin');
      break;
    case 1: // 教师
      router.push('/teacher'); // 或者其他教师主页面
      break;
    case 2: // 学生
      router.push('/student'); // 或者其他学生主页面
      break;
    default:
      router.push('/'); // 默认返回首页
  }
};

// 初始化用户信息
onMounted(() => {
  loadUserProfile();
});

const loadUserProfile = async () => {
  const token = localStorage.getItem('token');
  
  if (!token) {
    alert('请先登录');
    return;
  }
  
  try {
    // 从token中解析用户ID
    const decoded: any = jwtDecode(token);
    const userId = decoded.id; // 根据您提供的JWT，用户ID存储在id字段中
    
    // 根据用户ID获取用户信息
    const response = await getUserInfoByIdAPI(userId, token);
    
    if (response['code'] === 200) {
      const data = response['data'];
      userProfile.value = {
        id: data.userId,
        username: data.username,
        realName: data.realName,
        phone: data.phone,
        userType: data.userType,
        avatar: data.avatarUrl
      };
      
      // 同步到表单
      profileForm.username = userProfile.value.username;
      profileForm.realName = userProfile.value.realName;
      profileForm.phone = userProfile.value.phone;
      
      // 设置头像
      if (userProfile.value.avatar) {
        userAvatar.value = `http://localhost:80${userProfile.value.avatar}`;
      }
    } else {
      console.error('获取用户信息失败:', response['message']);
      alert('获取用户信息失败，请重试');
    }
  } catch (error) {
    console.error('获取用户信息失败:', error);
    alert('获取用户信息失败，请重试');
  }
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

const handleAvatarUpload = async (event: Event) => {
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
      userAvatar.value = e.target?.result as string;
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

const toggleChangePassword = () => {
  showChangePasswordForm.value = !showChangePasswordForm.value;
  if (!showChangePasswordForm.value) {
    // 重置表单
    passwordForm.oldPassword = '';
    passwordForm.newPassword = '';
    passwordForm.confirmNewPassword = '';
    passwordError.value = '';
  }
};

const cancelChangePassword = () => {
  showChangePasswordForm.value = false;
  // 重置表单
  passwordForm.oldPassword = '';
  passwordForm.newPassword = '';
  passwordForm.confirmNewPassword = '';
  passwordError.value = '';
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
    showChangePasswordForm.value = false;
    
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

const toggleUploadFaceForm = () => {
  showUploadFaceForm.value = !showUploadFaceForm.value;
  if (!showUploadFaceForm.value) {
    selectedFaceFile.value = null;
  }
};

const cancelUploadFace = () => {
  showUploadFaceForm.value = false;
  selectedFaceFile.value = null;
};

const handleFaceImageUpload = (event: Event) => {
  const target = event.target as HTMLInputElement;
  const file = target.files?.[0];
  selectedFaceFile.value = file || null;
};

const uploadFaceImage = async () => {
  if (!selectedFaceFile.value) {
    alert('请选择人脸图片');
    return;
  }
  
  // 验证文件类型
  const validTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif'];
  if (!validTypes.includes(selectedFaceFile.value.type)) {
    alert('请选择有效的图片文件(jpg, jpeg, png, gif)');
    return;
  }
  
  // 验证文件大小 (例如：不超过5MB)
  if (selectedFaceFile.value.size > 5 * 1024 * 1024) {
    alert('图片大小不能超过5MB');
    return;
  }
  
  isUploadingFace.value = true;
  
  try {
    const token = localStorage.getItem('token');
    if (!token) {
      alert('请先登录');
      return;
    }
    
    await uploadFaceImageAPI(selectedFaceFile.value, token);
    
    alert('人脸信息上传成功');
    showUploadFaceForm.value = false;
    selectedFaceFile.value = null;
  } catch (error) {
    console.error('上传人脸信息失败:', error);
    alert('上传失败，请重试');
  } finally {
    isUploadingFace.value = false;
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

.avatar:hover .bg-black {
  opacity: 0.5;
}
</style>