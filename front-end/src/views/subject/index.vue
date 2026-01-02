<template>
  <div class="p-6 max-w-7xl mx-auto w-full">
    <!-- 🎯 全新筛选栏 -->
    <div class="mb-6 p-5 bg-base-100 rounded-xl shadow-sm border">
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-6 gap-5">
        <!-- 适用年级段 - 标签式多选 -->
        <div class="lg:col-span-2">
          <label class="label mb-2">
            <span class="label-text text-base font-medium">适用年级段</span>
          </label>
          <div class="flex flex-wrap gap-2 min-h-[44px] p-2.5 border border-base-300 rounded-lg bg-base-50">
            <button
                v-for="opt in gradeOptions"
                :key="opt.value"
                type="button"
                class="px-3.5 py-1.5 text-base rounded-full transition-all"
                :class="{
            'bg-primary text-primary-content': filters.gradeLevels.includes(opt.value),
            'bg-base-200 hover:bg-base-300 text-base-content': !filters.gradeLevels.includes(opt.value)
          }"
                @click="toggleGradeLevel(opt.value)"
            >
              {{ opt.label }}
            </button>
          </div>
        </div>

        <!-- 启用状态 -->
        <div>
          <label class="label mb-2">
            <span class="label-text text-base font-medium">启用状态</span>
          </label>
          <select
              v-model="filters.status"
              class="select select-bordered w-full h-11 text-base"
          >
            <option value="">全部</option>
            <option value="1">启用</option>
            <option value="0">禁用</option>
          </select>
        </div>

        <!-- 学科名称 -->
        <div>
          <label class="label mb-2">
            <span class="label-text text-base font-medium">学科名称</span>
          </label>
          <input
              v-model="filters.nameKeyword"
              type="text"
              placeholder="如：数学"
              class="input input-bordered w-full h-11 text-base"
              @keyup.enter="applyFilters"
          />
        </div>

        <!-- 学科编码 -->
        <div>
          <label class="label mb-2">
            <span class="label-text text-base font-medium">学科编码</span>
          </label>
          <input
              v-model="filters.codeKeyword"
              type="text"
              placeholder="如：MATH001"
              class="input input-bordered w-full h-11 text-base"
              @keyup.enter="applyFilters"
          />
        </div>

        <!-- 操作按钮 -->
        <div class="flex flex-col justify-end gap-3">
          <div class="flex gap-3">
            <button class="btn btn-outline flex-1 h-11" @click="resetFilters">
              重置
            </button>
            <button class="btn btn-primary flex-1 h-11" @click="applyFilters">
              搜索
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 主内容区（保持原有结构，仅微调样式） -->
    <div class="p-4 bg-base-100 rounded-xl shadow-sm border">
      <h2 class="text-xl font-bold mb-4">📚 学科管理</h2>

      <!-- 操作栏 -->
      <div class="flex flex-wrap gap-3 mb-4">

        <button class="btn btn-primary btn-sm" @click="openCreateDialog">新增学科</button>
        <button class="btn btn-accent btn-sm" @click="openBatchCreateDialog">批量创建</button>

        <div v-if="!showCheckboxes" class="ms-auto flex items-center gap-3">
          <button
              class="btn btn-outline btn-sm"
              @click="enterBatchMode"
          >
            批量操作
          </button>
        </div>

        <div v-if="showCheckboxes" class="ms-auto flex items-center gap-3">
          <span>已选 {{ selectedIds.length }} 项</span>
          <button class="btn btn-outline btn-sm" @click="exitBatchMode">退出</button>
          <button
              class="btn btn-sm btn-error"
              :disabled="selectedIds.length === 0"
              @click="handleBatchDelete"
          >
            批量删除
          </button>
        </div>
      </div>

      <!-- 表格 -->
      <!-- 表格 -->
      <div class="overflow-x-auto rounded-lg border">
        <table class="table">
          <thead>
          <tr>
            <th v-if="showCheckboxes" class="w-10">
              <input
                  type="checkbox"
                  class="checkbox checkbox-sm"
                  :checked="isAllSelected"
                  @change="toggleSelectAll"
              />
            </th>
            <th>序号</th>
            <th>学科名称</th>
            <th>学科编码</th>
            <th>适用年级</th>
            <th>启用状态</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="(subject, index) in subjects" :key="subject.subjectId" class="hover">
            <td v-if="showCheckboxes" class="text-center">
              <input
                  type="checkbox"
                  class="checkbox checkbox-sm"
                  :checked="selectedIds.includes(subject.subjectId)"
                  @change="() => toggleSelect(subject.subjectId)"
              />
            </td>
            <td>{{ (pageInfo.pageNum - 1) * pageInfo.pageSize + index + 1 }}</td>
            <td>
          <span v-if="!editingSubject || editingSubject.subjectId !== subject.subjectId">
            {{ subject.subjectName }}
          </span>
              <input
                  v-else
                  v-model="editingSubject.subjectName"
                  type="text"
                  class="input input-xs input-bordered w-full max-w-32"
                  @blur="saveEdit"
                  @keyup.enter="saveEdit"
                  ref="editInputRef"
              />
            </td>
            <td>{{ subject['subjectCode'] || '—' }}</td>
            <td>
              {{
                gradeOptions.find(opt => opt.value === String(subject['gradeLevel']))?.label || '未知'
              }}
            </td>
            <td>
          <span
              class="px-2 py-0.5 rounded-full"
              :class="{
              'bg-success text-success-content': subject['status'] === 1,
              'bg-error text-error-content': subject['status'] === 0
            }"
          >
            {{ subject['status'] === 1 ? '启用' : '禁用' }}
          </span>
            </td>
            <td>{{ formatDate(subject.createTime) }}</td>
            <td class="flex gap-2">
              <button
                  v-if="!showCheckboxes"
                  class="btn btn-ghost btn-xs text-base"
                  @click="openEditDialog(subject)"
              >
                ✏️ 编辑
              </button>
              <button
                  v-if="!showCheckboxes"
                  class="btn btn-ghost btn-xs text-error text-base"
                  @click="handleDelete(subject.subjectId)"
              >
                ❌ 删除
              </button>
            </td>
          </tr>
          </tbody>
        </table>
      </div>

      <!-- 分页 & 状态 -->
      <div v-if="subjects.length > 0" class="flex justify-between items-center mt-4">
        <span class="text-sm text-gray-600">共 {{ total }} 条记录</span>
        <div class="join">
          <button
              @click="prevPage"
              class="join-item btn btn-sm"
              :disabled="pageInfo.pageNum <= 1"
          >«</button>
          <button class="join-item btn btn-sm">第 {{ pageInfo.pageNum }} 页</button>
          <button
              @click="nextPage"
              class="join-item btn btn-sm"
              :disabled="pageInfo.pageNum >= totalPage"
          >»</button>
        </div>
      </div>

      <div v-else-if="!loading" class="text-center py-8 text-gray-500">
        暂无学科，请点击“新增学科”添加。
      </div>

      <div v-if="loading" class="flex justify-center my-6">
        <span class="loading loading-spinner"></span>
      </div>
    </div>

    <!-- 弹窗（保持不变） -->
    <!-- 新增学科弹窗 -->
    <dialog ref="createDialogRef" class="modal">
      <div class="modal-box w-full max-w-md">
        <h3 class="font-bold text-lg mb-4">新增学科</h3>

        <!-- 学科名称 -->
        <div class="form-control mb-4">
          <label class="label">
            <span class="label-text">学科名称 *</span>
          </label>
          <input
              v-model="newSubjectForm.subjectName"
              type="text"
              placeholder="请输入学科名称"
              class="input input-bordered"
          />
        </div>

        <!-- 学科编码 -->
        <div class="form-control mb-4">
          <label class="label">
            <span class="label-text">学科编码 *</span>
          </label>
          <input
              v-model="newSubjectForm.subjectCode"
              type="text"
              placeholder="如：MATH001"
              class="input input-bordered"
          />
        </div>
        <!-- 适用年级段（单选下拉） -->
        <div class="form-control mb-4">
          <label class="label">
            <span class="label-text">适用年级段 *</span>
          </label>
          <select
              v-model.number="newSubjectForm.gradeLevel"
              class="select select-bordered"
          >
            <option value="" disabled>请选择年级段</option>
            <option
                v-for="opt in gradeOptions"
                :value="opt.value"
            >
              {{ opt.label }}
            </option>
          </select>
          <p v-if="!newSubjectForm.gradeLevel" class="text-error text-xs mt-1">
            必须选择一个年级段
          </p>
        </div>

        <!-- 启用状态 -->
        <div class="form-control mb-6">
          <label class="label cursor-pointer justify-between">
            <span class="label-text">启用状态</span>
            <input
                v-model="newSubjectForm.status"
                type="checkbox"
                class="toggle toggle-primary"
                :true-value="1"
                :false-value="0"
            />
          </label>
        </div>

        <!-- 操作按钮 -->
        <div class="modal-action">
          <button class="btn" @click="closeCreateDialog">取消</button>
          <button
              class="btn btn-primary"
              :disabled="!isCreateFormValid"
              @click="createSingle"
          >
            创建
          </button>
        </div>
      </div>
      <form method="dialog" class="modal-backdrop"><button>close</button></form>
    </dialog>

    <!-- 编辑学科弹窗 -->
    <dialog ref="editDialogRef" class="modal">
      <div class="modal-box w-full max-w-md">
        <h3 class="font-bold text-lg mb-4">编辑学科</h3>

        <!-- 学科名称 -->
        <div class="form-control mb-4">
          <label class="label">
            <span class="label-text">学科名称 *</span>
          </label>
          <input
              v-model="editingForm.subjectName"
              type="text"
              placeholder="请输入学科名称"
              class="input input-bordered"
          />
        </div>

        <!-- 学科编码 -->
        <div class="form-control mb-4">
          <label class="label">
            <span class="label-text">学科编码 *</span>
          </label>
          <input
              v-model="editingForm.subjectCode"
              type="text"
              placeholder="如：MATH001"
              class="input input-bordered"
          />
        </div>

        <!-- 适用年级段（单选） -->
        <div class="form-control mb-4">
          <label class="label">
            <span class="label-text">适用年级段 *</span>
          </label>
          <select
              v-model.number="editingForm.gradeLevel"
              class="select select-bordered"
          >
            <option
                v-for="opt in gradeOptions"
                :key="opt.value"
                :value="Number(opt.value)"
            >
              {{ opt.label }}
            </option>
          </select>
        </div>

        <!-- 启用状态 -->
        <div class="form-control mb-6">
          <label class="label cursor-pointer justify-between">
            <span class="label-text">启用状态</span>
            <input
                v-model="editingForm.status"
                type="checkbox"
                class="toggle toggle-primary"
                :true-value="1"
                :false-value="0"
            />
          </label>
        </div>

        <!-- 操作按钮 -->
        <div class="modal-action">
          <button class="btn" @click="closeEditDialog">取消</button>
          <button
              class="btn btn-primary"
              :disabled="!isEditFormValid"
              @click="saveEdit"
          >
            保存
          </button>
        </div>
      </div>
      <form method="dialog" class="modal-backdrop"><button>close</button></form>
    </dialog>

    <dialog ref="batchCreateDialogRef" class="modal">
      <div class="modal-box w-full max-w-4xl">
        <h3 class="font-bold text-lg">批量创建学科</h3>
        <p class="text-sm text-gray-600 mb-4">
          点击“添加一行”逐个填写学科信息。
        </p>

        <!-- 行列表 -->
        <div class="space-y-4 mb-5 max-h-80 overflow-y-auto pr-2">
          <div v-for="(item, index) in batchItems" :key="index" class="grid grid-cols-1 sm:grid-cols-4 gap-4 p-4 border rounded-lg bg-base-100">
            <!-- 学科名称 -->
            <input
                v-model="item.subjectName"
                type="text"
                placeholder="学科名称 *"
                class="input input-bordered w-full"
            />
            <!-- 学科编码 -->
            <input
                v-model="item.subjectCode"
                type="text"
                placeholder="编码（可选）"
                class="input input-bordered w-full"
            />
            <!-- 年级段 -->
            <select v-model.number="item.gradeLevel" class="select select-bordered w-full">
              <option value="" disabled>请选择年级 *</option>
              <option v-for="opt in gradeOptions" :value="Number(opt.value)">
                {{ opt.label }}
              </option>
            </select>
            <!-- 启用状态 + 删除 -->
            <div class="flex items-center justify-between">
              <label class="label cursor-pointer gap-2 p-0">
                <input
                    v-model="item.status"
                    type="checkbox"
                    class="toggle toggle-primary"
                    :true-value="1"
                    :false-value="0"
                />
                <span class="label-text">{{ item.status === 1 ? '启用' : '禁用' }}</span>
              </label>
              <button
                  v-if="batchItems.length > 1"
                  class="btn btn-ghost btn-sm text-error"
                  @click="removeBatchItem(index)"
              >
                删除
              </button>
            </div>
          </div>
        </div>

        <!-- 底部操作 -->
        <div class="flex justify-between items-center">
          <button class="btn btn-outline btn-sm" @click="addBatchItem">
            ➕ 添加一行
          </button>
          <div class="modal-action flex gap-3">
            <button class="btn" @click="closeBatchCreateDialog">取消</button>
            <button
                class="btn btn-primary"
                @click="createBatch"
                :disabled="!isBatchInputValid"
            >
              批量创建（{{ validItemCount }} 项）
            </button>
          </div>
        </div>
      </div>
      <form method="dialog" class="modal-backdrop">
        <button @click="closeBatchCreateDialog">close</button>
      </form>
    </dialog>
  </div>
