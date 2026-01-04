<template>
  <div class="journal-entry-question">
    <!-- 题目 -->
    <h3 class="question-title">{{ question.title }}</h3>

    <!-- 答题区域（非只读时） -->
    <div v-if="!readonly" class="answer-input-area">
      <div
          v-for="(entry, index) in localAnswer.entries"
          :key="index"
          class="entry-input-group mb-4 p-3 border rounded relative"
      >
        <!-- 删除按钮（右上角） -->
        <button
            v-if="localAnswer.entries.length > 1"
            type="button"
            @click="removeEntry(index)"
            class="absolute top-1 right-1 text-xs text-red-500 hover:text-red-700"
            title="删除此行分录"
        >
          ✕
        </button>

        <div class="grid grid-cols-2 gap-2">
          <div>
            <label class="block text-sm font-medium text-gray-700">借方科目</label>
            <input
                v-model="entry.debitAccount"
                type="text"
                @input="onInput"
                class="mt-1 block w-full rounded-md border border-gray-300 px-2 py-1 text-sm"
                placeholder="如：银行存款"
            />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700">借方金额</label>
            <input
                v-model="entry.debitAmount"
                type="text"
                inputmode="decimal"
                @input="onInput"
                class="mt-1 block w-full rounded-md border border-gray-300 px-2 py-1 text-sm"
                placeholder="如：200000"
            />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700">贷方科目</label>
            <input
                v-model="entry.creditAccount"
                type="text"
                @input="onInput"
                class="mt-1 block w-full rounded-md border border-gray-300 px-2 py-1 text-sm"
                placeholder="如：短期借款"
            />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700">贷方金额</label>
            <input
                v-model="entry.creditAmount"
                type="text"
                inputmode="decimal"
                @input="onInput"
                class="mt-1 block w-full rounded-md border border-gray-300 px-2 py-1 text-sm"
                placeholder="如：200000"
            />
          </div>
        </div>
      </div>

      <button
          type="button"
          @click="addEntry"
          class="text-sm text-blue-600 hover:text-blue-800"
      >
        + 添加一行分录
      </button>
    </div>

    <!-- 用户已答内容（只读时显示） -->
    <div v-else-if="localAnswer.entries && localAnswer.entries.length" class="user-answer mt-2">
      <strong>您的答案：</strong>
      <div class="entries mt-2">
        <div v-for="(entry, index) in localAnswer.entries" :key="index" class="entry-row">
          <div class="entry-line">
            <span class="debit">借：{{ entry.debitAccount || '——' }}</span>
            <span class="amount">{{ formatAmount(entry.debitAmount) }}</span>
          </div>
          <div class="entry-line">
            <span class="credit">贷：{{ entry.creditAccount || '——' }}</span>
            <span class="amount">{{ formatAmount(entry.creditAmount) }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 标准答案（当 showSolution 为 true 时） -->
    <div v-if="showSolution && answerData" class="answer-desc mt-4">
      <strong>标准分录：</strong>
      <div class="entries mt-2">
        <div v-for="(entry, index) in answerData.entryList" :key="index" class="entry-row">
          <div class="entry-line">
            <span class="debit">借：{{ entry.debitAccount }}</span>
            <span class="amount">{{ formatAmount(entry.debitAmount) }}</span>
          </div>
          <div class="entry-line">
            <span class="credit">贷：{{ entry.creditAccount }}</span>
            <span class="amount">{{ formatAmount(entry.creditAmount) }}</span>
          </div>
        </div>
      </div>
      <div v-if="answerData.entryDesc" class="entry-desc mt-3">
        {{ answerData.entryDesc }}
      </div>
    </div>

    <!-- 解析 -->
    <div v-if="showSolution && analysisData" class="analysis mt-4">
      <strong>解析：</strong>{{ analysisData.analysisDesc }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'

interface JournalEntry {
  debitAccount?: string;
  debitAmount?: string | number;
  creditAccount?: string;
  creditAmount?: string | number;
}

const props = defineProps<{
  question: any;
  score?: number;
  readonly?: boolean;           // 是否只读（预览/批改）
  showSolution?: boolean;       // 是否显示标准答案和解析
  modelValue?: {
    entries?: JournalEntry[];
  };
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: { entries: JournalEntry[] }): void;
}>()

// 解析标准答案 & 解析
const answerData = ref<any>(null)
const analysisData = ref<any>(null)

// 本地答案（响应式）
const localAnswer = ref<{ entries: JournalEntry[] }>({
  entries: props.modelValue?.entries?.map(e => ({ ...e })) || [{
    debitAccount: '', debitAmount: '',
    creditAccount: '', creditAmount: ''
  }]
})

// 同步外部 modelValue
watch(() => props.modelValue, (val) => {
  if (val?.entries) {
    localAnswer.value.entries = val.entries.map(e => ({ ...e }))
  } else {
    localAnswer.value.entries = [{
      debitAccount: '', debitAmount: '',
      creditAccount: '', creditAmount: ''
    }]
  }
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
    console.error('解析分录题失败', err)
  }
})

