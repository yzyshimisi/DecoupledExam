 import { request } from "../axios";

// 创建考试
export const createExamAPI = (data: {
  title: string;
  paperId: number;
  teacherId: number;
  startTime: string;
  endTime: string;
  examCode: string;
  durationMinute: number;
  allowLateEnter: boolean;
  questionShuffle: boolean;
  optionShuffle: boolean;
  preventScreenSwitch: boolean;
  passingScore: number;
  autoSubmit: boolean;
  allowViewPaper: boolean;
  allowViewScore: boolean;
  multiChoicePartialRatio: number;
  fillCaseSensitive: boolean;
  fillIgnoreSymbols: boolean;
  fillManualMark: boolean;
  peerReview: boolean;
}) => {
  return request<{ code: number; data: number; msg: string }>(
    "/api/exam",
    {
      method: "POST",
      data,
    }
  );
};

// 查询考试列表
export const getExamListAPI = (params?: {
  status?: string;
  teacherId?: number;
}) => {
  return request<{ code: number; data: any[]; msg: string }>(
    "/api/exam",
    {
      method: "GET",
      params,
    }
  );
};

// 根据ID获取考试详情
export const getExamDetailAPI = (examId: number) => {
  return request<{ code: number; data: any; msg: string }>(
    `/api/exam/${examId}`,
    {
        method: "GET",
        headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${localStorage.getItem("token")}`,
            "Exam-Token": localStorage.getItem("examToken")
        },
    }
  );
};

// 根据考试记录ID获取考试记录
export const getExamRecordAPI = (recordId: number) => {
  return request<{ code: number; data: any; msg: string }>(
    `/api/exam/record/${recordId}`,
    {
      method: "GET",
    }
  );
};

// 更新考试信息
export const updateExamAPI = (examId: number, data: {
  title?: string;
  startTime?: string;
  endTime?: string;
  examCode?: string;
}) => {
  return request<{ code: number; data: boolean; msg: string }>(
    `/api/exam/${examId}/update`,
    {
      method: "POST",
      data,
    }
  );
};

// 为考试添加考生
export const addStudentsToExamAPI = (examId: number, studentIds: number[]) => {
  return request<{ code: number; data: boolean; msg: string }>(
    `/api/exam/${examId}/students`,
    {
      method: "POST",
      data: studentIds,
    }
  );
};

// 从考试中移除考生
export const removeStudentsFromExamAPI = (examId: number, studentIds: number[]) => {
  return request<{ code: number; data: boolean; msg: string }>(
    `/api/exam/${examId}/students`,
    {
      method: "DELETE",
      data: studentIds,
    }
  );
};

// 删除考试
export const deleteExamAPI = (examId: number) => {
  return request<{ code: number; data: boolean; msg: string }>(
    `/api/exam/${examId}/delete`,
    {
      method: "POST",
    }
  );
};

// 获取考试的所有考生
export const getExamStudentsAPI = (examId: number) => {
  return request<{ code: number; data: any[]; msg: string }>(
    `/api/exam/${examId}/students`,
    {
      method: "GET",
    }
  );
};

// 导出考试监考数据
export const exportExamDataAPI = (examId: number) => {
  return request<Blob>(
    `/api/exam/${examId}/export`,
    {
      method: "GET",
      responseType: "blob",
    }
  );
};

// 将考试发布到课程
export const publishExamToCourseAPI = (examId: number, courseIds: number[]) => {
  return request<{ code: number; data: boolean; msg: string }>(
    `/api/exam/${examId}/publish`,
    {
      method: "POST",
      data: courseIds,
    }
  );
};

// 从课程中移除考试发布
export const unpublishExamFromCourseAPI = (examId: number, courseIds: number[]) => {
  return request<{ code: number; data: boolean; msg: string }>(
    `/api/exam/${examId}/unpublish`,
    {
      method: "POST",
      data: courseIds,
    }
  );
};

// 获取考试已发布的课程列表
export const getExamPublishedCoursesAPI = (examId: number) => {
  return request<{ code: number; data: number[]; msg: string }>(
    `/api/exam/${examId}/courses`,
    {
      method: "GET",
    }
  );
};

// 获取课程中的考试列表
export const getCourseExamsAPI = (courseId: number) => {
  return request<{ code: number; data: number[]; msg: string }>(
    `/api/exam/course/${courseId}/exams`,
    {
      method: "GET",
    }
  );
};

// 获取学生自己的考试列表
export const getStudentExamsAPI = () => {
  return request<{ code: number; data: any[]; msg: string }>(
    "/api/exam/student/exams",
    {
      method: "GET",
    }
  );
};


// 获取学生自己的考试通知列表
export const getStudentNotificationsAPI = () => {
  return request<{ code: number; data: any[]; msg: string }>(
    "/api/exam/student/notifications",
    {
      method: "GET",
    }
  );
};

// 获取学生特定考试的通知
export const getExamNotificationsAPI = (examId: number) => {
  return request<{ code: number; data: any[]; msg: string }>(
    `/api/exam/student/notifications/exam/${examId}`,
    {
      method: "GET",
    }
  );
};

// 为所有现有考试创建通知(管理员专用)
export const initExamNotificationsAPI = () => {
  return request<{ code: number; data: number; msg: string }>(
    "/api/exam/notifications/init",
    {
      method: "POST",
    }
  );
};