</template>

<script setup lang="ts">
import {ref, computed, onMounted, nextTick} from 'vue'
import { useRequest } from 'vue-hooks-plus'
import {
  getSubjectsAPI,
  createSubjectsAPI,
  deleteSubjectsAPI, importSubjectsAPI,
} from '../../apis'

// ====== 数据模型 ======
interface Subject {
  subjectId: number
  subjectName: string
  subjectCode: string      // 学科编码，如 "MATH001"
  gradeLevel: number       // 年级段：1=小学, 2=初中, 3=高中, 4=大学, 9=通用
  status: number           // 启用状态：0=禁用, 1=启用
  createTime: number        // 创建时间（ISO 8601 格式）
}

// 筛选条件
const filters = ref({
  gradeLevels: [] as string[], // 多选：['1', '2']
  status: '',                  // '' | '1' | '0'
  nameKeyword: '',
  codeKeyword: ''
})

// 年级选项
const gradeOptions = [
  { value: '1', label: '小学' },
  { value: '2', label: '初中' },
  { value: '3', label: '高中' },
  { value: '4', label: '大学' },
  { value: '9', label: '通用' }
]

// 切换年级段选择
const toggleGradeLevel = (value: string) => {
  const idx = filters.value.gradeLevels.indexOf(value)
  if (idx === -1) {
    filters.value.gradeLevels.push(value)
  } else {
    filters.value.gradeLevels.splice(idx, 1)
  }
}

