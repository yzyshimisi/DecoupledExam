<template>
  <dialog ref="dialogRef" class="modal">
    <div class="modal-box w-full max-w-4xl max-h-[90vh] overflow-y-auto">
      <div class="flex justify-between items-center mb-4">
        <h3 class="font-bold text-lg">
          {{ isEditing ? '编辑题目' : '题目详情' }}
        </h3>
        <div class="flex gap-2">
          <button
              v-if="!isEditing"
              class="btn btn-sm btn-outline"
              @click="toggleEdit"
          >
            编辑
          </button>
          <button class="btn btn-sm btn-ghost" @click="close">关闭</button>
        </div>
      </div>

      <!-- 题目基本信息 -->
      <div class="space-y-4 mb-6">
        <div>
          <label class="label"><span class="label-text">题干</span></label>
          <textarea
              v-if="isEditing"
              v-model="localQuestion.title"
              class="textarea textarea-bordered w-full"
              rows="3"
          ></textarea>
          <div v-else class="p-3 bg-base-100 rounded">{{ localQuestion.title }}</div>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div>
            <label class="label"><span class="label-text">题型</span></label>
            <select
                v-if="isEditing"
                v-model.number="localQuestion.typeId"
                class="select select-bordered w-full"
            >
              <option v-for="type in questionTypes" :value="type.typeId">
                {{ type.typeName }}
              </option>
            </select>
            <div v-else>{{ getQuestionTypeName(localQuestion.typeId) }}</div>
          </div>

          <div>
            <label class="label"><span class="label-text">难度 (1-5)</span></label>
            <input
                v-if="isEditing"
                v-model.number="localQuestion.difficulty"
                type="number"
                min="1"
                max="5"
                class="input input-bordered w-full"
            />
            <div v-else>{{ localQuestion.difficulty }}</div>
          </div>

          <div>
            <label class="label"><span class="label-text">所属学科</span></label>
            <select
                v-if="isEditing"
                v-model.number="localQuestion.subjectId"
                class="select select-bordered w-full"
            >
              <option v-for="sub in subjectsList" :value="sub.subjectId">
                {{ sub.subjectName }}
              </option>
            </select>
            <div v-else>{{ getSubjectName(localQuestion.subjectId) }}</div>
          </div>
        </div>
      </div>

      <!-- 题型专属内容 -->
      <div class="border-t pt-4">
        <h4 class="font-semibold mb-3">题目内容</h4>
        <component
            :is="getContentComponent(localQuestion.typeId)"
            :question="localQuestion"
            :isEditing="isEditing"
            @update="handleContentUpdate"
        />
      </div>

      <!-- 底部操作 -->
      <div v-if="isEditing" class="modal-action mt-6">
        <button class="btn" @click="cancelEdit">取消</button>
        <button class="btn btn-primary" @click="save">保存</button>
      </div>
    </div>
    <form method="dialog" class="modal-backdrop">
      <button @click="close">close</button>
    </form>
  </dialog>
</template>

<script setup lang="ts">
import {onMounted, ref, watch} from 'vue'
import { cloneDeep } from 'lodash-es'

// 子组件：不同题型的内容展示/编辑
// import SingleChoiceContent from './render/SingleChoiceContent.vue'
// import MultipleChoiceContent from './question-types/MultipleChoiceContent.vue'
// import BlankContent from './question-types/BlankContent.vue'
// import JudgmentContent from './question-types/JudgmentContent.vue'
// import TermExplanationContent from './question-types/TermExplanationContent.vue'
// import EssayContent from './question-types/EssayContent.vue'
// import CalculationContent from './question-types/CalculationContent.vue'
// import AccountingEntryContent from './question-types/AccountingEntryContent.vue'
// import MatchingContent from './question-types/MatchingContent.vue'
// import SortingContent from './question-types/SortingContent.vue'
// import ClozeContent from './question-types/ClozeContent.vue'
// import ReadingComprehensionContent from './question-types/ReadingComprehensionContent.vue'
// import ListeningContent from './question-types/ListeningContent.vue'
// import CodingContent from './question-types/CodingContent.vue'
// import SpeakingContent from './question-types/SpeakingContent.vue'
// import VotingContent from './question-types/VotingContent.vue'

// 映射题型 ID 到组件
const componentMap: Record<number, any> = {
  // 1: SingleChoiceContent,
  // 2: MultipleChoiceContent,
  // 4: BlankContent,
  // 3: JudgmentContent,
  // 5: TermExplanationContent,
  // 6: EssayContent,
  // 7: CalculationContent,
  // 8: AccountingEntryContent,
  // 9: MatchingContent,
  // 10: SortingContent,
  // 11: ClozeContent,
  // 12: ReadingComprehensionContent,
  // 13: ListeningContent,
  // 14: CodingContent,
  // 15: SpeakingContent,
  // 17: VotingContent,
}

onMounted(()=>{
  // console.log(props.questionTypes)
  // console.log(props.modelValue)
})

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
  questionTypes: Array<{ typeId: number; typeName: string }>
  subjectsList: Array<{ subjectId: number; subjectName: string }>
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: Question): void
  (e: 'save', value: Question): void
  (e: 'close'): void
}>()

const dialogRef = ref<HTMLDialogElement | null>(null)
const isEditing = ref(false)
const localQuestion = ref<Question>({} as Question)

const getContentComponent = (typeId: number) => {
  return componentMap[typeId] || 'div'
}

const getQuestionTypeName = (typeId: number) => {
  return props.questionTypes.find(t => t.typeId === typeId)?.typeName || '未知题型'
}

const getSubjectName = (subjectId: number) => {
  return props.subjectsList.find(s => s.subjectId === subjectId)?.subjectName || '未知学科'
}

// 监听外部传入的题目变化
watch(
    () => props.modelValue,
    (newVal) => {
      if (newVal) {
        localQuestion.value = cloneDeep(newVal)
        console.log(localQuestion.value)
        isEditing.value = false
        dialogRef.value?.showModal()
      } else {
        dialogRef.value?.close()
      }
    },
    { immediate: true }
)

const toggleEdit = () => {
  isEditing.value = true
  localQuestion.value = cloneDeep(props.modelValue!)
}

const cancelEdit = () => {
  isEditing.value = false
  localQuestion.value = cloneDeep(props.modelValue!)
}

const handleContentUpdate = (updates: Partial<Question>) => {
  Object.assign(localQuestion.value, updates)
}

const save = () => {
  emit('save', localQuestion.value)
  close()
}

const close = () => {
  emit('close')
}
</script>