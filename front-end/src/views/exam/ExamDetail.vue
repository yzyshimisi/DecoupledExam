  <template>
  <div class="container mx-auto px-4 py-8 max-w-4xl">
    <!-- 返回按钮 -->
    <div class="mb-6">
      <button 
        @click="goBack" 
        class="btn btn-ghost btn-sm flex items-center"
      >
        <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
        </svg>
        返回
      </button>
    </div>

    <!-- 考试信息卡片 -->
    <div class="card bg-base-100 shadow-xl border border-base-200">
      <div class="card-body">
        <div class="flex justify-between items-start">
          <div>
            <h1 class="text-2xl font-bold text-base-content">{{ exam?.title }}</h1>
            <p class="text-base-content/70 mt-1">考试ID: {{ exam?.id }}</p>
          </div>
          <div class="badge" :class="getStatusClass(exam?.status)">
            {{ getStatusText(exam?.status) }}
          </div>
        </div>

        <div class="mt-6 grid grid-cols-1 md:grid-cols-2 gap-6">
          <div class="space-y-4">
            <div class="flex items-center text-base-content/80">
              <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
              <div>
                <p class="text-sm text-base-content/60">考试时间</p>
                <p>{{ formatDateTime(exam?.startTime) }} - {{ formatDateTime(exam?.endTime) }}</p>
              </div>
            </div>

            <div class="flex items-center text-base-content/80 mr-8">
              <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <div>
                <p class="text-sm text-base-content/60">考试时长</p>
                <p>{{ exam ? calculateDuration() : 0 }} 分钟</p>
              </div>
            </div>

            <div v-if="userType !== '2'" class="flex items-center text-base-content/80">
              <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
              </svg>
              <div>
                <p class="text-sm text-base-content/60">创建教师</p>
                <p>{{ exam?.teacherName || exam?.teacher_name || exam?.teacherId || exam?.teacher_id || '未知' }}</p>
              </div>
            </div>
          </div>

          <div class="space-y-4">
            <div class="flex items-center text-base-content/80">
              <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
              </svg>
              <div>
                <p class="text-sm text-base-content/60">试卷ID</p>
                <p>{{ exam?.paperId || exam?.paper_id || exam?.paper || '未设置' }}</p>
              </div>
            </div>



            <div v-if="exam?.description" class="flex flex-col">
              <p class="text-sm text-base-content/60">考试说明</p>
              <p class="text-sm text-base-content/80">{{ exam.description }}</p>
            </div>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="card-actions justify-end mt-8 pt-6 border-t border-base-200">
          <template v-if="userType === '2'">
            <!-- 学生操作 -->
            <button 
              v-if="exam?.status === 'ongoing'"
              class="btn btn-primary"
              @click="enterExam"
            >
              进入考试
            </button>
            <button 
              v-else
              class="btn btn-outline"
              @click="viewResult"
            >
              查看成绩
            </button>
          </template>
          <template v-else>
            <!-- 教师操作 -->
            <button 
              class="btn btn-outline"
              @click="viewStudents"
            >
              查看学生列表
            </button>
            <button 
              v-if="isManagerOrDirector() || (isTeacher() && exam && canEditExam)"
              class="btn btn-outline btn-primary"
              @click="editExam"
            >
              编辑考试
            </button>
            <button 
              v-if="isManagerOrDirector() || (isTeacher() && exam && canDeleteExam)"
              class="btn btn-outline btn-error"
              @click="deleteExam"
            >
              删除考试
            </button>
            <button 
              v-if="isManagerOrDirector()"
              class="btn btn-outline btn-success"
              @click="publishExam"
            >
              发布到课程
            </button>
          </template>
        </div>
      </div>
    </div>



    <!-- 空状态 -->
    <div v-if="!exam" class="text-center py-12">
      <div class="text-5xl mb-4">⚠️</div>
      <h3 class="text-xl font-semibold text-base-content mb-2">考试不存在</h3>
      <p class="text-base-content/70 mb-6">您要查看的考试可能已被删除或不存在</p>
      <button class="btn btn-primary" @click="goBack">返回考试列表</button>
    </div>

    <!-- 发布考试到课程的模态框 -->
    <div v-if="showPublishModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-base-100 rounded-lg p-6 w-full max-w-md mx-4">
        <h3 class="text-lg font-semibold mb-4">发布考试到课程</h3>
        <p class="mb-4">考试: {{ exam?.title }}</p>
        
        <!-- 课程ID输入 -->
        <div class="mb-4">
          <label class="label">
            <span class="label-text">课程ID (用逗号分隔多个ID)</span>
          </label>
          <input 
            type="text" 
            v-model="selectedCourseIdsStr" 
            placeholder="例如: 1,2,3" 
            class="input input-bordered w-full"
          />
        </div>
        
        <!-- 已选择的课程ID列表 -->
        <div v-if="selectedCourseIds.length > 0" class="mb-4">
          <p class="font-medium mb-2">已选择的课程ID:</p>
          <div class="flex flex-wrap gap-2">
            <span 
              v-for="id in selectedCourseIds" 
              :key="id" 
              class="badge badge-primary"
            >
              {{ id }}
            </span>
          </div>
        </div>
        
        <!-- 操作按钮 -->
        <div class="flex justify-end gap-3 mt-6">
          <button 
            class="btn btn-outline" 
            @click="cancelPublishExam"
          >
            取消
          </button>
          <button 
            class="btn btn-primary" 
            @click="confirmPublishExam"
          >
            确认发布
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useRequest } from 'vue-hooks-plus';
import { getExamDetailAPI, deleteExamAPI, getStudentExamsAPI, publishExamToCourseAPI } from '../../apis';