// ====== 响应式数据 ======
const subjects = ref<Subject[]>([])
const loading = ref(false)
const total = ref(0)
const totalPage = ref(0)

const pageInfo = ref({
  pageNum: 1,
  pageSize: 10
})

// 新增学科表单数据
const newSubjectForm = ref({
  subjectName: '',
  subjectCode: '',
  gradeLevel: 1,   // ← number 类型，默认选“小学”
  status: 1        // ← number 类型，1 = 启用
})

const isCreateFormValid = computed(() => {
  return (
      newSubjectForm.value.subjectName.trim() !== '' &&
      newSubjectForm.value.subjectCode.trim() !== ''
      // gradeLevel 和 status 有默认值，无需判空
  )
})

// 编辑状态
const editingSubject = ref<Subject | null>(null)
const editInputRef = ref<HTMLInputElement | null>(null)

// 创建单个
const createDialogRef = ref<HTMLDialogElement | null>(null)

// 批量创建
const batchInput = ref('')
const batchCreateDialogRef = ref<HTMLDialogElement | null>(null)

// 批量操作
const showCheckboxes = ref(false)
const selectedIds = ref<number[]>([])

// 编辑弹窗
const editDialogRef = ref<HTMLDialogElement | null>(null)

// 编辑表单（注意：不是 editingSubject，而是可编辑副本）
const editingForm = ref({
  subjectId: 0,
  subjectName: '',
  subjectCode: '',
  gradeLevel: 1,
  status: 1
})

