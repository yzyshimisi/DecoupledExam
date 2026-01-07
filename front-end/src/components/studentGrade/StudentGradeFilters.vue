<template>
  <div class="filter-container">
    <div class="form-control">
      <label class="label">
        <span class="label-text">学生ID</span>
      </label>
      <input
        v-model="tempInput.studentId"
        type="text"
        placeholder="学生ID"
        class="input input-bordered input-sm"
        @input="applyFilters"
      />
    </div>

    <div class="form-control">
      <label class="label">
        <span class="label-text">课程ID</span>
      </label>
      <input
        v-model="tempInput.courseId"
        type="text"
        placeholder="课程ID"
        class="input input-bordered input-sm"
        @input="applyFilters"
      />
    </div>

    <div class="form-control">
      <label class="label">
        <span class="label-text">成绩类型</span>
      </label>
      <input
        v-model="tempInput.gradeType"
        type="text"
        placeholder="成绩类型"
        class="input input-bordered input-sm"
        @input="applyFilters"
      />
    </div>
    
    <div class="form-control">
      <button @click="resetFilters" class="btn btn-sm btn-ghost">重置</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive } from 'vue';

// 定义筛选条件（保持原有结构以兼容后端API）
const filters = reactive({
  studentId: null as number | null,
  courseId: null as number | null,
  gradeType: '' as string | '',
});

// 新增：临时输入字段，用于处理用户输入
const tempInput = reactive({
  studentId: '',
  courseId: '',
  gradeType: '',
});

// 应用筛选条件（实现模糊查询）
const applyFilters = () => {
  // 处理学生ID：如果输入为空则设为null，否则尝试转换为数字
  filters.studentId = tempInput.studentId.trim() 
    ? (isNaN(Number(tempInput.studentId.trim())) ? null : Number(tempInput.studentId.trim()))
    : null;

  // 处理课程ID：如果输入为空则设为null，否则尝试转换为数字
  filters.courseId = tempInput.courseId.trim()
    ? (isNaN(Number(tempInput.courseId.trim())) ? null : Number(tempInput.courseId.trim()))
    : null;

  // 处理成绩类型：去除前后空格
  filters.gradeType = tempInput.gradeType.trim();

  // 发送筛选条件变化事件
  emit('filter-change', { ...filters });
};

// 定义组件事件
const emit = defineEmits<{
  'filter-change': [filters: typeof filters]
}>();

// 重置筛选条件
const resetFilters = () => {
  tempInput.studentId = '';
  tempInput.courseId = '';
  tempInput.gradeType = '';
  
  filters.studentId = null;
  filters.courseId = null;
  filters.gradeType = '';
  
  applyFilters();
};

// 暴露重置方法
defineExpose({
  resetFilters
});
</script>

<style scoped>
.filter-container {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.form-control {
  min-width: 150px;
}
</style>