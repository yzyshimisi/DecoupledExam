<template>
  <div class="calculation-question">
    <!-- 题目 -->
    <h3 class="question-title">{{ question.title }}</h3>

    <!-- 答题区域（非只读时） -->
    <div v-if="!readonly" class="answer-input-area">
      <label for="calc-answer" class="sr-only">请输入计算结果</label>
      <input
          id="calc-answer"
          v-model="localAnswer"
          type="text"
          inputmode="decimal"
          :placeholder="placeholderText"
          @input="onInput"
          class="answer-input"
      />
    </div>

    <!-- 标准答案与步骤 -->
    <div v-if="showSolution && answerData" class="answer-desc">
      <strong>标准答案：</strong>{{ answerData.finalAnswer }}
      <div v-if="answerData.steps && answerData.steps.length" class="steps mt-2">
        <strong>解题步骤：</strong>
        <div class="steps-content">
          <div v-for="(step, index) in answerData.steps" :key="index" class="step-item">
            {{ step }}
          </div>
        </div>
      </div>
    </div>

    <!-- 解析 -->
    <div v-if="showSolution && analysisData" class="analysis">
      <strong>解析：</strong>{{ analysisData.analysisDesc }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue'

const props = defineProps<{
  question: any;
  score?: number;
  readonly?: boolean;
  showSolution?: boolean; // 由父组件控制
  modelValue?: string;    // 用户输入的答案（字符串形式）
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void;
}>()

// 解析数据
const answerData = ref<any>(null)
const analysisData = ref<any>(null)

// 本地答案
const localAnswer = ref<string>(props.modelValue || '')

// 同步外部 modelValue
watch(() => props.modelValue, (val) => {
  localAnswer.value = val || ''
})

onMounted(() => {
  try {
    const getComponentContent = (type: string) => {
      const comp = props.question.questionComponents.find((c: any) => c.componentType === type)
      return comp ? JSON.parse(comp.content) : null
    }

    answerData.value = getComponentContent('answer')
    analysisData.value = getComponentContent('analysis')
  } catch (err) {
    console.error('解析计算题失败', err)
  }
})

const onInput = () => {
  if (props.readonly) return
  // 允许用户输入数字、小数点、负号等，不做强制格式化
  emit('update:modelValue', localAnswer.value.trim())
}

const placeholderText = computed(() => {
  return props.score ? `请输入计算结果（本题 ${props.score} 分）` : '请输入计算结果'
})
</script>

<style scoped>
.calculation-question {
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

.answer-input-area {
  margin-bottom: 16px;
}

.answer-input {
  width: 100%;
  max-width: 240px;
  padding: 10px 12px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 1rem;
  background-color: white;
  transition: border-color 0.2s;
}

.answer-input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.2);
}

/* 答案与步骤样式 */
.answer-desc,
.analysis {
  margin-top: 16px;
  padding: 12px;
  background-color: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 0.95rem;
  color: #4b5563;
}

.answer-desc {
  border-left: 4px solid #10b981;
  color: #10b981;
  font-weight: 500;
}

.steps-list {
  margin-top: 8px;
  padding-left: 20px;
  list-style-type: decimal;
}

.step-item {
  margin-top: 4px;
  color: #4b5563;
}

.analysis {
  border-left: 4px solid #6366f1;
  color: #6366f1;
}

.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}
</style>