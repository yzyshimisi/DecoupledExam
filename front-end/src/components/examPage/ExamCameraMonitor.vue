<template>
  <div class="exam-camera-monitor w-full h-full flex flex-col bg-base-200 rounded-lg overflow-hidden shadow-md">
    <!-- 标题 -->
    <div class="bg-base-300 px-3 py-2 text-sm font-medium border-b border-base-300 flex justify-between items-center">
      <span class="text-base-content">实时监考</span>
      <span v-if="isRecordingSnippet" class="text-xs text-error flex items-center">
        <span class="w-2 h-2 bg-error rounded-full mr-1 animate-pulse"></span>
        录制中...
      </span>
    </div>

    <!-- 视频区域 -->
    <div class="relative flex-1 bg-neutral flex items-center justify-center overflow-hidden">
      <video
          ref="videoRef"
          class="w-full h-full object-cover"
          autoplay
          muted
          playsinline
      ></video>

      <div
          v-if="!isCameraActive"
          class="absolute inset-0 flex flex-col items-center justify-center bg-black/60 text-base-content/50 backdrop-blur-sm"
      >
        <svg class="w-8 h-8 mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 10l4.553-2.276A1 1 0 0121 8.618v6.764a1 1 0 01-1.447.894L15 14M5 18h8a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v8a2 2 0 002 2z" />
        </svg>
        <span class="text-xs">监考未启动</span>
      </div>
    </div>

    <!-- 控制按钮（仅用于调试，正式环境可隐藏） -->
    <div class="p-2 bg-base-100 flex gap-2 border-t border-base-200" v-if="showControls">
      <button
          @click="toggleMonitoring"
          class="btn btn-xs"
          :class="isMonitoring ? 'btn-error' : 'btn-primary'"
      >
        {{ isMonitoring ? '停止监考' : '开始监考' }}
      </button>
    </div>

    <!-- 监控数据面板 -->
    <div class="p-3 bg-base-100 border-t border-base-200">
      <div class="space-y-2 text-xs">
        <!-- 网络状态 -->
        <div class="flex justify-between">
          <span class="text-base-content/60">网络</span>
          <span :class="networkStatus === 'good' ? 'text-success' : networkStatus === 'warning' ? 'text-warning' : 'text-error'">
            {{ networkStatus }}
          </span>
        </div>

        <!-- 设备状态 -->
        <div class="flex justify-between">
          <span class="text-base-content/60">摄像头</span>
          <span :class="isCameraActive ? 'text-success' : 'text-error'">
            {{ isCameraActive ? '已连接' : '未连接' }}
          </span>
        </div>

        <!-- 录制统计 -->
        <div class="flex justify-between">
          <span class="text-base-content/60">已录制</span>
          <span class="text-primary">{{ recordedCount }} 段</span>
        </div>
      </div>

      <!-- 行为分析图表（模拟） -->
      <div class="mt-3 pt-2 border-t border-base-200">
        <div class="flex justify-between text-xs text-base-content/60 mb-1">
          <span>专注度趋势</span>
          <span>{{ attentionScore }}%</span>
        </div>
        <div class="h-8 w-full bg-base-200 rounded relative overflow-hidden">
          <div class="absolute bottom-0 left-0 h-1 bg-primary transition-all duration-500" :style="{ width: `${attentionScore}%` }"></div>
        </div>
      </div>

      <!-- 异常提醒 -->
      <div v-if="hasAlert" class="mt-2 p-2 bg-error/10 text-error text-xs rounded border border-error/20 flex items-center">
        <svg class="w-3 h-3 mr-1 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/></svg>
        检测到异常行为，请注意！
      </div>
    </div>

  </div>
</template>


<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'

const props = withDefaults(defineProps<{
  interval?: number       // 多久录一次（秒），默认 30
  duration?: number       // 每次录多久（秒），默认 5
  autoStart?: boolean     // 是否自动开始监考
  showControls?: boolean  // 是否显示控制按钮（调试用）
  attentionScore?: number      // 专注度阈值，默认 85
}>(), {
  interval: 30,
  duration: 5,
  autoStart: true,
  showControls: false,
  attentionScore: 85
})

const emit = defineEmits<{
  (e: 'capture', base64: string): void
}>()

// refs
const videoRef = ref<HTMLVideoElement | null>(null)

// 状态
const isCameraActive = ref(false)
const isMonitoring = ref(false)
const isRecordingSnippet = ref(false)

const isRecording = ref(false)
const recordedCount = ref(0)
const hasAlert = ref(false)
const networkStatus = ref('good') // good / warning / poor

// 媒体
let mediaStream: MediaStream | null = null
let mediaRecorder: MediaRecorder | null = null
let captureIntervalId: number | null = null
let stopRecordingTimeout: number | null = null

