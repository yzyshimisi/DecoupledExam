<template>
<dialog id="cameraRecorderDia" class="modal">
  <div class="modal-box w-[60vw] max-w-[60vw]">
    <form method="dialog">
      <button class="btn btn-sm btn-circle btn-ghost absolute right-2 top-2">✕</button>
    </form>
    <div class="min-h-screen bg-base-200">
      <main class="container mx-auto px-4 py-8 max-w-5xl">
        <!-- 视频预览区 -->
        <div class="relative bg-base-300 rounded-xl overflow-hidden shadow-lg mb-6">
          <div class="aspect-video bg-gray-800 flex items-center justify-center">
            <video
                ref="videoRef"
                class="w-full h-full object-cover"
                autoplay
                muted
                playsinline
            ></video>

            <div v-if="!isCameraActive" class="absolute inset-0 flex flex-col items-center justify-center bg-gray-800/80">
              <svg class="w-16 h-16 text-gray-400 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 10l4.553-2.276A1 1 0 0121 8.618v6.764a1 1 0 01-1.447.894L15 14M5 18h8a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v8a2 2 0 002 2z" />
              </svg>
              <p class="text-gray-300 text-lg">摄像头未开启</p>
            </div>

            <!-- 录制指示器 -->
            <div v-if="isRecording" class="absolute top-4 left-4 flex items-center bg-red-500/90 backdrop-blur-sm px-3 py-1.5 rounded-full">
              <span class="w-3 h-3 bg-white rounded-full animate-pulse mr-2"></span>
              <span class="text-white text-sm font-medium">{{ formattedRecordingTime }}</span>
            </div>
          </div>

          <!-- 设备选择下拉框 -->
          <div class="absolute top-3 right-3 z-10">
            <select
                v-model="selectedCameraId"
                @change="switchCamera"
                class="bg-white/90 backdrop-blur-sm text-base-content px-3 py-1.5 rounded-lg border border-gray-300 shadow-sm focus:outline-none focus:ring-2 focus:ring-primary/50 text-sm"
            >
              <option value="">选择摄像头设备...</option>
              <option v-for="device in cameraDevices" :key="device.deviceId" :value="device.deviceId">
                {{ device.label || `摄像头 ${device.deviceId.slice(0, 8)}...` }}
              </option>
            </select>
          </div>
        </div>

        <!-- 控制面板 -->
        <div class="bg-base-100 rounded-xl shadow-md p-6 mb-8">
          <div class="flex flex-wrap gap-4 justify-center">
            <button
                @click="startCamera"
                class="btn btn-primary"
                :disabled="isCameraActive"
            >
              <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 10l4.553-2.276A1 1 0 0121 8.618v6.764a1 1 0 01-1.447.894L15 14M5 18h8a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v8a2 2 0 002 2z" />
              </svg>
              开启摄像头
            </button>

            <button
                @click="stopCamera"
                class="btn btn-neutral"
                :disabled="!isCameraActive"
            >
              <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18.364 18.364A9 9 0 005.636 5.636m12.728 12.728A9 9 0 015.636 5.636m12.728 12.728L5.636 5.636" />
              </svg>
              关闭摄像头
            </button>

            <button
                @click="toggleRecording"
                :class="isRecording ? 'btn btn-error' : 'btn btn-success'"
                :disabled="!isCameraActive"
            >
              <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path v-if="!isRecording" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" />
                <path v-else stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              {{ isRecording ? '停止录制' : '开始录制' }}
            </button>

            <button
                @click="takePhoto"
                class="btn btn-secondary"
                :disabled="!isCameraActive || isRecording"
            >
              <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z" />
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 13a3 3 0 11-6 0 3 3 0 016 0z" />
              </svg>
              拍照
            </button>
          </div>
        </div>

        <!-- 拍摄结果展示 -->
        <div class="mt-8">
          <h2 class="text-xl font-bold mb-4 flex items-center">
            <svg class="w-6 h-6 mr-2 text-primary" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 4v16M17 4v16M3 8h4m10 0h4M3 12h18M3 16h4m10 0h4M4 20h16a1 1 0 001-1V5a1 1 0 00-1-1H4a1 1 0 00-1 1v14a1 1 0 001 1z" />
            </svg>
            拍摄结果
            <span class="ml-2 text-sm font-normal text-base-content/70">({{ recordedMedia.length }})</span>
          </h2>

          <div v-if="recordedMedia.length === 0" class="text-center text-gray-500 py-12">
            <svg class="w-16 h-16 mx-auto mb-4 opacity-30" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 4v16M17 4v16M3 8h4m10 0h4M3 12h18M3 16h4m10 0h4M4 20h16a1 1 0 001-1V5a1 1 0 00-1-1H4a1 1 0 00-1 1v14a1 1 0 001 1z" />
            </svg>
            <p>您的视频和照片将显示在这里</p>
          </div>

          <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            <div
                v-for="(item, index) in recordedMedia"
                :key="index"
                class="bg-base-100 rounded-lg overflow-hidden shadow-md hover:shadow-lg transition-shadow duration-300"
            >
              <div class="aspect-video bg-gray-900">
                <video
                    v-if="item.type === 'video'"
                    :src="item.url"
                    class="w-full h-full object-contain"
                    controls
                ></video>
                <img
                    v-else
                    :src="item.url"
                    class="w-full h-full object-cover"
                    :alt="`照片 ${index + 1}`"
                >
              </div>
              <div class="p-3">
                <div class="flex items-center justify-between">
              <span class="text-sm font-medium">
                {{ item.type === 'video' ? '视频' : '照片' }}
              </span>
                  <span class="text-xs text-base-content/70">{{ item.timestamp }}</span>
                </div>
                <div class="flex gap-2 mt-2">
                  <a
                      :href="item.url"
                      :download="`camera_${item.type}_${index + 1}`"
                      class="btn btn-xs btn-primary flex-1"
                  >
                    下载
                  </a>
                  <a
                      @click="submitVideo(item.url)"
                      class="btn btn-xs btn-primary flex-1"
                  >
                    提交
                  </a>
                  <button
                      @click="deleteMedia(index)"
                      class="btn btn-xs btn-error"
                  >
                    删除
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </main>

      <!-- 隐藏的 Canvas 用于拍照 -->
      <canvas ref="canvasRef" class="hidden"></canvas>
    </div>
  </div>
