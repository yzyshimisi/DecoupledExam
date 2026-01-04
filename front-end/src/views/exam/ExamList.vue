<template>
  <div class="container mx-auto px-4 py-8">
    <!-- 页面标题 -->
    <div class="mb-8">
      <h1 class="text-3xl font-bold text-base-content">
        {{ userType === '2' ? '我的考试' : userType === '0' ? '考试管理' : '我的考试' }}
      </h1>
      <p class="text-base-content/70 mt-2">
        {{ userType === '2' ? '查看您参与的考试' : userType === '0' ? '管理所有考试' : '管理您创建的考试' }}
      </p>
    </div>

    <!-- 顶部操作栏 -->
    <div class="mb-6 flex flex-col sm:flex-row gap-4 items-start sm:items-center justify-between">
      <div class="flex flex-wrap gap-3">
        <!-- 状态筛选 -->
        <div class="flex items-center gap-2">

        </div>

        <!-- 教师筛选（仅教务老师） -->
        <div v-if="userType === '0'" class="flex items-center gap-2">
          <span class="text-base-content/70">教师:</span>
          <input
            v-model="teacherFilter"
            type="text"
            placeholder="输入教师ID"
            class="input input-bordered input-sm w-40"
            @change="fetchExams"
          />
        </div>
      </div>

      <!-- 批量操作按钮（仅教务老师） -->
      <div class="flex gap-2">
        <button 
          v-if="userType === '0'" 
          class="btn btn-sm btn-outline"
        >
          批量管理
        </button>
        <button 
          v-if="userType !== '2'" 
          class="btn btn-primary btn-sm"
          @click="createExam"
        >
          {{ userType === '1' ? '创建考试' : '创建考试' }}
        </button>
      </div>
    </div>

    <!-- 考试列表 -->
    <div class="grid grid-cols-1 lg:grid-cols-2 xl:grid-cols-3 gap-6">
      <div 
        v-for="exam in exams" 
        :key="exam.id || exam.exam_id"
        class="card bg-base-100 shadow-lg border border-base-200 hover:shadow-xl transition-shadow"
      >
        <div class="card-body p-6">
          <div class="flex justify-between items-start">
            <div>
              <h2 class="card-title text-lg font-bold text-base-content">{{ exam.title }}</h2>
              <p class="text-sm text-base-content/70 mt-1">ID: {{ exam.id || exam.exam_id || exam.examId || exam.examid }}</p>
            </div>
            <div class="badge" :class="getStatusClass(exam.status)">
              {{ getStatusText(exam.status) }}
            </div>
          </div>

          <div class="mt-4 space-y-2">
            <div class="flex items-center text-sm text-base-content/80">
              <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
              <span>{{ formatDateTime(exam.startTime) }} - {{ formatDateTime(exam.endTime) }}</span>
            </div>

            <div v-if="userType !== '2'" class="flex items-center text-sm text-base-content/80">
              <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
              </svg>
              <span>创建者ID: {{ exam.teacherName || exam.teacher_name || exam.teacherId || exam.teacher_id || '未知' }}</span>
            </div>
          </div>

          <!-- 防作弊设置 -->
          <div v-if="exam.antiCheatSettings || exam.questionShuffle || exam.preventScreenSwitch" class="mt-3 pt-3 border-t border-base-200">
            <div class="flex flex-wrap gap-2">
              <div v-if="exam.questionShuffle || exam.question_shuffle || exam.antiCheatSettings?.questionShuffle" class="badge badge-info badge-sm">题目乱序</div>
              <div v-if="exam.preventScreenSwitch || exam.screenProhibition || exam.antiCheatSettings?.screenProhibition" class="badge badge-warning badge-sm">禁止切屏</div>
              <div v-if="exam.antiCheatSettings?.cameraMonitoring || exam.antiCheatSettings?.camera_monitoring" class="badge badge-info badge-sm">摄像头监控</div>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="card-actions justify-end mt-4">
            <button 
              v-if="userType === '2'" 
              :class="getActionBtnClass(exam.status) || 'btn btn-outline'"
              @click="viewExam(exam)"
            >
              {{ exam.status === 'ongoing' ? '进入考试' : '考试详情' }}
            </button>
            <template v-else>
              <button 
                class="btn btn-sm btn-outline"
                @click="viewExam(exam)"
              >
                查看详情
              </button>
              <button 
                v-if="canEditExam(exam)"
                class="btn btn-sm btn-outline btn-primary"
                @click="editExam(exam)"
              >
                编辑
              </button>
              <button 
                v-if="canDeleteExam(exam)"
                class="btn btn-sm btn-outline btn-error"
                @click="deleteExam(exam)"
              >
                删除
              </button>
              <button 
                v-if="userType !== '2' && canManageStudents(exam)"
                class="btn btn-sm btn-outline"
                @click="manageStudents(exam)"
              >
                {{ userType === '0' ? '管理学生' : '管理学生' }}
              </button>
            </template>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-if="exams.length === 0" class="text-center py-12">
      <div class="text-5xl mb-4">📚</div>
      <h3 class="text-xl font-semibold text-base-content mb-2">暂无考试</h3>
      <p class="text-base-content/70 mb-6">
        {{ userType === '2' ? '您没有参与的考试' : userType === '1' ? '您没有创建的考试' : userType === '0' ? '暂无考试或没有符合筛选条件的考试' : '暂无考试' }}
      </p>
      <button 
        v-if="userType !== '2'" 
        class="btn btn-primary"
        @click="createExam"
      >
        创建考试
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useRequest } from 'vue-hooks-plus';
import { getStudentExamsAPI, getExamListAPI, deleteExamAPI } from '../../apis';

