<template>
  <div class="filter-container">
    <div class="form-control">
      <label class="label">
        <span class="label-text">学生ID</span>
      </label>
      <input 
        v-model="filters.studentId" 
        type="number" 
        placeholder="输入学生ID" 
        class="input input-bordered input-sm" 
        @change="applyFilters"
      />
    </div>
    
    <div class="form-control">
      <label class="label">
        <span class="label-text">课程ID</span>
      </label>
      <input 
        v-model="filters.courseId" 
        type="number" 
        placeholder="输入课程ID" 
        class="input input-bordered input-sm" 
        @change="applyFilters"
      />
    </div>
    
    <div class="form-control">
      <label class="label">
        <span class="label-text">成绩类型</span>
      </label>
      <input 
        v-model="filters.gradeType" 
        type="text" 
        placeholder="输入成绩类型" 
        class="input input-bordered input-sm" 
        @input="applyFilters"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive } from 'vue';

const filters = reactive({
  studentId: null as number | null,
  courseId: null as number | null,
  gradeType: '' as string | '',
});

// 应用筛选条件
const applyFilters = () => {
  // 发送筛选条件变化事件
  emit('filter-change', { ...filters });
};

// 定义组件事件
const emit = defineEmits<{
  'filter-change': [filters: typeof filters]
}>();

// 重置筛选条件
const resetFilters = () => {
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