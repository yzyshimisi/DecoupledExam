import { request } from '../axios';

export const updateStudentGradeAPI = async (data: {
  gradeId: number;
  studentId: number;
  courseId: number;
  subjectId: number;
  gradeType: string;
  gradeName: string;
  score: number;
  fullScore: number;
  teacherId: number;
  recordTime?: string; // 修改为可选字段
  remark?: string;
}) => {
  // 后端API路径不包含ID参数，ID包含在请求体中
  return request(`/api/student-grades`, {
    method: "PUT",
    headers: { "Content-Type": "application/json", "Authorization": `Bearer ${localStorage.getItem("token")}` },
    data: data
  });
};