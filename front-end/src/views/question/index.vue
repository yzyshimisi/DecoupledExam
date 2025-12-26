<template>
  <div class="p-4 text-base">
    <div class="flex flex-col gap-3">
      <!-- 筛选栏 -->
      <aside
          v-if="showSidebar"
          class="w-full flex-shrink-0 rounded-box bg-base-100"
      >
        <QuestionFilters
            v-model="filters"
            @reset="resetFilters"
        />
      </aside>

      <!-- 主内容区（表格 + 批量操作） -->
      <main class="flex-1 flex flex-col items-center">
        <!-- 操作栏、表格等保持不变 -->
        <!-- ...（此处省略，与之前一致，但注意 filteredQuestions 使用 filters） -->
        <div class="p-6 space-y-6 text-base w-3/4">
          <!-- 操作区域 -->
          <div class="flex flex-wrap gap-3 items-center">
            <button class="btn btn-primary" @click="handleCreate">
              创建题目
            </button>

            <button
                v-if="!showCheckboxes"
                class="btn btn-secondary"
                @click="enterBatchMode"
            >
              批量操作
            </button>

            <div v-else class="flex items-center gap-3">
              <span class="text-sm">已选 {{ selectedIds.length }} 项</span>
              <button class="btn btn-outline btn-sm" @click="exitBatchMode">
                退出批量
              </button>
              <button
                  class="btn btn-error btn-sm"
                  :disabled="selectedIds.length === 0"
                  @click="handleBatchDelete"
              >
                批量删除
              </button>
              <!-- 可扩展更多批量操作 -->
            </div>
          </div>

          <!-- 题目表格 -->
          <div v-show="!isLoading" class="overflow-x-auto rounded-box border">
            <table class="table w-full">
              <thead>
              <tr>
                <!-- 多选框列（仅在批量模式下显示） -->
                <th v-if="showCheckboxes" class="w-12">
                  <input
                      type="checkbox"
                      class="checkbox checkbox-sm"
                      :checked="isAllSelected"
                      @change="toggleSelectAll"
                  />
                </th>
                <th>顺序</th>
                <th>题型</th>
                <th>题干</th>
                <th>难度</th>
                <th>所属学科</th>
                <th>审核状态</th>
                <th>创建时间</th>
                <th>更新时间</th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="(question, index) in questions" :key="question.id" class="hover">
                <!-- 多选框单元格 -->
                <td v-if="showCheckboxes" class="text-center">
                  <input
                      type="checkbox"
                      class="checkbox checkbox-sm"
                      :checked="selectedIds.includes(question.id)"
                      @change="() => toggleSelect(question.id)"
                  />
                </td>
                <td>{{ index + 1 }}</td>
                <td class="whitespace-nowrap">{{ questionTypes.find(type => type.typeId === question.typeId)?.typeName }}</td>
                <td class="max-w-xs truncate" :title="question.title">
                  {{ question.title }}
                </td>
                <td>{{ question.difficulty }}</td>
                <td class="whitespace-nowrap max-w-32 truncate">{{ subjectsList.find(obj => obj.subjectId === question.subjectId)?.subjectName }}</td>
                <td>
              <span
                  class="badge"
                  :class="{
                  'badge-success': question.reviewStatus == 1,
                  'badge-warning': question.reviewStatus == 0,
                  'badge-error': question.reviewStatus == 2
                }"
              >
                {{ statusMap[question.reviewStatus] }}
              </span>
                </td>
                <td>{{ formatDate(question.createdAt) }}</td>
                <td>{{ formatDate(question.updatedAt) }}</td>
              </tr>
              </tbody>
            </table>
          </div>
          <div v-show="isLoading" class="flex gap-3 justify-center">
            <span class="loading loading-ring loading-xs"></span>
            <span class="loading loading-ring loading-sm"></span>
            <span class="loading loading-ring loading-md"></span>
            <span class="loading loading-ring loading-lg"></span>
          </div>

          <div v-if="questions==null || questions.length === 0" class="text-center py-8 text-gray-500">
            暂无题目，请点击“创建题目”添加。
          </div>
          <div v-else class="flex justify-center items-center gap-4">
            <div class="join">
              <button @click="handlePrevPage" class="join-item btn">«</button>
              <button @dblclick="modifyPageNum" class="join-item btn">Page
                <div v-if="!isModifyPageNum">{{pageInfo.pageNum}}
                </div>
                <div v-else>
                  <input
                      v-model="tempPageNum"
                      @blur="overModifyPageNum"
                      @keyup.enter="overModifyPageNum"
                      id="pageNumInput"
                      type="text"
                      placeholder="Type here"
                      class="input-xs max-w-10 min-w-4 text-base" />
                </div>
              </button>
              <button @click="handleNextPage" class="join-item btn">»</button>
            </div>
            <span>共 {{totalPageNum}} 页</span>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import {ref, computed, watch, onMounted, nextTick, onBeforeMount} from 'vue'
