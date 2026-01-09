<template>
  <dialog id="chooseExamTeacherDia" class="modal">
    <div class="modal-box">
      <form method="dialog">
        <button class="btn btn-sm btn-circle btn-ghost absolute right-2 top-2">✕</button>
      </form>

      <!-- 学科筛选 -->
      <div class="form-control w-full mb-4">
        <label class="label">
          <span class="label-text text-base">学科</span>
        </label>
        <select
            v-model="selectedId"
            class="select select-bordered w-full text-base"
        >
          <option value="-1" disabled selected>请选择</option>
          <option
              v-for="subject in subjects"
              :key="subject.subjectId"
              :value="subject.subjectId"
          >
            {{ subject['subjectName'] || '未指定学科' }}
          </option>
        </select>
      </div>

      <div class="grid grid-cols-2 w-full">
        <div v-for="(value, index) in teachers">
          <TeacherCard
              :teacher="value"
              @select="sendMail"
          />
        </div>
      </div>

    </div>
    <form method="dialog" class="modal-backdrop">
      <button>close</button>
    </form>
  </dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue';
import { useRequest } from "vue-hooks-plus";
import {getSubjectsAPI, getSubjectTeachersAPI, sendPrepareExamPaperMailAPI} from "../../apis"
import { getUserInfoByIdAPI  } from "../../apis/Server/userAPI";
import { TeacherCard } from "../../components";
import {ElNotification} from "element-plus";


const emit = defineEmits<{
  (e: 'select', teacher : any): void;
}>();

const teachers = ref([]);

const subjects = ref([]);
const selectedId = ref<number>(-1);

watch(selectedId, (newId) => {
  if(selectedId.value >= 0){
    getSubjectTeachers()
  }
});

onMounted(()=>{
  getSubjects()
})

const sendMail = (teacher) => {

  console.log(teacher)
  const currentDate = new Date();
  const formattedDate = currentDate.toISOString().split('T')[0];

  const oneWeekLater = new Date(currentDate);
  oneWeekLater.setDate(currentDate.getDate() + 7);
  const oneWeekLaterFormatted = oneWeekLater.toISOString().split('T')[0];

// 邮件主题
  const emailSubject = `【考试系统】出卷请求通知 - ${teacher["realName"]} - ${formattedDate}`;

// 邮件内容
  const emailContent = `
尊敬的 ${teacher["realName"]} 老师：

您好！我是教务处的老师。根据教学安排，现需要您协助完成以下课程的出卷工作：

出卷详情：
- 学科：${subjects.value.find(item => item.subjectId === selectedId.value)['subjectName']}
- 预计考试时间：90分钟
- 试卷难度：3
- 试卷数量：1 份
- 截止日期：${oneWeekLaterFormatted}

请您在 ${oneWeekLaterFormatted} 前完成试卷的编制工作，并将电子版发送至教务处邮箱。如有任何疑问，请随时与我联系。

感谢您的辛勤付出！

教务处
${currentDate}
联系电话：18333333333
邮箱：3333333333@qq.com
`;

// 使用示例
  console.log('邮件主题：', emailSubject);
  console.log('邮件内容：', emailContent);

  useRequest(()=>sendPrepareExamPaperMailAPI({ mail: teacher['email'], subject: emailSubject, content: emailContent}),{
    onSuccess: (res) => {
      if(res['code']==200){
        ElNotification({title: 'Success', message: "成功发送", type: 'success',})
        chooseExamTeacherDia.close()
      }else{
        ElNotification({title: 'Warning', message: res['msg'], type: 'warning',})
      }
    }
  })
}

const getSubjectTeachers = () => {
  useRequest(()=>getSubjectTeachersAPI({subjectId: selectedId.value}),{
    onSuccess: (res) => {
      if(res['code']==200){
        for(let i=0;i<res['data'].length;i++){
          console.log(res['data'][i])
          teachers.value = []
          getTeacherInfo(res['data'][i]['teacherId'])
        }
      }
    }
  })
}

const getSubjects = () => {
  useRequest(()=>getSubjectsAPI(null),{
    onSuccess: (res) => {
      if(res['code']==200){
        subjects.value = res['data']['subjects']
        // console.log(subjects.value)
      }
    }
  })
}

const getTeacherInfo = (userId: number) => {
  useRequest(()=>getUserInfoByIdAPI(userId, localStorage.getItem('token')),{
    onSuccess: (res) => {
      if(res['code']==200){
        console.log(res['data'])
        teachers.value.push(res['data'])
      }
    }
  })
}
</script>

<style scoped>

</style>