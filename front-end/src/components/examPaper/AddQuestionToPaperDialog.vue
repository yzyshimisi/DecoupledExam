<template>
  <dialog :open="open" class="modal modal-bottom sm:modal-middle">
    <div class="modal-box w-11/12 max-w-5xl">
      <h3 class="font-bold text-lg mb-4">添加题目</h3>

      <!-- 筛选区 -->
      <div class="flex flex-wrap gap-4 mb-4 p-4 bg-base-100 rounded-lg">
        <div>
          <label class="label">
            <span class="label-text">题型</span>
          </label>
          <select v-model="filters.typeId" class="select select-bordered">
            <option value="">全部题型</option>
            <option
                v-for="type in questionTypes"
                :key="type.typeId"
                :value="type.typeId"
            >
              {{ type.typeName }}
            </option>
          </select>
        </div>

        <div class="flex-1">
          <label class="label">
            <span class="label-text">关键词</span>
          </label>
          <input
              v-model.trim="filters.keyword"
              type="text"
              placeholder="题目内容关键词"
              class="input input-bordered w-full"
              @keyup.enter="loadQuestions"
          />
        </div>

        <div class="flex items-end">
          <button class="btn btn-primary" @click="loadQuestions">搜索</button>
        </div>
      </div>

      <!-- 题目列表 -->
      <div class="mb-4 max-h-96 overflow-y-auto border rounded-lg p-2">
        <div v-if="loading" class="text-center py-4 flex gap-2 justify-center">
          <span class="loading loading-ring loading-xs"></span>
          <span class="loading loading-ring loading-sm"></span>
          <span class="loading loading-ring loading-md"></span>
          <span class="loading loading-ring loading-lg"></span>
        </div>
        <div v-else-if="questions.length === 0" class="text-gray-500 py-4 text-center">
          暂无题目
        </div>
        <div v-else class="space-y-2">
          <div
              v-for="q in questions"
              :key="q.id"
              class="flex items-start gap-3 p-3 border rounded hover:bg-base-200 cursor-pointer"
              :class="{ 'bg-blue-50 border-blue-400': selectedIds.has(q.id) }"
              @click="toggleSelect(q.id)"
          >
            <div class="form-control">
              <label class="cursor-pointer label">
                <input
                    type="checkbox"
                    :checked="selectedIds.has(q.id)"
                    class="checkbox checkbox-sm"
                    @click.stop
                />
              </label>
            </div>
            <div class="flex-1 min-w-0">
              <div class="font-medium text-sm line-clamp-2">{{ q.title }}</div>
              <div class="text-xs text-gray-500 mt-1">
                {{ getQuestionTypeName(q.typeId) }} · ID: {{ q.id }}
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 底部按钮 -->
      <div class="modal-action">
        <button class="btn" @click="close">取消</button>
        <button
            class="btn btn-primary"
            :disabled="selectedIds.size === 0"
            @click="confirmSelect"
        >
          确定（已选 {{ selectedIds.size }} 题）
        </button>
      </div>
    </div>

    <!-- 背景遮罩 -->
    <form method="dialog" class="modal-backdrop fixed inset-0 bg-black/50">
      <button @click="close">close</button>
    </form>
  </dialog>
</template>

<script setup lang="ts">
import { ref, watch, computed, onMounted } from 'vue'
import { useRequest } from 'vue-hooks-plus'
import { getQuestionsAPI } from "../../apis";
// import { getQuestionListAPI } from '@/apis/questionAPIs' // 你需要实现这个 API

const props = defineProps<{
  open: boolean
  questionTypes: any[]
}>()

const emit = defineEmits<{
  (e: 'update:open', val: boolean): void
  (e: 'select', questions: any[]): void
}>()

// --- 状态 ---
const loading = ref(false)
const filters = ref({
  typeId: '',
  keyword: ''
})

const questions = ref<any[]>([])
const selectedIds = ref(new Set<number>())

// --- 计算题型名称 ---
const getQuestionTypeName = (typeId: number) => {
  const type = props.questionTypes.find(t => t.typeId === typeId)
  return type ? type.typeName : '未知'
}

onMounted(()=>{
  loadQuestions()
})

// --- 加载题目 ---
const loadQuestions = () => {
  const params = {
    selectedTypes: [filters.value.typeId || undefined],
    stemKeyword: filters.value.keyword || undefined,
  }

  loading.value = true

  useRequest(()=>getQuestionsAPI(params),{
    onSuccess(res){
      if(res['code']==200){
        questions.value = res['data']['questions']
        // console.log(questions.value)
      }
    },
    onFinally(){
      loading.value = false
    }
  })
}

// --- 选择操作 ---
const toggleSelect = (id: number) => {
  if (selectedIds.value.has(id)) {
    selectedIds.value.delete(id)
  } else {
    selectedIds.value.add(id)
  }
}

// --- 确认选择 ---
const confirmSelect = () => {
  const selected = questions.value.filter(q => selectedIds.value.has(q.id))
  emit('select', selected)
  close()
}

// --- 关闭 ---
const close = () => {
  emit('update:open', false)
  // 清空状态（可选）
  selectedIds.value.clear()
  filters.value = { typeId: '', keyword: '' }
}

// --- 监听打开，自动加载题目 ---
watch(
    () => props.open,
    (isOpen) => {
      if (isOpen) {
        loadQuestions()
      }
    }
)
</script>