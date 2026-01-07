<template>
  <div class="max-w-6xl mx-auto px-4 py-8">
    <div class="mb-6">
      <h1 class="text-2xl font-bold">课程管理</h1>
      <p class="text-gray-500">创建、管理您的课程</p>
    </div>

    <!-- 课程操作按钮 -->
    <div class="mb-6 flex justify-between items-center">
      <div class="flex gap-2">
        <button class="btn btn-primary" @click="showCreateModal = true">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
            <path fill-rule="evenodd" d="M10 5a1 1 0 011 1v3h3a1 1 0 110 2h-3v3a1 1 0 11-2 0v-3H6a1 1 0 110-2h3V6a1 1 0 011-1z" clip-rule="evenodd" />
          </svg>
          创建课程
        </button>
        <button class="btn btn-outline" @click="loadCourses">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
            <path fill-rule="evenodd" d="M4 2a1 1 0 011 1v2.101a7.002 7.002 0 0111.601 2.566 1 1 0 11-1.885.666A5.002 5.002 0 005.999 7H9a1 1 0 010 2H4a1 1 0 01-1-1V3a1 1 0 011-1zm.008 9.057a1 1 0 011.276.61A5.002 5.002 0 0014.001 13H11a1 1 0 110-2h5a1 1 0 011 1v5a1 1 0 11-2 0v-2.101a7.002 7.002 0 01-11.601-2.566 1 1 0 01.61-1.276z" clip-rule="evenodd" />
          </svg>
          刷新
        </button>
      </div>
      <div class="form-control">
        <input 
          type="text" 
          placeholder="搜索课程..." 
          class="input input-bordered" 
          v-model="searchQuery"
        />
      </div>
    </div>

    <!-- 课程列表 -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <div 
        v-for="course in filteredCourses" 
        :key="course.id"
        :class="['card bg-base-100 shadow-xl', course.status === 1 ? 'opacity-60' : '']"
      >
        <div class="card-body">
          <div class="flex justify-between items-start">
            <h2 class="card-title">
              {{ course.name }}
              <span v-if="course.status === 1" class="badge badge-error ml-2">已结课</span>
            </h2>
            <div class="flex gap-2">
              <button 
                class="btn btn-xs btn-outline btn-warning" 
                :disabled="course.status === 1"
                @click="openEditModal(course)"
              >
                编辑
              </button>
              <button 
                class="btn btn-xs btn-outline btn-error" 
                :disabled="course.status === 1"
                @click="deleteCourse(course.id)"
              >
                删除
              </button>
            </div>
          </div>
          
          <p class="text-gray-500 text-sm">{{ course.description }}</p>
          
          <div class="mt-2">
            <div class="flex justify-between text-sm">
              <span v-if="course.subjectId">学科: {{ getSubjectName(course.subjectId) }}</span>
              <span v-if="course.inviteCode">邀请码: {{ course.inviteCode }}</span>
            </div>
          </div>
          
          <div class="card-actions justify-between items-center mt-4">
            <div>
              <span class="badge" :class="course.status === 0 ? 'badge-success' : 'badge-error'">
                {{ course.status === 0 ? '开设中' : '已结课' }}
              </span>
            </div>
            <div class="flex gap-2">
              <button 
                class="btn btn-xs" 
                :class="course.status === 0 ? 'btn-error' : 'btn-success'"
                @click="toggleCourseStatus(course)"
              >
                {{ course.status === 0 ? '结课' : '开启' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 创建课程模态框 -->
    <div class="modal" :class="{ 'modal-open': showCreateModal }">
      <div class="modal-box">
        <h3 class="font-bold text-lg">创建新课程</h3>
        <form @submit.prevent="createCourse" class="mt-4">
          <div class="form-control">
            <label class="label">
              <span class="label-text">课程名称</span>
            </label>
            <input 
              type="text" 
              placeholder="输入课程名称" 
              class="input input-bordered" 
              v-model="newCourse.name"
              required
            />
          </div>
          <div class="form-control mt-2">
            <label class="label">
              <span class="label-text">课程描述</span>
            </label>
            <textarea 
              placeholder="输入课程描述" 
              class="textarea textarea-bordered" 
              v-model="newCourse.description"
            ></textarea>
          </div>
          <div class="form-control mt-2">
            <label class="label">
              <span class="label-text">学科</span>
            </label>
            <select 
              class="select select-bordered" 
              v-model="newCourse.subjectId"
              required
            >
              <option value="" disabled>选择学科</option>
              <option 
                v-for="subject in subjects" 
                :key="subject.subjectId" 
                :value="subject.subjectId"
              >
                {{ subject.subjectName }}
              </option>
            </select>
          </div>
          <div class="modal-action">
            <button type="submit" class="btn btn-primary">创建课程</button>
            <button type="button" class="btn" @click="showCreateModal = false">取消</button>
          </div>
        </form>
      </div>
    </div>

    <!-- 编辑课程模态框 -->
    <div class="modal" :class="{ 'modal-open': showEditModal }">
      <div class="modal-box">
        <h3 class="font-bold text-lg">编辑课程</h3>
        <form @submit.prevent="updateCourse" class="mt-4">
          <div class="form-control">
            <label class="label">
              <span class="label-text">课程名称</span>
            </label>
            <input 
              type="text" 
              placeholder="输入课程名称" 
              class="input input-bordered" 
              v-model="editingCourse.name"
              required
            />
          </div>
          <div class="form-control mt-2">
            <label class="label">
              <span class="label-text">课程描述</span>
            </label>
            <textarea 
              placeholder="输入课程描述" 
              class="textarea textarea-bordered" 
              v-model="editingCourse.description"
            ></textarea>
          </div>
          <div class="form-control mt-2">
            <label class="label">
              <span class="label-text">学科</span>
            </label>
            <select 
              class="select select-bordered" 
              v-model="editingCourse.subjectId"
              required
            >
              <option 
                v-for="subject in subjects" 
                :key="subject.subjectId" 
                :value="subject.subjectId"
              >
                {{ subject.subjectName }}
              </option>
            </select>
          </div>
          <div class="modal-action">
            <button type="submit" class="btn btn-primary">更新课程</button>
            <button type="button" class="btn" @click="showEditModal = false">取消</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue';
import { createCourseAPI, getSubjectListAPI, Subject, getTeacherCoursesAPI, TeacherCourse, deleteCourseAPI, updateCourseInfoAPI, updateCourseStatusAPI } from '../../../apis/Server/userAPI';

// 课程数据结构现在使用API返回的结构
// 课程数据结构
interface Course {
  id: number;
  name: string;
  description: string;
  code?: string; // 可选字段，因为API返回可能不包含此字段
  subjectId?: number;
  status: number; // 0 开设中，1 已结课
  studentCount?: number;
  // API返回的字段
  courseId?: number;
  inviteCode?: string;
  teacherId?: number;
  createTime?: number;
}

// 模态框显示状态
const showCreateModal = ref(false);
const showEditModal = ref(false);

// 学科列表
const subjects = ref<Subject[]>([]);

// 课程数据
const courses = ref<Course[]>([]);
const searchQuery = ref('');

// 新课程数据
interface NewCourse {
  name: string;
  description: string;
  subjectId: number | string; // 使用string是因为select的值初始可能是空字符串
}

const newCourse = ref<NewCourse>({
  name: '',
  description: '',
  subjectId: ''
});

// 编辑中的课程数据
const editingCourse = ref<Course>({
  id: 0,
  name: '',
  description: '',
  subjectId: 1,
  status: 0
});

// 计算属性：过滤后的课程列表
const filteredCourses = computed(() => {
  if (!searchQuery.value) {
    return courses.value;
  }
  return courses.value.filter(course => 
    course.name.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
    (course.code && course.code.toLowerCase().includes(searchQuery.value.toLowerCase()))
  );
});

// 监听编辑模态框是否打开，关闭时重置编辑数据
watch(showEditModal, (value) => {
  if (!value) {
    editingCourse.value = {
      id: 0,
      name: '',
      description: '',
      subjectId: 1,
      status: 0
    };
  }
});

// 打开编辑模态框
const openEditModal = (course: Course) => {
  editingCourse.value = { ...course };
  showEditModal.value = true;
};

// 创建课程
const createCourse = async () => {
  try {
    const token = localStorage.getItem('token');
    if (!token) {
      alert('请先登录');
      return;
    }
    
    // 准备请求数据
    const requestData = {
      courseName: newCourse.value.name,
      subjectId: Number(newCourse.value.subjectId), // 转换为数字
      description: newCourse.value.description
    };
    
    // 调用API创建课程
    const response: any = await createCourseAPI(requestData, token);
    
    if (response && response.code === 200) {
      // 创建成功，刷新课程列表
      loadCourses();
      showCreateModal.value = false;
      newCourse.value = { name: '', description: '', subjectId: '' };
      alert('课程创建成功');
    } else {
      alert(response?.msg || '创建课程失败');
    }
  } catch (error) {
    console.error('创建课程失败:', error);
    alert('创建课程失败，请检查网络连接或联系管理员');
  }
};

// 更新课程
const updateCourse = async () => {
  try {
    const token = localStorage.getItem('token');
    if (!token) {
      alert('请先登录');
      return;
    }
    
    // 准备请求数据
    const requestData = {
      courseName: editingCourse.value.name,
      description: editingCourse.value.description
    };
    
    const response: any = await updateCourseInfoAPI(editingCourse.value.id, requestData, token);
    
    if (response && response.code === 200) {
      // 更新成功，直接更新本地数据以立即显示新信息
      const index = courses.value.findIndex(course => course.id === editingCourse.value.id);
      if (index !== -1) {
        courses.value[index] = { ...courses.value[index], ...editingCourse.value };
      }
      showEditModal.value = false;
      alert('课程信息更新成功');
    } else {
      alert(response?.msg || '更新课程失败');
    }
  } catch (error) {
    console.error('更新课程失败:', error);
    alert('更新课程失败，请检查网络连接或联系管理员');
  }
};

// 删除课程
const deleteCourse = async (id: number) => {
  if (confirm('确定要删除这个课程吗？')) {
    try {
      const token = localStorage.getItem('token');
      if (!token) {
        alert('请先登录');
        return;
      }
      
      const response: any = await deleteCourseAPI(id, token);
      
      if (response && response.code === 200) {
        // 删除成功，直接从本地数据中移除课程以立即显示更新
        courses.value = courses.value.filter(course => course.id !== id);
        alert('课程删除成功');
      } else {
        alert(response?.msg || '删除课程失败');
      }
    } catch (error) {
      console.error('删除课程失败:', error);
      alert('删除课程失败，请检查网络连接或联系管理员');
    }
  }
};

// 切换课程状态
const toggleCourseStatus = async (course: Course) => {
  try {
    const token = localStorage.getItem('token');
    if (!token) {
      alert('请先登录');
      return;
    }
    
    // 确定目标状态：0变为1，1变为0
    const newStatus = course.status === 0 ? 1 : 0;
    
    const response: any = await updateCourseStatusAPI(course.id, newStatus, token);
    
    if (response && response.code === 200) {
      // 更新成功，直接更新本地数据以立即显示新状态
      const index = courses.value.findIndex(c => c.id === course.id);
      if (index !== -1) {
        courses.value[index] = { ...courses.value[index], status: newStatus };
      }
      alert(`课程状态已${newStatus === 1 ? '结课' : '开启'}`);
    } else {
      alert(response?.msg || '更新课程状态失败');
    }
  } catch (error) {
    console.error('更新课程状态失败:', error);
    alert('更新课程状态失败，请检查网络连接或联系管理员');
  }
};

// 获取学科名称
const getSubjectName = (subjectId: number) => {
  const subject = subjects.value.find(s => s.subjectId === subjectId);
  return subject?.subjectName || '未知学科';
};

const loadCourses = async () => {
  try {
    const token = localStorage.getItem('token');
    if (!token) {
      console.error('用户未登录');
      return;
    }
    
    const response: any = await getTeacherCoursesAPI(token);
    
    if (response && response.code === 200) {
      // 转换API返回的数据为页面使用的格式
      courses.value = response.data.map((apiCourse: TeacherCourse) => ({
        id: apiCourse.courseId,
        name: apiCourse.courseName,
        description: apiCourse.description,
        subjectId: apiCourse.subjectId,
        status: Number(apiCourse.status), // 将字符串转换为数字
        inviteCode: apiCourse.inviteCode,
        teacherId: apiCourse.teacherId,
        createTime: apiCourse.createTime
      }));
    } else {
      console.error('获取课程列表失败:', response?.msg || '未知错误');
    }
  } catch (error) {
    console.error('获取课程列表时发生错误:', error);
  }
};

// 页面加载时获取学科列表和课程列表
onMounted(async () => {
  await loadSubjects();
  await loadCourses();
});

// 加载学科列表
const loadSubjects = async () => {
  try {
    const token = localStorage.getItem('token');
    if (!token) {
      console.error('用户未登录');
      return;
    }
    
    const response: any = await getSubjectListAPI(token);

    console.log('学科列表:', response);

    if (response && response.code === 200) {
      subjects.value = response.data['subjects'] || [];
    } else {
      console.error('获取学科列表失败:', response?.msg || '未知错误');
    }
  } catch (error) {
    console.error('获取学科列表时发生错误:', error);
  }
};
</script>