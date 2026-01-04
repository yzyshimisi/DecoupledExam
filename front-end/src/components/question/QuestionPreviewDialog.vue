<template>
  <dialog id="questionPreviewDialog" class="modal">
    <div class="modal-box w-full max-w-4xl max-h-[90vh] overflow-y-auto">
      <div class="flex justify-between items-center mb-4">
        <h3 class="font-bold text-lg">
          题目预览
        </h3>
        <div class="flex gap-2">
          <button class="btn btn-sm btn-ghost" @click="close">关闭</button>
        </div>
      </div>

      <!-- 题型专属内容 -->
      <div class="border-t pt-4">
        <!-- 动态组件：根据 typeId 加载对应视图 -->
        <component
            :is="getQuestionComponent(modelValue.typeId)"
            :question="modelValue"
            :showSolution="true"
            :readonly="false"
        />
      </div>
    </div>
    <form method="dialog" class="modal-backdrop">
      <button @click="close">close</button>
    </form>
  </dialog>
</template>

<script setup lang="ts">
import {defineAsyncComponent, onMounted, ref, watch} from 'vue'
import { cloneDeep } from 'lodash-es'

interface Question {
  id: number
  title: string
  typeId: number
  difficulty: number
  subjectId: number
  reviewStatus: number
  createdAt: string
  updatedAt: string
  [key: string]: any
}

const props = defineProps<{
  modelValue: Question | null
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: Question): void
  (e: 'save', value: Question): void
  (e: 'close'): void
}>()

const localQuestion = ref<Question>({} as Question)

// 监听外部传入的题目变化
watch(
    () => props.modelValue,
    (newVal) => {
      if (newVal) {
        localQuestion.value = cloneDeep(newVal)
      }
    },
    { immediate: true }
)

const close = () => {
  emit('close')
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
</script>