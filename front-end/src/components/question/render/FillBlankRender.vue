<template>
  <div class="blank-question">
    <!-- 题目标题 -->
    <h3 class="question-title">{{ question.title }}</h3>

    <!-- 填空输入区 -->
    <div class="blanks-container">
      <div v-for="blank in blanks" :key="blank.label" class="blank-item">
        <label :for="`blank-${blank.label}`" class="blank-label">
          {{ blank.position }}：
        </label>
        <input
            :id="`blank-${blank.label}`"
            v-model="localAnswer[blank.label]"
            :placeholder="blank.placeholder || '请输入'"
            :readonly="readonly"
            @input="onInput"
            class="blank-input"
            type="text"
        />
      </div>
    </div>

    <!-- 正确答案 -->
    <div v-if="showSolution && answerData" class="answer-desc">
      <strong>正确答案：</strong>
      <span v-for="(item, idx) in answerData.answers" :key="item.blankLabel" class="answer-item">
        {{ item.blankLabel }}: {{ item.correctAnswer }}
        <span v-if="idx < answerData.answers.length - 1">；</span>
      </span>
      <div v-if="answerData.tip" class="answer-tip mt-1 text-sm text-gray-600">
        {{ answerData.tip }}
      </div>
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
  showSolution?: boolean; // 👈 由父组件控制是否显示答案/解析
  modelValue?: Record<string, string>; // 如 { '1': 'abstract', '2': 'interface' }
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: Record<string, string>): void;
}>()

// 解析后的数据
const blanks = ref<any[]>([])
const answerData = ref<any>(null)
const analysisData = ref<any>(null)

// 本地答案（响应式对象）
const localAnswer = ref<Record<string, string>>({})

// 同步外部 modelValue
watch(() => props.modelValue, (val) => {
  localAnswer.value = val ? { ...val } : {}
}, { immediate: true })

onMounted(() => {
  try {
    const getComponentContent = (type: string) => {
      const comp = props.question.questionComponents.find((c: any) => c.componentType === type)
      return comp ? JSON.parse(comp.content) : null
    }

    const blankComp = getComponentContent('blank')
    if (blankComp?.blanks) {
      blanks.value = blankComp.blanks.sort((a: any, b: any) => a.sort - b.sort)
      // 初始化 localAnswer（如果 modelValue 为空）
      const init: Record<string, string> = {}
      blankComp.blanks.forEach((b: any) => {
        init[b.label] = ''
      })
      if (!props.modelValue) {
        localAnswer.value = init
      }
    }

    answerData.value = getComponentContent('answer')
    analysisData.value = getComponentContent('analysis')
  } catch (err) {
    console.error('解析填空题失败', err)
  }
})

const onInput = () => {
  if (props.readonly) return
  // 触发更新（确保是新对象引用）
  emit('update:modelValue', { ...localAnswer.value })
}
</script>

<style scoped>
.blank-question {
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

.blanks-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 16px;
}

.blank-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.blank-label {
  font-weight: 500;
  color: #4b5563;
  min-width: 60px;
}

.blank-input {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 1rem;
  background-color: white;
  transition: border-color 0.2s;
}

.blank-input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.2);
}

.blank-input:read-only {
  background-color: #f9fafb;
  cursor: not-allowed;
}

/* 答案与解析样式（复用） */
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

.answer-item {
  margin-right: 8px;
}
</style>