const isEditFormValid = computed(() => {
  return (
      editingForm.value.subjectName.trim() !== '' &&
      editingForm.value.subjectCode.trim() !== ''
  )
})

// 应用筛选（带防抖）
const applyFilters = () => {
  pageInfo.value.pageNum = 1 // 重置页码
  getSubjects()
}

// 重置筛选
const resetFilters = () => {
  filters.value = {
    gradeLevels: [],
    status: '',
    nameKeyword: '',
    codeKeyword: ''
  }
  pageInfo.value.pageNum = 1
  getSubjects()
}

// ====== API 调用 ======
const getSubjects = async () => {
  // 从 filters 和 pageInfo 构造请求参数
  const params = {
    // 学科名称（模糊）
    subjectName: filters.value.nameKeyword.trim() || undefined,

    // 学科编码（模糊）
    subjectCode: filters.value.codeKeyword.trim() || undefined,

    // 年级段：取第一个选中的（因为后端 DTO 是单个 gradeLevel）
    gradeLevels: filters.value.gradeLevels.length > 0
        ? filters.value.gradeLevels.map(v => Number(v))
        : undefined, // 或 []，取决于后端是否接受空数组

    // 启用状态：'1' -> 1, '0' -> 0, '' -> undefined
    status: filters.value.status !== ''
        ? Number(filters.value.status)
        : undefined,

    // 分页参数
    pageNum: pageInfo.value.pageNum,
    pageSize: pageInfo.value.pageSize
  }

  loading.value = true

  useRequest(()=>getSubjectsAPI(params), {
    onSuccess(res){
      if(res['code']==200){
        subjects.value = res['data']['subjects']
        totalPage.value = res['data']['total']
        total.value = subjects.value.length
      }
    },
    onFinally(){
      loading.value = false
    }
  })
}

