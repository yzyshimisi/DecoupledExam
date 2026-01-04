<template>
  <div class="container mx-auto px-4 py-8">
    <!-- 页面标题 -->
    <div class="mb-8">
      <h1 class="text-3xl font-bold text-base-content">考试通知</h1>
      <p class="text-base-content/70 mt-2">查看您的考试通知和安排</p>
    </div>



    <!-- 通知列表 -->
    <div class="space-y-4">
      <div 
        v-for="notification in notifications" 
        :key="notification.id"
        class="card bg-base-100 shadow-lg border border-base-200 hover:shadow-xl transition-shadow"
      >
        <div class="card-body p-6">
          <div class="flex justify-between items-start">
            <div class="flex-1">
              <div class="flex items-center gap-3 mb-2">
                <h2 class="card-title text-lg font-bold text-base-content">{{ notification.examTitle }}</h2>
                <span class="badge" :class="getStatusClass(notification.status)">
                  {{ getStatusText(notification.status) }}
                </span>
              </div>

              <div class="text-sm text-base-content/80 space-y-1">
                <div class="flex items-center">
                  <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                  </svg>
                  <span>考试时间: {{ formatDateTime(notification.startTime) }} - {{ formatDateTime(notification.endTime) }}</span>
                </div>

                <div class="flex items-center">
                  <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                  <span>通知时间: {{ formatDateTime(notification.send_time) }}</span>
                </div>
                
                <div class="flex items-center">
                  <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                  </svg>
                  <span>教师ID: {{ notification.teacherName }}</span>
                </div>
                
                <div v-if="notification.content" class="mt-2 p-3 bg-base-200 rounded-lg">
                  <p class="text-sm text-base-content">通知内容: {{ notification.content }}</p>
                </div>
              </div>
              
              <div v-if="notification.description" class="mt-3 text-sm text-base-content/70">
                <p>{{ notification.description }}</p>
              </div>
              
              <div v-if="notification.antiCheatSettings" class="mt-3 pt-3 border-t border-base-200">
                <div class="flex flex-wrap gap-2">
                  <div v-if="notification.antiCheatSettings.questionShuffle" class="badge badge-info badge-sm">题目乱序</div>
                  <div v-if="notification.antiCheatSettings.screenProhibition" class="badge badge-warning badge-sm">禁止切屏</div>
                  <div v-if="notification.antiCheatSettings.cameraMonitoring" class="badge badge-info badge-sm">摄像头监控</div>
                </div>
              </div>
            </div>
            
            <div class="flex flex-col gap-2 ml-4">
              <button 
                v-if="notification.status === 'ongoing'"
                class="btn btn-primary btn-sm"
                @click="enterExam(notification.examId)"
              >
                进入考试
              </button>
              <button 
                v-if="!notification.isRead"
                class="btn btn-ghost btn-sm"
                @click="markAsRead(notification.id)"
              >
                标记已读
              </button>
              <div 
                v-else
                class="btn btn-disabled btn-sm"
              >
                <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                </svg>
                已读
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-if="notifications.length === 0" class="text-center py-12">
      <div class="text-5xl mb-4">📋</div>
      <h3 class="text-xl font-semibold text-base-content mb-2">暂无考试通知</h3>
      <p class="text-base-content/70 mb-6">您当前没有考试通知</p>
      <button class="btn btn-primary" @click="goToExams">查看所有考试</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useRequest } from 'vue-hooks-plus';
import { getStudentExamsAPI, getStudentNotificationsAPI } from '../../apis';

const router = useRouter();

// 用户类型检查
const userType = localStorage.getItem('userType');
console.log('ExamNotifications - 当前用户类型:', userType);

// 状态响应式变量
const notifications = ref<any[]>([]);
const statusFilter = ref('');
const timeRangeFilter = ref('');

