<template>
  <div>
    <!-- Modal -->
    <div v-if="open" class="modal modal-open">
      <div class="modal-box w-11/12 max-w-4xl text-base">
        <h3 class="font-bold text-lg mb-4">智能组卷</h3>

        <!-- 基本信息 -->
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
                  placeholder="例如：Java 高级面试卷"
              />
            </div>

            <div>
              <label class="label">
                <span class="label-text">学科 *</span>
              </label>
              <select v-model="form.subjectId" class="select select-bordered w-full text-base">
                <option :value="null">请选择学科</option>
                <option v-for="sub in subjects" :key="sub.subjectId" :value="sub.subjectId">
                  {{ sub.subjectName }}
                </option>
              </select>
            </div>

            <div>
              <label class="label">
                <span class="label-text">试卷总分（默认 100）</span>
              </label>
              <input
                  v-model.number="form.totalScore"
                  type="number"
                  min="1"
                  class="input input-bordered w-full text-base"
              />
            </div>

            <div>
              <label class="label">
                <span class="label-text">总题数（可选）</span>
              </label>
              <input
                  v-model.number="form.totalQuestions"
                  type="number"
                  min="1"
                  class="input input-bordered w-full text-base"
                  placeholder="可留空"
              />
            </div>
          </div>
        </div>

        <!-- 进度条 -->
        <div v-if="isGenerating" class="mt-4 p-4 bg-base-100 rounded-lg border">
          <div class="flex items-center justify-between mb-2">
        <span class="text-sm font-medium text-gray-700">
          {{ progress?.message || '正在处理...' }}
        </span>
            <span class="text-xs text-gray-500">{{ progress?.progress || 0 }}%</span>
          </div>
          <progress
              class="progress progress-success w-full h-2"
              :value="progress?.progress || 0"
              max="100"
          ></progress>

          <!-- 可选：显示详细信息 -->
          <div v-if="progress?.details" class="mt-2 text-xs text-gray-500">
            {{ progress.details }}
          </div>
        </div>

        <!-- 题型要求 -->
        <div v-else class="mb-6">
          <div class="flex justify-between items-center mb-3">
            <h4 class="font-semibold">题型要求</h4>
            <button class="btn btn-sm btn-outline" @click="addQuestionType">
              + 添加题型
            </button>
          </div>

          <div v-if="form.questionTypes.length === 0" class="text-gray-500 py-2">
            点击“添加题型”开始配置
          </div>

          <div v-for="(qt, index) in form.questionTypes" :key="index" class="border rounded-lg p-4 mb-3 bg-base-100">
            <div class="flex justify-between items-start mb-2">
              <h5 class="font-medium">题型 {{ index + 1 }}</h5>
              <button class="btn btn-xs btn-error" @click="removeQuestionType(index)">删除</button>
            </div>

            <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
              <div>
                <label class="label">
                  <span class="label-text">题型 *</span>
                </label>
                <select v-model="qt.typeId" class="select select-bordered w-full text-base">
                  <option :value="null">请选择题型</option>
                  <option v-for="t in questionTypes" :key="t.typeId" :value="t.typeId">
                    {{ t.typeName }}
                  </option>
                </select>
              </div>

              <div>
                <label class="label">
                  <span class="label-text">题目数量 *</span>
                </label>
                <input
                    v-model.number="qt.count"
                    type="number"
                    min="1"
                    class="input input-bordered w-full text-base"
                />
              </div>

              <div>
                <label class="label">
                  <span class="label-text">难度（1-5，可选）</span>
                </label>
                <select v-model="qt.difficulty" class="select select-bordered w-full text-base">
                  <option :value="null">不限</option>
                  <option :value="1">1 - 容易</option>
                  <option :value="2">2</option>
                  <option :value="3">3 - 中等</option>
                  <option :value="4">4</option>
                  <option :value="5">5 - 困难</option>
                </select>
              </div>

              <div>
                <label class="label">
                  <span class="label-text">知识点标签（用英文逗号分隔）</span>
                </label>
                <input
                    v-model="tagInput[index]"
                    @blur="updateTags(index)"
                    type="text"
                    class="input input-bordered w-full text-base"
                    placeholder="如：HashMap,volatile,线程池"
                />
              </div>

              <div class="md:col-span-2">
                <label class="label">
                  <span class="label-text">自然语言描述（可选）</span>
                </label>
                <textarea
                    v-model="qt.description"
                    class="textarea textarea-bordered w-full text-base"
                    placeholder="例如：希望考察并发编程中的锁机制"
                ></textarea>
              </div>
            </div>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="modal-action">
          <button class="btn" @click="open = false">取消</button>
          <button class="btn btn-primary" :disabled="isGenerating || !canSubmit" @click="handleSubmit">
            <span v-if="isGenerating" class="loading loading-spinner"></span>
            {{ isGenerating ? '正在生成...' : '生成试卷'}}
          </button>
        </div>
      </div>

      <div class="modal-backdrop cursor-pointer" @click="open = false"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {ref, computed, watch, onMounted} from 'vue'
import {generateExamPapersAPI, getPaperGenerateProgressAPI} from '../../apis'
import { useRequest } from "vue-hooks-plus"; // 请替换为你的实际 API

const props = defineProps<{
  open: boolean
  questionTypes: any[]
  subjects: any[]
}>()
const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'success'): void
}>()

