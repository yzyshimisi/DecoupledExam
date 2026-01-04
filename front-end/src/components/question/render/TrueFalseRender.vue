<template>
  <div class="true-false-question">
    <!-- 题目 -->
    <h3 class="question-title">{{ question.title }}</h3>

    <!-- 判断选项：正确 / 错误 -->
    <div class="tf-options">
      <button
          type="button"
          @click="!readonly && selectAnswer(true)"
          :class="{ selected: localAnswer === true }"
          class="tf-btn tf-btn-true"
          :disabled="readonly"
      >
        正确
      </button>
      <button
          type="button"
          @click="!readonly && selectAnswer(false)"
          :class="{ selected: localAnswer === false }"
          class="tf-btn tf-btn-false"
          :disabled="readonly"
      >
        错误
      </button>
    </div>

    <!-- 正确答案 -->
    <div v-if="showSolution && answerData" class="answer-desc">
      <strong>正确答案：</strong>
      {{ answerData.correctResult ? '正确' : '错误' }} — {{ answerData.answerDesc }}
    </div>

    <!-- 解析 -->
    <div v-if="showSolution && analysisData" class="analysis">
      <strong>解析：</strong>{{ analysisData.analysisDesc }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'

const props = defineProps<{
  question: any;
  score?: number;
  readonly?: boolean;
  showSolution?: boolean; // 由父组件控制
  modelValue?: boolean | null; // true=正确, false=错误, null=未选
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean | null): void;
}>()

// 解析后的数据
const answerData = ref<any>(null)
const analysisData = ref<any>(null)

// 本地响应式答案
const localAnswer = ref<boolean | null>(props.modelValue ?? null)

// 同步外部 modelValue
watch(() => props.modelValue, (val) => {
  localAnswer.value = val ?? null
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
    console.error('解析判断题失败', err)
  }
})

const selectAnswer = (value: boolean) => {
  if (props.readonly) return

  localAnswer.value = value
  emit('update:modelValue', value)
}
</script>

<style scoped>
.true-false-question {
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

.tf-options {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.tf-btn {
  flex: 1;
  padding: 10px 16px;
  border: 2px solid #d1d5db;
  border-radius: 8px;
  background-color: white;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.tf-btn:hover:not(:disabled):not(.selected) {
  background-color: #f9fafb;
  border-color: #9ca3af;
}

.tf-btn.selected {
  font-weight: 600;
}

.tf-btn-true.selected {
  border-color: #10b981;
  background-color: #ecfdf5;
  color: #065f46;
}

.tf-btn-false.selected {
  border-color: #ef4444;
  background-color: #fef2f2;
  color: #b91c1c;
}

.tf-btn:disabled {
  cursor: not-allowed;
  opacity: 0.7;
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
</style>