const router = useRouter();
const route = useRoute();

// 用户类型：根据teacher_position的role字段判断 - 0-管理员, 1-教务老师, 2-任课老师
const userType = localStorage.getItem('userType');
const userRole = localStorage.getItem('userRole'); // 新增：根据teacher_position的role字段
console.log('ExamDetail - 当前用户类型:', userType);
console.log('ExamDetail - 当前用户角色:', userRole);
const examId = route.params.id as string;
console.log('ExamDetail - 考试ID:', examId);

// 状态响应式变量
const exam = ref<any>(null);

// 添加发布考试相关的响应式变量
const showPublishModal = ref(false);
const selectedCourseIds = ref<number[]>([]);
const selectedCourseIdsStr = ref('');

// 辅助函数：查找对象中有效的ID值
const findValidId = (obj: any) => {
  const possibleFields = ['id', 'exam_id', 'examId', 'examid', 'exam-id', 'exam.id'];
  for (const field of possibleFields) {
    if (obj[field] !== undefined && obj[field] !== null) {
      return obj[field];
    }
  }
  return null;
};

// 获取考试详情
const fetchExamDetail = () => {
  console.log('开始获取考试详情，examId:', examId);
  console.log('当前用户类型:', userType);
  console.log('当前用户ID:', localStorage.getItem('userId'));
  if (!examId) {
    console.error('examId is undefined or null');
    exam.value = null;
    return;
  }
  
  const examIdNum = parseInt(examId);
  if (isNaN(examIdNum)) {
    console.error('Invalid examId:', examId);
    exam.value = null;
    return;
  }
  
  console.log('准备调用API，examIdNum:', examIdNum);
  
  // 根据用户类型使用不同的API
  if (userType === '2') { // 学生
    console.log('学生用户，使用学生考试列表接口获取考试详情');
    // 学生用户应使用getStudentExamsAPI获取自己有权访问的考试，然后在返回的列表中查找指定考试
    useRequest(() => getStudentExamsAPI(), {
      onSuccess(res) {
        console.log('获取学生考试列表响应:', res);
        if (res && res['code'] === 200) {
          const data = res['data'] || [];
          console.log('获取到的学生考试数据:', data);
          console.log('考试数据长度:', data.length);
          
          // 添加调试日志以查看数据结构
          if (data.length > 0) {
            console.log('第一个考试对象的所有属性:', Object.keys(data[0]));
            console.log('第一个考试对象的详细信息:', data[0]);
          }
          
          // 在返回的考试列表中查找指定ID的考试
          // 使用多种可能的ID字段进行匹配
          const examData = data.find((e: any) => {
            // 检查对象的多个可能的ID字段
            const possibleIds = [
              e.id,
              e.exam_id,
              e.examId,
              e.examid,
              e['exam-id'],
              e['exam.id']
            ];
            
            // 查找第一个有效的ID并进行比较
            for (const id of possibleIds) {
              if (id !== undefined && id !== null) {
                if (Number(id) === examIdNum) {
                  console.log('找到匹配的考试，ID字段:', id, '值:', id, '类型:', typeof id);
                  return true;
                }
              }
            }
            return false;
          });
          
          if (examData) {
            console.log('从学生考试列表找到考试数据:', examData);
            // 确保考试数据格式正确，兼容不同可能的字段名
            exam.value = {
              ...examData,
              id: examData.id || examData.exam_id || examData.examId || examIdNum, // 确保ID字段正确
              title: examData.title || examData.name || examData.examName || '未命名考试',
              durationMinute: examData.durationMinute || examData.duration_minute || examData.duration || 0,
              teacherName: examData.teacherName || examData.teacher_name || examData.teacherId || examData.teacher_id || '未知教师',
              teacherId: examData.teacherId || examData.teacher_id,
              paperId: examData.paperId || examData.paper_id || examData.paper || '未设置',
              startTime: examData.startTime || examData.start_time,
              endTime: examData.endTime || examData.end_time,
              questionShuffle: examData.questionShuffle || examData.question_shuffle || examData.question_shuffle || false,
              preventScreenSwitch: examData.preventScreenSwitch || examData.prevent_screen_switch || examData.screenProhibition || false,
              description: examData.description || examData.desc || examData.examDesc || '',
              status: getExamStatus(examData.startTime || examData.start_time, examData.endTime || examData.end_time)
            };
            console.log('处理后的考试详情数据:', exam.value);
          } else {
            console.error('在学生考试列表中未找到指定考试ID:', examIdNum);
            console.log('可用的考试ID列表:', data.map((e: any) => e.id || e.exam_id || e.examId || e.examid).map(id => Number(id)));
            console.log('正在查找的考试ID:', examIdNum);
            // 显示每个考试对象的ID字段详情，帮助调试
            console.log('每个考试对象的ID字段详情:', data.map((e: any, index: number) => ({
              index: index,
              id: e.id,
              exam_id: e.exam_id,
              examId: e.examId,
              examid: e.examid,
              calculatedId: e.id || e.exam_id || e.examId || e.examid,
              allProps: Object.keys(e)
            })));
            exam.value = null;
          }
        } else {
          console.error('获取学生考试列表失败:', res ? res['msg'] : '响应为空');
          exam.value = null;
        }
      },
      onError(err) {
        console.error('获取学生考试列表失败:', err);
        console.error('错误详情:', err.message || err);
        exam.value = null;
      }
    });
  } else {
    // 教师用户使用通用考试详情API
    useRequest(() => getExamDetailAPI(examIdNum), {
      onSuccess(res) {
        console.log('获取考试详情响应:', res);
        if (res && res['code'] === 200) {
          // 确保考试数据格式正确
          const examData = res['data'];
          console.log('API返回的考试数据:', examData);
          if (examData) {
            console.log('原始考试数据:', examData);
            // 尝试映射可能的字段名，增强兼容性
            exam.value = {
              ...examData,
              id: examData.id || examData.exam_id || examData.examId,
              title: examData.title || examData.name || examData.examName || '未命名考试',
              durationMinute: examData.durationMinute || examData.duration_minute || examData.duration || 0,
              teacherName: examData.teacherName || examData.teacher_name || examData.teacherId || examData.teacher_id || '未知教师',
              teacherId: examData.teacherId || examData.teacher_id,
              paperId: examData.paperId || examData.paper_id || examData.paper || '未设置',
              startTime: examData.startTime || examData.start_time,
              endTime: examData.endTime || examData.end_time,
              questionShuffle: examData.questionShuffle || examData.question_shuffle || examData.question_shuffle || false,
              preventScreenSwitch: examData.preventScreenSwitch || examData.prevent_screen_switch || examData.screenProhibition || false,
              description: examData.description || examData.desc || examData.examDesc || '',
              status: getExamStatus(examData.startTime || examData.start_time, examData.endTime || examData.end_time)
            };
            console.log('处理后的考试详情数据:', exam.value);
          } else {
            exam.value = null;
            console.error('考试数据为空');
          }
        } else {
          console.error('获取考试详情失败:', res ? res['msg'] : '响应为空');
          console.log('错误响应详情:', res);
          console.log('错误码:', res ? res['code'] : 'unknown');
          exam.value = null;
        }
      },
      onError(err) {
        console.error('获取考试详情失败:', err);
        console.error('错误详情:', err.message || err);
        console.error('错误堆栈:', err.stack);
        exam.value = null;
      }
    });
  }
};



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

