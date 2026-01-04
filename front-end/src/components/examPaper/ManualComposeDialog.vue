<template>
  <div>
    <!-- Modal -->
    <div v-if="open" class="modal modal-open">
      <div class="modal-box w-11/12 max-w-5xl text-base">
        <h3 class="font-bold text-lg mb-4">手动组卷</h3>

        <!-- 试卷基本信息 -->
        <div class="card bg-base-200 p-4 mb-6">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label class="label">
                <span class="label-text">试卷名称 *</span>
              </label>
              <input
                  v-model="form.paperName"
                  type="text"
                  class="input input-bordered w-full text-base"
                  placeholder="请输入试卷名称"
              />
            </div>

            <div>
              <label class="label">
                <span class="label-text">是否封存</span>
              </label>
              <select v-model="form.isSealed" class="select select-bordered w-full text-base">
                <option value="0">否</option>
                <option value="1">是</option>
              </select>
            </div>

            <div>
              <label class="label">
                <span class="label-text">试卷总分（只读，由题目分数自动计算）</span>
              </label>
              <input
                  :value="computedTotalScore"
                  disabled
                  class="input input-bordered w-full text-base bg-base-100"
              />
            </div>
          </div>
        </div>

        <!-- 题库选择区域 -->
        <div class="mb-6">
          <div class="flex flex-row gap-2 items-center mb-4">
            <h4 class="font-semibold">从题库选择题目</h4>
            <button
                class="btn btn-sm btn-outline"
                onclick="questionDialog.showModal()"
            >
              高级选择
            </button>
          </div>
          <div class="flex gap-2 mb-3">
            <input
                v-model="searchKeyword"
                type="text"
                class="input input-bordered flex-grow text-base"
                placeholder="搜索题目（模拟）"
            />
            <button class="btn btn-outline btn-sm" @click="loadQuestionBank">搜索</button>
          </div>

          <!-- 题库列表（多选） -->
          <div class="border rounded-lg max-h-60 overflow-y-auto p-2">
            <div v-for="q in filteredQuestions" :key="q.id" class="flex items-center py-2 border-b">
              <input
                  type="checkbox"
                  :id="`q-${q.id}`"
                  :checked="isSelected(q.id)"
                  @change="toggleQuestion(q)"
                  class="mr-3"
              />
              <label :for="`q-${q.id}`" class="text-base cursor-pointer">
                [{{ questionTypes.find(t => t.typeId === q.typeId)?.typeName }}] {{ q.title }}
              </label>
            </div>
            <div v-if="filteredQuestions.length === 0" class="text-center text-gray-500 py-4">
              无匹配题目
            </div>
          </div>
        </div>

        <!-- 已选题目 -->
        <div>
          <h4 class="font-semibold mb-2">已选题目（{{ selectedQuestions.length }} 道）</h4>
          <div v-if="selectedQuestions.length === 0" class="text-gray-500">暂未选择任何题目</div>
          <table v-else class="table table-zebra w-full text-base">
            <thead>
            <tr>
              <th>题目内容</th>
              <th>题型</th>
              <th>分数</th>
              <th>排序</th>
              <th>操作</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="(item, index) in selectedQuestions" :key="item.id">
              <td>{{ item.title }}</td>
              <td>{{ questionTypes.find(t => t.typeId === item.typeId)?.typeName }}</td>
              <td>
                <input
                    v-model.number="item.score"
                    type="number"
                    min="0"
                    step="0.5"
                    class="input input-xs input-bordered w-20 text-base"
                />
              </td>
              <td>
                <input
                    v-model.number="item.sortOrder"
                    type="number"
                    min="1"
                    class="input input-xs input-bordered w-20 text-base"
                />
              </td>
              <td>
                <button class="btn btn-xs btn-error" @click="removeQuestion(item.id)">
                  删除
                </button>
              </td>
            </tr>
            </tbody>
          </table>
        </div>

        <!-- 操作按钮 -->
        <div class="modal-action">
          <button class="btn" @click="open = false">取消</button>
          <button class="btn btn-primary" :disabled="!canSubmit" @click="handleSubmit">
            创建试卷
          </button>
        </div>
      </div>

      <!-- 背景遮罩点击关闭 -->
      <div class="modal-backdrop cursor-pointer" @click="open = false"></div>
    </div>
  </div>
  <dialog id="questionDialog" class="modal">
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
import { ref, computed, watch, onMounted } from 'vue'
import { Question } from "../../views"
import { addExamPapersAPI, getQuestionsAPI } from '../../apis'
import { useRequest } from "vue-hooks-plus"; // 假设你有这个 API

