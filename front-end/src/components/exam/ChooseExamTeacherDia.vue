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

      <!-- 老师选择 -->
<!--      <div class="form-control w-full">-->
<!--        <label class="label">-->
<!--          <span class="label-text">老师姓名</span>-->
<!--        </label>-->
<!--        <select-->
<!--            v-model="selectedTeacherId"-->
<!--            class="select select-bordered w-full text-base"-->
<!--            :disabled="filteredTeachers.length === 0"-->
<!--        >-->
<!--          <option value="" disabled selected>-->
<!--            {{ filteredTeachers.length === 0 ? '无符合条件的老师' : '请选择一位老师' }}-->
<!--          </option>-->
<!--          <option-->
<!--              v-for="teacher in filteredTeachers"-->
<!--              :key="teacher.id"-->
<!--              :value="teacher.id"-->
<!--          >-->
<!--            {{ teacher.name }}（{{ teacher.subject || '未指定学科' }}）-->
<!--          </option>-->
<!--        </select>-->
<!--      </div>-->

<!--      <div class="modal-action mt-6">-->
<!--        <button-->
<!--            type="button"-->
<!--            class="btn btn-ghost"-->
<!--            @click="closeModal"-->
<!--        >-->
<!--          取消-->
<!--        </button>-->
<!--        <button-->
<!--            type="button"-->
<!--            class="btn btn-primary"-->
<!--            :disabled="!selectedTeacherId"-->
<!--            @click="confirmSelection"-->
<!--        >-->
<!--          确认-->
<!--        </button>-->
<!--      </div>-->
    </div>
    <form method="dialog" class="modal-backdrop">
      <button>close</button>
    </form>
  </dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue';
import { useRequest } from "vue-hooks-plus";
import {getSubjectsAPI, getSubjectTeachersAPI} from "../../apis"

// 老师数据结构：新增 subject 字段
interface Teacher {
  id: string | number;
  name: string;
  subject: string; // 学科，如 "Java", "数据结构", "操作系统"
  department?: string;
}

const emit = defineEmits<{
  (e: 'select', teacher: Teacher): void;
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

const getSubjectTeachers = () => {
  useRequest(()=>getSubjectTeachersAPI({subjectId: selectedId.value}),{
    onSuccess: (res) => {
      if(res['code']==200){
        console.log(res['data'])
      }
    }
  })
}

const getSubjects = () => {
  useRequest(()=>getSubjectsAPI(null),{
    onSuccess: (res) => {
      if(res['code']==200){
        subjects.value = res['data']['subjects']
        console.log(subjects.value)
      }
    }
  })
}
</script>

<style scoped>

</style>