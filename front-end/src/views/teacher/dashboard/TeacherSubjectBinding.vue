<template>
  <div class="p-6 bg-base-100 rounded-lg shadow">
    <h1 class="text-2xl font-bold mb-6 text-base-content">学科绑定管理</h1>

    <div class="grid grid-cols-1 lg:grid-cols-2 gap-8">
      <!-- 左侧：可绑定学科列表 -->
      <div class="border border-base-300 rounded-lg p-6 bg-base-200">
        <h2 class="text-xl font-semibold mb-4 text-base-content">可绑定学科</h2>

        <!-- 加载状态 -->
        <div v-if="loading" class="text-center py-8">
          <div class="inline-block">
            <span class="loading loading-spinner loading-md text-primary"></span>
          </div>
          <p class="text-base-content mt-2">加载中...</p>
        </div>

        <!-- 错误提示 -->
        <div v-else-if="error" class="alert alert-error">
          <svg xmlns="http://www.w3.org/2000/svg" class="stroke-current shrink-0 h-6 w-6" fill="none" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
          <span>{{ error }}</span>
        </div>

        <!-- 学科列表 -->
        <div v-else class="space-y-2 max-h-96 overflow-y-auto">
          <div
            v-for="subject in unboundSubjects"
            :key="subject.id"
            class="flex items-center p-3 bg-base-100 border border-base-300 rounded-lg hover:bg-primary hover:text-primary-content cursor-pointer transition"
            @click="selectSubject(subject)"
            :class="{ 'bg-primary text-primary-content border-primary': selectedSubject?.id === subject.id }"
          >
            <input
              type="radio"
              :id="`subject-${subject.id}`"
              :value="subject.id"
              v-model="selectedSubjectId"
              class="radio radio-primary mr-3"
            />
            <label :for="`subject-${subject.id}`" class="flex-1 cursor-pointer font-medium">
              {{ subject.name }}
            </label>
          </div>

          <div v-if="unboundSubjects.length === 0" class="text-center text-base-content py-8">
            暂无可绑定学科
          </div>
        </div>

        <!-- 绑定按钮 -->
        <div class="mt-6 flex gap-2">
          <button
            @click="bindSubject"
            :disabled="!selectedSubject || isBinding"
            class="btn btn-primary flex-1"
            :class="{'loading': isBinding}"
          >
            {{ isBinding ? '绑定中...' : '绑定' }}
          </button>
        </div>
      </div>

      <!-- 右侧：已绑定学科列表 -->
      <div class="border border-base-300 rounded-lg p-6 bg-base-200">
        <h2 class="text-xl font-semibold mb-4 text-base-content">已绑定学科</h2>

        <!-- 加载状态 -->
        <div v-if="loadingBound" class="text-center py-8">
          <div class="inline-block">
            <span class="loading loading-spinner loading-md text-secondary"></span>
          </div>
          <p class="text-base-content mt-2">加载中...</p>
        </div>

        <!-- 错误提示 -->
        <div v-else-if="errorBound" class="alert alert-error">
          <svg xmlns="http://www.w3.org/2000/svg" class="stroke-current shrink-0 h-6 w-6" fill="none" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
          <span>{{ errorBound }}</span>
        </div>

        <!-- 已绑定学科列表 -->
        <div v-else class="space-y-2 max-h-96 overflow-y-auto">
          <div
            v-for="binding in boundSubjectsWithNames"
            :key="binding.id"
            class="flex items-center justify-between p-3 bg-base-100 border border-base-300 rounded-lg hover:bg-error hover:text-error-content transition"
          >
            <div class="flex items-center">
              <span class="font-medium">{{ binding.subjectName }}</span>
              <span v-if="binding.isMain === 1" class="ml-2 badge badge-warning badge-sm">
                主要
              </span>
            </div>
            <button
              @click="unbindSubject(binding.subjectId)"
              :disabled="isUnbinding === binding.subjectId"
              class="btn btn-error btn-sm"
              :class="{'loading': isUnbinding === binding.subjectId}"
            >
              {{ isUnbinding === binding.subjectId ? '取消中...' : '取消绑定' }}
            </button>
          </div>

          <div v-if="boundSubjectsWithNames.length === 0" class="text-center text-base-content py-8">
            暂未绑定学科
          </div>
        </div>

        <!-- 刷新按钮 -->
        <div class="mt-6">
          <button
            @click="loadBoundSubjects"
            :disabled="loadingBound"
            class="btn btn-secondary w-full"
          >
            刷新已绑定学科
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { jwtDecode } from 'jwt-decode';
import getSubjectsAPI from '../../../apis/Server/subject/getSubjectsAPI';
import {
  bindTeacherSubjectAPI,
  getTeacherSubjectsAPI,
  deleteTeacherSubjectAPI
} from '../../../apis/Server/teacherSubjectAPI';