const open = ref(props.open)
watch(() => props.open, (val) => (open.value = val))
watch(open, (val) => emit('update:open', val))

// 模拟选项数据（实际应从 API 获取）
const subjectOptions = ref([
  { id: 1, name: 'Java' },
  { id: 2, name: 'Python' },
  { id: 3, name: '前端开发' }
])

// 表单数据
interface QuestionTypeRequirement {
  typeId: number | null
  count: number
  difficulty: number | null
  tags: string[]
  description: string
}

const form = ref({
  paperName: '',
  subjectId: null as number | null,
  totalScore: 100,
  totalQuestions: null as number | null,
  questionTypes: [] as QuestionTypeRequirement[]
})

// 用于 tags 输入的临时变量（每个题型一个）
const tagInput = ref<string[]>([])

// 初始化 tagInput 长度
watch(
    () => form.value.questionTypes.length,
    (newLen) => {
      while (tagInput.value.length < newLen) {
        tagInput.value.push('')
      }
      if (tagInput.value.length > newLen) {
        tagInput.value = tagInput.value.slice(0, newLen)
      }
    },
    { immediate: true }
)

onMounted(()=>{
  taskId.value = localStorage.getItem("generatePaperTaskId");
  if(taskId.value !== null && taskId.value !== ""){
    isGenerating.value = true
    startPolling()
  }
})

const isGenerating = ref<boolean>(false)

const pollingInterval = ref<number>(2000); // 默认2秒
let pollTimer = null;  // 定时器对象
const taskId = ref<string | null>('');
const progress = ref(null);

// 开始轮询
const startPolling = () => {
  checkProgress();
  pollTimer = setInterval(checkProgress, pollingInterval.value);
}

// 停止轮询
const stopPolling = () => {
  if (pollTimer) {
    clearInterval(pollTimer);
    pollTimer = null;
  }
}

// 查看进度
const checkProgress = () => {

  if(!isGenerating.value) return

  getProgress()
}

const {run: getProgress} = useRequest(()=> getPaperGenerateProgressAPI(taskId.value),{
  onSuccess(res){
    if(res['code']==200){

      progress.value = res['data'];

      if (progress.value.status === 'completed') {
        isGenerating.value = false

        stopPolling();
        window.removeEventListener('visibilitychange', handleVisibilityChange)
        localStorage.removeItem('generatePaperTaskId')

        emit("success")
        open.value = false
        // 重置表单
        form.value = {
          paperName: '',
          subjectId: null,
          totalScore: 100,
          totalQuestions: null,
          questionTypes: []
        }
        tagInput.value = []

        alert("组卷成功")

      }else if(progress.value.status === 'failed'){
        stopPolling()
        isGenerating.value = false
        localStorage.removeItem('generatePaperTaskId')
        window.removeEventListener('visibilitychange', handleVisibilityChange)
        alert('组卷失败')
      }
    }
  },

  onError(err){
    alert('轮询失败:' + err);
    stopPolling()
    window.removeEventListener('visibilitychange', handleVisibilityChange)
    isGenerating.value = false
  },

  manual: true,
})

const handleVisibilityChange = () => {
  if (!isGenerating.value) return;

  if (document.hidden) {
    // 页面隐藏：降低频率
    stopPolling();
    pollingInterval.value = 10000; // 10秒
    setTimeout(startPolling, pollingInterval.value); // 延迟启动
  } else {
    // 页面恢复：立即查一次 + 恢复1秒频率
    stopPolling();
    pollingInterval.value = 2000;
    startPolling();
  }
}

// 更新 tags 数组
const updateTags = (index: number) => {
  const input = tagInput.value[index]?.trim() || ''
  if (input === '') {
    form.value.questionTypes[index].tags = []
  } else {
    form.value.questionTypes[index].tags = input
        .split(',')
        .map((s) => s.trim())
        .filter((s) => s !== '')
  }
}

// 添加题型
const addQuestionType = () => {
  form.value.questionTypes.push({
    typeId: null,
    count: 1,
    difficulty: null,
    tags: [],
    description: ''
  })
  tagInput.value.push('')
}

// 删除题型
const removeQuestionType = (index: number) => {
  form.value.questionTypes.splice(index, 1)
  tagInput.value.splice(index, 1)
}

// 校验是否可提交
const canSubmit = computed(() => {
  if (!form.value.paperName.trim() || form.value.subjectId === null) return false
  if (form.value.questionTypes.length === 0) return false

  return form.value.questionTypes.every(
      (qt) => qt.typeId !== null && qt.count > 0
  )
})

// 提交
const handleSubmit = async () => {
  if (!canSubmit.value) return

  // 确保所有 tags 已同步
  form.value.questionTypes.forEach((_, i) => updateTags(i))

  useRequest(()=> generateExamPapersAPI(form.value), {
    onBefore(){
      isGenerating.value = true
    },
    onSuccess(res){
      if(res['code']==200){
        console.log('taskId：：', res['data'])
        taskId.value = res['data']
        localStorage.setItem('generatePaperTaskId', res['data'])
        startPolling()
        document.addEventListener('visibilitychange', handleVisibilityChange);
      }else{
        alert('生成失败：' + (res['msg'] || '未知错误'))
      }
    },

    onError(err){
      alert('生成失败：' + (err['msg'] || '未知错误'))
    }
  })
  }
  </script>