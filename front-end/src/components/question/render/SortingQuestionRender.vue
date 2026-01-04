<!-- src/components/question/render/SortingQuestionRender.vue -->
<template>
  <div class="sorting-question">
    <!-- 题干 -->
    <h3 class="question-title" v-html="question.title || ''"></h3>

    <!-- 提示 -->
    <div class="text-sm text-gray-600 mb-3">
      {{ readonly ? '正确顺序如下：' : '请拖动选项调整顺序：' }}
    </div>

    <!-- 只读模式：显示有序列表 -->
    <ol v-if="readonly" class="list-decimal pl-5 space-y-2">
      <li
          v-for="id in correctOrder"
          :key="id"
          class="p-3 bg-white border border-gray-200 rounded shadow-sm"
      >
        {{ itemMap[id] || `未知项（${id}）` }}
      </li>
    </ol>

    <!-- 答题模式：可拖拽 -->
    <!-- 修改点：移除了 component-data，使用 tag="ul" -->
    <draggable
        v-else
        v-model="localOrder"
        tag="ul"
        item-key="id"
        :animation="150"
        ghost-class="sortable-ghost"
        handle=".drag-handle"
        class="list-container pl-0 space-y-2"
    >
      <template #item="{ element }">
        <li
            class="flex items-center gap-3 p-3 bg-white border border-gray-200 rounded shadow-sm hover:bg-gray-50 transition-colors list-none"
        >
          <!-- 拖拽手柄 -->
          <!-- 修改点：加宽点击区域，禁止默认行为，确保 cursor 生效 -->
          <div class="drag-handle p-2 -ml-2 text-gray-400 hover:text-gray-600 cursor-grab active:cursor-grabbing select-none">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 8h16M4 16h16" />
            </svg>
          </div>
          <!-- 内容 -->
          <span class="flex-1 select-none">{{ element.content }}</span>
        </li>
      </template>
    </draggable>

    <!-- 正确答案说明 -->
    <div v-if="showSolution && answerData" class="answer-desc mt-4">
      <strong>正确答案：</strong>
      {{ answerData.orderDesc || '按指定顺序排列' }}
    </div>

    <!-- 解析 -->
    <div v-if="showSolution && analysisData" class="analysis mt-2">
      <strong>解析：</strong>
      <span v-html="analysisData.analysisDesc || ''"></span>
    </div>

    <!-- 分值 -->
    <div v-if="score !== undefined" class="mt-2 text-right text-sm text-gray-500">
      （{{ score }} 分）
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
// 修改点：确保导入的是支持 Vue 3 的版本
import draggable from 'vuedraggable'

interface Question {
  title?: string
  questionComponents?: Array<{
    componentType: string
    content: string
  }>
}

const props = defineProps<{
  question: Question
  showSolution?: boolean
  score?: number
  readonly?: boolean
  modelValue?: string[]
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: string[]): void
}>()

// === 解析题目数据 ===
const sortComponent = computed(() =>
    props.question.questionComponents?.find(c => c.componentType === 'sort')
)
const answerComponent = computed(() =>
    props.question.questionComponents?.find(c => c.componentType === 'answer')
)
const analysisComponent = computed(() =>
    props.question.questionComponents?.find(c => c.componentType === 'analysis')
)

const itemMap = computed<Record<string, string>>(() => {
  try {
    const data = JSON.parse(sortComponent.value?.content || '{}')
    return Object.fromEntries(
        (data.itemList || []).map((item: any) => [item.id, item.content])
    )
  } catch (e) {
    console.error('解析排序项失败', e)
    return {}
  }
})

const correctOrder = computed<string[]>(() => {
  try {
    const ans = JSON.parse(answerComponent.value?.content || '{}')
    return ans.correctOrder || []
  } catch {
    return []
  }
})

const answerData = computed(() => {
  try {
    return JSON.parse(answerComponent.value?.content || '{}')
  } catch {
    return null
  }
})

const analysisData = computed(() => {
  try {
    return JSON.parse(analysisComponent.value?.content || '{}')
  } catch {
    return null
  }
})

// 所有选项（用于初始化）
const allItems = computed(() => {
  return Object.entries(itemMap.value).map(([id, content]) => ({ id, content }))
})

// 本地响应式顺序
const localOrder = ref<{ id: string; content: string }[]>([])

// 初始化
watch(
    () => [props.modelValue, props.readonly],
    () => {
      if (props.readonly) {
        localOrder.value = correctOrder.value.map(id => ({
          id,
          content: itemMap.value[id] || `未知项（${id}）`
        }))
      } else {
        if (props.modelValue && props.modelValue.length > 0) {
          const map = new Map(allItems.value.map(i => [i.id, i]))
          localOrder.value = props.modelValue
              .map(id => map.get(id))
              .filter(Boolean) as { id: string; content: string }[]

          const currentIds = new Set(localOrder.value.map(i => i.id))
          allItems.value.forEach(item => {
            if (!currentIds.has(item.id)) {
              localOrder.value.push(item)
            }
          })
        } else {
          // 随机打乱
          const shuffled = [...allItems.value].sort(() => Math.random() - 0.5)
          localOrder.value = shuffled
        }
      }
    },
    { immediate: true, deep: true }
)

// 同步到父组件
watch(
    localOrder,
    (newOrder) => {
      console.log('拖拽后的顺序:', newOrder) // 调试用：看这里是否有输出
      const ids = newOrder.map(item => item.id)
      emit('update:modelValue', ids)
    },
    { deep: true }
)
</script>

<style scoped>
.sorting-question {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  line-height: 1.6;
  color: #333;
}

.question-title {
  font-size: 1.1rem;
  font-weight: 500;
  margin-bottom: 16px;
  color: #1f2937;
}

/* 修改点：更明显的拖拽占位样式 */
.sorting-question :deep(.sortable-ghost) {
  opacity: 0.4;
  background-color: #eff6ff;
  border: 2px dashed #3b82f6;
}

.answer-desc,
.analysis {
  padding: 12px;
  background-color: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 0.95rem;
}

.answer-desc {
  border-left: 4px solid #10b981;
  color: #10b981;
  font-weight: 500;
}

.analysis {
  border-left: 4px solid #6366f1;
  color: #6366f1;
}

/* 修改点：确保拖拽手柄样式优先级高 */
.sorting-question :deep(.drag-handle) {
  cursor: grab !important;
  touch-action: none; /* 防止移动端滚动干扰拖拽 */
}

.sorting-question :deep(.drag-handle:active) {
  cursor: grabbing !important;
  color: #333;
}

/* 移动端优化 */
@media (max-width: 768px) {
  .question-title {
    font-size: 1rem;
  }
}
</style>