interface Subject {
  id: number;
  name: string;
}

interface BoundSubject {
  teacherId: number;
  subjectId: number;
  isMain: number;
}

interface BoundSubjectWithName extends BoundSubject {
  subjectName: string;
  id: string;
}

interface DecodedToken {
  id: number;
  username: string;
  userType: number;
  [key: string]: any;
}

// 状态管理
const subjects = ref<Subject[]>([]);
const boundSubjects = ref<BoundSubject[]>([]);
const selectedSubject = ref<Subject | null>(null);
const selectedSubjectId = ref<number | null>(null);

// 加载状态
const loading = ref(false);
const loadingBound = ref(false);
const isBinding = ref(false);
const isUnbinding = ref<number | null>(null);

// 错误提示
const error = ref('');
const errorBound = ref('');

// 获取 token
const getToken = (): string => {
  const token = localStorage.getItem('token') || sessionStorage.getItem('token') || '';
  console.log('获取的 token:', token);
  return token;
};

// 解析 token 获取教师 ID
const getTeacherId = (): number | null => {
  const token = getToken();
  if (!token) {
    console.error('未找到 token');
    return null;
  }
  try {
    const decoded = jwtDecode<DecodedToken>(token);
    console.log('解析的 token:', decoded);
    console.log('教师 ID:', decoded.id);
    return decoded.id;
  } catch (error) {
    console.error('解析 token 失败:', error);
    return null;
  }
};

// 加载所有学科
const loadSubjects = async () => {
  loading.value = true;
  error.value = '';
  try {
    const response = await getSubjectsAPI({});
    console.log('获取所有学科响应:', response);

    // 处理多种可能的响应格式
    if (Array.isArray(response)) {
      subjects.value = response.map(subject => ({
        id: subject.subjectId,
        name: subject.subjectName
      }));
    } else if (response.data && Array.isArray(response.data.subjects)) {
      // 处理 {code: 200, msg: '操作成功', data: {subjects: [...]}} 格式
      subjects.value = response.data.subjects.map(subject => ({
        id: subject.subjectId,
        name: subject.subjectName
      }));
    } else if (response.data && Array.isArray(response.data)) {
      // 处理 {code: 200, msg: '操作成功', data: [...]} 格式
      subjects.value = response.data.map(subject => ({
        id: subject.subjectId,
        name: subject.subjectName
      }));
    } else if (response.subjects && Array.isArray(response.subjects)) {
      subjects.value = response.subjects.map(subject => ({
        id: subject.subjectId,
        name: subject.subjectName
      }));
    } else {
      console.warn('未识别的学科响应格式:', response);
      subjects.value = [];
    }
    console.log('学科列表:', subjects.value);
  } catch (err: any) {
    error.value = `加载学科失败: ${err.message || '未知错误'}`;
    console.error('加载学科出错:', err);
  } finally {
    loading.value = false;
  }
};

