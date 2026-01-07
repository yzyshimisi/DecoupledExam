<template>
  <div class="student-grade-management">
    <h1>成绩管理</h1>
    
    <!-- 搜索和操作区域 -->
    <div class="action-bar">
      <StudentGradeFilters @filter-change="handleFilterChange" />
      <div class="button-group">
        <button @click="showCreateModal = true" class="btn btn-primary">添加成绩</button>
      </div>
    </div>
    
    <!-- 成绩列表 -->
    <div class="grade-list">
      <table>
        <thead>
          <tr>
            <th>成绩ID</th>
            <th>学生ID</th>
            <th>课程ID</th>
            <th>科目ID</th>
            <th>成绩类型</th>
            <th>成绩名称</th>
            <th>得分</th>
            <th>满分</th>
            <th>教师ID</th>
            <th>记录时间</th>
            <th>备注</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="grade in grades" :key="grade.gradeId">
            <td>{{ grade.gradeId || grade.grade_id }}</td>
            <td>{{ grade.studentId || grade.student_id }}</td>
            <td>{{ grade.courseId || grade.course_id }}</td>
            <td>{{ grade.subjectId || grade.subject_id }}</td>
            <td>{{ grade.gradeType || grade.grade_type }}</td>
            <td>{{ grade.gradeName || grade.grade_name }}</td>
            <td>{{ grade.score }}</td>
            <td>{{ grade.fullScore || grade.full_score }}</td>
            <td>{{ grade.teacherId || grade.teacher_id }}</td>
            <td>{{ grade.recordTime || grade.record_time }}</td>
            <td>{{ grade.remark || grade.remark || '-' }}</td>
            <td>
              <button @click="editGrade(grade)" class="btn btn-sm btn-outline mr-2">编辑</button>
              <button @click="deleteGrade(grade.gradeId)" class="btn btn-sm btn-error">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    
    <!-- 创建/编辑成绩弹窗 -->
    <div v-if="showCreateModal || showEditModal" class="modal modal-open">
      <div class="modal-box">
        <h3 class="font-bold text-lg">{{ showCreateModal ? '添加成绩' : '编辑成绩' }}</h3>
        
        <form @submit.prevent="showCreateModal ? createGrade() : updateGrade()">
          <div class="form-control">
            <label class="label">学生ID</label>
            <input v-model="currentGrade.studentId" type="number" class="input input-bordered" required />
          </div>
          
          <div class="form-control">
            <label class="label">课程ID</label>
            <input v-model="currentGrade.courseId" type="number" class="input input-bordered" required />
          </div>
          
          <div class="form-control">
            <label class="label">科目ID</label>
            <input v-model="currentGrade.subjectId" type="number" class="input input-bordered" required />
          </div>
          
          <div class="form-control">
            <label class="label">成绩类型</label>
            <input v-model="currentGrade.gradeType" type="text" class="input input-bordered" required />
          </div>
          
          <div class="form-control">
            <label class="label">成绩名称</label>
            <input v-model="currentGrade.gradeName" type="text" class="input input-bordered" required />
          </div>
          
          <div class="form-control">
            <label class="label">得分</label>
            <input v-model="currentGrade.score" type="number" step="0.01" class="input input-bordered" required />
          </div>
          
          <div class="form-control">
            <label class="label">满分</label>
            <input v-model="currentGrade.fullScore" type="number" step="0.01" class="input input-bordered" required />
          </div>
          
          <div class="form-control">
            <label class="label">教师ID</label>
            <input v-model="currentGrade.teacherId" type="number" class="input input-bordered" required />
          </div>
          
          <div class="form-control">
            <label class="label">记录时间</label>
            <input v-model="currentGrade.recordTime" type="datetime-local" class="input input-bordered" />
          </div>
          
          <div class="form-control">
            <label class="label">备注</label>
            <input v-model="currentGrade.remark" type="text" class="input input-bordered" />
          </div>
          
          <div class="modal-action">
            <button type="submit" class="btn btn-primary">{{ showCreateModal ? '创建' : '更新' }}</button>
            <button type="button" class="btn" @click="closeModal">取消</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useStudentGradeStore } from '../../stores/service/studentGradeStore';
