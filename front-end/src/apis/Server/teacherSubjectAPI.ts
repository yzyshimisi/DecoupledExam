import { request } from '../axios';


/**
 * 教师绑定学科
 * @param subjectId 学科ID
 * @param isMain 是否为主要学科（1=是，0=否）
 * @param token JWT token
 * @returns 接口响应
 */
export const bindTeacherSubjectAPI = async (
  subjectId: number,
  isMain: number,
  token: string
) => {
  try {
    const response = await request("/api/teacher/subject", {
      method: "POST",
      headers: {"Content-Type":"application/json", "Authorization": `Bearer ${token}`},
      data: {
        subjectId,
        isMain
      }
    });
    return response;
  } catch (error) {
    console.error('教师绑定学科失败:', error);
    throw error;
  }
};

/**
 * 获取教师的绑定学科列表
 * @param token JWT token
 * @returns 接口响应
 */
export const getTeacherSubjectsAPI = async (token: string) => {
  try {
    const response = await request("/api/teacher/subject", {
      method: "GET",
      headers: {"Content-Type":"application/json", "Authorization": `Bearer ${token}`}
    });
    return response;
  } catch (error) {
    console.error('获取教师学科列表失败:', error);
    throw error;
  }
};

/**
 * 删除教师的学科绑定
 * @param subjectId 学科ID
 * @param token JWT token
 * @returns 接口响应
 */
export const deleteTeacherSubjectAPI = async (
  subjectId: number,
  token: string
) => {
  try {
    const response = await request("/api/teacher/subject", {
      method: "DELETE",
      headers: {"Content-Type":"application/json", "Authorization": `Bearer ${token}`},
      params: {subjectId}
    });
    return response;
  } catch (error) {
    console.error('删除教师学科绑定失败:', error);
    throw error;
  }
};