<!-- 导入题目模态框 -->
<template>
  <dialog id="importQuestionDialog" ref="importDialogRef" class="modal">
    <div class="modal-box w-11/12 max-w-2xl">
      <h3 class="font-bold text-lg mb-4">📁 导入题目（.docx 格式）</h3>

      <!-- 说明文字 -->
      <div class="alert alert-info mb-4 text-sm">
        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" class="stroke-current flex-shrink-0 w-6 h-6"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
        <span>请确保文档中每道题以 <code>科目-题型-编号</code> 开头（例如：<code>Java-单选题-001</code>），系统将自动识别并分类。</span>
      </div>

      <!-- 文件上传区 -->
      <div
          class="border-2 border-dashed border-base-300 rounded-lg p-6 text-center cursor-pointer transition hover:bg-base-200"
          @dragover.prevent
          @drop.prevent="handleDrop"
          @click="triggerFileInput"
      >
        <input
            ref="fileInputRef"
            type="file"
            accept=".docx"
            class="hidden"
            @change="handleFileSelect"
        />
        <div v-if="!selectedFile" class="space-y-2">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-10 w-10 mx-auto text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12" />
          </svg>
          <p class="text-gray-600">拖拽 .docx 文件到这里，或点击选择</p>
          <p class="text-xs text-gray-500">仅支持 Microsoft Word (.docx) 格式，单个文件 ≤20MB</p>
        </div>
        <div v-else class="flex items-center justify-between">
          <div class="flex items-center gap-2">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-green-500" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
            </svg>
            <span class="font-medium">{{ selectedFile.name }}</span>
            <span class="text-xs text-gray-500">({{ formatFileSize(selectedFile.size) }})</span>
          </div>
          <button class="btn btn-sm btn-circle btn-ghost" @click="clearFile">✕</button>
        </div>
      </div>

      <!-- 进度状态区 -->
      <div v-if="isUploading" class="mt-4 p-4 bg-base-100 rounded-lg border">
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

      <!-- 操作按钮 -->
      <div class="modal-action mt-6">
        <button class="btn" @click="closeImportDialog">取消</button>
        <button
            class="btn btn-primary"
            :disabled="!selectedFile || isUploading"
            @click="uploadFile"
        >
        <span v-if="isUploading">
          <span class="loading loading-spinner loading-xs mr-2"></span> 正在解析...
        </span>
          <span v-else>开始导入</span>
        </button>
      </div>

      <!-- 解析结果预览（可选） -->
      <div v-if="parseResult" class="mt-4 pt-4 border-t">
        <h4 class="font-semibold mb-2">📊 解析结果预览</h4>
        <div class="text-sm space-y-1">
          <p>✅ 成功识别 <span class="font-bold">{{ parseResult.successCount }}</span> 道题目</p>
          <p v-if="parseResult.warningCount > 0" class="text-warning">⚠️ <span class="font-bold">{{ parseResult.warningCount }}</span> 道题目格式不规范</p>
          <p v-if="parseResult.errorCount > 0" class="text-error">❌ <span class="font-bold">{{ parseResult.errorCount }}</span> 道题目无法识别</p>
        </div>
      </div>
    </div>
    <form method="dialog" class="modal-backdrop">
      <button @click="closeImportDialog">close</button>
    </form>
  </dialog>
</template>

<script setup lang="ts">
import {onMounted, ref} from "vue"
import {getImportProgressAPI, importQuestionsAPI} from "../../apis"
import { useRequest } from "vue-hooks-plus/es";

onMounted(()=>{
  taskId.value = localStorage.getItem("importQuestionTaskId");
  if(taskId.value !== null && taskId.value !== ""){
    isUploading.value = true
    startPolling()
  }
})

const varemit = defineEmits(["close", "getQuestions"])