import StudentGradeFilters from '../../components/studentGrade/StudentGradeFilters.vue';
import * as jwtDecode from 'jwt-decode';

const studentGradeStore = useStudentGradeStore();
const grades = ref<any[]>([]);
const allGrades = ref<any[]>([]); // 保存所有成绩的副本，用于过滤
const showCreateModal = ref(false);
const showEditModal = ref(false);
const currentGrade = ref<any>({
  studentId: 0,
  courseId: 0,
  subjectId: 0,
  gradeType: '',
  gradeName: '',
  score: 0,
  fullScore: 100,
  teacherId: 0,
  recordTime: new Date().toISOString().slice(0, 16),
  remark: ''
});

// 初始化加载成绩数据
onMounted(async () => {
  console.log('开始加载成绩数据...');
  await loadGrades();
});

// 加载成绩数据
const loadGrades = async () => {
  console.log('loadGrades函数开始执行');
  
  try {
    // 获取当前用户的令牌
    const token = localStorage.getItem('token');
    console.log('从localStorage获取到token:', token ? '存在' : '不存在');
    
    if (!token) {
      console.error('未找到认证令牌');
      grades.value = [];
      allGrades.value = [];
      return;
    }
      
    // 从JWT token中获取用户信息
    let decoded: any;
    try {
      decoded = jwtDecode.jwtDecode(token);
      console.log('JWT解码成功，解码后数据:', decoded);
    } catch (error) {
      console.error('JWT解码失败:', error);
      grades.value = [];
      allGrades.value = [];
      return;
    }
    
    // 根据项目规范，JWT中用户ID字段名为'id'，用户类型字段名为'userType'
    const userId = decoded.id;
    const userType = decoded.userType || decoded.role; // 尝试userType或role字段
    
    console.log('从JWT获取的用户信息:', { userId, userType });
    
    // 检查用户ID和用户类型是否有效
    if (!userId) {
      console.error('错误：无法从JWT中获取用户ID，userId为:', userId);
      grades.value = [];
      allGrades.value = [];
      return;
    }
    
    if (!userType) {
      console.error('错误：无法从JWT中获取用户类型，userType为:', userType);
      grades.value = [];
      allGrades.value = [];
      return;
    }
    
    // 根据用户类型获取对应的成绩数据
    let response;
    console.log('开始根据用户类型获取数据，用户类型:', userType);
    
    if (userType === 'STUDENT' || userType === 'student') {
      console.log('用户类型为学生，准备调用getStudentGradesByStudentId，参数:', userId);
      response = await studentGradeStore.getStudentGradesByStudentId(userId);
      console.log('getStudentGradesByStudentId调用完成，响应:', response);
    } else if (userType === 'TEACHER' || userType === 'teacher') {
      console.log('用户类型为教师，准备调用getStudentGradesByTeacherId，参数:', userId);
      response = await studentGradeStore.getStudentGradesByTeacherId(userId);
      console.log('getStudentGradesByTeacherId调用完成，响应:', response);
    } else {
      console.log('用户类型为其他(可能是管理员)，准备调用getAllStudentGrades');
      response = await studentGradeStore.getAllStudentGrades();
      console.log('getAllStudentGrades调用完成，响应:', response);
    }
      
    if (response && response.data) {
      allGrades.value = response.data;
      grades.value = response.data; // 显示所有获取到的成绩
      console.log('成功获取到成绩数据，总数:', response.data.length);
      console.log('获取到的成绩数据:', response.data);
    } else {
      allGrades.value = [];
      grades.value = [];
      console.log('未获取到成绩数据，response为:', response);
    }
  } catch (error) {
    console.error('加载成绩失败，详细错误信息:', error);
    console.error('错误堆栈:', error instanceof Error ? error.stack : 'No stack');
    allGrades.value = [];
    grades.value = [];
  }
};

