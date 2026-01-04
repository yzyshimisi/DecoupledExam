<template>
  <div class="descriptive-question">
    <!-- 题目 -->
    <h3 class="question-title">{{ question.title }}</h3>

    <!-- 答题区域（仅在非 readonly 模式下显示） -->
    <div v-if="!readonly" class="answer-area">
      <label for="descriptive-answer" class="sr-only">请输入您的答案</label>
      <textarea
          id="descriptive-answer"
          v-model="localAnswer"
          :placeholder="placeholderText"
          @input="onInput"
          class="answer-textarea"
          rows="6"
      ></textarea>
    </div>

    <!-- 参考答案 -->
    <div v-if="showSolution && answerData" class="answer-desc">
      <strong>参考答案：</strong>{{ answerData.completeAnswer }}
    </div>

    <!-- 解析 -->
    <div v-if="showSolution && analysisData" class="analysis">
      <strong>解析：</strong>{{ analysisData.analysisDesc }}
    </div>
  </div>
</template>

<script setup lang="ts">
import {ref, onMounted, watch, computed} from 'vue'

const props = defineProps<{
  question: any;
  score?: number;
  readonly?: boolean;
  showSolution?: boolean; // 由父组件控制是否显示答案/解析
  modelValue?: string;    // 用户输入的文本答案
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
    console.error('解析简答题失败', err)
  }
})

const onInput = () => {
  if (props.readonly) return
  emit('update:modelValue', localAnswer.value)
}

// 占位提示文本
const placeholderText = computed(() => {
  return props.score ? `请输入您的答案（本题 ${props.score} 分）` : '请输入您的答案'
})
</script>

<style scoped>
.descriptive-question {
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

.answer-area {
  margin-bottom: 16px;
}

.answer-textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 1rem;
  font-family: inherit;
  resize: vertical;
  background-color: white;
  transition: border-color 0.2s;
}

.answer-textarea:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.2);
}

.answer-textarea:disabled {
  background-color: #f9fafb;
  cursor: not-allowed;
}

/* 复用统一的答案/解析样式 */
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

.analysis {
  border-left: 4px solid #6366f1;
  color: #6366f1;
}

/* 屏幕阅读器辅助 */
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