// ========================
// 启动摄像头（仅视频）
// ========================
const startCamera = async () => {
  if (isCameraActive.value) return

  try {
    const constraints: MediaStreamConstraints = {
      video: { width: { ideal: 640 }, height: { ideal: 480 } },
      audio: false
    }

    mediaStream = await navigator.mediaDevices.getUserMedia(constraints)

    if (videoRef.value) {
      videoRef.value.srcObject = mediaStream
    }

    isCameraActive.value = true
  } catch (err: any) {
    console.error('摄像头启动失败:', err)
    alert(`无法访问摄像头：${err.message}`)
  }
}

// ========================
// 停止摄像头
// ========================
const stopCamera = () => {
  if (mediaStream) {
    mediaStream.getTracks().forEach(t => t.stop())
    mediaStream = null
  }
  if (videoRef.value) {
    videoRef.value.srcObject = null
  }
  isCameraActive.value = false
}

// ========================
// 开始一次片段录制
// ========================
const startRecordingSnippet = () => {
  if (!mediaStream || isRecordingSnippet.value) return

  // 清理旧 recorder
  if (mediaRecorder) {
    mediaRecorder.ondataavailable = null
    mediaRecorder.onstop = null
    if (mediaRecorder.state !== 'inactive') {
      mediaRecorder.stop()
    }
  }

  // 查找支持的 MIME 类型
  const mimeTypes = [
    'video/webm;codecs=vp9',
    'video/webm;codecs=vp8',
    'video/webm',
    'video/mp4'
  ]
  const mimeType = mimeTypes.find(type => MediaRecorder.isTypeSupported(type)) || 'video/webm'

  recordedChunks.length = 0
  mediaRecorder = new MediaRecorder(mediaStream, { mimeType })

  mediaRecorder.ondataavailable = (event) => {
    if (event.data.size > 0) {
      recordedChunks.push(event.data)
    }
  }

  mediaRecorder.onstop = async () => {
    try {
      const blob = new Blob(recordedChunks, { type: mimeType })
      const base64 = await blobToBase64(blob)
      emit('capture', base64.split(',')[1]) // 👈 发送给父组件
    } catch (err) {
      console.error('片段转 Base64 失败:', err)
    } finally {
      isRecordingSnippet.value = false
    }
  }

  recordedChunks.length = 0
  mediaRecorder.start()
  isRecordingSnippet.value = true

  // 自动停止录制（duration 秒后）
  if (stopRecordingTimeout) clearTimeout(stopRecordingTimeout)
  stopRecordingTimeout = window.setTimeout(() => {
    if (mediaRecorder && mediaRecorder.state === 'recording') {
      mediaRecorder.stop()
    }
  }, props.duration * 1000)
}

// ========================
// Blob 转 Base64
// ========================
const recordedChunks: Blob[] = []
const blobToBase64 = (blob: Blob): Promise<string> => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(reader.result as string)
    reader.onerror = () => reject(reader.error)
    reader.readAsDataURL(blob)
  })
}

// ========================
// 启动/停止自动监考循环
// ========================
const startMonitoringLoop = () => {
  if (captureIntervalId) clearInterval(captureIntervalId)

  captureIntervalId = window.setInterval(() => {
    startRecordingSnippet()
  }, props.interval * 1000)

  isMonitoring.value = true
}

const stopMonitoringLoop = () => {
  if (captureIntervalId) {
    clearInterval(captureIntervalId)
    captureIntervalId = null
  }
  if (stopRecordingTimeout) {
    clearTimeout(stopRecordingTimeout)
    stopRecordingTimeout = null
  }
  if (mediaRecorder && mediaRecorder.state === 'recording') {
    mediaRecorder.stop()
  }
  isMonitoring.value = false
  isRecordingSnippet.value = false
}

// ========================
// 对外控制方法
// ========================
const toggleMonitoring = () => {
  if (isMonitoring.value) {
    stopMonitoringLoop()
  } else {
    startMonitoring()
  }
}

const startMonitoring = async () => {
  await startCamera()
  if (isCameraActive.value) {
    startMonitoringLoop()
  }
}

// ========================
// 生命周期 & 监听
// ========================
onMounted(() => {
  if (props.autoStart) {
    startMonitoring()
  }
})

onBeforeUnmount(() => {
  stopMonitoringLoop()
  stopCamera()
})

// 如果 props.autoStart 变化，重新控制状态
watch(() => props.autoStart, (newVal) => {
  if (newVal) {
    startMonitoring()
  } else {
    stopMonitoringLoop()
  }
})
</script>

<style scoped>
</style>