const router = useRouter();

// 用户类型：0-教务老师, 1-普通老师, 2-学生
const userType = localStorage.getItem('userType');
console.log('ExamList - 当前用户类型:', userType);
if (!userType) {
  console.error('未找到用户类型，可能用户未登录');
  // 可能需要重定向到登录页面
  // router.push('/login');
}

// 状态响应式变量
const exams = ref<any[]>([]);
const statusFilter = ref('');
const teacherFilter = ref('');

// 状态相关方法
const getStatusClass = (status: string) => {
  switch (status) {
    case 'upcoming': return 'badge-info';
    case 'ongoing': return 'badge-success';
    case 'ended': return 'badge-neutral';
    default: return 'badge-ghost';
  }
};

const getStatusText = (status: string) => {
  switch (status) {
    case 'upcoming': return '未开始';
    case 'ongoing': return '进行中';
    case 'ended': return '已结束';
    default: return '未知';
  }
};

const getActionBtnClass = (status: string) => {
  switch (status) {
    case 'ongoing': return 'btn btn-primary';
    case 'ended': return 'btn btn-info';
    default: return 'btn btn-outline';
  }
};

// 根据考试时间计算状态
const getExamStatus = (startTime: string, endTime: string) => {
  if (!startTime || !endTime) {
    console.log('考试时间信息不完整，startTime:', startTime, 'endTime:', endTime);
    return 'unknown';
  }
  
  try {
    // 确保日期格式正确，处理可能的时区问题
    const now = new Date();
    // 如果日期字符串不包含时区信息，假设为本地时间
    const start = new Date(startTime);
    const end = new Date(endTime);
    
    console.log('时间比较:', {
      now: now,
      start: start,
      end: end,
      startValid: !isNaN(start.getTime()),
      endValid: !isNaN(end.getTime())
    });
    
    if (isNaN(start.getTime()) || isNaN(end.getTime())) {
      console.error('无效的日期格式，startTime:', startTime, 'endTime:', endTime);
      return 'unknown';
    }
    
    if (now < start) {
      return 'upcoming';
    } else if (now >= start && now <= end) {
      return 'ongoing';
    } else {
      return 'ended';
    }
  } catch (e) {
    console.error('计算考试状态时出错:', e);
    return 'unknown';
  }
};