import { QuestionFilters } from "../../components";
import { useRequest } from "vue-hooks-plus";
import { getQuestionsAPI, getQuestionTypeAPI, getSubjectAPI } from '../../apis'

// 审核状态映射（根据你的实际字段调整）
const statusMap = {
  1: '已通过',
  0: '待审核',
  2: '已拒绝'
} as const

interface Question {
  id: number
  title: string
  typeId: number
  difficulty: number
  subjectId: number
  reviewStatus: number
  createdAt: string
  updatedAt: string
}

// 示例数据（替换为真实 API 数据）
const questions = ref<Question[]>([])

const showSidebar = ref(window.innerWidth >= 1024)

const questionTypes = ref()
const subjectsList = ref()

onMounted(()=>{
  useRequest(()=>getQuestionTypeAPI(), {
    onSuccess(res) {
      if (res['code'] === 200) {
        questionTypes.value = res['data']
      }
    }
  })

  useRequest(()=>getSubjectAPI(), {
    onSuccess(res) {
      if (res['code'] === 200) {
        subjectsList.value = res['data']
      }
    }
  })

  getQuestions(false)
})

// ========== 筛选状态 ==========
const filters = ref({
  mineOnly: false,
  stemKeyword: '',
  selectedTypes: [] as string[],
  selectedSubjects: [] as string[],
  maxDifficulty: 5,
  selectedStatuses: [] as ('0' | '1' | '2')[],
  authorQuery: '',
})

const pageInfo = ref({
  pageNum: 1,        // 当前页码
  pageSize: 10       // 每页显示条数
})

// 过滤逻辑
watch(()=>filters, (newVal) => {
  pageInfo.value.pageNum = 1
  getQuestions(true)
}, { deep: true })

const isLoading = ref<boolean>(false);

const isModifyPageNum = ref(false);
const tempPageNum = ref<number>()

const modifyPageNum = () => {        // 双击事件
  tempPageNum.value = pageInfo.value.pageNum
  isModifyPageNum.value = true;
  nextTick(()=>{    // 等待页面渲染完（等输入框渲染）
    let pageNumInput = document.getElementById('pageNumInput') as HTMLInputElement;
    pageNumInput.focus();    // 自动将光标选中输入框的内容尾部
    pageNumInput.setSelectionRange(pageNumInput.value.length, pageNumInput.value.length);
  })
}
const overModifyPageNum = () => {
  isModifyPageNum.value = false;
  if(tempPageNum.value>=1 && tempPageNum.value<=totalPageNum.value){
    pageInfo.value.pageNum = tempPageNum.value
    getQuestions(false)
  }
}

const totalPageNum = ref<number>()

const handlePrevPage = () => {
  if(pageInfo.value.pageNum > 1){
    pageInfo.value.pageNum--
    getQuestions(false)
  }
}

const handleNextPage = () => {
  if(pageInfo.value.pageNum < totalPageNum.value){
    pageInfo.value.pageNum++
    getQuestions(false)
  }
}

