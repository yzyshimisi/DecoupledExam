import { request } from '../axios';

interface TeacherRegisterData {
  username: string;
  password: string;
  realName: string;
  userType: number; // 1 表示教师
  phone: string;
}

const teacherRegisterAPI = async (data: TeacherRegisterData, token: string) => {
  return request("/api/admin/teacher/register", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "Authorization": `Bearer ${token}`
    },
    data: data
  })
}

export default teacherRegisterAPI;
