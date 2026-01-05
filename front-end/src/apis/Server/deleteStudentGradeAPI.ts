import { request } from '../axios';

export const deleteStudentGradeAPI = async (gradeId: number) => {
  return request(`/api/student-grades/${gradeId}`, {
    method: "DELETE",
    headers: { "Content-Type": "application/json", "Authorization": `Bearer ${localStorage.getItem("token")}` }
  });
};