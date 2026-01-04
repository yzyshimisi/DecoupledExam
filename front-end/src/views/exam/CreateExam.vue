<template>
  <div class="container mx-auto px-4 py-8 max-w-3xl">
    <!-- 页面标题 -->
    <div class="mb-8">
      <button 
        @click="goBack" 
        class="btn btn-ghost btn-sm flex items-center mb-4"
      >
        <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
        </svg>
        返回
      </button>
      <h1 class="text-2xl font-bold text-base-content">
        {{ isEditMode ? '编辑考试' : '创建考试' }}
      </h1>
      <p class="text-base-content/70 mt-2">
        {{ isEditMode ? '修改现有考试信息' : '创建新的考试' }}
      </p>
    </div>

    <!-- 表单 -->
    <form @submit.prevent="handleSubmit" class="card bg-base-100 shadow-xl border border-base-200">
      <div class="card-body p-6">
        <!-- 考试标题 -->
        <div class="form-control mb-6">
          <label class="label">
            <span class="label-text text-base-content font-medium">考试标题 *</span>
          </label>
          <input
            v-model="form.title"
            type="text"
            placeholder="请输入考试标题"
            class="input input-bordered w-full"
            :class="{ 'input-error': errors.title }"
            required
          />
          <label class="label" v-if="errors.title">
            <span class="label-text-alt text-error">{{ errors.title }}</span>
          </label>
        </div>

        <!-- 试卷选择 -->
        <!-- 试卷选择 -->
        <div class="form-control mb-6">
          <label class="label">
            <span class="label-text text-base-content font-medium">试卷 *</span>
          </label>

          <!-- 未选择状态 -->
          <div v-if="!paperInfo?.paperId" class="flex flex-col gap-2">
            <button
                type="button"
                class="btn btn-outline btn-primary gap-2"
                onclick="ExamPaperDialog.showModal()"
            >
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
              </svg>
              选择试卷
            </button>
            <label class="label" v-if="errors.paperId">
              <span class="label-text-alt text-error">{{ errors.paperId }}</span>
            </label>
          </div>

          <!-- 已选择状态 -->
          <div v-else class="card bg-base-100 border border-base-200 p-4">
            <div class="flex justify-between items-start">
              <div>
                <h3 class="font-semibold text-base-content">{{ paperInfo.paperName }}</h3>
                <div class="text-sm text-base-content/70 mt-1">
                  <div>ID: {{ paperInfo.paperId }} · 总分: {{ paperInfo.totalScore }} 分</div>
                  <div class="mt-1">
                    <span class="badge badge-ghost badge-sm mr-2">类型: {{ paperInfo.composeType === '1' ? '手动组卷' : '自动组卷' }}</span>
                    <span class="badge badge-ghost badge-sm" :class="paperInfo.isSealed === '1' ? 'badge-error' : 'badge-success'">
              {{ paperInfo.isSealed === '1' ? '已封存' : '可用' }}
            </span>
                  </div>
                </div>
              </div>
              <button
                  type="button"
                  class="btn btn-sm btn-ghost text-error hover:bg-error/10"
                  @click="() => { paperInfo = null; form.paperId = 0; }"
                  title="取消选择"
              >
                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>
            <label class="label" v-if="errors.paperId">
              <span class="label-text-alt text-error">{{ errors.paperId }}</span>
            </label>
          </div>
        </div>
