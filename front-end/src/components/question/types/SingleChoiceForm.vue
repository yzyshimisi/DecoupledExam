<template>
  <div class="space-y-6">
    <!-- 题目内容（富文本感：支持图文） -->
    <div>
      <label class="block text-base mb-2">题目内容 *</label>
      <textarea
          v-model="formData.title"
          ref="titleTextareaRef"
          class="textarea textarea-bordered w-full text-base"
          rows="6"
          placeholder="请输入题目内容..."
      ></textarea>

      <div class="mt-2">
        <input
            type="file"
            ref="fileInputRef"
            accept="image/*"
            multiple
            class="hidden"
            @change="handleImageUpload"
        />
        <button
            type="button"
            class="btn btn-sm btn-outline"
            @click="fileInputRef?.click?.()"
        >
          插入图片
        </button>
        <span class="text-xs text-gray-500 ml-2">
          图片将自动插入到题目中
        </span>
      </div>
    </div>

    <!-- 难度 & 学科 -->
    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
      <div>
        <label class="block text-base mb-2">难度等级</label>
        <select v-model.number="formData.difficulty" class="select select-bordered w-full">
          <option :value="1">1 - 入门</option>
          <option :value="2">2 - 简单</option>
          <option :value="3">3 - 中等</option>
          <option :value="4">4 - 困难</option>
          <option :value="5">5 - 挑战</option>
        </select>
      </div>
      <div>
        <label class="block text-base mb-2">所属学科</label>
        <select v-model.number="formData.subjectId" class="select select-bordered w-full">
          <option v-for="(value, index) in subjectList" :value="value['subjectId']">{{ value['subjectName'] }}</option>
        </select>
      </div>
    </div>

    <!-- 选项 -->
    <div>
      <div class="flex justify-between items-center mb-2">
        <label class="text-base">选项列表 *</label>
        <button
            type="button"
            class="btn btn-sm btn-ghost"
            :disabled="formData.options.length >= 7"
            @click="addOption"
        >
          + 添加选项
        </button>
      </div>
      <div class="space-y-3">
        <div v-for="(opt, index) in formData.options" :key="index" class="flex items-start gap-3">
          <div class="flex flex-col">
            <span class="text-sm font-mono">{{ labels[index] }}</span>
            <input
                v-model="opt.value"
                type="text"
                class="input input-bordered w-64 mt-1"
                placeholder="选项内容"
            />
          </div>
          <div class="mt-6">
            <label class="cursor-pointer label">
              <span class="label-text text-sm">正确</span>
              <input
                  v-model="correctIndex"
                  type="radio"
                  name="correctOption"
                  :value="index"
                  class="radio radio-primary radio-sm ml-2"
              />
            </label>
          </div>
          <button
              type="button"
              class="btn btn-sm btn-circle btn-ghost mt-6"
              @click="removeOption(index)"
              :disabled="formData.options.length <= 2"
          >
            ✕
          </button>
        </div>
      </div>
    </div>

    <!-- 答案说明 -->
    <div>
      <label class="block text-base mb-2">答案说明（可选）</label>
      <textarea
          v-model="formData.answerDesc"
          class="textarea textarea-bordered w-full"
          rows="2"
      ></textarea>
    </div>

    <!-- 解析 -->
    <div>
      <label class="block text-base mb-2">题目解析（可选）</label>
      <textarea
          v-model="formData.analysisDesc"
          class="textarea textarea-bordered w-full"
          rows="3"
      ></textarea>
    </div>

    <!-- 操作 -->
    <div class="flex justify-end gap-3 pt-4">
      <button type="button" class="btn" @click="onCancel">取消</button>
      <button type="button" class="btn btn-primary" @click="onSubmit">保存题目</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import {ref, watch, watchEffect} from 'vue'

const props = defineProps(['subjectList', "initialData"])

// ====== Emits ======
const emit = defineEmits<{
  (e: 'submit', payload: SubmitPayload): void
  (e: 'cancel'): void
}>()

// ====== Types ======
interface Option {
  value: string
  isCorrect: boolean
}

interface SubmitPayload {
  typeId: number
  title: string
  difficulty: number
  subjectId: number
  questionComponents: Array<{
    componentType: string
    content: string
    meta?: string
  }>
}

interface OptionItem {
  label: string
  value: string
  isCorrect: boolean
}

// ====== Data ======
const labels = ['A', 'B', 'C', 'D', 'E', 'F', 'G']

const formData = ref({
  title: '',
  difficulty: 1,
  subjectId: 1,
  options: [
    { value: '', isCorrect: false },
    { value: '', isCorrect: false }
  ] as Option[],
  answerDesc: '',
  analysisDesc: ''
})

const correctIndex = ref<number | null>(null)

