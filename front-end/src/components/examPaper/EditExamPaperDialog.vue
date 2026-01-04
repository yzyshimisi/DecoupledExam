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
              onclick="questionDialog2.showModal()"
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
              <div class="text-sm text-gray-500" v-if="tmpPaper">
                {{ questionTypes.find(q => q['typeId'] === item['typeId'])['typeName'] }} · {{ tmpPaper['questions'][index]['score'] }} 分
              </div>
            </div>

            <!-- 分值输入 -->
            <input
                v-if="!isSealed"
                v-model.number="tmpPaper['questions'][index]['score']"
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
            @click="savePaper"
        >
          <span v-if="saving" class="loading loading-spinner"></span>
          {{ saving ? "正在保存..." : "保存" }}
        </button>
      </div>
    </div>

    <!-- 背景遮罩点击关闭 -->
    <form method="dialog" class="modal-backdrop fixed inset-0 bg-black/50">
      <button @click="emit('update:open', false)">close</button>
    </form>
  </dialog>
  <dialog id="questionDialog2" class="modal">
    <div class="modal-box max-w-[95vw] w-[95vw] ">
      <form method="dialog">
        <button class="btn btn-sm btn-circle btn-ghost absolute right-2 top-2">✕</button>
      </form>
      <Question
          :is-component="true"
          @selectQuestions ="selectQuestions"
      />
    </div>
    <form method="dialog" class="modal-backdrop">
      <button>close</button>
    </form>
  </dialog>
</template>

<script setup lang="ts">
import {computed, ref, watch} from 'vue'
import {useRequest} from 'vue-hooks-plus'
import {
  addExamPaperQuestionsAPI,
  deleteExamPaperQuestionsAPI,
  getQuestionsByIdAPI,
  modifyExamPaperAPI,
  modifyExamPaperQuestionAPI
} from '../../apis';
import {Question} from "../../views";
import {cloneDeep} from "lodash-es"

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

const tmpPaper = ref();

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
        // 使用 cloneDeep 进行深拷贝
        tmpPaper.value = cloneDeep(props.paper)

        Object.assign(form.value, tmpPaper.value)
        let n = tmpPaper.value['questions'].length
        for (let i = 0; i < n - 1; i++) {
          for (let j = 0; j < n - i - 1; j++) {
            if (tmpPaper.value['questions'][j].sortOrder > tmpPaper.value['questions'][j + 1].sortOrder) {
              // 交换元素
              ;[tmpPaper.value['questions'][j], tmpPaper.value['questions'][j + 1]] = [tmpPaper.value['questions'][j + 1], tmpPaper.value['questions'][j]]
            }
          }
        }
        for(let i = 0; i < tmpPaper.value['questions'].length; i++) {
          getQuestionById(tmpPaper.value['questions'][i].questionId)
        }
      }else {
        tmpPaper.value = {}; // 或者 null，根据你的业务逻辑
      }
    },
    { immediate: true }
)

// --- 计算总分 ---
const recalculateTotal = () => {
  form.value.totalScore = tmpPaper.value['questions'].reduce((sum, item) => sum + (item.score || 0), 0)
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

  let exists = newQuestion.value.some(item => {
    // 注意：这里 item.id 对象的属性访问
    return item.id.questionId === questions.value[index].id;
  });

  if(exists){ // 删除的题目是临时添加的（则只需删除newQuestion中的值即可）
    newQuestion.value = newQuestion.value.filter(item => {
      return item.id.questionId !== questions.value[index].id;
    });
  }else{  // 加入到删除数组中
    deletedQuestionsId.value.push({
      questionId: questions.value[index].id,
      paperId: form.value.paperId
    })
  }

  questions.value.splice(index, 1)
  tmpPaper.value['questions'].splice(index, 1)
  recalculateTotal()
}

const newQuestion = ref([])

const selectQuestions = (selectedId: any) => {

  for(let i = 0; i < selectedId.length; i++){
    newQuestion.value.push({
      id:{
        paperId: tmpPaper.value['paperId'],
        questionId: selectedId[i]
      },
      score: 5,
      sortOrder: tmpPaper.value['questions'].length + 1
    })

    tmpPaper.value['questions'].push({
      questionId: selectedId[i],
      score: 5,
      sortOrder: tmpPaper.value['questions'].length + 1
    })

    getQuestionById(selectedId[i])
  }

  questionDialog2.close()
}

// --- 保存 ---
const savePaper = () => {
  if (isSealed.value) return

  saving.value = true

  // 修改试卷信息
  useRequest(()=>modifyExamPaperAPI(form.value),{
    onSuccess(res) {
      if (res['code'] === 200) {

        // 修改试卷题目（分值、顺序，新增的排除）
        let payload = []
        for(let i = 0; i < questions.value.length; i++){
          let exists = newQuestion.value.some(item => {
            // 注意：这里 item.id 对象的属性访问
            return item.id.questionId === questions.value[i].id;
          });

          if(!exists) {   // 临时添加的题目就算修改了信息，还是走添加试卷题目的接口
            payload.push({
              id:{
                paperId: tmpPaper.value['paperId'],
                questionId: questions.value[i]['id']
              },
              score: tmpPaper.value['questions'][i]['score'],
              sortOrder: i + 1
            })
          }
        }
        useRequest(()=>modifyExamPaperQuestionAPI(payload),{
          onSuccess(res){
            if(res['code']==200){

              // 添加题目
              useRequest(()=>addExamPaperQuestionsAPI(newQuestion.value),{
                onSuccess(res) {
                  if (res['code'] === 200) {

                    // 删除试卷题目
                    if(deletedQuestionsId.value != null && deletedQuestionsId.value.length > 0){
                      useRequest(()=>deleteExamPaperQuestionsAPI(deletedQuestionsId.value),{
                        onSuccess(res){
                          if(res['code']==200){
                            emit("update:open", false)
                            emit("success")
                            alert("保存成功")
                          }else{
                            alert("试卷题目删除失败：" + (res['msg'] || '未知错误'))
                            saving.value = false
                            return
                          }
                        }
                      })
                    }else{
                      emit("update:open", false)
                      emit("success")
                      alert("保存成功")
                    }

                  }else{
                    alert('保存失败：' + (res['msg'] || '未知错误'))
                    saving.value = false
                    return
                  }
                }
              })

            }else{
              saving.value = false
              alert(res['msg'])
              return
            }
          }
        })

      } else {
        alert('试卷信息保存失败：' + (res['msg'] || '未知错误'))
        saving.value = false
        return
      }
    },
    onFinally(){
      saving.value = false
    }
  })
}
</script>