<!--        <div class="form-control mb-6">-->
<!--          <label class="label">-->
<!--            <span class="label-text text-base-content font-medium">试卷ID *</span>-->
<!--          </label>-->
<!--          <input-->
<!--            v-model="form.paperId"-->
<!--            type="number"-->
<!--            placeholder="请输入试卷ID"-->
<!--            class="input input-bordered w-full"-->
<!--            :class="{ 'input-error': errors.paperId }"-->
<!--            required-->
<!--          />-->
<!--          <label class="label" v-if="errors.paperId">-->
<!--            <span class="label-text-alt text-error">{{ errors.paperId }}</span>-->
<!--          </label>-->
<!--        </div>-->

        <!-- 教师ID（仅教务老师可见） -->
        <div v-if="userType === '0'" class="form-control mb-6">
          <label class="label">
            <span class="label-text text-base-content font-medium">教师ID *</span>
          </label>
          <input
            v-model="form.teacherId"
            type="number"
            placeholder="请输入教师ID"
            class="input input-bordered w-full"
            :class="{ 'input-error': errors.teacherId }"
            required
          />
          <label class="label">
            <span class="label-text-alt text-base-content/70">留空则默认为当前用户</span>
          </label>
          <label class="label" v-if="errors.teacherId">
            <span class="label-text-alt text-error">{{ errors.teacherId }}</span>
          </label>
        </div>

        <!-- 考试时间设置 -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
          <div class="form-control">
            <label class="label">
              <span class="label-text text-base-content font-medium">开始时间 *</span>
            </label>
            <input
              v-model="form.startTime"
              type="datetime-local"
              class="input input-bordered w-full"
              :class="{ 'input-error': errors.startTime }"
              required
            />
            <label class="label" v-if="errors.startTime">
              <span class="label-text-alt text-error">{{ errors.startTime }}</span>
            </label>
          </div>

          <div class="form-control">
            <label class="label">
              <span class="label-text text-base-content font-medium">结束时间 *</span>
            </label>
            <input
              v-model="form.endTime"
              type="datetime-local"
              class="input input-bordered w-full"
              :class="{ 'input-error': errors.endTime }"
              required
            />
            <label class="label" v-if="errors.endTime">
              <span class="label-text-alt text-error">{{ errors.endTime }}</span>
            </label>
          </div>
        </div>

        <!-- 考试时长 -->
        <div class="form-control mb-6">
          <label class="label">
            <span class="label-text text-base-content font-medium">考试时长（分钟） *</span>
          </label>
          <input
            v-model.number="form.durationMinute"
            type="number"
            min="1"
            placeholder="请输入考试时长（分钟）"
            class="input input-bordered w-full"
            :class="{ 'input-error': errors.durationMinute }"
            required
          />
          <label class="label" v-if="errors.durationMinute">
            <span class="label-text-alt text-error">{{ errors.durationMinute }}</span>
          </label>
        </div>

        <!-- 考试代码 -->
        <div class="form-control mb-6">
          <label class="label">
            <span class="label-text text-base-content font-medium">考试代码</span>
          </label>
          <input
            v-model="form.examCode"
            type="text"
            placeholder="请输入考试代码（可选）"
            class="input input-bordered w-full"
            :class="{ 'input-error': errors.examCode }"
          />
          <label class="label">
            <span class="label-text-alt text-base-content/70">例如：JAVA202601</span>
          </label>
          <label class="label" v-if="errors.examCode">
            <span class="label-text-alt text-error">{{ errors.examCode }}</span>
          </label>
        </div>

        <!-- 基本设置 -->
        <div class="mb-6">
          <h3 class="text-lg font-semibold text-base-content mb-4">基本设置</h3>
          <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div class="form-control">
              <label class="cursor-pointer label flex">
                <input 
                  v-model="form.allowLateEnter" 
                  type="checkbox" 
                  class="checkbox checkbox-primary"
                />
                <span class="label-text ml-2">允许迟到</span>
              </label>
            </div>
            <div class="form-control">
              <label class="cursor-pointer label">
                <input 
                  v-model="form.autoSubmit" 
                  type="checkbox" 
                  class="checkbox checkbox-primary"
                />
                <span class="label-text ml-2">超时自动提交</span>
              </label>
            </div>
            <div class="form-control">
              <label class="cursor-pointer label">
                <input 
                  v-model="form.allowViewPaper" 
                  type="checkbox" 
                  class="checkbox checkbox-primary"
                />
                <span class="label-text ml-2">允许查看试卷</span>
              </label>
            </div>
            <div class="form-control">
              <label class="cursor-pointer label">
                <input 
                  v-model="form.allowViewScore" 
                  type="checkbox" 
                  class="checkbox checkbox-primary"
                />
                <span class="label-text ml-2">允许查看成绩</span>
              </label>
            </div>
          </div>
        </div>

        <!-- 防作弊设置 -->
        <div class="mb-6">
          <h3 class="text-lg font-semibold text-base-content mb-4">防作弊设置</h3>
          <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div class="form-control">
              <label class="cursor-pointer label">
                <input
                  v-model="form.questionShuffle"
                  type="checkbox"
                  class="checkbox checkbox-primary"
                />
                <span class="label-text ml-2">题目乱序</span>
              </label>
            </div>
            <div class="form-control">
              <label class="cursor-pointer label">
                <input
                  v-model="form.optionShuffle"
                  type="checkbox"
                  class="checkbox checkbox-primary"
                />
                <span class="label-text ml-2">选项乱序</span>
              </label>
            </div>
            <div class="form-control">
              <label class="cursor-pointer label">
                <input
                  v-model="form.preventScreenSwitch"
                  type="checkbox"
                  class="checkbox checkbox-primary"
                />
                <span class="label-text ml-2">防止切屏</span>
              </label>
            </div>
            <div class="form-control">
              <label class="cursor-pointer label">
                <input
                  v-model="form.peerReview"
                  type="checkbox"
                  class="checkbox checkbox-primary"
                />
                <span class="label-text ml-2">生生互评</span>
              </label>
            </div>
          </div>
        </div>

        <!-- 分数设置 -->
        <div class="mb-6">
          <h3 class="text-lg font-semibold text-base-content mb-4">分数设置</h3>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div class="form-control">
              <label class="label">
                <span class="label-text text-base-content font-medium">及格分数</span>
              </label>
              <input
                v-model.number="form.passingScore"
                type="number"
                step="0.01"
                min="0"
                placeholder="请输入及格分数"
                class="input input-bordered w-full"
              />
              <label class="label">
                <span class="label-text-alt text-base-content/70">默认：60.0</span>
              </label>
            </div>
            <div class="form-control">
              <label class="label">
                <span class="label-text text-base-content font-medium">多选题部分得分比例</span>
              </label>
              <input
                v-model.number="form.multiChoicePartialRatio"
                type="number"
                step="0.01"
                min="0"
                max="1"
                placeholder="请输入多选题部分得分比例"
                class="input input-bordered w-full"
              />
              <label class="label">
                <span class="label-text-alt text-base-content/70">默认：0.5</span>
              </label>
            </div>
          </div>
        </div>

        <!-- 填空题设置 -->
        <div class="mb-6">
          <h3 class="text-lg font-semibold text-base-content mb-4">填空题设置</h3>
          <div class="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <div class="form-control">
              <label class="cursor-pointer label">
                <input 
                  v-model="form.fillCaseSensitive" 
                  type="checkbox" 
                  class="checkbox checkbox-primary"
                />
                <span class="label-text ml-2">区分大小写</span>
              </label>
            </div>
            <div class="form-control">
              <label class="cursor-pointer label">
                <input 
                  v-model="form.fillIgnoreSymbols" 
                  type="checkbox" 
                  class="checkbox checkbox-primary"
                />
                <span class="label-text ml-2">忽略符号</span>
              </label>
            </div>
            <div class="form-control">
              <label class="cursor-pointer label">
                <input 
                  v-model="form.fillManualMark" 
                  type="checkbox" 
                  class="checkbox checkbox-primary"
                />
                <span class="label-text ml-2">人工批改</span>
              </label>
            </div>
          </div>
        </div>

        <!-- 描述 -->
        <div class="form-control mb-6">
          <label class="label">
            <span class="label-text text-base-content font-medium">考试描述</span>
          </label>
          <textarea
            v-model="form.description"
            placeholder="请输入考试描述（可选）"
            class="textarea textarea-bordered w-full h-24"
          ></textarea>
        </div>

        <!-- 提交按钮 -->
        <div class="card-actions justify-end pt-4 border-t border-base-200">
          <button 
            type="button" 
            class="btn btn-ghost"
            @click="goBack"
          >
            取消
          </button>
          <button 
            type="submit" 
            class="btn btn-primary"
            :disabled="isSubmitting"
          >
            <span v-if="!isSubmitting">{{ isEditMode ? '更新考试' : '创建考试' }}</span>
            <span v-else>提交中...</span>
          </button>
        </div>
      </div>
    </form>
  </div>

  <dialog id="ExamPaperDialog" class="modal">
    <div class="modal-box max-w-[95vw] w-[95vw]">
      <ExamPaper
        :is-component="true"
        @selectExamPaper="selectExamPaper"
      />
    </div>
    <form method="dialog" class="modal-backdrop">
      <button>close</button>
    </form>
  </dialog>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useRequest } from 'vue-hooks-plus';
