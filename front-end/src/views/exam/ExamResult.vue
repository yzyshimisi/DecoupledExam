<template>
  <div class="container mx-auto px-4 py-8 max-w-4xl">
    <!-- 返回按钮 -->
    <div class="mb-6">
      <button 
        @click="goBack" 
        class="btn btn-ghost btn-sm flex items-center"
      >
        <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
        </svg>
        返回
      </button>
    </div>

    <!-- 成绩卡片 -->
    <div class="card bg-base-100 shadow-xl border border-base-200 mb-8">
      <div class="card-body text-center py-12">
        <div class="flex justify-center mb-6">
          <div class="radial-progress text-primary" :style="radialProgressStyle">
            {{ result?.totalScore || 0 }}<span class="text-sm">/{{ result?.maxScore || 100 }}</span>
          </div>
        </div>
        
        <h2 class="text-2xl font-bold text-base-content mb-2">{{ exam?.title }}</h2>
        <p class="text-base-content/70 mb-4">考试结果</p>
        
        <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mt-6">
          <div class="stat">
            <div class="stat-title">总分</div>
            <div class="stat-value text-primary">{{ result?.totalScore || 0 }}/{{ result?.maxScore || 100 }}</div>
          </div>
          <div class="stat">
            <div class="stat-title">正确率</div>
            <div class="stat-value text-success">{{ scorePercentage }}%</div>
          </div>
          <div class="stat">
            <div class="stat-title">用时</div>
            <div class="stat-value text-info">{{ formatDuration(result?.duration || 0) }}</div>
          </div>
        </div>
        
        <div class="mt-8">
          <div class="badge badge-lg text-xl" :class="getScoreLevelClass(result?.totalScore, result?.maxScore)">
            {{ getScoreLevelText(result?.totalScore, result?.maxScore) }}
          </div>
        </div>
      </div>
    </div>

    <!-- 详细结果 -->
    <div class="card bg-base-100 shadow-xl border border-base-200">
      <div class="card-body">
        <h2 class="card-title text-xl font-semibold text-base-content mb-6 flex items-center">
          <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
          </svg>
          答题详情
        </h2>

        <div class="space-y-6">
          <div 
            v-for="(question, index) in result?.questions" 
            :key="question.id"
            class="border border-base-200 rounded-lg p-4"
          >
            <div class="flex justify-between items-start mb-3">
              <h3 class="font-semibold text-base-content">
                {{ index + 1 }}. {{ question.stem }}
              </h3>
              <div class="flex items-center gap-2">
                <span class="badge badge-info">分值: {{ question.score }}</span>
                <span :class="question.isCorrect ? 'badge badge-success' : 'badge badge-error'">
                  {{ question.isCorrect ? '正确' : '错误' }}
                </span>
              </div>
            </div>

            <!-- 题目类型和答案展示 -->
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <h4 class="font-medium text-base-content/80 mb-2">您的答案</h4>
                <div class="bg-base-200 p-3 rounded">
                  <template v-if="question.type === 'choice' || question.type === 'multiple'">
                    <div 
                      v-for="(option, optIndex) in question.options" 
                      :key="optIndex"
                      :class="[
                        'p-2 rounded mb-1',
                        isOptionSelectedByStudent(question.studentAnswer, optIndex) ? 
                          (isOptionCorrect(question.correctAnswer, optIndex) ? 'bg-success/20' : 'bg-error/20') : 
                          (isOptionCorrect(question.correctAnswer, optIndex) ? 'bg-success/10' : 'bg-base-100')
                      ]"
                    >
                      <span class="font-medium">{{ String.fromCharCode(65 + optIndex) }}.</span> 
                      {{ option.text }}
                      <span v-if="isOptionSelectedByStudent(question.studentAnswer, optIndex)" class="ml-2 text-primary">✓ 您的选择</span>
                      <span v-if="isOptionCorrect(question.correctAnswer, optIndex)" class="ml-2 text-success">✓ 正确答案</span>
                    </div>
                  </template>
                  <template v-else-if="question.type === 'fill_blank'">
                    <div 
                      v-for="(blank, blankIndex) in question.studentAnswer" 
                      :key="blankIndex"
                      class="mb-2"
                    >
                      <p class="text-sm text-base-content/70">第{{ blankIndex + 1 }}空:</p>
                      <p :class="blank === question.correctAnswer[blankIndex] ? 'text-success' : 'text-error'">
                        {{ blank || '(未填写)' }}
                      </p>
                    </div>
                  </template>
                  <template v-else>
                    <p :class="question.isCorrect ? 'text-success' : 'text-error'">
                      {{ question.studentAnswer || '(未回答)' }}
                    </p>
                  </template>
                </div>
              </div>

              <div>
                <h4 class="font-medium text-base-content/80 mb-2">正确答案</h4>
                <div class="bg-base-200 p-3 rounded">
                  <template v-if="question.type === 'choice' || question.type === 'multiple'">
                    <div 
                      v-for="(option, optIndex) in question.options" 
                      :key="optIndex"
                      :class="[
                        'p-2 rounded mb-1',
                        isOptionCorrect(question.correctAnswer, optIndex) ? 'bg-success/20' : 'bg-base-100'
                      ]"
                    >
                      <span class="font-medium">{{ String.fromCharCode(65 + optIndex) }}.</span> 
                      {{ option.text }}
                      <span v-if="isOptionCorrect(question.correctAnswer, optIndex)" class="ml-2 text-success">✓ 正确答案</span>
                    </div>
                  </template>
                  <template v-else-if="question.type === 'fill_blank'">
                    <div 
                      v-for="(correct, blankIndex) in question.correctAnswer" 
                      :key="blankIndex"
                      class="mb-2"
                    >
                      <p class="text-sm text-base-content/70">第{{ blankIndex + 1 }}空:</p>
                      <p class="text-success">{{ correct }}</p>
                    </div>
                  </template>
                  <template v-else>
                    <p class="text-success">{{ question.correctAnswer }}</p>
                  </template>
                </div>
              </div>
            </div>

            <!-- 得分情况 -->
            <div class="mt-3 pt-3 border-t border-base-200 flex justify-between">
              <div class="text-sm text-base-content/70">
                本题分值: {{ question.score }}
              </div>
              <div class="text-sm font-medium" :class="question.isCorrect ? 'text-success' : 'text-error'">
                得分: {{ question.obtainedScore || 0 }}/{{ question.score }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useRequest } from 'vue-hooks-plus';