</dialog>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'

const varemit = defineEmits(["loginFace"])

// ============ 类型定义 ============
interface CameraDevice {
  deviceId: string
  kind: string
  label: string
  groupId: string
}

interface RecordedMedia {
  url: string
  type: 'video' | 'photo'
  timestamp: string
}

// ============ 响应式状态 ============
const videoRef = ref<HTMLVideoElement | null>(null)
const canvasRef = ref<HTMLCanvasElement | null>(null)

// 状态管理
const isCameraActive = ref(false)
const isRecording = ref(false)
const selectedCameraId = ref('')
const cameraDevices = ref<CameraDevice[]>([])
const recordedMedia = ref<RecordedMedia[]>([])

// 录制相关
let mediaStream: MediaStream | null = null
let mediaRecorder: MediaRecorder | null = null
let recordedChunks: Blob[] = []
let recordingTimer: number | null = null
const recordedSeconds = ref(0)

// 状态提示
const statusType = ref<'info' | 'success' | 'warning' | 'error'>('info')
const statusMessage = ref('请点击"开启摄像头"按钮开始使用')

// ============ 状态配置 ============
const statusConfig = {
  info: {
    bg: 'bg-blue-100',
    border: 'border-blue-500',
    icon: 'svg' as any,
    iconClass: 'text-blue-500'
  },
  success: {
    bg: 'bg-green-100',
    border: 'border-green-500',
    icon: 'svg' as any,
    iconClass: 'text-green-500'
  },
  warning: {
    bg: 'bg-yellow-100',
    border: 'border-yellow-500',
    icon: 'svg' as any,
    iconClass: 'text-yellow-500'
  },
  error: {
    bg: 'bg-red-100',
    border: 'border-red-500',
    icon: 'svg' as any,
    iconClass: 'text-red-500'
  }
}