import { getExamDetailAPI, createExamAPI, updateExamAPI } from '../../apis';
import { ExamPaper } from "../../views"

const router = useRouter();
const route = useRoute();

// 用户类型：0-教务老师, 1-普通老师, 2-学生
const userType = localStorage.getItem('userType');
const examId = route.params.id as string;
const isEditMode = computed(() => !!examId);

// 表单数据
const form = ref({
  title: '',
  paperId: 0,
  teacherId: userType === '0' ? 0 : parseInt(localStorage.getItem('userId') || '0'), // 教务老师可指定，普通老师默认当前用户
  startTime: '',
  endTime: '',
  durationMinute: 60,
  examCode: '',
  allowLateEnter: true,
  questionShuffle: true,
  optionShuffle: true,
  preventScreenSwitch: true,
  passingScore: 60.0,
  autoSubmit: true,
  allowViewPaper: true,
  allowViewScore: true,
  multiChoicePartialRatio: 0.5,
  fillCaseSensitive: false,
  fillIgnoreSymbols: true,
  fillManualMark: false,
  peerReview: false,
  description: ''
});

const paperInfo = ref<{
  paperId?: number;
  paperName?: string;
  totalScore?: number;
  composeType?: string;
  isSealed?: string;
  creatorId?: number;
  createTime?: number;
  updatedAt?: number;
} | null>(null);