const getStudentStatusClass = (status: string) => {
  switch (status) {
    case 'registered': return 'badge-info';
    case 'completed': return 'badge-success';
    case 'absent': return 'badge-warning';
    default: return 'badge-ghost';
  }
};

const getStudentStatusText = (status: string) => {
  switch (status) {
    case 'registered': return '已报名';
    case 'completed': return '已完成';
    case 'absent': return '缺考';
    default: return '未知';
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

// 计算考试时长（分钟）
const calculateDuration = () => {
  if (!exam.value?.startTime || !exam.value?.endTime) {
    return 0;
  }
  
  try {
    const start = new Date(exam.value.startTime);
    const end = new Date(exam.value.endTime);
    
    if (isNaN(start.getTime()) || isNaN(end.getTime())) {
      console.error('无效的日期格式，无法计算时长');
      return 0;
    }
    
    // 计算时间差（毫秒），然后转换为分钟
    const durationMs = end.getTime() - start.getTime();
    const durationMinutes = Math.round(durationMs / (1000 * 60));
    
    return durationMinutes > 0 ? durationMinutes : 0;
  } catch (e) {
    console.error('计算考试时长时出错:', e);
    return 0;
  }
};

// 获取用户角色，如果不存在则使用userType作为备选
const getUserRole = () => {
  return localStorage.getItem('userRole') || userType;
};

// 判断是否为管理员或教务老师
const isManagerOrDirector = () => {
  const role = getUserRole();
  return role === '0' || role === '1'; // 管理员或教务老师
};

// 判断是否为任课老师
const isTeacher = () => {
  const role = getUserRole();
  return role === '2'; // 任课老师
};

// 权限检查
const canEditExam = computed(() => {
  if (!exam.value) return false;
  console.log('检查编辑权限，用户角色:', getUserRole(), '考试教师ID:', exam.value.teacherId || exam.value.teacher_id, '当前用户ID:', localStorage.getItem('userId'));
  const currentUserId = localStorage.getItem('userId');
  if (!currentUserId) {
    console.error('未找到当前用户ID');
    return false;
  }
  // 管理员或教务老师可以编辑所有考试
  if (isManagerOrDirector()) {
    return true;
  }
  // 任课老师只能编辑自己创建的考试
  if (isTeacher()) {
    // 检查多个可能的教师ID字段
    const examTeacherId = exam.value.teacherId || exam.value.teacher_id || exam.value.teacher;
    return examTeacherId == currentUserId;
  }
  return false;
});

const canDeleteExam = computed(() => {
  if (!exam.value) return false;
  console.log('检查删除权限，用户角色:', getUserRole(), '考试教师ID:', exam.value.teacherId || exam.value.teacher_id, '当前用户ID:', localStorage.getItem('userId'));
  const currentUserId = localStorage.getItem('userId');
  if (!currentUserId) {
    console.error('未找到当前用户ID');
    return false;
  }
  // 管理员或教务老师可以删除所有考试
  if (isManagerOrDirector()) {
    return true;
  }
  // 任课老师只能删除自己创建的考试
  if (isTeacher()) {
    // 检查多个可能的教师ID字段
    const examTeacherId = exam.value.teacherId || exam.value.teacher_id || exam.value.teacher;
    return examTeacherId == currentUserId;
  }
  return false;
});

const canPublishExam = computed(() => {
  // 只有管理员或教务老师可以发布考试到课程
  console.log('检查发布权限，用户角色:', getUserRole());
  return isManagerOrDirector();
});

// 操作方法
const goBack = () => {
  router.push('/exam');
};

const enterExam = () => {
  router.push(`/exam/${examId}/take`);
};

const viewResult = () => {
  // 学生在考试结束后查看成绩，其他情况也跳转到成绩页面（可能显示未开始等信息）
  router.push(`/exam/${examId}/result`);
};

const viewExamInfo = () => {
  // 学生查看考试信息
  console.log('查看考试信息');
};

const viewStudents = () => {
  router.push(`/exam/${examId}/students`);
};

const editExam = () => {
  router.push(`/exam/${examId}/edit`);
};

const deleteExam = () => {
  if (!exam.value) {
    console.error('考试数据为空，无法删除');
    return;
  }
  if (confirm('确定要删除这个考试吗？')) {
    console.log('开始删除考试，examId:', examId);
    const examIdNum = parseInt(examId);
    if (isNaN(examIdNum)) {
      console.error('Invalid examId:', examId);
      return;
    }
    
    useRequest(() => deleteExamAPI(examIdNum), {
      onSuccess(res) {
        console.log('删除考试响应:', res);
        if (res && res['code'] === 200 && res['data'] === true) {
          alert('考试删除成功！');
          router.push('/exam');
        } else {
          alert('考试删除失败：' + (res && res['msg'] ? res['msg'] : '未知错误'));
        }
      },
      onError(err) {
        console.error('删除考试失败:', err);
        alert('考试删除失败：' + (err.message || '网络错误'));
      }
    });
  }
};

const removeStudent = (student: any) => {
  if (confirm(`确定要将学生 ${student.name} 从考试中移除吗？`)) {
    useRequest(() => removeStudentsFromExamAPI(parseInt(examId), [student.id]), {
      onSuccess(res) {
        if (res && res['code'] === 200 && res['data'] === true) {
          alert('学生移除成功！');
          // 重新获取学生列表
          if (userType !== '2') {
            fetchStudents();
          }
        } else {
          alert('学生移除失败：' + (res && res['msg'] ? res['msg'] : '未知错误'));
        }
      },
      onError(err) {
        console.error('移除学生失败:', err);
        alert('学生移除失败：' + (err.message || '网络错误'));
      }
    });
  }
};

const addStudents = () => {
  router.push(`/exam/${examId}/students/add`);
};

// 发布考试到课程
const publishExam = () => {
  if (!exam.value) {
    alert('考试数据为空，无法发布');
    return;
  }
  
  // 显示发布考试的模态框
  showPublishModal.value = true;
};

// 确认发布考试到课程
const confirmPublishExam = () => {
  if (selectedCourseIds.value.length === 0) {
    alert('请至少选择一个课程');
    return;
  }
  
  console.log('准备发布考试到课程，examId:', exam.value.id, 'courseIds:', selectedCourseIds.value);
  
  // 调用API发布考试到课程
  useRequest(() => publishExamToCourseAPI(exam.value.id, selectedCourseIds.value), {
    onSuccess(res) {
      console.log('发布考试到课程响应:', res);
      if (res && res['code'] === 200 && res['data'] === true) {
        alert('考试发布成功！');
        showPublishModal.value = false;
        selectedCourseIds.value = [];
      } else {
        alert('考试发布失败：' + (res && res['msg'] ? res['msg'] : '未知错误'));
      }
    },
    onError(err) {
      console.error('发布考试到课程失败:', err);
      alert('考试发布失败：' + (err.message || '网络错误'));
    }
  });
};

// 取消发布考试
const cancelPublishExam = () => {
  showPublishModal.value = false;
  selectedCourseIds.value = [];
  selectedCourseIdsStr.value = '';
};

// 解析课程ID字符串
const parseCourseIds = () => {
  if (!selectedCourseIdsStr.value.trim()) {
    selectedCourseIds.value = [];
    return;
  }
  
  try {
    // 分割输入的ID并转换为数字数组
    const ids = selectedCourseIdsStr.value.split(',').map(id => {
      const numId = parseInt(id.trim());
      if (isNaN(numId)) {
        throw new Error(`无效的课程ID: ${id}`);
      }
      return numId;
    });
    
    // 去重并更新selectedCourseIds
    selectedCourseIds.value = [...new Set(ids)];
  } catch (error) {
    console.error('解析课程ID失败:', error);
  }
};

// 监听课程ID字符串变化
watch(selectedCourseIdsStr, () => {
  parseCourseIds();
});

// 页面加载时获取数据
onMounted(() => {
  fetchExamDetail();
});
</script>