// 加载已绑定学科
const loadBoundSubjects = async () => {
  loadingBound.value = true;
  errorBound.value = '';
  const token = getToken();

  if (!token) {
    errorBound.value = '未找到授权信息，请重新登录';
    loadingBound.value = false;
    return;
  }

  try {
    const response = await getTeacherSubjectsAPI(token);
    console.log('获取已绑定学科响应:', response);

    // 处理多种可能的响应格式
    if (Array.isArray(response)) {
      boundSubjects.value = response;
    } else if (response.data && Array.isArray(response.data)) {
      boundSubjects.value = response.data;
    } else if (response.subjects && Array.isArray(response.subjects)) {
      boundSubjects.value = response.subjects;
    } else {
      console.warn('未识别的绑定学科响应格式:', response);
      boundSubjects.value = [];
    }
    console.log('已绑定学科列表:', boundSubjects.value);
  } catch (err: any) {
    errorBound.value = `加载已绑定学科失败: ${err.response?.data?.msg || err.message || '未知错误'}`;
    console.error('加载已绑定学科出错:', err);
  } finally {
    loadingBound.value = false;
  }
};

// 选择学科
const selectSubject = (subject: Subject) => {
  selectedSubject.value = subject;
  selectedSubjectId.value = subject.id;
  console.log('选择学科:', subject);
};

// 绑定学科
const bindSubject = async () => {
  if (!selectedSubject.value) {
    alert('请选择要绑定的学科');
    return;
  }

  const token = getToken();
  if (!token) {
    alert('未找到授权信息，请重新登录');
    return;
  }

  isBinding.value = true;
  error.value = '';

  try {
    console.log('准备绑定学科:', {
      subjectId: selectedSubject.value.id,
      isMain: 1
    });

    const response = await bindTeacherSubjectAPI(
      selectedSubject.value.id,
      1, // isMain
      token
    );

    console.log('绑定成功:', response);
    alert('学科绑定成功');

    // 重新加载已绑定学科列表
    await loadBoundSubjects();

    // 重置选择
    selectedSubject.value = null;
    selectedSubjectId.value = null;
  } catch (err: any) {
    const errorMsg = err.response?.data?.msg || err.message || '绑定失败';
    error.value = `绑定学科失败: ${errorMsg}`;
    console.error('绑定学科出错:', err);
    alert(`绑定学科失败: ${errorMsg}`);
  } finally {
    isBinding.value = false;
  }
};

// 解绑学科
const unbindSubject = async (subjectId: number) => {
  if (!confirm('确定要取消绑定该学科吗？')) {
    return;
  }

  const token = getToken();
  if (!token) {
    alert('未找到授权信息，请重新登录');
    return;
  }

  isUnbinding.value = subjectId;
  errorBound.value = '';

  try {
    console.log('准备解绑学科:', { subjectId });

    const response = await deleteTeacherSubjectAPI(subjectId, token);

    console.log('解绑成功:', response);
    alert('学科解绑成功');

    // 重新加载已绑定学科列表
    await loadBoundSubjects();
  } catch (err: any) {
    const errorMsg = err.response?.data?.msg || err.message || '解绑失败';
    errorBound.value = `解绑学科失败: ${errorMsg}`;
    console.error('解绑学科出错:', err);
    alert(`解绑学科失败: ${errorMsg}`);
  } finally {
    isUnbinding.value = null;
  }
};

// 计算未绑定学科列表
const unboundSubjects = computed(() => {
  return subjects.value.filter(
    subject => !boundSubjects.value.some(binding => binding.subjectId === subject.id)
  );
});

// 计算已绑定学科（包含名称）
const boundSubjectsWithNames = computed(() => {
  return boundSubjects.value
    .map(binding => {
      const subjectName =
        subjects.value.find(s => s.id === binding.subjectId)?.name || `学科 ${binding.subjectId}`;
      return {
        ...binding,
        id: `${binding.teacherId}-${binding.subjectId}`,
        subjectName
      } as BoundSubjectWithName;
    })
    .sort((a, b) => b.isMain - a.isMain); // 主要学科排在前面
});

// 页面初始化
onMounted(async () => {
  console.log('页面初始化...');
  const teacherId = getTeacherId();
  console.log('教师 ID:', teacherId);

  await loadSubjects();
  await loadBoundSubjects();
});
</script>

<style scoped>
/* 添加必要的滚动条样式 */
::-webkit-scrollbar {
  width: 6px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
}

::-webkit-scrollbar-thumb {
  background: #888;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: #555;
}
</style>