// Props / Emits
const props = defineProps<{
  open: boolean
  questionTypes: any[]
}>()
const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'success'): void
}>()

// 同步 open 状态
const open = ref(props.open)
watch(() => props.open, (val) => open.value = val)
watch(open, (val) => emit('update:open', val))

onMounted(()=>{
  getQuestions();
})

const selectQuestions = (ids) => {  // 通过Question组件来选择题目
  for (const id of ids) {
    const q = allQuestions.value.find(q => q.id === id)
    toggleQuestion(q)
  }
  questionDialog.close()
}

// 表单数据
const form = ref({
  paperName: '',
  isSealed: '0',
  composeType: '1' // 手动组卷
})

// 模拟题库（实际应从 API 获取）

const allQuestions = ref([])

const getQuestions = () => {
  useRequest(()=> getQuestionsAPI(null), {
    onSuccess(res){
      if(res['code']==200){
        allQuestions.value = res['data']['questions']
      }
    }
  })
}

const searchKeyword = ref('')
const filteredQuestions = computed(() => {
  if (!searchKeyword.value) return allQuestions.value
  return allQuestions.value.filter(q =>
      q.content.toLowerCase().includes(searchKeyword.value.toLowerCase)
  )
})

// 已选题目（带 score 和 sortOrder）
const selectedQuestions = ref([])

const isSelected = (id: number) => {
  return selectedQuestions.value.some(q => q.id === id)
}

const toggleQuestion = (q) => {
  const idx = selectedQuestions.value.findIndex(item => item.id === q.id)
  if (idx >= 0) {
    selectedQuestions.value.splice(idx, 1)
  } else {
    selectedQuestions.value.push({
      ...q,
      score: 10,       // 默认分值
      sortOrder: selectedQuestions.value.length + 1 // 默认排序
    })
  }
}

const removeQuestion = (id: number) => {
  const idx = selectedQuestions.value.findIndex(q => q.id === id)
  if (idx >= 0) selectedQuestions.value.splice(idx, 1)
}

// 计算总分
const computedTotalScore = computed(() => {
  return selectedQuestions.value.reduce((sum, q) => sum + q.score, 0)
})

// 校验能否提交
const canSubmit = computed(() => {
  return (
      form.value.paperName.trim() !== '' &&
      selectedQuestions.value.length > 0
  )
})

// 提交
const handleSubmit = async () => {
  if (!canSubmit.value) return

  // 构造 ExamPaperDTO
  const payload = {
    paperName: form.value.paperName,
    totalScore: Math.round(computedTotalScore.value), // 取整
    isSealed: form.value.isSealed,
    composeType: form.value.composeType,
    questions: selectedQuestions.value.map(q => ({
      questionId: q.id,
      score: q.score,
      sortOrder: q.sortOrder
    }))
  }

  useRequest(()=> addExamPapersAPI(payload), {
    onSuccess(res){
      if(res['code']==200){
        emit("success")
        open.value = false
        // 重置表单
        form.value = {
          paperName: '',
          isSealed: '0',
          composeType: '1'
        }
        selectedQuestions.value = []
        searchKeyword.value = ''

        alert("组卷成功")
      }else{
        alert(res['msg'])
      }
    }
  })
}

// 模拟搜索（实际可调用 API）
const loadQuestionBank = () => {
  // 这里可以加 loading 或调用 API
}
</script>