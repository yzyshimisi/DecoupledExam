<template>
  <div class="container mx-auto px-4 py-8 max-w-6xl">
    <!-- 返回按钮 -->
    <div class="mb-6">
      <button 
        @click="goBack" 
        class="btn btn-ghost btn-sm flex items-center"
      >
        <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
        </svg>
        返回考试详情
      </button>
    </div>

    <!-- 页面标题 -->
    <div class="mb-8">
      <h1 class="text-3xl font-bold text-base-content">{{ exam?.title }}</h1>
      <p class="text-base-content/70 mt-2">考试ID: {{ exam?.id }} | 管理参加考试的学生</p>
    </div>

    <!-- 操作面板 -->
    <div class="card bg-base-100 shadow-xl border border-base-200 mb-6">
      <div class="card-body">
        <div class="flex flex-col md:flex-row gap-4">
          <!-- 搜索学生 -->
          <div class="flex-1">
            <div class="form-control">
              <label class="label">
                <span class="label-text">搜索学生</span>
              </label>
              <div class="flex gap-2">
                <input
                  v-model="searchQuery"
                  type="text"
                  placeholder="输入学生ID"
                  class="input input-bordered flex-1"
                  @keyup.enter="searchStudents"
                />
                <button 
                  class="btn btn-primary"
                  @click="searchStudents"
                >
                  搜索
                </button>
              </div>
            </div>
          </div>

          <!-- 添加学生按钮 -->
          <div class="flex items-end">
            <button 
              class="btn btn-primary h-12"
              @click="showAddModal = true"
            >
              <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
              </svg>
              添加学生
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 学生列表 -->
    <div class="card bg-base-100 shadow-xl border border-base-200">
      <div class="card-body">
        <h2 class="card-title text-xl font-semibold text-base-content mb-6 flex items-center">
          <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197m13.5-9a2.5 2.5 0 11-5 0 2.5 2.5 0 015 0z" />
          </svg>
          已添加学生 ({{ students.length }})
        </h2>

        <div class="overflow-x-auto">
          <table class="table table-zebra">
            <thead>
              <tr>
                <th>学号</th>

                <th>状态</th>
                <th>客观题得分</th>
                <th>主观题得分</th>
                <th>总成绩</th>
                <th>交卷时间</th>
                <th>AI评语</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="student in students" :key="student.recordId">
                <td>{{ student.studentId }}</td>
                <td>
                  <span class="badge" :class="getStudentStatusClass(student.status)">
                    {{ student.statusDescription }}
                  </span>
                </td>
                <td>
                  <span v-if="student.objectiveScore !== null">{{ student.objectiveScore }}</span>
                  <span v-else class="text-base-content/50">-</span>
                </td>
                <td>
                  <span v-if="student.subjectiveScore !== null">{{ student.subjectiveScore }}</span>
                  <span v-else class="text-base-content/50">-</span>
                </td>
                <td>
                  <span v-if="student.totalScore !== null">{{ student.totalScore }}</span>
                  <span v-else class="text-base-content/50">-</span>
                </td>
                <td>
                  <span v-if="student.submitTime">{{ student.submitTime }}</span>
                  <span v-else class="text-base-content/50">-</span>
                </td>
                <td>
                  <span v-if="student.aiComment" class="tooltip" :data-tip="student.aiComment">{{ student.aiComment.substring(0, 10) }}...</span>
                  <span v-else class="text-base-content/50">-</span>
                </td>
                <td>
                  <div class="flex gap-2">
                    <button 
                      class="btn btn-xs btn-outline btn-error"
                      @click="removeStudent(student)"
                    >
                      移除
                    </button>
                    <button 
                      class="btn btn-xs btn-outline"
                      @click="viewStudentDetail(student)"
                    >
                      详情
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- 分页 -->
        <div class="flex justify-center mt-6" v-if="totalPages > 1">
          <div class="join">
            <button 
              class="join-item btn" 
              :disabled="currentPage === 1"
              @click="currentPage--"
            >
              «
            </button>
            <button class="join-item btn">第 {{ currentPage }} 页</button>
            <button 
              class="join-item btn" 
              :disabled="currentPage >= totalPages"
              @click="currentPage++"
            >
              »
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 添加学生模态框 -->
    <dialog v-if="showAddModal" class="modal modal-open">
      <div class="modal-box max-w-4xl">
        <h3 class="font-bold text-lg mb-4">添加学生到考试</h3>
        
        <!-- 输入学生ID -->
        <div class="form-control mb-4">
          <label class="label">
            <span class="label-text">输入学生ID（用逗号分隔多个ID）</span>
          </label>
          <input
            v-model="addStudentQuery"
            type="text"
            placeholder="例如: 1,2,3 或 4"
            class="input input-bordered"
          />
        </div>

        <!-- 可添加的学生列表（用于确认） -->
        <div class="overflow-x-auto max-h-60" v-if="parsedStudentIds.length > 0">
          <table class="table table-zebra">
            <thead>
              <tr>
                <th>序号</th>
                <th>学生ID</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(id, index) in parsedStudentIds" :key="index">
                <td>{{ index + 1 }}</td>
                <td>{{ id }}</td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- 操作按钮 -->
        <div class="modal-action">
          <button 
            class="btn btn-primary"
            :disabled="!addStudentQuery.trim() || parsedStudentIds.length === 0"
            @click="addStudentsByIds"
          >
            添加学生 ({{ parsedStudentIds.length }})
          </button>
          <button 
            class="btn"
            @click="closeAddModal"
          >
            取消
          </button>
        </div>
      </div>
    </dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useRequest } from 'vue-hooks-plus';