// 获取通知列表
const fetchNotifications = () => {
  console.log('开始获取考试通知');
  console.log('当前用户类型:', userType);
  console.log('当前用户ID:', localStorage.getItem('userId'));
  
  // 首先获取学生考试列表以获得完整的考试信息
  useRequest(() => getStudentExamsAPI(), {
    onSuccess(examRes) {
      console.log('获取学生考试列表响应:', examRes);
      if (examRes && examRes['code'] === 200) {
        const examsData = examRes['data'] || [];
        console.log('获取到的考试数据:', examsData);
        
        // 然后获取通知列表
        useRequest(() => getStudentNotificationsAPI(), {
          onSuccess(notificationRes) {
            console.log('获取考试通知响应:', notificationRes);
            if (notificationRes && notificationRes['code'] === 200) {
              let notificationData = notificationRes['data'] || [];
              console.log('获取到的通知数据:', notificationData);
              
              // 根据筛选条件过滤
              if (statusFilter.value) {
                notificationData = notificationData.filter((notification: any) => notification.status === statusFilter.value);
              }
              
              // 将通知数据与考试数据结合
              const notificationsData = notificationData.map((notification: any) => {
                console.log('处理通知数据:', notification);
                
                // 在考试列表中查找对应的考试信息，尝试多种可能的ID字段
                const examInfo = examsData.find((exam: any) => {
                  const examIds = [exam.id, exam.exam_id, exam.examId, exam.examid];
                  const notificationIds = [notification.examId, notification.exam_id, notification.examId, notification.examid];
                  
                  for (const examId of examIds) {
                    for (const notificationId of notificationIds) {
                      if (examId !== undefined && notificationId !== undefined && examId == notificationId) {
                        return true;
                      }
                    }
                  }
                  return false;
                });
                
                return {
                  id: notification.id,
                  examId: notification.examId,
                  examTitle: examInfo?.title || notification.title || notification.examTitle || '未命名考试',
                  title: examInfo?.title || notification.title || notification.examTitle || '未命名考试',
                  content: notification.content || notification.notificationContent || notification.description || '', // 通知内容
                  send_time: notification.send_time || notification.sendTime || notification.create_time || notification.createTime || '', // 发送时间
                  startTime: examInfo?.startTime || notification.startTime,
                  endTime: examInfo?.endTime || notification.endTime,
                  duration: examInfo?.durationMinute || examInfo?.duration || notification.durationMinute || notification.duration || 0,
                  status: getExamStatus(examInfo?.startTime || notification.startTime, examInfo?.endTime || notification.endTime),
                  teacherName: examInfo?.teacherName || examInfo?.teacherId || notification.teacherName || notification.teacherId || '未知教师',
                  description: examInfo?.description || notification.description || '',
                  antiCheatSettings: {
                    questionShuffle: examInfo?.questionShuffle || notification.questionShuffle,
                    screenProhibition: examInfo?.preventScreenSwitch || notification.preventScreenSwitch
                  },
                  isRead: false // 默认未读状态
                };
              });
              
              notifications.value = notificationsData;
              console.log('转换后的通知数据:', notifications.value);
            } else {
              console.error('获取考试通知失败:', notificationRes ? notificationRes['msg'] : '响应为空');
              // 即使API返回错误，也显示空数组以避免界面卡住
              notifications.value = [];
            }
          },
          onError(err) {
            console.error('获取考试通知失败:', err);
            console.error('错误详情:', err.message || err);
            // 显示错误信息并确保界面不会卡住
            notifications.value = [];
          }
        });
      } else {
        console.error('获取学生考试列表失败:', examRes ? examRes['msg'] : '响应为空');
        notifications.value = [];
      }
    },
    onError(err) {
      console.error('获取学生考试列表失败:', err);
      console.error('错误详情:', err.message || err);
      notifications.value = [];
    }
  });
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
    case 'upcoming': return '即将开始';
    case 'ongoing': return '进行中';
    case 'ended': return '已结束';
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
  if (!dateString) return '';
  return new Date(dateString).toLocaleString('zh-CN');
};

// 操作方法
const enterExam = (examId: number) => {
  console.log('进入考试:', examId);
  router.push(`/exam/${examId}/take`);
};

const viewResult = (examId: number) => {
  console.log('查看成绩:', examId);
  router.push(`/exam/${examId}/result`);
};

const viewExam = (examId: number) => {
  console.log('查看考试详情:', examId);
  router.push(`/exam/${examId}`);
};

const markAsRead = (notificationId: number) => {
  console.log('标记通知为已读:', notificationId);
  // 更新本地状态，将指定通知标记为已读
  const notificationIndex = notifications.value.findIndex((n: any) => n.id == notificationId);
  if (notificationIndex !== -1) {
    // 创建新对象以确保响应式更新
    notifications.value[notificationIndex] = {
      ...notifications.value[notificationIndex],
      isRead: true
    };
  }
};

const goToExams = () => {
  console.log('跳转到考试列表');
  router.push('/exam');
};

// 页面加载时获取数据
onMounted(() => {
  console.log('ExamNotifications组件挂载');
  fetchNotifications();
});
</script>