onMounted(() => {
  getSubjects()
})

// ====== 单个创建 ======
const openCreateDialog = () => {
  newSubjectForm.value = {
    subjectName: '',
    subjectCode: '',
    gradeLevel: 1, // ← 单值字符串（如 '2'）
    status: 1
  }
  createDialogRef.value?.showModal()
}

const closeCreateDialog = () => {
  createDialogRef.value?.close()
}

const createSingle = async () => {
  if (!newSubjectForm.value) return
  useRequest(()=>createSubjectsAPI(newSubjectForm.value), {
    onSuccess(res){
      if(res['code']==200){
        getSubjects()
        closeCreateDialog()
        alert('学科创建成功！')
      }else{
        alert('创建失败，请重试')
      }
    }
  })
}


const closeBatchCreateDialog = () => {
  batchCreateDialogRef.value?.close()
}

// ====== 编辑 ======
const openEditDialog = (subject: Subject) => {
  editingForm.value = { ...subject } // 深拷贝
  editDialogRef.value?.showModal()
}

const saveEdit = async () => {
  if (!isEditFormValid.value) return

  // try {
  //   await updateSubjectAPI(editingForm.value)
  //   await getSubjects()
  //   closeEditDialog()
  //   alert('学科更新成功！')
  // } catch (err) {
  //   alert('更新失败，请重试')
  // }
}

const closeEditDialog = () => {
  editDialogRef.value?.close()
}

// ====== 删除 ======
const handleDelete = async (id: number) => {
  if (!confirm('确定删除该学科？')) return

  useRequest(()=>deleteSubjectsAPI([id]), {
    onSuccess(res){
      if(res['code']==200){
        getSubjects()
      }else{
        alert('删除失败')
      }
    }
  })
}

// ====== 批量删除 ======
const isAllSelected = computed(() => {
  return subjects.value.length > 0 && selectedIds.value.length === subjects.value.length
})

const toggleSelect = (id: number) => {
  const idx = selectedIds.value.indexOf(id)
  if (idx === -1) {
    selectedIds.value.push(id)
  } else {
    selectedIds.value.splice(idx, 1)
  }
}

