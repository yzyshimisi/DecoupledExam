import { defineStore } from 'pinia';
import { 
  createStudentGradeAPI, 
  getStudentGradeAPI, 
  getAllStudentGradesAPI, 
  getStudentGradesByStudentIdAPI, 
  getStudentGradesByCourseIdAPI, 
  getStudentGradesByGradeTypeAPI,
  getStudentGradesByTeacherIdAPI,
  updateStudentGradeAPI, 
  deleteStudentGradeAPI,
} from '../../apis';

interface StudentGrade {
  gradeId?: number;
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
}

interface StudentGradeState {
  studentGrades: StudentGrade[];
  currentStudentGrade: StudentGrade | null;
  loading: boolean;
  error: string | null;
}

export const useStudentGradeStore = defineStore('studentGrade', {
  state: (): StudentGradeState => ({
    studentGrades: [],
    currentStudentGrade: null,
    loading: false,
    error: null,
  }),

  actions: {
    // 创建成绩
    async createStudentGrade(gradeData: StudentGrade) {
      this.loading = true;
      this.error = null;
      
      try {
        await createStudentGradeAPI(gradeData);
        // 重新获取所有成绩
        await this.getAllStudentGrades();
      } catch (error: any) {
        this.error = error.message || '创建成绩失败';
        throw error;
      } finally {
        this.loading = false;
      }
    },

    // 根据ID获取成绩
    async getStudentGradeById(gradeId: number) {
      this.loading = true;
      this.error = null;
      
      try {
        const response = await getStudentGradeAPI(gradeId);
        this.currentStudentGrade = response.data;
        return response;
      } catch (error: any) {
        this.error = error.message || '获取成绩失败';
        throw error;
      } finally {
        this.loading = false;
      }
    },

    // 获取所有成绩
    async getAllStudentGrades() {
      this.loading = true;
      this.error = null;
      
      try {
        const response = await getAllStudentGradesAPI();
        this.studentGrades = response.data || [];
        return response;
      } catch (error: any) {
        this.error = error.message || '获取成绩列表失败';
        throw error;
      } finally {
        this.loading = false;
      }
    },

    // 根据学生ID获取成绩
    async getStudentGradesByStudentId(studentId: number) {
      this.loading = true;
      this.error = null;
      
      try {
        const response = await getStudentGradesByStudentIdAPI(studentId);
        this.studentGrades = response.data || [];
        return response;
      } catch (error: any) {
        this.error = error.message || '获取学生成绩失败';
        throw error;
      } finally {
        this.loading = false;
      }
    },

    // 根据课程ID获取成绩
    async getStudentGradesByCourseId(courseId: number) {
      this.loading = true;
      this.error = null;
      
      try {
        const response = await getStudentGradesByCourseIdAPI(courseId);
        this.studentGrades = response.data || [];
        return response;
      } catch (error: any) {
        this.error = error.message || '获取课程成绩失败';
        throw error;
      } finally {
        this.loading = false;
      }
    },

    // 根据成绩类型获取成绩
    async getStudentGradesByGradeType(gradeType: string) {
      this.loading = true;
      this.error = null;
      
      try {
        const response = await getStudentGradesByGradeTypeAPI(gradeType);
        this.studentGrades = response.data || [];
        return response;
      } catch (error: any) {
        this.error = error.message || '获取成绩类型数据失败';
        throw error;
      } finally {
        this.loading = false;
      }
    },

    // 更新成绩
    async updateStudentGrade(gradeData: StudentGrade) {
      this.loading = true;
      this.error = null;
      
      try {
        await updateStudentGradeAPI(gradeData);
        // 重新获取所有成绩
        await this.getAllStudentGrades();
      } catch (error: any) {
        this.error = error.message || '更新成绩失败';
        throw error;
      } finally {
        this.loading = false;
      }
    },

    // 删除成绩
    async deleteStudentGrade(gradeId: number) {
      this.loading = true;
      this.error = null;
      
      try {
        await deleteStudentGradeAPI(gradeId);
        // 重新获取所有成绩
        await this.getAllStudentGrades();
      } catch (error: any) {
        this.error = error.message || '删除成绩失败';
        throw error;
      } finally {
        this.loading = false;
      }
    },

    async getStudentGradesByTeacherId(teacherId: number) {
      this.loading = true;
      this.error = null;

      try {
        const response = await getStudentGradesByTeacherIdAPI(teacherId);
        this.studentGrades = response.data || [];
        return response;
      } catch (error: any) {
        this.error = error.message || '获取教师成绩失败';
        throw error;
      } finally {
        this.loading = false;
      }
    },
    // 重置当前成绩
    resetCurrentStudentGrade() {
      this.currentStudentGrade = null;
    },
  },
});