const getQuestions = async (isDebounce:boolean) => {

  // 映射一下（ID之类的）
  const filterParam = ref({
    mineOnly: false,
    stemKeyword: '',
    selectedTypes: [] as string[],
    selectedSubjects: [] as string[],
    maxDifficulty: 5,
    selectedStatuses: [] as ('0' | '1' | '2')[],
    authorQuery: '',

    pageNum: 1,        // 当前页码
    pageSize: 10       // 每页显示条数
  })

  filterParam.value.mineOnly = filters.value.mineOnly
  filterParam.value.stemKeyword = filters.value.stemKeyword
  filterParam.value.maxDifficulty = filters.value.maxDifficulty
  filterParam.value.authorQuery = filters.value.authorQuery

  filterParam.value.pageNum = pageInfo.value.pageNum
  filterParam.value.pageSize = pageInfo.value.pageSize

  for(let i = 0; i < filters.value.selectedTypes.length; i++){
    filterParam.value.selectedTypes[i] = filters.value.selectedTypes[i]['typeId']
  }

  for(let i = 0; i < filters.value.selectedSubjects.length; i++){
    filterParam.value.selectedSubjects[i] = filters.value.selectedSubjects[i]['subjectId']
  }

  for(let i = 0; i < filters.value.selectedStatuses.length; i++){
    filterParam.value.selectedStatuses[i] = filters.value.selectedStatuses[i]['value']
  }

  if(isDebounce){
    run(filterParam.value)
  }else{
    runWithoutDebounce(filterParam.value)
  }
}

// 获取题目（防抖）
const { data, run } = useRequest((filterParam)=>getQuestionsAPI(<any>filterParam),{
  onBefore(){
    isLoading.value = true;
  },
  onSuccess(res){
    if(res['code'] === 200){
      questions.value = res['data']['questions']
      totalPageNum.value = res['data']['total']
    }
  },
  onFinally(){
    isLoading.value = false
  },
  debounceWait: 500,
  manual: true
})

// 获取题目（不防抖）
const { run: runWithoutDebounce } = useRequest((filterParam)=>getQuestionsAPI(<any>filterParam),{
  onBefore(){
    isLoading.value = true;
  },
  onSuccess(res){
    if(res['code'] === 200){
      questions.value = res['data']['questions']
      totalPageNum.value = res['data']['total']
    }
  },
  onFinally(){
    isLoading.value = false
  },
  manual: true
})


// 重置方法
const resetFilters = () => {
  filters.value = {
    mineOnly: false,
    stemKeyword: '',
    selectedTypes: [] as string[],
    selectedSubjects: [] as string[],
    maxDifficulty: 5,
    selectedStatuses: [] as ('0' | '1' | '2')[],
    authorQuery: '',
  }

  pageInfo.value = {
    pageNum: 1,
    pageSize: 10
  }
}

// 批量操作状态
const showCheckboxes = ref(false)
const selectedIds = ref<(string | number)[]>([])

// 全选逻辑
const isAllSelected = computed(() => {
  return questions.value.length > 0 && selectedIds.value.length === questions.value.length
})

const isIndeterminate = computed(() => {
  const selectedCount = selectedIds.value.length
  return selectedCount > 0 && selectedCount < questions.value.length
})

// 切换单个选择
const toggleSelect = (id: string | number) => {
  const index = selectedIds.value.indexOf(id)
  if (index === -1) {
    selectedIds.value.push(id)
  } else {
    selectedIds.value.splice(index, 1)
  }
}

// 全选/取消全选
const toggleSelectAll = () => {
  if (isAllSelected.value) {
    selectedIds.value = []
  } else {
    selectedIds.value = questions.value.map(q => q.id)
  }
}

// 进入批量模式
const enterBatchMode = () => {
  showCheckboxes.value = true
  selectedIds.value = [] // 可选：清空上次选择
}

// 退出批量模式
const exitBatchMode = () => {
  showCheckboxes.value = false
  selectedIds.value = []
}

// 格式化日期（简化版，生产建议用 dayjs 或 date-fns）
const formatDate = (isoString: string): string => {
  const date = new Date(isoString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 操作处理
const handleCreate = () => {
  console.log('跳转到创建题目页面')
  // 例如：router.push('/questions/create')
}

const handleBatchDelete = () => {
  if (selectedIds.value.length === 0) return
  console.log('批量删除题目 IDs:', selectedIds.value)
  // 调用 API 删除
  // 删除成功后可刷新列表，并退出批量模式
  // exitBatchMode()
}
</script>