const onInput = () => {
  if (props.readonly) return
  // 清理空字符串转为 undefined（可选）
  const cleaned = localAnswer.value.entries.map(entry => ({
    debitAccount: entry.debitAccount || undefined,
    debitAmount: entry.debitAmount || undefined,
    creditAccount: entry.creditAccount || undefined,
    creditAmount: entry.creditAmount || undefined,
  }))
  emit('update:modelValue', { entries: cleaned })
}

const addEntry = () => {
  if (props.readonly) return
  localAnswer.value.entries.push({
    debitAccount: '',
    debitAmount: '',
    creditAccount: '',
    creditAmount: ''
  })
  onInput()
}

const formatAmount = (num: string | number | undefined): string => {
  if (num == null || num === '') return '——'
  const n = typeof num === 'string' ? parseFloat(num) : num
  return isNaN(n) ? String(num) : n.toFixed(2)
}

const removeEntry = (index: number) => {
  if (props.readonly) return

  if (localAnswer.value.entries.length === 1) {
    // 清空当前行
    const entry = localAnswer.value.entries[0]
    entry.debitAccount = ''
    entry.debitAmount = ''
    entry.creditAccount = ''
    entry.creditAmount = ''
  } else {
    // 删除该行
    localAnswer.value.entries.splice(index, 1)
  }
  onInput()
}
</script>

<style scoped>
.journal-entry-question {
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

.entry-input-group {
  background-color: #fafafa;
}

/* 分录显示样式 */
.entries {
  padding-left: 16px;
  border-left: 2px solid #cbd5e1;
}

.entry-row {
  margin-bottom: 12px;
}

.entry-line {
  display: flex;
  justify-content: space-between;
  margin: 4px 0;
  font-family: 'Courier New', monospace;
  font-size: 1rem;
}

.debit {
  color: #10b981;
}

.credit {
  color: #ef4444;
}

.amount {
  font-weight: bold;
  min-width: 80px;
  text-align: right;
}

.entry-desc {
  font-style: italic;
  color: #64748b;
  font-size: 0.95rem;
}

/* 答案卡片 */
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

.mb-4 { margin-bottom: 1rem; }
.mt-4 { margin-top: 1rem; }
.mt-3 { margin-top: 0.75rem; }
.mt-2 { margin-top: 0.5rem; }
.p-3 { padding: 0.75rem; }
.rounded { border-radius: 0.375rem; }
.border { border-width: 1px; }
.text-sm { font-size: 0.875rem; }
.font-medium { font-weight: 500; }
.text-gray-700 { color: #374151; }
.block { display: block; }
.w-full { width: 100%; }
.grid { display: grid; }
.gap-2 { gap: 0.5rem; }
.grid-cols-2 { grid-template-columns: repeat(2, minmax(0, 1fr)); }

@media (max-width: 768px) {
  .grid-cols-2 { grid-template-columns: 1fr; }
  .entry-line {
    flex-direction: column;
    gap: 2px;
  }
  .amount {
    text-align: left;
    min-width: auto;
  }
}
</style>