import { getExamDetailAPI, getExamStudentsAPI, addStudentsToExamAPI, removeStudentsFromExamAPI } from '../../apis';

const router = useRouter();
const route = useRoute();

const examId = route.params.id as string;

// 状态响应式变量
const exam = ref<any>(null);
const students = ref<any[]>([]);
const originalStudents = ref<any[]>([]);
const searchQuery = ref('');
const addStudentQuery = ref('');
const showAddModal = ref(false);
const currentPage = ref(1);
const itemsPerPage = ref(10);

// 计算属性：解析学生ID
const parsedStudentIds = computed(() => {
  if (!addStudentQuery.value.trim()) return [];
  return addStudentQuery.value
    .split(',')
    .map(id => id.trim())
    .filter(id => id && !isNaN(Number(id)))
    .map(id => parseInt(id));
});

// 计算属性：总页数
const totalPages = computed(() => {
  return Math.ceil(students.value.length / itemsPerPage.value);
});

// 获取考试详情
const fetchExamDetail = () => {
  useRequest(() => getExamDetailAPI(parseInt(examId)), {
    onSuccess(res) {
      if (res['code'] === 200) {
        exam.value = res['data'] || null;
      }
    },
    onError(err) {
      console.error('获取考试详情失败:', err);
      router.push('/exam');
    }
  });
};

// 获取学生列表
const fetchStudents = () => {
  useRequest(() => getExamStudentsAPI(parseInt(examId)), {
    onSuccess(res) {
      if (res['code'] === 200) {
        // 处理返回的ExamRecord对象，转换为适合前端显示的格式
        const examRecords = res['data'] || [];
        const processedStudents = examRecords.map((record: any) => ({
          recordId: record.recordId, // 记录ID
          examId: record.examId, // 考试ID
          studentId: record.studentId, // 学生ID
          studentName: record.studentName || `学生${record.studentId}`, // 学生姓名
          objectiveScore: record.objectiveScore, // 客观题得分
          subjectiveScore: record.subjectiveScore, // 主观题得分
          totalScore: record.totalScore, // 总成绩
          aiComment: record.aiComment, // AI评语
          status: record.status, // 状态
          statusDescription: record.statusDescription, // 状态描述
          submitTime: record.submitTime ? new Date(record.submitTime).toLocaleString('zh-CN') : null // 交卷时间
        }));
        
        students.value = processedStudents;
        originalStudents.value = processedStudents; // 保存原始数据用于搜索
      }
    },
    onError(err) {
      console.error('获取学生列表失败:', err);
    }
  });
};

