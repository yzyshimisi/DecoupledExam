<template>
<div class="exam-paper-container">
  <!-- 左侧主体内容 -->
  <main class="exam-main">
    <div class="paper-preview max-w-4xl mx-auto p-6 bg-white shadow rounded-lg">
      <div class="text-center mb-8">
        <h1 class="text-2xl font-bold">{{ paper.paperName }}</h1>
        <p class="text-gray-500 mt-2">总分：{{ paper.totalScore }} 分</p>
      </div>

      <!-- 按题型分组渲染 -->
      <div v-for="group in groupedQuestions" :key="group.typeId" class="mb-10">
        <div class="flex items-center mb-4 border-b pb-2">
          <h2 class="text-xl font-semibold text-primary">
            {{ getQuestionTypeName(group.typeId) }}
          </h2>
          <span class="ml-2 text-sm text-gray-500">（共 {{ group.items.length }} 题）</span>
        </div>

        <!-- 渲染该题型下的所有题目 -->
        <div v-for="(pq, index) in group.items" :key="pq.questionId" class="mb-6" :ref="el => setQuestionRef(pq.questionId, el)">
          <div class="flex">
            <span class="mr-2 font-mono text-gray-700">{{ index + 1 }}.</span>
            <div class="flex-1">
              <!-- 动态组件：根据 typeId 加载对应视图 -->
              <component
                  :is="getQuestionComponent(group.typeId)"
                  :question="pq.question"
                  :showSolution="true"
                  :score="pq.score"
                  :readonly="false"
              />
            </div>
          </div>
        </div>
      </div>

      <div v-if="!hasQuestions" class="text-center py-10 text-gray-500">
        试卷暂无题目
      </div>
    </div>
  </main>

  <!-- 右侧导航栏 -->
  <aside class="exam-nav">
    <h4 class="font-semibold mb-3">题目导航</h4>
    <ul class="space-y-2">
      <li
          v-for="(fq, idx) in flatQuestions"
          :key="fq.questionId"
          @click="jumpToQuestion(fq.questionId)"
          class="cursor-pointer hover:bg-blue-50 px-2 py-1 rounded text-sm flex items-center"
      >
        <span class="w-6 inline-block">{{ idx + 1 }}.</span>
        <span class="text-gray-600 ml-1">
        {{ getQuestionTypeName(fq.question.typeId) }}
      </span>
      </li>
    </ul>
  </aside>
</div>
</template>

<script setup lang="ts">
import { ref, computed, defineAsyncComponent, onMounted, ComponentPublicInstance } from 'vue'
import { useRequest } from 'vue-hooks-plus'
import { getQuestionsByIdAPI } from '../../apis'

const props = defineProps<{
  paper: {
    paperId: number
    paperName: string
    totalScore: number
    questions: Array<{
      questionId: number
      score: number
      sortOrder: number
    }>
  }
  questionTypes: Array<{
    typeId: number
    typeName: string
  }>
}>()

interface PaperQuestionItem {
  questionId: number
  score: number
  sortOrder: number
  question: any
}

const paperQuestions = ref<PaperQuestionItem[]>([])

// 初始化元数据
const initPaperQuestions = () => {
  paperQuestions.value = props.paper.questions.map(pq => ({
    questionId: pq.questionId,
    score: pq.score,
    sortOrder: pq.sortOrder,
    question: null
  }))
}

// 填充题目详情
const fillQuestionDetail = (questionId: number, detail: any) => {
  const item = paperQuestions.value.find(pq => pq.questionId === questionId)
  if (item) {
    item.question = detail
  }
}

const getQuestionById = (questionId: number) => {
  useRequest(() => getQuestionsByIdAPI({ questionId }), {
    onSuccess(res) {
      if (res['code'] === 200) {
        fillQuestionDetail(questionId, res['data'])
      }
    }
  })
}

onMounted(() => {
  initPaperQuestions()
  props.paper.questions.forEach(pq => {
    getQuestionById(pq.questionId)
  })

  // console.log(paperQuestions.value)
})

// 分组逻辑
const groupedQuestions = computed(() => {
  const loadedItems = paperQuestions.value
      .filter(item => item.question != null)
      .sort((a, b) => a.sortOrder - b.sortOrder)

  const groups: Record<number, { typeId: number; items: typeof loadedItems }> = {}
  for (const item of loadedItems) {
    const typeId = item.question.typeId
    if (!groups[typeId]) {
      groups[typeId] = { typeId, items: [] }
    }
    groups[typeId].items.push(item)
  }

  console.log(Object.values(groups))
  return Object.values(groups)
})

const hasQuestions = computed(() => {
  return paperQuestions.value.some(item => item.question != null)
})

const getQuestionTypeName = (typeId: number) => {
  const type = props.questionTypes.find(t => t.typeId === typeId)
  return type ? type.typeName : '未知题型'
}

const getQuestionComponent = (typeId: number) => {
  const componentMap: Record<number, string> = {
    1: 'SingleChoiceRender',
    2: 'MultipleChoiceRender',
    3: 'TrueFalseRender',
    4: 'FillBlankRender',
    5: 'NounAnalysisRender',
    6: 'EssayQuestionRender',
    7: 'CalculationQuestionRender',
    8: 'AccountingEntryRender',
    9: 'MatchingQuestionRender',
    10: 'SortingQuestionRender',
    11: 'ClozeQuestionRender',
    12: 'ReadingComprehensionRender',
    17: 'PollQuestionRender',
  }

  const componentName = componentMap[typeId] || 'DefaultQuestionView'

  return defineAsyncComponent(() =>
          import(`../question/render/${componentName}.vue`)
      //     .catch(() =>
      //   import('./question-types/DefaultQuestionView.vue')
      // )
  )
}

// 所有已加载题目的扁平列表（按 sortOrder 排序）
const flatQuestions = computed(() => {
  return paperQuestions.value
      .filter(item => item.question != null)
      .sort((a, b) => a.sortOrder - b.sortOrder)
})

// 存储每个题目的 DOM 引用，key 为 questionId
const questionElementMap = ref<Record<number, HTMLElement | null>>({})

// 设置 ref 的函数
const setQuestionRef = (questionId: number, el: Element | ComponentPublicInstance | null) => {
  if (el instanceof HTMLElement) {
    questionElementMap.value[questionId] = el
  } else {
    // 如果不是 HTMLElement（比如是组件），不保存
    questionElementMap.value[questionId] = null
  }
}

const jumpToQuestion = (questionId: number) => {
  const el = questionElementMap.value[questionId]
  if (el) {
    el.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}
</script>

<style scoped>
.exam-paper-container {
  display: flex;
}

.exam-main {
  width: calc(100% - 200px); /* 根据aside宽度调整 */
  padding-right: 20px; /* 给aside留出空间 */
}

.exam-nav {
  width: 200px;
  position: sticky;
  top: 20px; /* 根据需要调整 */
  height: fit-content;
  background-color: #f8fafc;
  padding: 1rem;
  border-left: 1px solid #e5e7eb;
}

.paper-preview {
  font-size: 16px;
  line-height: 1.6;
}
</style>