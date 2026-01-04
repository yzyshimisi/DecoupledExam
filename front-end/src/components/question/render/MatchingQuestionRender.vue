<template>
  <div class="matching-question">
    <!-- 题目 -->
    <h3 class="question-title">{{ question.title }}</h3>

    <!-- 答题区域（非只读） -->
    <div v-if="!readonly" class="matching-input-area mt-4">
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <!-- 左侧：待匹配项 -->
        <div>
          <h4 class="text-sm font-medium text-gray-700 mb-2">左侧项</h4>
          <div v-for="left in matchData.leftList" :key="left.id" class="mb-3">
            <label class="block text-sm">{{ left.content }}</label>
            <select
                v-model="localMatches[left.id]"
                @change="onInput"
                class="mt-1 block w-full rounded-md border border-gray-300 px-2 py-1 text-sm"
            >
              <option value="" disabled>—— 请选择 ——</option>
              <option
                  v-for="right in matchData.rightList"
                  :key="right.id"
                  :value="right.id"
                  :disabled="isRightOptionUsed(right.id) && localMatches[left.id] !== right.id"
              >
                {{ right.content }}
              </option>
            </select>
          </div>
        </div>

        <!-- 右侧：候选答案（只读展示） -->
        <div>
          <h4 class="text-sm font-medium text-gray-700 mb-2">右侧项</h4>
          <ul class="space-y-2">
            <li
                v-for="right in matchData.rightList"
                :key="right.id"
                class="px-3 py-1.5 bg-gray-50 rounded border text-sm"
            >
              {{ right.content }}
            </li>
          </ul>
        </div>
      </div>
    </div>

    <!-- 用户已答（只读模式） -->
    <div v-else-if="Object.keys(localMatches).length > 0" class="user-answer mt-4">
      <strong>您的匹配：</strong>
      <div class="mt-2 space-y-2">
        <div v-for="left in matchData.leftList" :key="left.id" class="flex items-center gap-2 text-sm">
          <span>{{ left.content }} → </span>
          <span class="font-medium">
            {{
              getRightContentById(localMatches[left.id]) || '未匹配'
            }}
          </span>
        </div>
      </div>
    </div>

    <!-- 标准答案（当 showSolution 为 true） -->
    <div v-if="showSolution && answerData" class="answer-desc mt-4">
      <strong>正确匹配：</strong>
      <div class="mt-2 space-y-2">
        <div v-for="pair in answerData.matchPairs" :key="pair.leftId" class="flex items-center gap-2 text-sm">
          <span>{{ getLeftContentById(pair.leftId) }} → </span>
          <span class="font-medium text-green-600">{{ getRightContentById(pair.rightId) }}</span>
        </div>
      </div>
      <div v-if="answerData.ruleDesc" class="mt-3 text-sm italic text-gray-600">
        {{ answerData.ruleDesc }}
      </div>
    </div>

    <!-- 解析 -->
    <div v-if="showSolution && analysisData" class="analysis mt-4">
      <strong>解析：</strong>{{ analysisData.analysisDesc }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue'

interface MatchItem {
  id: string;
  content: string;
}

const props = defineProps<{
  question: any;
  score?: number;
  readonly?: boolean;
  showSolution?: boolean;
  modelValue?: Record<string, string>; // e.g. { L1: 'R2', L2: 'R3' }
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: Record<string, string>): void;
}>()

// 解析数据
const matchData = ref<{ leftList: MatchItem[]; rightList: MatchItem[] }>({
  leftList: [],
  rightList: []
})
const answerData = ref<any>(null)
const analysisData = ref<any>(null)

// 本地匹配状态：{ L1: 'R2', L2: 'R3', ... }
const localMatches = ref<Record<string, string>>({})

// 同步外部 modelValue
watch(() => props.modelValue, (val) => {
  localMatches.value = val ? { ...val } : {}
})

onMounted(() => {
  try {
    const getComponentContent = (type: string) => {
      const comp = props.question.questionComponents.find((c: any) => c.componentType === type)
      return comp ? JSON.parse(comp.content) : null
    }

    const matchComp = props.question.questionComponents.find((c: any) => c.componentType === 'match')
    if (matchComp) {
      const data = JSON.parse(matchComp.content)
      matchData.value = {
        leftList: data.leftList || [],
        rightList: data.rightList || []
      }
    }

    answerData.value = getComponentContent('answer')
    analysisData.value = getComponentContent('analysis')
  } catch (err) {
    console.error('解析连线题失败', err)
  }
})

const onInput = () => {
  if (props.readonly) return
  emit('update:modelValue', { ...localMatches.value })
}

// 判断某个右侧选项是否已被其他左侧项占用（用于禁用）
const isRightOptionUsed = (rightId: string): boolean => {
  return Object.values(localMatches.value).some(id => id === rightId)
}

// 辅助函数：通过 ID 获取内容
const getLeftContentById = (id: string): string => {
  const item = matchData.value.leftList.find(i => i.id === id)
  return item ? item.content : id
}

const getRightContentById = (id: string): string => {
  const item = matchData.value.rightList.find(i => i.id === id)
  return item ? item.content : id
}
</script>

<style scoped>
.matching-question {
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

/* 卡片样式 */
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

/* 响应式网格 */
.grid {
  display: grid;
}
.gap-6 { gap: 1.5rem; }
.grid-cols-1 { grid-template-columns: 1fr; }
@media (min-width: 768px) {
  .md\:grid-cols-2 { grid-template-columns: 1fr 1fr; }
}

.mb-2 { margin-bottom: 0.5rem; }
.mb-3 { margin-bottom: 0.75rem; }
.mt-4 { margin-top: 1rem; }
.mt-2 { margin-top: 0.5rem; }
.mt-3 { margin-top: 0.75rem; }

.text-sm { font-size: 0.875rem; }
.font-medium { font-weight: 500; }
.text-gray-700 { color: #374151; }
.block { display: block; }
.w-full { width: 100%; }
.rounded-md { border-radius: 0.375rem; }
.px-2 { padding-left: 0.5rem; padding-right: 0.5rem; }
.py-1 { padding-top: 0.25rem; padding-bottom: 0.25rem; }
.border { border-width: 1px; }
.bg-gray-50 { background-color: #f9fafb; }
.space-y-2 > * + * { margin-top: 0.5rem; }
</style>