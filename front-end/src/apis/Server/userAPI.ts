import { request } from '../axios';

export default function userAPI() {
}


// 获取用户信息接口
export const getUserInfoAPI = async (token: string) => {
  return request("/api/user", {
    method: "GET",
    headers: {
      "Authorization": `Bearer ${token}`
    }
  })
}

// 根据用户ID获取用户信息接口
export const getUserInfoByIdAPI = async (userId: number, token: string) => {
  return request(`/api/user/${userId}`, {
    method: "GET",
    headers: {
      "Authorization": `Bearer ${token}`
    }
  })
}

// 获取头像接口
export const getAvatarAPI = async (token: string) => {
  return request("/api/user/avatar", {
    method: "GET",
    headers: {
      "Authorization": `Bearer ${token}`
    }
  })
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
      // 不设置Content-Type，让浏览器自动设置
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
      // 不设置Content-Type，让浏览器自动设置
    },
    data: formData
  })
}

// 加入课程接口
export const joinCourseAPI = async (inviteCode: string, token: string) => {
  return request("/api/course/join", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "Authorization": `Bearer ${token}`
    },
    data: {
      inviteCode: inviteCode
    }
  })
}

// 获取当前用户已加入课程列表
export const getJoinedCoursesAPI = async (token: string) => {
  return request('/api/course/my/joined', {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  })
}

// 退出课程接口
export const quitCourseAPI = async (courseId: number, token: string) => {
  return request(`/api/course/quit/${courseId}`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  })
}

// 获取教师职位接口
export interface TeacherPositionInfo {
  id: number;
  teacherId: number;
  role: number; // 0: 任课老师, 1: 教务老师
}

export const getTeacherPositionAPI = async (teacherId: number, token: string) => {
  return request(`/api/teacher/position?teacherId=${teacherId}`, {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  })
}

// 学科接口
export interface Subject {
  createTime?: string;
  gradeLevel?: number;
  sortOrder?: number;
  status?: number;
  subjectCode?: string;
  subjectId?: number;
  subjectName?: string;
}

// 创建课程接口
interface CreateCourseData {
  courseName: string;
  subjectId: number;
  description: string;
}

export const createCourseAPI = async (data: CreateCourseData, token: string) => {
  return request('/api/course/create', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    data: data
  })
}

// 获取学科列表接口
export const getSubjectListAPI = async (token: string) => {
  return request('/api/subject', {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  })
}

// 获取教师课程列表接口
export interface TeacherCourse {
  courseId: number;
  courseName: string;
  inviteCode: string;
  subjectId: number;
  teacherId: number;
  description: string;
  status: string; // '0' 开设中，'1' 已结课
  createTime: number;
}

export const getTeacherCoursesAPI = async (token: string) => {
  return request('/api/course/my', {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  })
}

// 删除课程接口
export const deleteCourseAPI = async (courseId: number, token: string) => {
  return request(`/api/course/delete/${courseId}`, {
    method: 'DELETE',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  })
}

// 修改课程信息接口
interface UpdateCourseData {
  courseName?: string;
  description?: string;
}

export const updateCourseInfoAPI = async (courseId: number, data: UpdateCourseData, token: string) => {
  return request(`/api/course/update/${courseId}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    data: data
  })
}

// 修改课程状态接口
interface UpdateCourseStatusData {
  status: string;
}

export const updateCourseStatusAPI = async (courseId: number, status: number, token: string) => {
  return request(`/api/course/status/${courseId}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    data: { status: status.toString() }
  })
}
