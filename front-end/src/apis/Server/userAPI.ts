import { request } from '../axios';

export default function userAPI() {
}


// 更新用户信息接口
interface UpdateUserInfoData {
  username?: string;
  realName?: string;
  phone?: string;
}

export const updateUserInfoAPI = async (data: UpdateUserInfoData, token: string) => {
  return request("/api/user", {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
      "Authorization": `Bearer ${token}`
    },
    data: data
  })
}

// 修改密码接口
interface ChangePasswordData {
  oldPassword: string;
  newPassword: string;
}

export const changePasswordAPI = async (data: ChangePasswordData, token: string) => {
  return request("/api/user/password", {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
      "Authorization": `Bearer ${token}`
    },
    data: data
  })
}

// 上传头像接口
export const uploadAvatarAPI = async (file: File, token: string) => {
  const formData = new FormData();
  formData.append('file', file);

  return request("/api/user/avatar/upload", {
    method: "POST",
    headers: {
      "Authorization": `Bearer ${token}`
    },
    data: formData
  })
}

// 上传人脸图像接口
export const uploadFaceImageAPI = async (file: File, token: string) => {
  const formData = new FormData();
  formData.append('file', file);

  return request("/api/user/face-image/upload", {
    method: "POST",
    headers: {
      "Authorization": `Bearer ${token}`
    },
    data: formData
  })
}