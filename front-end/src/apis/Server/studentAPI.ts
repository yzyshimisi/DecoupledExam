import { request } from "../axios";

// 获取所有学生列表
export const getAllStudentsAPI = (params?: {
  page?: number;
  size?: number;
  search?: string;
}) => {
  return request<{ code: number; data: any[]; msg: string }>(
    "/api/student",
    {
      method: "GET",
      params,
    }
  );
};

// 根据ID获取学生详情
export const getStudentByIdAPI = (studentId: number) => {
  return request<{ code: number; data: any; msg: string }>(
    `/api/student/${studentId}`,
    {
      method: "GET",
    }
  );
};

// 搜索学生
export const searchStudentsAPI = (query: string) => {
  return request<{ code: number; data: any[]; msg: string }>(
    "/api/student/search",
    {
      method: "GET",
      params: { query },
    }
  );
};

// 注意：后端可能没有直接提供此接口，需要根据实际后端API调整
// 作为替代方案，可以获取所有学生，然后排除已在考试中的学生
export const getStudentsNotInExamAPI = (examId: number, params?: {
  page?: number;
  size?: number;
  search?: string;
}) => {
  // 这里使用通用的学生搜索接口，实际实现需要后端支持
  return request<{ code: number; data: any[]; msg: string }>(
    "/api/student/search",
    {
      method: "GET",
      params: { ...params, examId }, // 传递examId参数以获取不在考试中的学生
    }
  );
};