// 时间格式化
const formatDateTime = (dateString: string) => {
  if (!dateString) return '未设置';
  try {
    return new Date(dateString).toLocaleString('zh-CN');
  } catch (e) {
    console.error('日期格式化错误:', e);
    return dateString;
  }
};

// 获取考试列表
const fetchExams = () => {
  console.log('开始获取考试列表，用户类型:', userType);
  
  if (userType === '2') {
    // 学生：获取自己的考试列表
    console.log('调用getStudentExamsAPI获取学生考试列表');
    console.log('当前用户ID:', localStorage.getItem('userId'));
    useRequest(() => getStudentExamsAPI(), {
      onSuccess(res) {
        console.log('获取学生考试列表响应:', res);
        if (res && res['code'] === 200) {
          let data = res['data'] || [];
          console.log('获取到的原始考试数据:', data);
          console.log('考试数据长度:', data.length);
          
          // 根据考试时间计算状态
          data = data.map((exam: any) => {
            console.log('处理考试数据:', exam);
            return {
              ...exam,
              status: getExamStatus(exam.startTime, exam.endTime),
              durationMinute: exam.durationMinute || exam.duration_minute || exam.duration || 0
            };
          });
          
          // 根据筛选条件过滤
          if (statusFilter.value) {
            data = data.filter((exam: any) => exam.status === statusFilter.value);
          }
          
          exams.value = data;
          console.log('最终考试列表:', exams.value);
          
          // 缓存学生考试数据，以便在考试详情页使用
          if (userType === '2') {
            sessionStorage.setItem('cachedStudentExams', JSON.stringify(data));
            console.log('已缓存学生考试数据到sessionStorage');
          }
        } else {
          console.error('获取学生考试列表失败:', res ? res['msg'] : '响应为空');
          exams.value = [];
        }
      },
      onError(err) {
        console.error('获取学生考试列表失败:', err);
        console.error('错误详情:', err.message || err);
        exams.value = [];
      }
    });
  } else {
    // 教师：获取考试列表，可按状态和教师筛选
    let params: any = {};
    if (statusFilter.value) {
      params.status = statusFilter.value;
    }
    if (userType === '0' && teacherFilter.value) {
      // 教务老师可以按教师筛选
      params.teacherId = parseInt(teacherFilter.value);
    } else if (userType === '1') {
      // 普通老师只能查看自己创建的考试
      const currentUserId = localStorage.getItem('userId');
      if (currentUserId) {
        params.teacherId = parseInt(currentUserId);
        console.log('普通老师，设置teacherId为:', params.teacherId);
      } else {
        console.error('无法获取当前用户ID');
        // 即使无法获取用户ID，也尝试调用API，后端会根据认证信息确定用户身份
        console.log('警告：无法获取用户ID，将尝试获取考试列表');
      }
    } else if (userType === '0') {
      // 教务老师可以查看所有考试，不设置teacherId参数
      console.log('教务老师，查看所有考试');
    }
    
    console.log('调用getExamListAPI获取教师考试列表，参数:', params);
    useRequest(() => getExamListAPI(params), {
      onSuccess(res) {
        console.log('获取考试列表响应:', res);
        if (res && res['code'] === 200) {
          let data = res['data'] || [];
          console.log('获取到的原始考试数据:', data);
          
          // 根据考试时间计算状态
          data = data.map((exam: any) => ({
            ...exam,
            status: getExamStatus(exam.startTime || exam.start_time, exam.endTime || exam.end_time),
            durationMinute: exam.durationMinute || exam.duration_minute || exam.duration || 0
          }));
          
          // 根据筛选条件过滤
          if (statusFilter.value) {
            data = data.filter((exam: any) => exam.status === statusFilter.value);
          }
          
          exams.value = data;
          console.log('最终考试列表:', exams.value);
        } else {
          console.error('获取考试列表失败:', res ? res['msg'] : '响应为空');
          exams.value = [];
        }
      },
      onError(err) {
        console.error('获取考试列表失败:', err);
        console.error('错误详情:', err.message || err);
        exams.value = [];
      }
    });
  }
};

