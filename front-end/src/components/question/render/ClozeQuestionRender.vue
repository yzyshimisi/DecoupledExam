<!-- ClozeQuestionRender.vue -->
<template>
  <div class="cloze-question">
    <!-- 题干：仅显示文本，用下划线表示空格 -->
    <div class="passage mb-6 leading-relaxed text-gray-800">
      <p v-html="formattedPassage"></p>
    </div>

    <!-- 选项区：每个空一组选项 -->
    <div class="options-section mb-6 space-y-4">
      <div
          v-for="item in sortedItems"
          :key="item.sequence"
          class="option-group border-b pb-3 last:border-0"
      >
        <div class="flex items-center gap-2 mb-2">
          <span class="font-medium text-blue-600">第 {{ item.sequence }} 空：</span>
        </div>
        <div class="flex flex-wrap gap-2">
          <label
              v-for="opt in getOptions(item.sequence)"
              :key="opt.key"
              class="cursor-pointer"
          >
            <input
                type="radio"
                :name="`cloze-${item.sequence}`"
                :value="opt.key"
                v-model="localAnswers[item.sequence]"
                class="sr-only"
            />
            <div
                class="px-3 py-1.5 border rounded transition-colors"
                :class="{
                'border-blue-500 bg-blue-50 text-blue-700 font-medium': localAnswers[item.sequence] === opt.key,
                'border-gray-300 hover:bg-gray-50': localAnswers[item.sequence] !== opt.key
              }"
            >
              {{ opt.key }}. {{ opt.value }}
            </div>
          </label>
        </div>
      </div>
    </div>

    <!-- 解析区：按空的 sequence 排序展示 -->
    <div v-if="showSolution" class="analysis-section mt-6 space-y-4">
      <div
          v-for="item in sortedItems"
          :key="item.sequence"
          class="analysis-item p-4 bg-blue-50 border-l-4 border-blue-600 rounded"
      >
        <div class="font-medium text-blue-700">
          第 {{ item.sequence }} 空：
          <span class="ml-2">{{ getCorrectAnswerLabel(item.sequence) }}</span>
        </div>
        <div class="mt-2 text-gray-700" v-html="getAnalysis(item.sequence)"></div>
      </div>
    </div>

    <!-- 分值 -->
    <div v-if="score !== undefined" class="mt-2 text-right text-sm text-gray-500">
      （{{ score }} 分）
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'

interface QuestionItem {
  sequence: number
  questionComponents: Array<{
    componentType: string
    content: string
  }>
}

interface Question {
  title: string
  questionItems: QuestionItem[]
}

const props = defineProps<{
  question: Question
  showSolution?: boolean
  score?: number
  readonly?: boolean
  modelValue?: Record<string, string>
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: Record<string, string>): void
}>()

// 用户答案
const localAnswers = ref<Record<string, string>>({})

// 同步外部 modelValue
watch(
    () => props.modelValue,
    (newValue) => {
      if (newValue) {
        localAnswers.value = { ...newValue }
      } else {
        localAnswers.value = {}
      }
    },
    { immediate: true }
)

// 发送更新
watch(localAnswers, (newVal) => {
  emit('update:modelValue', newVal)
}, { deep: true })

// === 格式化题干：将 __1__ 替换为 _ _ _ ===
const formattedPassage = computed(() => {
  const title = props.question.title || ''
  // 将 __数字__ 替换为 _ _ _
  return title.replace(/__(\d+)__/g, '<u>____</u>')
})

// === 获取选项 ===
const getOptions = (seq: number) => {
  const item = props.question.questionItems.find(i => i.sequence === seq)
  if (!item) return []
  const optionComp = item.questionComponents.find(c => c.componentType === 'option')
  try {
    const data = JSON.parse(optionComp?.content || '{}')
    return data.options || []
  } catch {
    return []
  }
}

// === 获取正确答案标签 ===
const getCorrectAnswer = (seq: number) => {
  const item = props.question.questionItems.find(i => i.sequence === seq)
  if (!item) return null
  const answerComp = item.questionComponents.find(c => c.componentType === 'answer')
  try {
    const data = JSON.parse(answerComp?.content || '{}')
    const correctKey = data.correctKey
    const options = getOptions(seq)
    const correctOpt = options.find((opt: any) => opt.key === correctKey)
    return correctOpt || { key: correctKey, value: '未知' }
  } catch {
    return null
  }
}

const getCorrectAnswerLabel = (seq: number) => {
  const ans = getCorrectAnswer(seq)
  return ans ? `${ans.key}. ${ans.value}` : '未设置'
}

// === 获取解析 ===
const getAnalysis = (seq: number) => {
  const item = props.question.questionItems.find(i => i.sequence === seq)
  if (!item) return ''
  const analysisComp = item.questionComponents.find(c => c.componentType === 'analysis')
  try {
    const data = JSON.parse(analysisComp?.content || '{}')
    return data.analysisDesc || ''
  } catch {
    return ''
  }
}

// === 按 sequence 排序项目（确保解析顺序正确）===
const sortedItems = computed(() => {
  return [...props.question.questionItems].sort((a, b) => a.sequence - b.sequence)
})
</script>

<style scoped>
.cloze-question {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  color: #333;
}

.passage u {
  @apply underline decoration-dashed decoration-2;
}

.option-group label input[type="radio"]:checked + div {
  @apply border-blue-500 bg-blue-50 text-blue-700;
}
</style>