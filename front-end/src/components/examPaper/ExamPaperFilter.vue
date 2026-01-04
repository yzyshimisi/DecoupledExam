<template>
  <div class="card bg-base-100 shadow-md p-4 text-base">
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-4">
      <!-- 所属课程 -->
      <div>
        <label class="label text-base">
          <span class="label-text text-base">所属课程</span>
        </label>
        <select v-model="filters.course" class="select select-bordered w-full text-base">
          <option value="">全部</option>
          <option v-for="course in courseOptions" :key="course" :value="course">
            {{ course }}
          </option>
        </select>
      </div>

      <!-- 组卷方式 -->
      <div>
        <label class="label text-base">
          <span class="label-text text-base">组卷方式</span>
        </label>
        <select v-model="filters.paperType" class="select select-bordered w-full text-base">
          <option value="">全部</option>
          <option value="manual">手动组卷</option>
          <option value="auto">自动组卷</option>
        </select>
      </div>

      <!-- 是否封存 -->
      <div>
        <label class="label text-base">
          <span class="label-text text-base">是否封存</span>
        </label>
        <select v-model="filters.isArchived" class="select select-bordered w-full text-base">
          <option value="">全部</option>
          <option :value="true">是</option>
          <option :value="false">否</option>
        </select>
      </div>

      <!-- 操作按钮（可选） -->
      <div class="flex items-end">
        <button class="btn btn-outline btn-primary text-base px-4 py-2">
          重置
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from "vue"

// 模拟课程选项
const courseOptions = ['高等数学', '大学物理', '数据结构', '操作系统', '计算机网络']

// 筛选条件
interface Filters {
  course: string
  paperType: string
  isArchived: string // 注意：HTML select 的值是字符串，需转换
}

const filters = ref<Filters>({
  course: '',
  paperType: '',
  isArchived: '',
})

// 过滤后的数据
const filteredPapers = computed(() => {

})

// 重置筛选
const resetFilters = () => {
  filters.value = {
    course: '',
    paperType: '',
    isArchived: '',
  }
}
</script>

<style scoped>

</style>