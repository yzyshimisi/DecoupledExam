<template>
  <div class="multiple-choice-question">
    <!-- 题目 -->
    <h3 class="question-title">{{ question.title }}</h3>

    <!-- 选项 -->
    <ul class="options-list">
      <li
          v-for="option in options"
          :key="option.label"
          @click="!readonly && toggleOption(option.label)"
          :class="{
          selected: localAnswer.includes(option.label),
          disabled: readonly
        }"
          class="option-item"
      >
        <span class="option-label">{{ option.label }}.</span>
        <span class="option-content">{{ option.value }}</span>
      </li>
    </ul>

    <!-- 正确答案 -->
    <div v-if="showSolution && answer" class="answer-desc">
      <strong>正确答案：</strong>{{ answer.correctOption }} — {{ answer.answerDesc }}
    </div>

    <!-- 解析 -->
    <div v-if="showSolution && analysis" class="analysis">
      <strong>解析：</strong>{{ analysis.analysisDesc }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'

const props = defineProps<{
  question: any;
  showSolution: boolean
  score?: number;
  readonly?: boolean;
  modelValue?: string[]; // 多选用数组：['A', 'C']
}>()

const emit = defineEmits<{
  (e: 'update:modelVale', value: string[]): void;
  (e: 'update:modelValue', value: string[]): void; // 注意拼写！
}>()

// 解析数据
const options = ref<any[]>([])
const answer = ref<any>(null)
const analysis = ref<any>(null)

// 本地答案（响应式）
const localAnswer = ref<string[]>(props.modelValue || [])

// 同步外部 modelValue
watch(() => props.modelValue, (val) => {
  localAnswer.value = val || []
})

onMounted(() => {
  try {
    const getComponentContent = (type: string) => {
      const comp = props.question.questionComponents.find((c: any) => c.componentType === type)
      return comp ? JSON.parse(comp.content) : null
    }

    const optionData = getComponentContent('option')
    if (optionData) {
      options.value = optionData.options
    }

    answer.value = getComponentContent('answer')
    analysis.value = getComponentContent('analysis')
  } catch (err) {
    console.error('解析多选题失败', err)
  }
})

const toggleOption = (label: string) => {
  if (props.readonly) return

  const index = localAnswer.value.indexOf(label)
  if (index > -1) {
    // 已选，取消
    localAnswer.value.splice(index, 1)
  } else {
    // 未选，加入
    localAnswer.value.push(label)
  }

  // 触发更新（注意：直接修改数组不会触发响应式，需替换引用）
  localAnswer.value = [...localAnswer.value]
  emit('update:modelValue', localAnswer.value)
}
</script>

<style scoped>
/* 复用 ChoiceQuestionView 的样式，保持统一 */
.multiple-choice-question {
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

.options-list {
  margin: 0;
  padding: 0;
  list-style: none;
}

.option-item {
  padding: 12px 16px;
  margin: 8px 0;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background-color: white;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 👇 关键：只有未选中的才响应 hover */
.option-item:not(.selected):hover {
  background-color: #f9fafb;
  border-color: #d1d5db;
  transform: scale(1.01);
}

.option-item.selected {
  border-color: #3b82f6;
  background-color: #dbeafe;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.2);
  font-weight: 500;
}

.option-item.disabled {
  cursor: not-allowed;
  opacity: 0.7;
  background-color: #f3f4f6;
}

.option-label {
  font-weight: 600;
  color: #1f2937;
}

.option-content {
  flex: 1;
  color: #4b5563;
}

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