<template>
  <dialog :open="open" class="modal modal-bottom sm:modal-middle">
    <div class="modal-box w-[60vw] !max-w-[60vw]">
      <h3 class="font-bold text-lg mb-4">编辑试卷</h3>

      <!-- 试卷基本信息 -->
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6 p-4 bg-base-100 rounded-lg">
        <div>
          <label class="label">
            <span class="label-text">试卷名称</span>
          </label>
          <input
              v-model="form.paperName"
              type="text"
              class="input input-bordered w-full"
              :disabled="isSealed"
          />
        </div>

        <div>
          <label class="label">
            <span class="label-text">总分</span>
          </label>
          <input
              v-model="form.totalScore"
              type="number"
              class="input input-bordered w-full"
              readonly
          />
        </div>

        <div>
          <label class="label">
            <span class="label-text">是否封存</span>
          </label>
          <div class="flex items-center">
            <span :class="form.isSealed === '1' ? 'text-success' : 'text-error'">
              {{ form.isSealed === '1' ? '是' : '否' }}
            </span>
            <span class="ml-2 text-sm text-gray-500">(封存后不可编辑)</span>
          </div>
        </div>
      </div>

      <!-- 题目列表 -->
      <div class="mb-4">
        <div class="flex justify-between items-center mb-2">
          <h4 class="font-semibold">题目列表（共 {{ questions.length }} 题）</h4>
          <button
              v-if="!isSealed"
              class="btn btn-sm btn-primary"
              @click="isOpenAddQuestion=true"
          >
            添加题目
          </button>
        </div>

        <div v-if="questions.length === 0" class="text-gray-500 py-4 text-center">
          暂无题目
        </div>

        <div v-else class="space-y-2">
          <div
              v-for="(item, index) in questions"
              :key="item.id"
              class="flex items-center gap-3 p-3 border rounded-lg bg-white"
          >
            <!-- 顺序号 -->
            <div class="w-8 text-center font-mono">{{ index + 1 }}</div>

            <!-- 题目信息 -->
            <div class="flex-1 min-w-0">
              <div class="font-medium truncate">{{ item['title'] }}</div>
              <div class="text-sm text-gray-500" v-if="paper">
                {{ questionTypes.find(q => q['typeId'] === item['typeId'])['typeName'] }} · {{ paper['questions'][index]['score'] }} 分
              </div>
            </div>

            <!-- 分值输入 -->
            <input
                v-if="!isSealed"
                v-model.number="paper['questions'][index]['score']"
                type="number"
                min="0"
                step="1"
                class="input input-xs input-bordered w-20 text-center"
                @blur="recalculateTotal"
            />

            <!-- 操作 -->
            <div v-if="!isSealed" class="flex gap-1">
              <!-- 上移 -->
              <button
                  class="btn btn-xs btn-ghost"
                  :disabled="index === 0"
                  @click="moveUp(index)"
              >
                ↑
              </button>
              <!-- 下移 -->
              <button
                  class="btn btn-xs btn-ghost"
                  :disabled="index === questions.length - 1"
                  @click="moveDown(index)"
              >
                ↓
              </button>
              <!-- 删除 -->
              <button
                  class="btn btn-xs btn-error"
                  @click="removeQuestion(index)"
              >
                删除
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 底部按钮 -->
      <div class="modal-action">
        <button class="btn" @click="emit('update:open', false)">取消</button>
        <button
            v-if="!isSealed"
            class="btn btn-primary"
            :loading="saving"
            @click="savePaper"
        >
          保存
        </button>
      </div>
    </div>

    <!-- 背景遮罩点击关闭 -->
    <form method="dialog" class="modal-backdrop fixed inset-0 bg-black/50">
      <button @click="emit('update:open', false)">close</button>
    </form>
  </dialog>

   添加题目对话框（占位，实际可替换为你的 QuestionSelector 组件）
  <AddQuestionToPaperDialog
      v-if="isOpenAddQuestion"
      :open="isOpenAddQuestion"
      :questionTypes="questionTypes"
      @update:open = "(val) => { isOpenAddQuestion = val }"
      @select="selectQuestions"
  />
</template>

<script setup lang="ts">
import { ref, watch, computed, onMounted } from 'vue'
import { useRequest } from 'vue-hooks-plus'
import {
  addExamPaperQuestionsAPI,
  deleteExamPaperQuestionsAPI,
  getQuestionsByIdAPI,
  modifyExamPaperAPI,
  modifyExamPaperQuestionAPI
} from '../../apis';
import { AddQuestionToPaperDialog } from "../../components"

// Props & Emits
const props = defineProps<{
  open: boolean
  paper: object
  questionTypes: any[]
}>()
const emit = defineEmits<{
  (e: 'update:open', val: boolean): void
  (e: 'success'): void
}>()

// --- 状态 ---
const saving = ref(false)
const isOpenAddQuestion = ref(false)