// 录制时间格式化
const formattedRecordingTime = computed(() => {
  if (!isRecording.value) return '00:00'
  const minutes = Math.floor(recordedSeconds.value / 60).toString().padStart(2, '0')
  const seconds = (recordedSeconds.value % 60).toString().padStart(2, '0')
  return `${minutes}:${seconds}`
})

// ============ 更新状态提示 ============
const updateStatus = (message: string, type: 'info' | 'success' | 'warning' | 'error' = 'info') => {
  statusType.value = type
  statusMessage.value = message
}

// ============ 获取摄像头设备列表 ============
const getCameraDevices = async () => {
  try {
    const devices = await navigator.mediaDevices.enumerateDevices()
    const videoDevices = devices.filter(device => device.kind === 'videoinput')
    cameraDevices.value = videoDevices

    // 如果有默认摄像头且未选择，选择第一个
    if (videoDevices.length > 0 && !selectedCameraId.value) {
      selectedCameraId.value = videoDevices[0].deviceId
    }

    return videoDevices
  } catch (err: any) {
    updateStatus(`获取设备列表失败: ${err.message}`, 'error')
    console.error('获取设备列表失败:', err)
    return []
  }
}

// ============ 开启摄像头 ============
const startCamera = async () => {
  try {
    // 停止现有流
    stopCamera()

    const constraints: MediaStreamConstraints = {
      video: {
        deviceId: selectedCameraId.value ? { exact: selectedCameraId.value } : undefined,
        width: { ideal: 1920 },
        height: { ideal: 1080 }
      },
      audio: true
    }

    mediaStream = await navigator.mediaDevices.getUserMedia(constraints)

    if (videoRef.value) {
      videoRef.value.srcObject = mediaStream
    }

    isCameraActive.value = true
    updateStatus('摄像头已开启', 'success')

    // 更新设备列表（获取设备标签）
    await getCameraDevices()

  } catch (err: any) {
    updateStatus(`无法开启摄像头: ${err.message}`, 'error')
    console.error('无法开启摄像头:', err)
  }
}

// ============ 关闭摄像头 ============
const stopCamera = () => {
  if (mediaStream) {
    mediaStream.getTracks().forEach(track => track.stop())
    mediaStream = null
  }

  if (videoRef.value) {
    videoRef.value.srcObject = null
  }

  // 停止录制
  if (isRecording.value) {
    stopRecording()
  }

  isCameraActive.value = false
  updateStatus('摄像头已关闭', 'info')

  if (recordingTimer) {
    clearInterval(recordingTimer)
    recordingTimer = null
  }
}

// ============ 切换摄像头 ============
const switchCamera = async () => {
  if (isCameraActive.value && selectedCameraId.value) {
    await startCamera()
  }
}

