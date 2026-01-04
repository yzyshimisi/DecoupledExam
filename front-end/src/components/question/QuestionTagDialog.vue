<!-- src/components/QuestionTagManagerDialog.vue -->
<template>
  <dialog ref="dialogRef" class="modal">
    <div class="modal-box w-full max-w-md">
      <h3 class="font-bold text-lg mb-4">题目标签管理</h3>

      <!-- 添加新标签 -->
      <div class="flex gap-2 mb-4">
        <input
            v-model="newTagInput"
            @keyup.enter="addNewTag"
            type="text"
            placeholder="输入新标签，回车添加"
            class="input input-bordered flex-1"
        />
        <button
            :disabled="!newTagInput.trim()"
            class="btn btn-primary btn-square"
            @click="addNewTag"
        >
          +
        </button>
      </div>

      <!-- 标签列表（支持编辑 + 勾选） -->
      <div class="space-y-2 max-h-60 overflow-y-auto pr-1 border rounded p-2 bg-base-100">
        <div
            v-for="(tag, index) in localTags"
            :key="index"
            class="flex items-center gap-2 p-1 hover:bg-base-200 rounded transition"
        >
          <!-- 批量选择复选框 -->
          <input
              type="checkbox"
              :checked="selectedIndices.has(index)"
              @change="toggleSelect(index)"
              class="checkbox checkbox-xs"
          />

          <!-- 可编辑标签 -->
          <div v-if="editingIndex === index" class="flex-1 flex gap-1">
            <input
                v-model="editValue"
                @blur="saveEdit(index)"
                @keyup.enter="saveEdit(index)"
                ref="editInputRef"
                type="text"
                class="input input-xs input-bordered flex-1"
            />
            <button class="btn btn-xs btn-ghost" @click="cancelEdit">×</button>
          </div>

          <!-- 显示模式 -->
          <div v-else class="flex-1 cursor-pointer" @dblclick="startEdit(index)">
            {{ tag.tagName }}
          </div>
        </div>

        <div v-if="localTags.length === 0" class="text-gray-500 text-sm py-2 text-center">
          暂无标签
        </div>
      </div>

      <!-- 批量操作栏 -->
      <div v-if="selectedIndices.size > 0" class="mt-3 flex justify-between items-center">
        <span class="text-sm text-info">{{ selectedIndices.size }} 项已选中</span>
        <button class="btn btn-error btn-xs" @click="deleteSelected">删除选中</button>
      </div>

      <!-- 底部按钮 -->
      <div class="modal-action mt-4">
        <button class="btn" @click="close">取消</button>
        <button class="btn btn-primary" @click="close">确定</button>
      </div>
    </div>
    <form method="dialog" class="modal-backdrop">
      <button @click="close">close</button>
    </form>
  </dialog>
</template>

<script setup lang="ts">
import { ref, watch, nextTick } from 'vue'
import { useRequest } from "vue-hooks-plus";
import {addQuestionTagsAPI, deleteQuestionTagsAPI, getQuestionTagsAPI, modifyQuestionTagsAPI} from "../../apis"
import { cloneDeep } from 'lodash-es'

interface TagItem {
  tagName: string
}

const props = defineProps<{
  modelValue: boolean
  questionId: number
  tags: TagItem[] // 初始标签列表，如 [{ tagName: '继承' }, ...]
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

const dialogRef = ref<HTMLDialogElement | null>(null)
const newTagInput = ref('')
const localTags = ref<TagItem[]>([])
const selectedIndices = ref<Set<number>>(new Set())
const editingIndex = ref<number | null>(null)
const editValue = ref('')
const editInputRef = ref<HTMLInputElement | null>(null)

const cancelEdit = () => {
  editingIndex.value = null
  editValue.value = ''
}

// 初始化本地标签
watch(
    () => props.tags,
    (newTags) => {
      localTags.value = cloneDeep(newTags)
      selectedIndices.value = new Set()
      cancelEdit()
    },
    { immediate: true }
)

// 控制对话框显示
watch(
    () => props.modelValue,
    (isOpen) => {
      if (isOpen) {
        dialogRef.value?.showModal()
      } else {
        dialogRef.value?.close()
      }
    },
    { immediate: true }
)

// --- 增：添加新标签 ---
const addNewTag = () => {
  const name = newTagInput.value.trim()
  if (!name) return

  newTagInput.value = ''

  let data = [{
    tagName: name,
    questionId: props.questionId
  }]

  useRequest(()=>addQuestionTagsAPI(data),{
    onSuccess(res) {
      if (res['code'] === 200) {
        alert('添加成功')
        getTags()
      }
    }
  })
}

// --- 改：编辑标签 ---
const startEdit = (index: number) => {
  editingIndex.value = index
  editValue.value = localTags.value[index].tagName
  nextTick(() => {
    editInputRef.value?.focus()
  })
}

const saveEdit = (index) => {
  const name = editValue.value.trim()
  if (name && editingIndex.value !== null) {
    // 去重（排除自身）
    const isDuplicate = localTags.value.some(
        (t, i) => i !== editingIndex.value && t.tagName === name
    )

    localTags.value[index].tagName = editValue.value

    if (!isDuplicate) {
      let data = {
        id: localTags.value[editingIndex.value]['id'],
        questionId: props.questionId,
        tagName: name
      }
      useRequest(()=>modifyQuestionTagsAPI(data), {
        onSuccess(res) {
          if (res['code'] === 200) {
            getTags()
            alert('修改成功')
          }
        }
      })
    }
  }
  cancelEdit()
}

// --- 删：单个 & 批量 ---
const toggleSelect = (index: number) => {
  const set = new Set(selectedIndices.value)
  if (set.has(index)) {
    set.delete(index)
  } else {
    set.add(index)
  }
  selectedIndices.value = set
}

const deleteSelected = () => {
  const indicesToDelete = Array.from(selectedIndices.value).sort((a, b) => b - a) // 从后往前删

  let data = []

  for (let i = 0; i < indicesToDelete.length; i++) {
    data.push(localTags.value[indicesToDelete[i]]['id'])
   }

  useRequest(()=>deleteQuestionTagsAPI(data),{
    onSuccess(res) {
      if (res['code'] === 200) {
        getTags()
        alert('删除成功')
      }
    }
  })

  selectedIndices.value = new Set()
}

const getTags = () => {
  useRequest(()=>getQuestionTagsAPI({questionId: props.questionId}),{
    onSuccess(res) {
      if (res['code'] === 200) {
        localTags.value = res['data']
      }
    }
  })
}

const close = () => {
  emit('update:modelValue', false)
}
</script>