// 创建成绩
const createGrade = async () => {
  try {
    await studentGradeStore.createStudentGrade(currentGrade.value);
    await loadGrades();
    closeModal();
  } catch (error) {
    console.error('创建成绩失败:', error);
  }
};

// 编辑成绩
const editGrade = (grade: any) => {
  currentGrade.value = { ...grade };
  showEditModal.value = true;
  showCreateModal.value = false;
};

// 更新成绩
const updateGrade = async () => {
  try {
    await studentGradeStore.updateStudentGrade(currentGrade.value);
    await loadGrades();
    closeModal();
  } catch (error) {
    console.error('更新成绩失败:', error);
  }
};

// 删除成绩
const deleteGrade = async (gradeId: number) => {
  if (confirm('确定要删除这个成绩吗？')) {
    try {
      await studentGradeStore.deleteStudentGrade(gradeId);
      await loadGrades();
    } catch (error) {
      console.error('删除成绩失败:', error);
    }
  }
};

// 关闭弹窗
const closeModal = () => {
  showCreateModal.value = false;
  showEditModal.value = false;
  currentGrade.value = {
    studentId: 0,
    courseId: 0,
    subjectId: 0,
    gradeType: '',
    gradeName: '',
    score: 0,
    fullScore: 100,
    teacherId: 0,
    recordTime: new Date().toISOString().slice(0, 16),
    remark: ''
  };
};

// 处理筛选器变化 - 实现三者模糊查询
const handleFilterChange = (filters: any) => {
  console.log('处理筛选器变化:', filters);
  try {
    // 从所有成绩中进行过滤
    let filteredGrades = [...allGrades.value];
    console.log('开始过滤，总成绩数:', allGrades.value.length);
    
    // 根据学生ID进行模糊过滤（如果提供了学生ID）
    if (filters.studentId !== null && filters.studentId !== '') {
      console.log('按学生ID过滤，过滤值:', filters.studentId);
      filteredGrades = filteredGrades.filter((grade: any) => 
        grade.studentId && 
        grade.studentId.toString().includes(filters.studentId.toString())
      );
      console.log('学生ID过滤后剩余:', filteredGrades.length);
    }
    
    // 根据课程ID进行模糊过滤（如果提供了课程ID）
    if (filters.courseId !== null && filters.courseId !== '') {
      console.log('按课程ID过滤，过滤值:', filters.courseId);
      filteredGrades = filteredGrades.filter((grade: any) => 
        grade.courseId && 
        grade.courseId.toString().includes(filters.courseId.toString())
      );
      console.log('课程ID过滤后剩余:', filteredGrades.length);
    }
    
    // 根据成绩类型进行模糊过滤（如果提供了成绩类型）
    if (filters.gradeType !== null && filters.gradeType !== '') {
      console.log('按成绩类型过滤，过滤值:', filters.gradeType);
      filteredGrades = filteredGrades.filter((grade: any) => 
        grade.gradeType && 
        grade.gradeType.toLowerCase().includes(filters.gradeType.toLowerCase())
      );
      console.log('成绩类型过滤后剩余:', filteredGrades.length);
    }
    
    // 更新显示的成绩列表
    grades.value = filteredGrades;
    console.log('最终显示成绩数:', filteredGrades.length);
  } catch (error) {
    console.error('筛选成绩失败:', error);
    grades.value = allGrades.value; // 如果出错，显示所有成绩
  }
};
</script>

<style scoped>
.student-grade-management {
  padding: 20px;
}

.action-bar {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}

.button-group {
  display: flex;
  gap: 10px;
}

.grade-list {
  overflow-x: auto;
}

.grade-list table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 10px;
}

.grade-list th,
.grade-list td {
  padding: 10px;
  text-align: left;
  border: 1px solid #ddd;
}

.grade-list th {
  background-color: #f2f2f2;
  font-weight: bold;
}

.grade-list tr:nth-child(even) {
  background-color: #f9f9f9;
}

.mr-2 {
  margin-right: 0.5rem;
}
</style>