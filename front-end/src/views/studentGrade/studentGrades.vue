<template>
  <!-- 学生成绩查询页面 -->
  <div class="student-grade-management">
    <h1>我的成绩</h1>
    
    <!-- 搜索和操作区域 -->
    <div class="action-bar">
      <div class="search-group">
        <label for="courseIdSearch">课程ID:</label>
        <input type="number" id="courseIdSearch" v-model="courseIdSearch" placeholder="请输入课程ID" />
        <button @click="searchByCourseId">搜索</button>
        <button @click="resetSearch">重置</button>
      </div>
    </div>
    
    <!-- 成绩列表 -->
    <div class="grade-list">
      <table>
        <thead>
          <tr>
            <th>课程ID</th>
            <th>科目ID</th>
            <th>成绩类型</th>
            <th>成绩名称</th>
            <th>得分</th>
            <th>满分</th>
            <th>教师ID</th>
            <th>记录时间</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="grade in grades" :key="grade.gradeId">
            <td>{{ grade.courseId }}</td>
            <td>{{ grade.subjectId }}</td>
            <td>{{ grade.gradeType }}</td>
            <td>{{ grade.gradeName }}</td>
            <td>{{ grade.score }}</td>
            <td>{{ grade.fullScore }}</td>
            <td>{{ grade.teacherId }}</td>
            <td>{{ grade.recordTime }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { getStudentGradesByStudentIdAPI } from '../../apis';

const grades = ref<any[]>([]);
const courseIdSearch = ref<number>(0);

// 初始化加载成绩数据
onMounted(async () => {
  await loadGrades();
});

// 加载成绩数据
const loadGrades = async () => {
  try {
    // 从JWT token中解析用户ID
    const token = localStorage.getItem('token');
    if (!token) {
      console.error('未找到认证令牌');
      grades.value = [];
      return;
    }
    
    // 解析JWT token获取用户ID
    const { jwtDecode } = await import('jwt-decode');
    const decoded = jwtDecode(token);
    const userId = decoded['userId'];
    
    // 获取当前学生的所有成绩
    const response = await getStudentGradesByStudentIdAPI(userId);
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

// 搜索成绩（按课程ID）
const searchByCourseId = async () => {
  const courseId = parseInt(courseIdSearch.value);
  if (isNaN(courseId) || courseId === 0) {
    await loadGrades();
    return;
  }
  
  try {
    // 从JWT token中解析用户ID
    const token = localStorage.getItem('token');
    if (!token) {
      console.error('未找到认证令牌');
      grades.value = [];
      return;
    }
    
    // 解析JWT token获取用户ID
    const { jwtDecode } = await import('jwt-decode');
    const decoded = jwtDecode(token);
    const userId = decoded['userId'];
    
    // 先获取当前学生的所有成绩
    const response = await getStudentGradesByStudentIdAPI(userId);
    if (response && response.data) {
      // 然后按课程ID筛选
      grades.value = response.data.filter(grade => grade.courseId === courseId);
    } else {
      grades.value = [];
    }
  } catch (error) {
    console.error('搜索课程成绩失败:', error);
    grades.value = [];
  }
};

// 重置搜索
const resetSearch = async () => {
  courseIdSearch.value = 0;
  await loadGrades();
};
</script>

<style scoped>
.student-grade-management {
  padding: 20px;
}

.action-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.action-bar input {
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
}

.action-bar button {
  padding: 8px 16px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.action-bar button:hover {
  background-color: #0056b3;
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
</style>