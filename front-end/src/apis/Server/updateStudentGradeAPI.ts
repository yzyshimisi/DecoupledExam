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
  recordTime: string;
  remark?: string;
}) => {
  return request(`/api/student-grades/${data.gradeId}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json", "Authorization": `Bearer ${localStorage.getItem("token")}` },
    data: data
  });
};