import { getExamDetailAPI, getExamRecordAPI, getStudentExamsAPI } from '../../apis';

const router = useRouter();
const route = useRoute();

const examId = route.params.id as string;

// 状态响应式变量
const exam = ref<any>(null);
const result = ref<any>(null);

// 计算属性：得分百分比
const scorePercentage = computed(() => {
  if (!result.value || !result.value.maxScore) return 0;
  return Math.round((result.value.totalScore / result.value.maxScore) * 100);
});

// 径向进度条样式
const radialProgressStyle = computed(() => {
  const percentage = scorePercentage.value;
  return {
    '--value': percentage,
    '--size': '12rem'
  };
});

// 获取考试结果
const fetchResult = () => {
  console.log('开始获取考试结果，examId:', examId);
  
  // 首先获取考试详情
  useRequest(() => getExamDetailAPI(parseInt(examId)), {
    onSuccess(res) {
      console.log('获取考试详情响应:', res);
      if (res['code'] === 200) {
        exam.value = res['data'] || null;
        console.log('考试详情:', exam.value);
        
        // 模拟获取考试结果数据，实际应用中需要替换为真实的结果获取逻辑
        // 实际中可能需要使用 getExamRecordAPI 或其他相关API
        // 比如：通过考试记录ID获取结果
        result.value = {
          totalScore: exam.value?.totalScore || exam.value?.score || 0,
          maxScore: exam.value?.maxScore || exam.value?.fullScore || 100,
          duration: exam.value?.duration || 0,
          questions: exam.value?.questions || []
        };
        console.log('模拟结果数据:', result.value);
      } else {
        console.error('获取考试详情失败:', res['msg']);
        // 如果获取考试详情失败，可能考试还没结束或未提交
        alert('暂无考试结果，请先完成考试');
        router.push(`/exam/${examId}`);
      }
    },
    onError(err) {
      console.error('获取考试详情失败:', err);
      // 如果获取考试详情失败，可能考试还没结束或未提交
      alert('暂无考试结果，请先完成考试');
      router.push(`/exam/${examId}`);
    }
  });
};

// 辅助函数：判断选项是否被学生选择
const isOptionSelectedByStudent = (studentAnswer: any, optionIndex: number) => {
  if (Array.isArray(studentAnswer)) {
    return studentAnswer.includes(optionIndex);
  }
  return studentAnswer === optionIndex;
};

// 辅助函数：判断选项是否为正确答案
const isOptionCorrect = (correctAnswer: any, optionIndex: number) => {
  if (Array.isArray(correctAnswer)) {
    return correctAnswer.includes(optionIndex);
  }
  return correctAnswer === optionIndex;
};

// 格式化时长
const formatDuration = (seconds: number) => {
  if (!seconds) return '00:00';
  const mins = Math.floor(seconds / 60);
  const secs = seconds % 60;
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
};

// 获取分数等级文本
const getScoreLevelText = (score: number, maxScore: number) => {
  if (!score || !maxScore) return '未评分';
  const percentage = (score / maxScore) * 100;
  if (percentage >= 90) return '优秀';
  if (percentage >= 80) return '良好';
  if (percentage >= 70) return '中等';
  if (percentage >= 60) return '及格';
  return '不及格';
};

// 获取分数等级样式
const getScoreLevelClass = (score: number, maxScore: number) => {
  if (!score || !maxScore) return 'badge-neutral';
  const percentage = (score / maxScore) * 100;
  if (percentage >= 90) return 'badge-success';
  if (percentage >= 80) return 'badge-primary';
  if (percentage >= 70) return 'badge-info';
  if (percentage >= 60) return 'badge-warning';
  return 'badge-error';
};

// 返回考试列表
const goBack = () => {
  router.push('/exam');
};

// 页面加载时获取数据
onMounted(() => {
  fetchResult();
});
</script>

<style scoped>
/* 自定义径向进度条样式 */
.radial-progress {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 9999px;
  background-color: transparent;
}

.radial-progress::before {
  position: absolute;
  content: \"\";
  top: 0;
  right: 0;
  width: 100%;
  height: 100%;
  border-radius: 9999px;
  background: conic-gradient(currentColor calc(var(--value, 0) * 1%), #0000 0) content-box;
  mask: radial-gradient(closest-side, #0000 calc(99% - 1em), #000 calc(100% - 1em));
  mask-mode: alpha;
  -webkit-mask-mode: alpha;
  -webkit-mask: radial-gradient(closest-side, #0000 calc(99% - 1em), #000 calc(100% - 1em));
}

.radial-progress > span {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 9999px;
  background-color: var(--fallback-b1,oklch(var(--b1)/1));
  box-shadow: var(--tw-ring-offset-shadow, 0 0 #0000), var(--tw-ring-shadow, 0 0 #0000), var(--tw-shadow);
  color: var(--fallback-bc,oklch(var(--bc)/1));
  width: calc(100% - 1em);
  height: calc(100% - 1em);
}
</style>