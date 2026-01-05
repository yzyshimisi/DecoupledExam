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
            <td>{{ grade.gradeId }}</td>
            <td>{{ grade.studentId }}</td>
            <td>{{ grade.courseId }}</td>
            <td>{{ grade.subjectId }}</td>
            <td>{{ grade.gradeType }}</td>
            <td>{{ grade.gradeName }}</td>
            <td>{{ grade.score }}</td>
            <td>{{ grade.fullScore }}</td>
            <td>{{ grade.teacherId }}</td>
            <td>{{ grade.recordTime }}</td>
            <td>{{ grade.remark || '-' }}</td>
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
            <input v-model="currentGrade.recordTime" type="datetime-local" class="input input-bordered" required />
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

const studentGradeStore = useStudentGradeStore();
const grades = ref<any[]>([]);
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
  await loadGrades();
});

// 加载成绩数据
const loadGrades = async () => {
  try {
    // 获取当前教师负责课程的成绩
    const token = localStorage.getItem('token');
    if (!token) {
      console.error('未找到认证令牌');
      grades.value = [];
      return;
    }
      
    // 从JWT token中获取教师ID
    const decoded: any = jwtDecode(token);
    const teacherId = decoded.id;
      
    // 使用教师ID获取成绩
    const response = await studentGradeStore.getStudentGradesByTeacherId(teacherId);
    if (response && response.data) {
      grades.value = response.data;
    } else {
      grades.value = [];
    }
  } catch (error) {
    console.error('加载成绩失败:', error);
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

// 处理筛选器变化
const handleFilterChange = async (filters: any) => {
  try {
    let response;
    if (filters.studentId) {
      response = await studentGradeStore.getStudentGradesByStudentId(filters.studentId);
    } else if (filters.courseId) {
      response = await studentGradeStore.getStudentGradesByCourseId(filters.courseId);
    } else if (filters.gradeType) {
      response = await studentGradeStore.getStudentGradesByGradeType(filters.gradeType);
    } else {
      response = await studentGradeStore.getAllStudentGrades();
    }
    
    if (response && response.data) {
      grades.value = response.data;
    } else {
      grades.value = [];
    }
  } catch (error) {
    console.error('筛选成绩失败:', error);
    grades.value = [];
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