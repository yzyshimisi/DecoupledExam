<!-- ReadingComprehensionRender.vue -->
<template>
  <div class="reading-comprehension">
    <!-- 文章部分 -->
    <div class="passage mb-8 p-4 bg-gray-50 rounded-lg border border-gray-200">
      <h3 class="font-bold text-gray-700 mb-2">Passage:</h3>
      <div class="prose prose-sm max-w-none" v-html="formattedPassage"></div>
    </div>

    <!-- 小题列表 -->
    <div class="questions space-y-6">
      <div
          v-for="item in question.questionItems"
          :key="item.sequence"
          class="question-item border-b pb-6 last:border-0 last:pb-0"
      >
        <!-- 题干 -->
        <div class="question-stem font-medium mb-3">
          {{ item.sequence }}. {{ item.content }}
        </div>

        <!-- 答题区 -->
        <div v-if="!readonly" class="options space-y-2">
          <label
              v-for="(option, idx) in parseOptions(item)"
              :key="idx"
              class="flex items-start cursor-pointer"
          >
            <input
                type="radio"
                :name="`q-${item.sequence}`"
                :value="option.key"
                v-model="localAnswers[item.sequence]"
                class="mt-1 mr-2 h-4 w-4 text-blue-600 focus:ring-blue-500"
            />
            <span class="text-gray-800">{{ option.key }}. {{ option.value }}</span>
          </label>
        </div>

        <!-- 只读模式：显示正确答案 -->
        <div v-if="showSolution" class="mt-4 p-3 bg-green-50 border-l-4 border-green-500 text-green-700 rounded">
          正确答案：{{ getCorrectAnswerLabel(item) }}
        </div>

        <!-- 解析（按需显示） -->
        <div
            v-if="showSolution"
            class="analysis mt-3 p-3 bg-blue-50 border-l-4 border-blue-500 text-blue-700 rounded"
        >
          <strong>解析：</strong>
          <span v-html="getAnalysis(item)"></span>
        </div>
      </div>
    </div>

    <!-- 全局解析 -->
    <div
        v-if="showSolution && globalAnalysis"
        class="global-analysis mt-8 p-4 bg-indigo-50 border-l-4 border-indigo-500 rounded"
    >
      <strong>全文解析：</strong>
      <span v-html="globalAnalysis"></span>
    </div>

    <!-- 分值 -->
    <div v-if="score !== undefined" class="mt-4 text-right text-sm text-gray-500">
      （共 {{ score }} 分）
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'

interface QuestionComponent {
  componentType: string
  content: string
}

interface QuestionItem {
  sequence: number
  content: string
  typeId: number
  questionComponents: QuestionComponent[]
}

interface Question {
  title: string
  questionItems: QuestionItem[]
  questionComponents?: QuestionComponent[]
}

const props = defineProps<{
  question: Question
  showSolution?: boolean
  score?: number
  readonly?: boolean
  modelValue?: Record<string, string> // e.g. { "1": "B", "2": "A" }
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
      localAnswers.value = newValue ? { ...newValue } : {}
    },
    { immediate: true }
)

// 当本地答案变化，emit 出去
watch(localAnswers, (newVal) => {
  emit('update:modelValue', newVal)
}, { deep: true })

// 格式化文章（保留换行）
const formattedPassage = computed(() => {
  return (props.question.title || '').replace(/\n/g, '<br />')
})

// 获取全局解析
const globalAnalysis = computed(() => {
  const comp = props.question.questionComponents?.find(c => c.componentType === 'global_analysis')
  if (!comp) return ''
  try {
    const data = JSON.parse(comp.content)
    return data.text || ''
  } catch {
    return comp.content || ''
  }
})

// 解析选项（兼容你的 content 结构）
const parseOptions = (item: QuestionItem) => {
  const optComp = item.questionComponents.find(c => c.componentType === 'option')
  if (!optComp) return []
  try {
    // 你的数据中 option.content 是 {"text": "A. xxx<br/>B. yyy"}
    const data = JSON.parse(optComp.content)
    const html = data.text || ''
    // 拆分 <br/> 得到每行
    const lines = html.split(/<br\s*\/?>/i).map((s: string) => s.trim()).filter(Boolean)
    return lines.map((line: string) => {
      const match = line.match(/^([A-Z])\.\s*(.*)$/)
      if (match) {
        return { key: match[1], value: match[2] }
      }
      return { key: '', value: line }
    })
  } catch (e) {
    console.error('解析选项失败', e)
    return []
  }
}

// 获取正确答案 key
const getCorrectKey = (item: QuestionItem) => {
  const ansComp = item.questionComponents.find(c => c.componentType === 'answer')
  if (!ansComp) return ''
  try {
    const data = JSON.parse(ansComp.content)
    return data.text || ''
  } catch {
    return ansComp.content || ''
  }
}

// 获取正确答案标签（如 "B. Over-reliance..."）
const getCorrectAnswerLabel = (item: QuestionItem) => {
  const correctKey = getCorrectKey(item)
  const options = parseOptions(item)
  const opt = options.find(o => o.key === correctKey)
  return opt ? `${opt.key}. ${opt.value}` : correctKey
}

// 获取解析文本
const getAnalysis = (item: QuestionItem) => {
  const analysisComp = item.questionComponents.find(c => c.componentType === 'analysis')
  if (!analysisComp) return ''
  try {
    const data = JSON.parse(analysisComp.content)
    return data.text || ''
  } catch {
    return analysisComp.content || ''
  }
}
</script>

<style scoped>
.reading-comprehension {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  color: #333;
  line-height: 1.6;
}

.prose :deep(p) {
  margin: 0.5em 0;
}

/* 单选按钮对齐 */
.reading-comprehension label {
  display: flex;
  align-items: flex-start;
}
</style>