const selectExamPaper = (paper: any) => {
  paperInfo.value = paper;
  form.value.paperId = paper.paperId;
  ExamPaperDialog.close()
}

// 错误信息
const errors = ref({
  title: '',
  paperId: '',
  teacherId: '',
  startTime: '',
  endTime: '',
  durationMinute: '',
  examCode: ''
});

// 提交状态
const isSubmitting = ref(false);

// 表单验证
const validateForm = (): boolean => {
  let isValid = true;
  
  // 重置错误
  errors.value = {
    title: '',
    paperId: '',
    teacherId: '',
    startTime: '',
    endTime: '',
    durationMinute: '',
    examCode: ''
  };

  if (!form.value.title.trim()) {
    errors.value.title = '请输入考试标题';
    isValid = false;
  }

  if (!form.value.paperId || form.value.paperId <= 0) {
    errors.value.paperId = '试卷ID必须大于0';
    isValid = false;
  }

  if (userType === '0' && (!form.value.teacherId || form.value.teacherId <= 0)) {
    errors.value.teacherId = '教师ID必须大于0';
    isValid = false;
  }

  if (!form.value.startTime) {
    errors.value.startTime = '请选择开始时间';
    isValid = false;
  }

  if (!form.value.endTime) {
    errors.value.endTime = '请选择结束时间';
    isValid = false;
  }

  if (form.value.startTime && form.value.endTime && new Date(form.value.startTime) >= new Date(form.value.endTime)) {
    errors.value.startTime = '开始时间必须早于结束时间';
    errors.value.endTime = '结束时间必须晚于开始时间';
    isValid = false;
  }

  if (!form.value.durationMinute || form.value.durationMinute <= 0) {
    errors.value.durationMinute = '考试时长必须大于0';
    isValid = false;
  }

  return isValid;
};

// 时间格式转换函数，将API格式转换为datetime-local格式
const formatDateTimeForInput = (dateTimeStr: string): string => {
  if (!dateTimeStr) return '';
  
  try {
    // 将API格式（yyyy-MM-dd HH:mm:ss）转换为datetime-local格式（yyyy-MM-ddTHH:mm）
    const date = new Date(dateTimeStr);
    return date.toISOString().slice(0, 16);
  } catch (e) {
    console.error('时间格式转换错误:', e);
    // 如果转换失败，尝试手动解析
    try {
      // 假设输入格式为 'yyyy-MM-dd HH:mm:ss'
      const match = dateTimeStr.match(/(\d{4})-(\d{2})-(\d{2}) (\d{2}):(\d{2}):(\d{2})/);
      if (match) {
        const [, year, month, day, hour, minute] = match;
        return `${year}-${month}-${day}T${hour}:${minute}`;
      }
    } catch (parseError) {
      console.error('手动解析时间失败:', parseError);
    }
    return dateTimeStr;
  }
};