// ============ 开始录制 ============
const startRecording = () => {
  if (!mediaStream) return

  try {
    recordedChunks = []

    // 尝试使用支持的 MIME 类型
    const mimeTypes = [
      'video/mp4',
      'video/webm;codecs=vp9',
      'video/webm;codecs=vp8',
      'video/webm',
    ]

    let selectedMimeType = ''
    for (const mimeType of mimeTypes) {
      if (MediaRecorder.isTypeSupported(mimeType)) {
        selectedMimeType = mimeType
        break
      }
    }

    if (!selectedMimeType) {
      updateStatus('浏览器不支持视频录制', 'error')
      return
    }

    mediaRecorder = new MediaRecorder(mediaStream, { mimeType: selectedMimeType })

    mediaRecorder.ondataavailable = (event) => {
      if (event.data.size > 0) {
        recordedChunks.push(event.data)
      }
    }

    mediaRecorder.onstop = () => {
      const blob = new Blob(recordedChunks, { type: selectedMimeType })
      const url = URL.createObjectURL(blob)
      const timestamp = new Date().toLocaleString('zh-CN', {
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      })

      recordedMedia.value.unshift({
        url,
        type: 'video',
        timestamp
      })

      updateStatus('视频录制完成', 'success')
    }

    mediaRecorder.start(1000) // 每秒收集一次数据
    isRecording.value = true

    // 清理旧定时器
    if (recordingTimer) clearInterval(recordingTimer)

    // 启动新定时器
    recordingTimer = window.setInterval(() => {
      recordedSeconds.value++ // 👈 关键：更新响应式变量
    }, 1000)

    // 更新录制时间显示
    updateStatus('正在录制...', 'warning')

  } catch (err: any) {
    updateStatus(`录制失败: ${err.message}`, 'error')
    console.error('录制失败:', err)
  }
}

// ============ 停止录制 ============
const stopRecording = () => {
  if (mediaRecorder && isRecording.value) {
    mediaRecorder.stop()
    isRecording.value = false
  }
  if (recordingTimer) {
    clearInterval(recordingTimer)
    recordingTimer = null
  }
}

// ============ 切换录制状态 ============
const toggleRecording = () => {
  if (isRecording.value) {
    stopRecording()
  } else {
    startRecording()
  }
}

// ============ 拍照 ============
const takePhoto = () => {
  if (!videoRef.value || !canvasRef.value) return

  const video = videoRef.value
  const canvas = canvasRef.value

  // 设置 canvas 尺寸与视频相同
  canvas.width = video.videoWidth
  canvas.height = video.videoHeight

  // 绘制当前帧
  const ctx = canvas.getContext('2d')
  if (ctx) {
    ctx.drawImage(video, 0, 0, canvas.width, canvas.height)

    // 转换为图片 URL
    const url = canvas.toDataURL('image/png')
    const timestamp = new Date().toLocaleString('zh-CN', {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    })

    recordedMedia.value.unshift({
      url,
      type: 'photo',
      timestamp
    })

    updateStatus('照片已拍摄', 'success')
  }
}

// ============ 删除媒体 ============
const deleteMedia = (index: number) => {
  // 释放 URL 对象
  URL.revokeObjectURL(recordedMedia.value[index].url)

  // 从数组中删除
  recordedMedia.value.splice(index, 1)

  updateStatus('媒体已删除', 'info')
}

// ============ 生命周期钩子 ============
onMounted(async () => {
  await getCameraDevices()
})

onBeforeUnmount(() => {
  stopCamera()

  // 清理所有 URL 对象
  recordedMedia.value.forEach(item => {
    URL.revokeObjectURL(item.url)
  })
})

const submitVideo = async (blobUrl: string) => {
  let videoBase64 = await convertVideoBlobToBase64(blobUrl)
  videoBase64 = videoBase64.split(',')[1]
  varemit("loginFace", videoBase64)
}

// ============ 视频 Blob 转 Base64 ============
const convertVideoBlobToBase64 = async (blobUrl: string): Promise<string> => {
  try {
    // 1. 获取 Blob
    const response = await fetch(blobUrl)
    const blob = await response.blob()

    // 2. 转为 ArrayBuffer
    return new Promise((resolve, reject) => {
      const reader = new FileReader()
      reader.onload = () => {
        if (typeof reader.result === 'string') {
          resolve(reader.result) // 已是 data:... base64 格式
        } else {
          reject(new Error('FileReader did not return a string'))
        }
      }
      reader.onerror = () => reject(reader.error)
      reader.readAsDataURL(blob) // 自动处理 MIME + Base64
    })
  } catch (err) {
    console.error('视频转 Base64 失败:', err)
    throw err
  }
}
</script>

<style scoped>
/* 自定义过渡效果 */
.transition-shadow {
  transition-property: box-shadow;
  transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
  transition-duration: 300ms;
}
</style>