// 表单数据
const form = ref({
  paperId: 0,
  paperName: '',
  totalScore: 0,
  isSealed: '0',
})

const getQuestionById = (questionId: any) => {
  useRequest(()=>getQuestionsByIdAPI({questionId: questionId}),{
    onSuccess(res) {
      if (res['code'] === 200) {
        questions.value.push(res['data'])
      }
    }
  })
}

// 题目列表：{ id: 关联ID, question: 题目详情, score: 分值 }
interface PaperQuestionItem {
  id: number // paper_question.id
  question: any
  score: number
}

const questions = ref<PaperQuestionItem[]>([])

// 是否封存（影响编辑权限）
const isSealed = computed(() => form.value.isSealed === '1')

// --- 监听打开状态 ---
watch(
    () => props.open,
    (isOpen) => {
      if (isOpen && props.paper) {
        Object.assign(form.value, props.paper)
        let n = props.paper['questions'].length
        for (let i = 0; i < n - 1; i++) {
          for (let j = 0; j < n - i - 1; j++) {
            if (props.paper['questions'][j].sortOrder > props.paper['questions'][j + 1].sortOrder) {
              // 交换元素
              ;[props.paper['questions'][j], props.paper['questions'][j + 1]] = [props.paper['questions'][j + 1], props.paper['questions'][j]]
            }
          }
        }
        for(let i = 0; i < props.paper['questions'].length; i++) {
          getQuestionById(props.paper['questions'][i].questionId)
        }
      }
    },
    { immediate: true }
)

// --- 计算总分 ---
const recalculateTotal = () => {
  const total = props.paper['questions'].reduce((sum, item) => sum + (item.score || 0), 0)
  form.value.totalScore = total
}

// --- 题目操作 ---
const moveUp = (index: number) => {
  if (index <= 0) return
  const temp = questions.value[index]
  questions.value[index] = questions.value[index - 1]
  questions.value[index - 1] = temp
  recalculateTotal()
}

const moveDown = (index: number) => {
  if (index >= questions.value.length - 1) return
  const temp = questions.value[index]
  questions.value[index] = questions.value[index + 1]
  questions.value[index + 1] = temp
  recalculateTotal()
}

const deletedQuestionsId = ref([])

const removeQuestion = (index: number) => {
  if (confirm('确定删除该题目？')) {
    deletedQuestionsId.value.push({
      questionId: questions.value[index].id,
      paperId: form.value.paperId
    })
    questions.value.splice(index, 1)
    props.paper['questions'].splice(index, 1)
    recalculateTotal()
  }
}

const newQuestion = ref([])

const selectQuestions = (selected) => {
  for(let i = 0; i < selected.length; i++){
    newQuestion.value.push({
      id:{
        paperId: props.paper['paperId'],
        questionId: selected[i].id
      },
      score: 5,
      sortOrder: props.paper['questions'].length + 1
    })
  }

  useRequest(()=>addExamPaperQuestionsAPI(newQuestion.value),{
    onSuccess(res) {
      if (res['code'] === 200) {
        questions.value.push(...selected)
        for(let i = 0; i < selected.length; i++){
          props.paper['questions'].push({
            questionId: selected[i].id,
            score: 5,
            sortOrder: props.paper['questions'].length + 1
          })
        }
        recalculateTotal()
      }else{
        alert('保存失败：' + (res['msg'] || '未知错误'))
      }
    }
  })
}

// --- 保存 ---
const savePaper = () => {
  if (isSealed.value) return

  const payload1 = {
    paperId: form.value.paperId,
    paperName: form.value.paperName,
    totalScore: form.value.totalScore,
  }

  saving.value = true

  modifyExamPaper(payload1)

  if(deletedQuestionsId.value != null && deletedQuestionsId.value.length > 0){
    deleteExamPaperQuestions()
  }

  const payload2 = questions.value.map((item, index) => ({
      id:{
        paperId: props.paper['paperId'],
        questionId: item['id']
      },
      score: props.paper['questions'][index]['score'],
      sortOrder: index + 1
    }))

  modifyExamPaperQuestions(payload2)
}

const modifyExamPaper = (data) => {
  useRequest(()=>modifyExamPaperAPI(data),{
    onSuccess(res) {
      if (res['code'] === 200) {

      } else {
        alert('保存失败：' + (res['msg'] || '未知错误'))
      }
    }
  })
}

const deleteExamPaperQuestions = () => {
  useRequest(()=>deleteExamPaperQuestionsAPI(deletedQuestionsId.value),{
    onSuccess(res){
      if(res['code']==200){

      }else{
        alert(res['msg'])
      }
    }
  })
}

const modifyExamPaperQuestions = (data) => {
  useRequest(()=>modifyExamPaperQuestionAPI(data),{
    onSuccess(res){
      if(res['code']==200){
        emit("update:open", false)
        emit("success")
        alert("保存成功")
      }else{
        alert(res['msg'])
      }
    }
  })
}
</script>