import { request } from '../axios';

export const getStudentGradeAPI = async (gradeId: number) => {
  return request(`/api/student-grades/${gradeId}`, {
    method: "GET",
    headers: { "Content-Type": "application/json", "Authorization": `Bearer ${localStorage.getItem("token")}` }
  });
};

export const getAllStudentGradesAPI = async () => {
  return request("/api/student-grades", {
    method: "GET",
    headers: { "Content-Type": "application/json", "Authorization": `Bearer ${localStorage.getItem("token")}` }
  });
};

export const getStudentGradesByStudentIdAPI = async (studentId: number) => {
  return request(`/api/student-grades/student/${studentId}`, {
    method: "GET",
    headers: { "Content-Type": "application/json", "Authorization": `Bearer ${localStorage.getItem("token")}` }
  });
};

export const getStudentGradesByCourseIdAPI = async (courseId: number) => {
  return request(`/api/student-grades/course/${courseId}`, {
    method: "GET",
    headers: { "Content-Type": "application/json", "Authorization": `Bearer ${localStorage.getItem("token")}` }
  });
};

export const getStudentGradesByGradeTypeAPI = async (gradeType: string) => {
  return request(`/api/student-grades/type/${gradeType}`, {
    method: "GET",
    headers: { "Content-Type": "application/json", "Authorization": `Bearer ${localStorage.getItem("token")}` }
  });
};
// 新增：根据老师ID获取成绩
export const getStudentGradesByTeacherIdAPI = async (teacherId: number) => {
  return request(`/api/teacher/student-grades`, {
    method: "GET",
    headers: { "Content-Type": "application/json", "Authorization": `Bearer ${localStorage.getItem("token")}` },
    params: { teacherId } // 传递老师ID作为查询参数
  });
};