// 根据ExamRecord确定参加情况
const getAttendanceStatusFromRecord = (record: any) => {
  if (record.status === '0') return 'not_started'; // 未考
  if (record.status === '2') return 'completed'; // 已阅
  if (record.submitTime) return 'present'; // 已提交
  return 'not_started'; // 默认未开始
};

// 搜索学生 - 只在已添加到考试的学生中搜索
const searchStudents = () => {
  if (!searchQuery.value.trim()) {
    fetchStudents(); // 如果搜索内容为空，重新获取全部学生
    return;
  }
  
  // 在考试列表中的学生中过滤
  const filteredStudents = originalStudents.value.filter((student: any) => 
    String(student.studentId).includes(searchQuery.value.trim()) || 
    student.studentName.toLowerCase().includes(searchQuery.value.trim().toLowerCase())
  );
  
  students.value = filteredStudents;
};

// 状态相关方法
const getStudentStatusClass = (status: string) => {
  switch (status) {
    case '0': return 'badge-info'; // 未考
    case '1': return 'badge-warning'; // 待阅
    case '2': return 'badge-success'; // 已阅
    default: return 'badge-ghost';
  }
};

// 操作方法
const goBack = () => {
  router.push(`/exam/${examId}`);
};

const removeStudent = (student: any) => {
  if (confirm(`确定要将学生 ${student.studentName} 从考试中移除吗？`)) {
    useRequest(() => removeStudentsFromExamAPI(parseInt(examId), [student.studentId]), {
      onSuccess(res) {
        if (res['code'] === 200 && res['data']) {
          alert('学生移除成功！');
          fetchStudents(); // 重新获取学生列表
        } else {
          alert('学生移除失败！');
        }
      },
      onError(err) {
        console.error('移除学生失败:', err);
        alert('学生移除失败！');
      }
    });
  }
};

const viewStudentDetail = (student: any) => {
  console.log('查看学生详情:', student);
  // 显示与后端返回的ExamRecord对象一致的详细信息
  const detailInfo = `
记录ID: ${student.recordId || 'N/A'}
考试ID: ${student.examId || 'N/A'}
学生ID: ${student.studentId || 'N/A'}
客观题得分: ${student.objectiveScore || 'N/A'}
主观题得分: ${student.subjectiveScore || 'N/A'}
总成绩: ${student.totalScore || 'N/A'}
AI评语: ${student.aiComment || 'N/A'}
状态: ${student.status} (${student.statusDescription || 'N/A'})
交卷时间: ${student.submitTime || 'N/A'}`;
  
  alert('学生考试详情:\n' + detailInfo);
};

const addStudentsByIds = () => {
  if (parsedStudentIds.value.length === 0) {
    alert('请至少输入一个学生ID');
    return;
  }
  
  useRequest(() => addStudentsToExamAPI(parseInt(examId), parsedStudentIds.value), {
    onSuccess(res) {
      if (res['code'] === 200 && res['data']) {
        alert('学生添加成功！');
        fetchStudents(); // 重新获取学生列表
        closeAddModal();
      } else {
        alert('学生添加失败：' + (res && res['msg'] ? res['msg'] : '未知错误'));
      }
    },
    onError(err) {
      console.error('添加学生失败:', err);
      alert('添加学生失败：' + (err.message || '网络错误'));
    }
  });
};

const closeAddModal = () => {
  showAddModal.value = false;
  addStudentQuery.value = '';
};

// 页面加载时获取数据
onMounted(() => {
  fetchExamDetail();
  fetchStudents();
});
</script>