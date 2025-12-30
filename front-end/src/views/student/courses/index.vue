<template>
  <div class="max-w-6xl mx-auto p-6">
    <div class="flex justify-between items-center mb-8">
      <h1 class="text-3xl font-bold">课程管理</h1>
      <router-link to="/student" class="btn btn-outline">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
        </svg>
        返回主页面
      </router-link>
    </div>

    <div class="flex justify-between items-center mb-8">
      <router-link to="/student/courses/join" class="btn btn-primary">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
        </svg>
        加入课程
      </router-link>
    </div>

    <div>
      <div v-if="joinedCourses.length === 0" class="p-6 bg-base-200 rounded-lg text-center">尚未加入任何课程，您可以通过“加入课程”按钮添加课程。</div>

      <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div v-for="(c, idx) in joinedCourses" :key="idx" class="card bg-base-100 shadow-xl" 
             :class="{'bg-base-200 opacity-60': isCourseEnded(c.status || c.course?.status)}">
          <div class="card-body">
            <div class="flex justify-between items-start">
              <div>
                <h2 class="card-title">{{ c.course?.courseName || `课程 ${c.courseId}` }}</h2>
                <p class="text-sm text-base-content/60">教师：{{ c.teacherName || (c.course?.teacherId ? `教师 ${c.course?.teacherId}` : '—') }}</p>
              </div>
              <div v-if="isCourseEnded(c.status || c.course?.status)">
                <span class="badge badge-lg badge-error">已结课</span>
              </div>
            </div>

            <p class="mt-4 text-sm text-base-content/80">{{ c.course?.description || '暂无课程简介' }}</p>

            <div class="card-actions justify-end mt-4">
              <button @click="viewCourse(c.courseId || c.course?.id)" 
                      class="btn btn-primary"
                      :class="{'btn-disabled bg-gray-300 border-gray-300 text-gray-500 hover:bg-gray-300 hover:border-gray-300': isCourseEnded(c.status || c.course?.status)}"
                      :disabled="isCourseEnded(c.status || c.course?.status)">查看详情</button>
              <button @click="leaveCourse(c.courseId || c.course?.id)" 
                      class="btn btn-ghost"
                      :class="{'btn-disabled bg-gray-300 border-gray-300 text-gray-500 hover:bg-gray-300 hover:border-gray-300': isCourseEnded(c.status || c.course?.status)}"
                      :disabled="isCourseEnded(c.status || c.course?.status)">退出课程</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import router from '../../../routers';
import { getJoinedCoursesAPI, getUserInfoByIdAPI, quitCourseAPI } from '../../../apis/Server/userAPI';

const joinedCourses = ref<Array<any>>([]);

const formatJoinTime = (t: string | null) => {
  if (!t) return '—';
  try {
    const d = new Date(t);
    return d.toLocaleString();
  } catch (e) {
    return t;
  }
}

const isCourseEnded = (status: string | number | undefined) => {
  // 状态为1表示已结课
  return status == 1 || status === '1' || status === '已结课' || status === 'finished' || status === 'completed' || status === 'closed';
}

const viewCourse = (courseId: number) => {
  if (isCourseEnded(joinedCourses.value.find(c => (c.courseId || c.course?.id) === courseId)?.status || joinedCourses.value.find(c => (c.courseId || c.course?.id) === courseId)?.course?.status)) {
    return; // 如果课程已结课，则不执行操作
  }
  router.push(`/student/courses/${courseId}`);
}

const leaveCourse = async (courseId: number) => {
  const course = joinedCourses.value.find(c => (c.courseId || c.course?.id) === courseId);
  if (isCourseEnded(course?.status || course?.course?.status)) {
    return; // 如果课程已结课，则不执行操作
  }
  
  const courseName = course?.course?.courseName || `课程 ${courseId}`;
  if (!confirm(`确定要退出课程 "${courseName}" 吗？退出后将无法查看课程内容。`)) {
    return; // 用户取消
  }
  
  try {
    const token = localStorage.getItem('token');
    if (!token) {
      alert('登录已过期，请重新登录');
      return;
    }
    
    const res: any = await quitCourseAPI(courseId, token);
    if (res && res.code === 200) {
      alert(`已成功退出课程 "${courseName}"`);
      // 从列表中移除该课程
      joinedCourses.value = joinedCourses.value.filter(c => (c.courseId || c.course?.id) !== courseId);
    } else {
      alert(`退出课程失败：${res?.message || '未知错误'}`);
    }
  } catch (err: any) {
    console.error('退出课程异常', err);
    alert(`退出课程失败：${err?.message || '网络连接错误'}`);
  }
}

onMounted(async () => {
  const token = localStorage.getItem('token');
  if (!token) return;
  try {
    const res: any = await getJoinedCoursesAPI(token);
    if (res && res.code === 200 && Array.isArray(res.data)) {
      const data = res.data;

      // 收集所有教师 ID（去重）
      const teacherIds = Array.from(new Set(data.map((it: any) => it.course?.teacherId).filter(Boolean)));

      // 并行请求教师信息
      const teacherMap: Record<number, string> = {};
      await Promise.all(teacherIds.map(async (tid: number) => {
        try {
          const u: any = await getUserInfoByIdAPI(tid, token);
          if (u && u.code === 200 && u.data) {
            teacherMap[tid] = u.data.realName || u.data.username || `用户 ${tid}`;
          } else {
            teacherMap[tid] = `用户 ${tid}`;
          }
        } catch (e) {
          teacherMap[tid] = `用户 ${tid}`;
        }
      }));

      // 将教师姓名合并到课程项中，优先显示 course.courseName 等
      joinedCourses.value = data.map((it: any) => ({
        ...it,
        teacherName: it.course && it.course.teacherId ? teacherMap[it.course.teacherId] : '—'
      }));
    }
  } catch (err) {
    console.error('获取已加入课程失败', err);
  }
});
</script>