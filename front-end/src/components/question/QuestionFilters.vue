<template>
  <div class="rounded-xl p-6 shadow-sm border-4 border-gray-200">
    <h3 class="font-semibold text-lg text-gray-800 mb-5">筛选条件</h3>

    <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
      <!-- 左列 -->
      <div class="space-y-6">
        <!-- 1. 仅显示我的题目 + 难度（同一行）+ 重置按钮 -->
        <div class="flex flex-row items-center gap-5 justify-between">
          <div class="flex flex-row items-center">
            <div>
              <input type="checkbox" class="toggle" v-model="modelValue.mineOnly" />
            </div>
            <span class="ml-3 text-gray-700 whitespace-nowrap">仅显示我的题目</span>
          </div>

          <div class="form-control flex flex-row">
            <label class="label text-gray-600 text-base whitespace-nowrap">难度：</label>
            <div class="flex items-center space-x-2">
              <input
                  type="range"
                  min="1"
                  max="5"
                  class="range range-xs w-[23vw]"
                  :value="modelValue.maxDifficulty"
                  @input="update('maxDifficulty', Number(($event.target as HTMLInputElement).value))"
              />
              <span class="text-xs text-gray-600">{{ modelValue.maxDifficulty }}</span>
            </div>
          </div>

          <div class="flex justify-end">
            <button
                @click="emit('reset')"
                class="btn btn-sm border border-gray-300 text-gray-600 hover:bg-gray-50 transition-colors">
              重置
            </button>
          </div>
        </div>

        <!-- 2. 题干关键词 + 出题人（同一列） -->
        <div class="space-y-4">
          <div class="form-control w-full flex flex-row">
            <label class="label text-gray-600 whitespace-nowrap">出题人ID：</label>
            <input
                type="text"
                class="input w-full border border-gray-300 rounded-lg focus:border-primary focus:ring-1 focus:ring-primary"
                placeholder="输入出题人ID"
                :value="modelValue.authorQuery"
                @input="update('authorQuery', ($event.target as HTMLInputElement).value.trim())"
            />
          </div>

          <div class="form-control w-full flex flex-row items-center">
            <label class="label text-gray-600 whitespace-nowrap">题干关键词：</label>
            <input
                type="text"
                class="input w-full border border-gray-300 rounded-lg focus:border-primary focus:ring-1 focus:ring-primary"
                placeholder="输入题干关键词"
                :value="modelValue.stemKeyword"
                @input="update('stemKeyword', ($event.target as HTMLInputElement).value.trim())"
            />
          </div>
        </div>
      </div>

      <!-- 右列 - 多选筛选 -->
      <div class="space-y-4">
        <!-- 3. 题型 + 所属学科 + 审核状态 -->
        <div class="form-control w-full flex flex-row">
          <label class="label text-gray-600 whitespace-nowrap">题型：</label>
          <Multiselect
              v-model="localSelectedTypes"
              :options="uniqueTypes"
              :multiple="true"
              :placeholder="'不限'"
              :close-on-select="false"
              label="typeName"
              track-by="typeId"
              @update:modelValue="handleTypesChange"
          />
        </div>

        <div class="form-control w-full flex flex-row">
          <label class="label text-gray-600 whitespace-nowrap">所属学科：</label>
          <Multiselect
              v-model="localSelectedSubjects"
              :options="uniqueSubjects"
              :multiple="true"
              :searchable="true"
              :placeholder="'不限'"
              :close-on-select="false"
              label="subjectName"
              track-by="subjectId"
              @update:modelValue="handleSubjectsChange"
              class="w-full"
          />
        </div>

        <div class="form-control w-full flex flex-row">
          <label class="label text-gray-600 whitespace-nowrap">审核状态：</label>
          <Multiselect
              v-model="localSelectedStatuses"
              :options="statusOptions"
              :multiple="true"
              :searchable="false"
              :placeholder="'不限'"
              :close-on-select="false"
              label="label"
              track-by="value"
              @update:modelValue="handleStatusesChange"
              class="w-full"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch, onMounted } from 'vue'
import Multiselect from 'vue-multiselect'
import 'vue-multiselect/dist/vue-multiselect.css'
import { getQuestionTypeAPI, getSubjectAPI } from "../../apis"
import { useRequest } from "vue-hooks-plus";

// ========== 接口定义 ==========
interface FilterModel {
  mineOnly: boolean
  stemKeyword: string
  selectedTypes: string[]
  selectedSubjects: string[]
  maxDifficulty: number
  selectedStatuses: ('0' | '1' | '2')[]
  authorQuery: string
}

const props = defineProps<{
  modelValue: FilterModel
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: FilterModel): void
  (e: 'reset'): void
}>()

onMounted(() => {
  useRequest(() => getQuestionTypeAPI(), {
    onSuccess(res) {
      if (res['code'] === 200) {
        uniqueTypes.value = res['data']
      }
    }
  })

  useRequest(() => getSubjectAPI(), {
    onSuccess(res) {
      if (res['code'] === 200) {
        uniqueSubjects.value = res['data']['subjects']
      }
    }
  })
})

// ========== 选项数据 ==========
const uniqueTypes = ref([])

const uniqueSubjects = ref([])

const statusOptions = [
  { label: '待审核', value: '0' },
  { label: '已通过', value: '1' },
  { label: '已拒绝', value: '2' }
]

// ========== 本地响应式状态 ==========
const localSelectedTypes = ref<string[]>([...props.modelValue.selectedTypes])
const localSelectedSubjects = ref<string[]>([...props.modelValue.selectedSubjects])
const localSelectedStatuses = ref<Array<'0' | '1' | '2'>>([...props.modelValue.selectedStatuses])

watch(
    () => props.modelValue,
    (newVal) => {
      localSelectedTypes.value = [...newVal.selectedTypes]
      localSelectedSubjects.value = [...newVal.selectedSubjects]
      localSelectedStatuses.value = [...newVal.selectedStatuses]
    },
    { deep: true }
)

// ========== 事件处理 ==========
const update = (key: keyof FilterModel, value: any) => {
  emit('update:modelValue', {
    ...props.modelValue,
    [key]: value
  })
}

const handleTypesChange = (value: string[]) => {
  update('selectedTypes', value)
}

const handleSubjectsChange = (value: string[]) => {
  update('selectedSubjects', value)
}

const handleStatusesChange = (value: Array<'pending' | 'approved' | 'rejected'>) => {
  update('selectedStatuses', value)
}

const reset = () => {
  emit('reset')
}
</script>

<style scoped>
/* 自定义样式优化 */
:deep(.multiselect) {
  @apply text-sm;
}
:deep(.multiselect__tags) {
  @apply border border-gray-300 rounded-lg bg-white;
}
:deep(.multiselect__single) {
  @apply text-gray-700;
}
:deep(.multiselect__option) {
  @apply text-gray-700;
}
:deep(.multiselect__option--selected) {
  @apply bg-primary text-white;
}
:deep(.multiselect__input) {
  @apply border-none outline-none;
}
:deep(.multiselect__select) {
  @apply text-gray-400;
}

.dot {
  transition: 0.3s;
}
</style>