const toggleSelectAll = () => {
  if (isAllSelected.value) {
    selectedIds.value = []
  } else {
    selectedIds.value = subjects.value.map(s => s.subjectId)
  }
}

const enterBatchMode = () => {
  showCheckboxes.value = true
  selectedIds.value = []
}

const exitBatchMode = () => {
  showCheckboxes.value = false
  selectedIds.value = []
}

const handleBatchDelete = async () => {
  if (selectedIds.value.length === 0) return
  if (!confirm(`确定删除选中的 ${selectedIds.value.length} 个学科？`)) return

  useRequest(()=>deleteSubjectsAPI(selectedIds.value), {
    onSuccess(res){
      if(res['code']==200){
        getSubjects()
        exitBatchMode()
        alert('删除成功')
      }else{
        alert('删除失败')
      }
    }
  })
}

// 批量创建表单（公共配置）
const batchForm = ref({
  gradeLevel: 1, // 默认小学
  status: 1      // 默认启用
})

// 预览计数
const previewCount = ref(0)

// 实时更新预览
const updatePreview = () => {
  const lines = batchInput.value
      .split('\n')
      .map(s => s.trim())
      .filter(s => s)
  previewCount.value = lines.length
}

// 获取年级标签
const getGradeLabel = (value: number | null): string => {
  const opt = gradeOptions.find(o => Number(o.value) === value)
  return opt ? opt.label : '未选择'
}

// 校验是否可提交
const isBatchValid = computed(() => {
  return (
      batchForm.value.gradeLevel != null &&
      previewCount.value > 0
  )
})

// 批量项：每行一个对象
const batchItems = ref([
  { subjectName: '', subjectCode: '', gradeLevel: 1 | null, status: 1 }
])

const addBatchItem = () => {
  batchItems.value.push({
    subjectName: '',
    subjectCode: '',
    gradeLevel: null,
    status: 1
  })
}

const removeBatchItem = (index: number) => {
  if (batchItems.value.length <= 1) return
  batchItems.value.splice(index, 1)
}

// 有效条目数（名称 + 年级非空）
const validItemCount = computed(() => {
  return batchItems.value.filter(
      item => item.subjectName.trim() && item.gradeLevel != null
  ).length
})

// 是否可提交
const isBatchInputValid = computed(() => {
  return validItemCount.value > 0
})

// 自动生成编码（如果未提供）
const generateCode = (name: string): string => {
  return name.replace(/[^a-zA-Z0-9\u4e00-\u9fa5]/g, '').slice(0, 6).toUpperCase() + '001'
}

// 批量创建
const createBatch = async () => {
  const subjectsToCreate = batchItems.value
      .filter(item => item.subjectName.trim() && item.gradeLevel != null)
      .map(item => ({
        subjectName: item.subjectName.trim(),
        subjectCode: item.subjectCode.trim() || generateCode(item.subjectName.trim()),
        gradeLevel: item.gradeLevel!,
        status: item.status ?? 1,
        sortOrder: 1
      }))

  if (subjectsToCreate.length === 0) return


  useRequest(()=>importSubjectsAPI(subjectsToCreate),{
    onSuccess(res){
      if(res['code']==200){
        getSubjects()
        closeBatchCreateDialog()
        alert('学科批量创建成功！')
      }else{
        alert('创建失败，请重试')
      }
    }
  })
}

// 打开弹窗
const openBatchCreateDialog = () => {
  batchItems.value = [
    { subjectName: '', subjectCode: '', gradeLevel: null, status: 1 }
  ]
  batchCreateDialogRef.value?.showModal()
}

// ====== 分页 ======
const prevPage = () => {
  if (pageInfo.value.pageNum > 1) {
    pageInfo.value.pageNum--
    getSubjects()
  }
}

const nextPage = () => {
  if (pageInfo.value.pageNum < totalPage.value) {
    pageInfo.value.pageNum++
    getSubjects()
  }
}

// ====== 工具函数 ======
const formatDate = (timestamp: number | string): string => {
  const date = typeof timestamp === 'number'
      ? new Date(timestamp)
      : new Date(timestamp)

  if (isNaN(date.getTime())) {
    return '—'
  }

  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}
</script>