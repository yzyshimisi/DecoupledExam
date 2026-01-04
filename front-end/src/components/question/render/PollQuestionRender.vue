<!-- PollQuestionRender.vue -->
<template>
  <div class="poll-question">
    <!-- 题干 -->
    <div class="question-stem mb-5">
      <h3 class="text-lg font-semibold text-gray-800 leading-relaxed">
        {{ question.title }}
      </h3>
    </div>

    <!-- 选项区 -->
    <div class="options space-y-3">
      <button
          v-for="opt in options"
          :key="opt.key"
          type="button"
          class="w-full text-left px-4 py-3 rounded-lg border transition-all duration-200 flex items-center"
          :class="{
          'border-blue-500 bg-blue-50 text-blue-800 font-medium shadow-sm': localVote === opt.key,
          'border-gray-300 bg-white text-gray-700 hover:bg-gray-50 hover:border-gray-400': localVote !== opt.key
        }"
          @click="selectOption(opt.key)"
      >
        <span class="inline-flex items-center justify-center w-6 h-6 mr-3 rounded-full border"
              :class="{
            'border-blue-500 bg-blue-500 text-white': localVote === opt.key,
            'border-gray-400': localVote !== opt.key
          }"
        >
          {{ opt.key }}
        </span>
        <span>{{ opt.value }}</span>
      </button>
    </div>

    <!-- 分析/说明（可选） -->
    <div
        v-if="showSolution && analysisText"
        class="analysis mt-4 p-3 bg-blue-50 border-l-4 border-blue-500 text-gray-700 rounded text-sm"
    >
      <strong>说明：</strong>
      <span v-html="analysisText"></span>
    </div>

    <!-- 分值（可选） -->
    <div v-if="score !== undefined" class="mt-3 text-right text-xs text-gray-500">
      （{{ score }} 分）
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'

interface QuestionComponent {
  componentType: string
  content: string
}

interface Question {
  title: string
  questionComponents: QuestionComponent[]
}

const props = defineProps<{
  question: Question
  showSolution?: boolean
  score?: number
  modelValue?: string // e.g. "B"
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()

// 解析选项
const options = computed(() => {
  const optComp = props.question.questionComponents.find(c => c.componentType === 'option')
  if (!optComp) return []
  try {
    const data = JSON.parse(optComp.content)
    return data.options || []
  } catch (e) {
    console.error('解析投票选项失败', e)
    return []
  }
})

// 解析分析文本
const analysisText = computed(() => {
  const analysisComp = props.question.questionComponents.find(c => c.componentType === 'analysis')
  if (!analysisComp) return ''
  try {
    const data = JSON.parse(analysisComp.content)
    return data.analysis || ''
  } catch (e) {
    return analysisComp.content || ''
  }
})

// 本地投票选择
const localVote = ref<string | undefined>(undefined)

// 同步外部 modelValue
watch(
    () => props.modelValue,
    (newValue) => {
      localVote.value = newValue
    },
    { immediate: true }
)

// 用户点击选项
const selectOption = (key: string) => {
  localVote.value = key
  emit('update:modelValue', key)
}
</script>

<style scoped>
.poll-question {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}
</style>