// 获取考试详情（编辑模式）
const fetchExamDetail = () => {
  if (isEditMode.value) {
    useRequest(() => getExamDetailAPI(parseInt(examId)), {
      onSuccess(res) {
        if (res['code'] === 200) {
          const exam = res['data'];
          if (exam) {
            form.value = {
              title: exam.title || '',
              paperId: exam.paperId || 1,
              teacherId: exam.teacherId || parseInt(localStorage.getItem('userId') || '0'),
              startTime: exam.startTime ? formatDateTimeForInput(exam.startTime) : '',
              endTime: exam.endTime ? formatDateTimeForInput(exam.endTime) : '',
              durationMinute: exam.durationMinute || 60,
              examCode: exam.examCode || '',
              allowLateEnter: exam.allowLateEnter || true,
              questionShuffle: exam.questionShuffle || true,
              optionShuffle: exam.optionShuffle || true,
              preventScreenSwitch: exam.preventScreenSwitch || true,
              passingScore: exam.passingScore || 60.0,
              autoSubmit: exam.autoSubmit || true,
              allowViewPaper: exam.allowViewPaper || true,
              allowViewScore: exam.allowViewScore || true,
              multiChoicePartialRatio: exam.multiChoicePartialRatio || 0.5,
              fillCaseSensitive: exam.fillCaseSensitive || false,
              fillIgnoreSymbols: exam.fillIgnoreSymbols || true,
              fillManualMark: exam.fillManualMark || false,
              peerReview: exam.peerReview || false,
              description: exam.description || ''
            };
          }
        }
      },
      onError(err) {
        console.error('获取考试详情失败:', err);
        router.push('/exam');
      }
    });
  } else if (userType === '1') {
    // 普通老师模式下，自动填入教师ID
    form.value.teacherId = parseInt(localStorage.getItem('userId') || '0');
  }
};

// 时间格式转换函数
const formatDateTimeForAPI = (dateTimeStr: string): string => {
  if (!dateTimeStr) return '';
  
  // 将ISO格式（yyyy-MM-ddTHH:mm）转换为API需要的格式（yyyy-MM-dd HH:mm:ss）
  try {
    const date = new Date(dateTimeStr);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    const seconds = '00'; // API需要秒，但datetime-local不提供秒
    
    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
  } catch (e) {
    console.error('时间格式转换错误:', e);
    return dateTimeStr;
  }
};

// 提交表单
const handleSubmit = () => {
  if (!validateForm()) return;

  isSubmitting.value = true;

  // 准备提交数据，转换时间格式
  const examData = {
    title: form.value.title,
    paperId: form.value.paperId,
    teacherId: form.value.teacherId,
    startTime: formatDateTimeForAPI(form.value.startTime),
    endTime: formatDateTimeForAPI(form.value.endTime),
    examCode: form.value.examCode || form.value.title.substring(0, 10).toUpperCase().replace(/\s+/g, ''), // 生成考试代码
    durationMinute: form.value.durationMinute,
    allowLateEnter: form.value.allowLateEnter,
    questionShuffle: form.value.questionShuffle,
    optionShuffle: form.value.optionShuffle,
    preventScreenSwitch: form.value.preventScreenSwitch,
    passingScore: form.value.passingScore,
    autoSubmit: form.value.autoSubmit,
    allowViewPaper: form.value.allowViewPaper,
    allowViewScore: form.value.allowViewScore,
    multiChoicePartialRatio: form.value.multiChoicePartialRatio,
    fillCaseSensitive: form.value.fillCaseSensitive,
    fillIgnoreSymbols: form.value.fillIgnoreSymbols,
    fillManualMark: form.value.fillManualMark,
    peerReview: form.value.peerReview
  };

  if (isEditMode.value) {
    // 编辑模式：更新考试
    useRequest(() => updateExamAPI(parseInt(examId), {
      title: examData.title,
      startTime: examData.startTime,
      endTime: examData.endTime,
      examCode: examData.examCode
    }), {
      onSuccess(res) {
        if (res['code'] === 200 && res['data']) {
          alert('考试更新成功！');
          router.push('/exam');
        } else {
          alert('考试更新失败：' + (res ? res['msg'] : '未知错误'));
        }
      },
      onError(err) {
        console.error('更新考试失败:', err);
        alert('考试更新失败：' + (err.message || '网络错误'));
      },
      onFinally() {
        isSubmitting.value = false;
      }
    });
  } else {
    // 创建模式：创建考试
    useRequest(() => createExamAPI(examData), {
      onSuccess(res) {
        if (res['code'] === 200) {
          alert('考试创建成功！');
          router.push('/exam');
        } else {
          alert('考试创建失败：' + (res ? res['msg'] : '未知错误'));
        }
      },
      onError(err) {
        console.error('创建考试失败:', err);
        alert('考试创建失败：' + (err.message || '网络错误'));
      },
      onFinally() {
        isSubmitting.value = false;
      }
    });
  }
};

// 返回考试列表
const goBack = () => {
  router.push('/exam');
};

// 页面加载时获取数据
onMounted(() => {
  fetchExamDetail();
});
</script>