// ====== 导入相关 refs ======
const importDialogRef = ref<HTMLDialogElement | null>(null)
const fileInputRef = ref<HTMLInputElement | null>(null)
const selectedFile = ref<File | null>(null)
const isUploading = ref(false)
const parseResult = ref<{ successCount: number; warningCount: number; errorCount: number } | null>(null)

const closeImportDialog = () => {
  varemit("close")
}

// 触发文件选择
const triggerFileInput = () => {
  fileInputRef.value?.click()
}

// 处理文件选择
const handleFileSelect = (e: Event) => {
  const input = e.target as HTMLInputElement
  if (input.files && input.files[0]) {
    const file = input.files[0]
    if (!file.name.endsWith('.docx')) {
      alert('仅支持 .docx 文件')
      return
    }
    if (file.size > 20 * 1024 * 1024) {
      alert('文件大小不能超过 20MB')
      return
    }
    selectedFile.value = file
  }
}

// 处理拖拽上传
const handleDrop = (e: DragEvent) => {
  const files = e.dataTransfer?.files
  if (files && files[0]) {
    const file = files[0]
    if (!file.name.endsWith('.docx')) {
      alert('仅支持 .docx 文件')
      return
    }
    if (file.size > 10 * 1024 * 1024) {
      alert('文件大小不能超过 20MB')
      return
    }
    selectedFile.value = file
  }
}

// 清空文件
const clearFile = () => {
  selectedFile.value = null
  if (fileInputRef.value) fileInputRef.value.value = ''
}

// 格式化文件大小
const formatFileSize = (bytes: number): string => {
  if (bytes < 1024) return bytes + ' B'
  else if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  else return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

// 上传并解析文件
const uploadFile = async () => {
  if (!selectedFile.value) return

  try {
    // 获取 Base64 字符串
    let base64String = await fileToBase64(selectedFile.value)
    base64String = base64String.split(',')[1]
    // console.log('文件 Base64:', base64String)

    useRequest(()=> importQuestionsAPI({file: base64String}),{
      onBefore(){
        isUploading.value = true
      },
      onSuccess(res){
        if(res['code']==200){

          taskId.value = res['data']

          localStorage.setItem('importQuestionTaskId', res['data'])

          startPolling()

          // 监听页面可见性变化
          document.addEventListener('visibilitychange', handleVisibilityChange);

        }else{
          isUploading.value = false
          alert('题目导入失败！')
        }
      },
      onError(error){
        isUploading.value = false
        alert('题目导入失败！')
      },
    })

  } catch (error) {
    console.error('Base64 转换失败:', error)
  } finally {
  }
}

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

  if(!isUploading.value) return

  useRequest(()=> getImportProgressAPI(taskId.value),{
    onSuccess(res){
      if(res['code']==200){

        progress.value = res['data'];

        if (progress.value.status === 'completed') {
          stopPolling();
          window.removeEventListener('visibilitychange', handleVisibilityChange)
          localStorage.removeItem('importQuestionTaskId')
          isUploading.value = false
          varemit("getQuestions")
          closeImportDialog()
          alert('导入成功')

        }else if(progress.value.status === 'failed'){
          stopPolling()
          isUploading.value = false
          localStorage.removeItem('importQuestionTaskId')
          window.removeEventListener('visibilitychange', handleVisibilityChange)
          alert('导入失败')
        }
      }else{
        alert('轮询失败:' + res['msg']);
        stopPolling()
        window.removeEventListener('visibilitychange', handleVisibilityChange)
        isUploading.value = false
      }
    },

    onError(err){
      alert('轮询失败:' + err);
      stopPolling()
      window.removeEventListener('visibilitychange', handleVisibilityChange)
      isUploading.value = false
    }
  })
}

const handleVisibilityChange = () => {
  if (!isUploading.value) return;

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

const fileToBase64 = (file: File): Promise<string> => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.readAsDataURL(file)

    reader.onload = () => {
      resolve(reader.result as string)
    }

    reader.onerror = (error) => {
      reject(error)
    }
  })
}
</script>