// 初始化逻辑
watch(
    () => props.initialData,
    (data) => {
      if (!data) {
        // 创建模式
        formData.value = {
          title: '',
          difficulty: 1,
          subjectId: 1,
          options: [
            { value: '', isCorrect: false },
            { value: '', isCorrect: false }
          ],
          answerDesc: '',
          analysisDesc: ''
        }
        correctIndex.value = null // 重置
        return
      }

      // 编辑模式
      const { subjectId, difficulty, title, questionComponents } = data

      let options: OptionItem[] = []
      let answerDesc = ''
      let analysisDesc = ''

      for (const comp of questionComponents || []) {
        try {
          if (comp.componentType === 'option') {
            const parsed = JSON.parse(comp.content)
            options = parsed.options || []
          } else if (comp.componentType === 'answer') {
            const parsed = JSON.parse(comp.content)
            answerDesc = parsed.answerDesc || ''
          } else if (comp.componentType === 'analysis') {
            const parsed = JSON.parse(comp.content)
            analysisDesc = parsed.analysisDesc || ''
          }
        } catch (e) {
          console.warn('Failed to parse component content:', comp)
        }
      }

      // 补足至少2个选项
      while (options.length < 2) {
        options.push({
          label: String.fromCharCode(65 + options.length),
          value: '',
          isCorrect: false
        })
      }

      // 赋值表单
      formData.value = {
        title: title ?? '',
        difficulty: difficulty ?? 1,
        subjectId: subjectId ?? 1,
        options,
        answerDesc,
        analysisDesc
      }

      // 🔥 关键修复：根据 isCorrect 设置 correctIndex
      const correctIdx = options.findIndex(opt => opt.isCorrect)
      correctIndex.value = correctIdx >= 0 ? correctIdx : null
    },
    { immediate: true }
)

// Refs for DOM
const fileInputRef = ref<HTMLInputElement | null>(null)
const titleTextareaRef = ref<HTMLTextAreaElement | null>(null)

// Sync correct option
watchEffect(() => {
  formData.value.options.forEach((opt, i) => {
    opt.isCorrect = i === correctIndex.value
  })
})

// ====== Image Handling ======
function compressImage(file: File): Promise<string> {
  return new Promise((resolve) => {
    if (!file.type.startsWith('image/')) return resolve('')

    const img = new Image()
    img.onload = () => {
      const canvas = document.createElement('canvas')
      const ctx = canvas.getContext('2d')
      let width = img.width
      let height = img.height

      if (width > 800) {
        const ratio = 800 / width
        width = 800
        height *= ratio
      }

      canvas.width = width
      canvas.height = height
      ctx?.drawImage(img, 0, 0, width, height)

      const mime = file.type === 'image/png' ? 'image/png' : 'image/jpeg'
      resolve(canvas.toDataURL(mime, 0.85))
    }
    img.src = URL.createObjectURL(file)
  })
}

// Insert image tag at cursor or end
function insertImageToTitle(imgSrc: string) {
  const textarea = titleTextareaRef.value
  if (!textarea) {
    formData.value.title += `\n<img src="${imgSrc}" alt="题图" style="max-width:100%;">`
    return
  }

  const startPos = textarea.selectionStart
  const endPos = textarea.selectionEnd
  const before = formData.value.title.substring(0, startPos)
  const after = formData.value.title.substring(endPos)

  const imgTag = `<img src="${imgSrc}" alt="题图" style="max-width:100%;height:auto;">`
  formData.value.title = before + imgTag + after

  // Move cursor after inserted image
  setTimeout(() => {
    textarea.setSelectionRange(startPos + imgTag.length, startPos + imgTag.length)
  }, 0)
}

const handleImageUpload = async (event: Event) => {
  const files = (event.target as HTMLInputElement)?.files
  if (!files) return

  for (const file of Array.from(files)) {
    if (!file.type.startsWith('image/')) continue
    const base64 = await compressImage(file)
    if (base64) insertImageToTitle(base64)
  }

  // Reset input
  if (fileInputRef.value) fileInputRef.value.value = ''
}

// ====== Option Management ======
const addOption = () => {
  if (formData.value.options.length < labels.length) {
    formData.value.options.push({ value: '', isCorrect: false })
  }
}

const removeOption = (index: number) => {
  if (formData.value.options.length <= 2) return
  formData.value.options.splice(index, 1)
  if (correctIndex.value === index) correctIndex.value = null
  else if (correctIndex.value !== null && correctIndex.value > index) correctIndex.value--
}

// ====== Submit ======
const onSubmit = () => {
  const { title, difficulty, subjectId, options, answerDesc, analysisDesc } = formData.value

  if (!title.trim()) return alert('题目不能为空')
  if (options.some(opt => !opt.value.trim())) return alert('选项内容不能为空')
  if (correctIndex.value === null) return alert('请选择正确答案')

  const total = options.length
  const correctLabel = labels[correctIndex.value]

  const payload: SubmitPayload = {
    typeId: 1,
    title,
    difficulty,
    subjectId,
    questionComponents: [
      {
        componentType: 'option',
        content: JSON.stringify({
          options: options.map((opt, i) => ({
            label: labels[i],
            value: opt.value,
            isCorrect: opt.isCorrect
          })),
          allowMultiple: false
        }),
        meta: JSON.stringify({
          questionType: 'singleChoice',
          totalOptions: total
        })
      },
      {
        componentType: 'answer',
        content: JSON.stringify({
          correctOption: correctLabel,
          answerDesc: answerDesc || ''
        })
      },
      {
        componentType: 'analysis',
        content: JSON.stringify({
          analysisDesc: analysisDesc || ''
        })
      }
    ]
  }

  emit('submit', payload)
}

const onCancel = () => emit('cancel')
</script>