// 权限检查
const canEditExam = (exam: any) => {
  if (!exam || !userType) {
    console.log('考试数据或用户类型为空');
    return false;
  }
  
  console.log('检查编辑权限，用户类型:', userType, '考试教师ID:', exam.teacherId || exam.teacher_id);
  if (userType === '0') {
    // 教务老师可以编辑所有考试
    return true;
  } else if (userType === '1') {
    // 普通老师只能编辑自己创建的考试
    const currentUserId = localStorage.getItem('userId');
    if (!currentUserId) {
      console.error('未找到当前用户ID');
      return false;
    }
    return exam.teacherId == currentUserId || exam.teacher_id == currentUserId;
  }
  return false;
};

const canDeleteExam = (exam: any) => {
  if (!exam || !userType) {
    console.log('考试数据或用户类型为空');
    return false;
  }
  
  console.log('检查删除权限，用户类型:', userType, '考试教师ID:', exam.teacherId || exam.teacher_id);
  if (userType === '0') {
    // 教务老师可以删除所有考试
    return true;
  } else if (userType === '1') {
    // 普通老师只能删除自己创建的考试
    const currentUserId = localStorage.getItem('userId');
    if (!currentUserId) {
      console.error('未找到当前用户ID');
      return false;
    }
    return exam.teacherId == currentUserId || exam.teacher_id == currentUserId;
  }
  return false;
};

const canManageStudents = (exam: any) => {
  if (!exam || !userType) {
    return false;
  }
  
  if (userType === '0') {
    // 教务老师可以管理所有考试的学生
    return true;
  } else if (userType === '1') {
    // 普通老师只能管理自己创建的考试的学生
    const currentUserId = localStorage.getItem('userId');
    if (!currentUserId) {
      return false;
    }
    return exam.teacherId == currentUserId || exam.teacher_id == currentUserId;
  }
  return false;
};

// 操作方法
const enterExam = (exam: any) => {
  // 使用多种可能的ID字段来确保正确跳转
  const examId = exam.id || exam.exam_id || exam.examId || exam.examid;
  if (exam.status === 'ongoing') {
    router.push(`/exam/${examId}/take`);
  } else {
    // 对于学生，无论考试状态如何，都跳转到考试详情页
    router.push(`/exam/${examId}`);
  }
};
const viewExam = (exam: any) => {
  // 使用多种可能的ID字段来确保正确跳转
  const examId = exam.id || exam.exam_id || exam.examId || exam.examid;

  if(userType === '1'){
    router.push(`/exam/${examId}`);
  }else if(userType === '2'){
    router.push(`/student/exam-page/${examId}`);
  }
};

const editExam = (exam: any) => {
  router.push(`/exam/${exam.id}/edit`);
};

const deleteExam = (exam: any) => {
  if (confirm('确定要删除这个考试吗？')) {
    console.log('开始删除考试，examId:', exam.id);
    useRequest(() => deleteExamAPI(exam.id), {
      onSuccess(res) {
        console.log('删除考试响应:', res);
        if (res['code'] === 200 && res['data'] === true) {
          alert('考试删除成功！');
          fetchExams(); // 重新获取考试列表
        } else {
          alert('考试删除失败：' + (res['msg'] || '未知错误'));
        }
      },
      onError(err) {
        console.error('删除考试失败:', err);
        alert('考试删除失败：' + (err.message || '网络错误'));
      }
    });
  }
};

const manageStudents = (exam: any) => {
  router.push(`/exam/${exam.id}/students`);
};

const createExam = () => {
  router.push('/exam/create');
};

// 页面加载时获取数据
onMounted(() => {
  console.log('ExamList组件挂载');
  fetchExams();
});
</script>