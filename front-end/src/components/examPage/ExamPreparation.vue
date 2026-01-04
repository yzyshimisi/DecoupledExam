<template>
  <div class="bg-base-100 py-16 px-4 w-full">
    <!-- 主要内容卡片 -->
    <div class="max-w-2xl mx-auto bg-white p-6 rounded-lg shadow-md">
      <!-- 考试信息 -->
      <div class="mb-6">
        <div class="flex justify-between items-start">
          <div>
            <h1 class="text-2xl font-bold text-base-content">{{ exam?.title }}</h1>
            <p class="text-base-content/70 mt-1">考试ID: {{ exam?.examId }}</p>
          </div>
          <div class="badge" :class="getStatusClass(exam?.status)">
            {{ getStatusText(exam?.status) }}
          </div>
        </div>

        <div class="mt-6 grid grid-cols-1 md:grid-cols-2 gap-6">
          <div class="space-y-4">
            <div class="flex items-center text-base-content/80">
              <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
              <div>
                <p class="text-sm text-base-content/60">考试时间</p>
                <p>{{ formatDateTime(exam?.startTime) }} - {{ formatDateTime(exam?.endTime) }}</p>
              </div>
            </div>

            <div class="flex items-center text-base-content/80 mr-8">
              <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <div>
                <p class="text-sm text-base-content/60">考试时长</p>
                <p>{{ exam ? calculateDuration() : 0 }} 分钟</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 注意事项 -->
      <div class="mb-8 p-4 bg-warning/10 border border-warning rounded-md">
        <h3 class="font-medium text-warning mb-2">考试注意事项：</h3>
        <ul class="list-disc list-inside text-sm text-gray-700 space-y-1">
          <li>请确保网络稳定，避免中断。</li>
          <li>考试期间禁止切换窗口或使用其他程序。</li>
          <li>请保持摄像头清晰可见，以便人脸识别。</li>
          <li>考试时间结束后将自动提交答卷。</li>
        </ul>
      </div>

      <!-- 操作按钮区 -->
      <div class="flex flex-col sm:flex-row gap-3 justify-end">
        <button @click="goBack" class="btn btn-outline text-sm">返回</button>
        <button onclick="cameraRecorderDia.showModal()" class="btn btn-primary">开始人脸识别验证</button>
      </div>
    </div>
  </div>
  <CameraRecorder
      :isLoading="isLoading"
      @loginFace="verifyFace"
  />

</template>

<script setup lang="ts">
import {ref, onMounted, watch} from 'vue';
import router from "../../routers";
import { useRequest } from "vue-hooks-plus";
import {getExamDetailAPI, verifyFaceAPI} from "../../apis";
import { CameraRecorder } from "../../components"
import { ElNotification } from "element-plus";

const props = defineProps<{ exam: any }>();

const varemit = defineEmits(['verifyFaceSuccess'])

const exam = ref();

watch(()=>props.exam,(newValue)=>{
  exam.value = newValue
})

const isLoading = ref<boolean>(false);

const verifyFace = (videoBase64) => {
  useRequest(()=>verifyFaceAPI(props.exam.examId, {video: videoBase64}),{
    onBefore: () => {
      isLoading.value = true
    },

    onSuccess: (res) => {
      if(res['code'] === 200){

        cameraRecorderDia.close()

        let token = res['data']
        if(token==null || token=='') return
        localStorage.setItem('examToken', token)

        varemit('verifyFaceSuccess')
      }else{
        alert(res['msg'])
      }
    },

    onFinally: () => {
      isLoading.value = false
    }
  })
}

const goBack = () => {
  router.push("/exam")
}

const getStatusClass = (status: string) => {
  switch (status) {
    case 'upcoming': return 'badge-info';
    case 'ongoing': return 'badge-success';
    case 'ended': return 'badge-neutral';
    default: return 'badge-ghost';
  }
};

const getStatusText = (status: string) => {
  switch (status) {
    case 'upcoming': return '未开始';
    case 'ongoing': return '进行中';
    case 'ended': return '已结束';
    default: return '未知';
  }
};
// 时间格式化
const formatDateTime = (dateString: string) => {
  if (!dateString) return '未设置';
  try {
    return new Date(dateString).toLocaleString('zh-CN');
  } catch (e) {
    console.error('日期格式化错误:', e);
    return dateString;
  }
};

// 计算考试时长（分钟）
const calculateDuration = () => {
  if (!exam.value?.startTime || !exam.value?.endTime) {
    return 0;
  }

  try {
    const start = new Date(exam.value.startTime);
    const end = new Date(exam.value.endTime);

    if (isNaN(start.getTime()) || isNaN(end.getTime())) {
      console.error('无效的日期格式，无法计算时长');
      return 0;
    }

    // 计算时间差（毫秒），然后转换为分钟
    const durationMs = end.getTime() - start.getTime();
    const durationMinutes = Math.round(durationMs / (1000 * 60));

    return durationMinutes > 0 ? durationMinutes : 0;
  } catch (e) {
    console.error('计算考试时长时出错:', e);
    return 0;
  }
};

</script>