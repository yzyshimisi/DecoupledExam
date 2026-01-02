<template>
  <div class="p-6 space-y-6 text-base">
    <!-- 筛选栏 -->
    <ExamPaperFilter />

    <!-- 试卷表格 -->
    <div class="overflow-x-auto flex flex-col items-center">

      <!-- 批量操作控制栏（仅在批量模式下显示） -->
      <div v-if="isBatchMode" class="w-3/4 flex justify-between items-center mt-4">
        <div>
          <label class="label cursor-pointer">
            <input
                type="checkbox"
                class="checkbox"
                :checked="selectedIds.size === papers.length && papers.length > 0"
                @change="toggleSelectAll"
            />
            <span class="label-text ml-2">全选</span>
          </label>
        </div>
        <div class="flex gap-2">
          <button
              v-if="selectedIds.size > 0"
              class="btn btn-error btn-sm"
              @click="batchDelete"
          >
            删除（{{ selectedIds.size }}）
          </button>
          <button class="btn btn-ghost btn-sm" @click="exitBatchMode">
            取消
          </button>
        </div>
      </div>

      <!-- 按钮区域  -->
      <div v-if="!isBatchMode" class="flex gap-4 w-3/4 mt-4">
        <button class="btn btn-primary text-base px-6" @click="isOpenManualComposeDia = true">
          手动组卷
        </button>
        <button class="btn btn-secondary text-base px-6" @click="isOpenSmartComposeDia = true">
          智能组卷
        </button>
        <!-- 批量操作 -->
        <button
            v-if="papers.length > 0"
            class="btn btn-primary text-base px-6 py-2 rounded-full"
            @click="enterBatchMode"
        >
          批量操作
        </button>
      </div>

      <table class="table w-3/4 text-base mt-4">
        <thead>
        <tr>
          <!-- 多选框列：仅在批量模式下显示 -->
          <th v-if="isBatchMode"></th>
          <th>顺序号</th>
          <th>试卷名称</th>
          <th>试卷总分</th>
          <th>组卷方式</th>
          <th>是否封存</th>
          <th>创建时间</th>
          <th>修改时间</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="(paper, index) in papers" :key="paper.id" class="hover cursor-pointer" @click="toggleSelect(paper.paperId)" @contextmenu.prevent="openMenu($event,index)">
          <!-- 多选框单元格：仅在批量模式下显示 -->
          <td v-if="isBatchMode">
            <input
                type="checkbox"
                class="checkbox"
                :value="paper.paperId"
                :checked="selectedIds.has(paper.paperId)"
                @click.stop="toggleSelect(paper.paperId)"
            />
          </td>
          <td>{{ index + 1 }}</td>
          <td>{{ paper['paperName'] }}</td>
          <td>{{ paper.totalScore }}</td>
          <td>
            <span v-if="paper['composeType'] === '1'" class="badge badge-neutral text-base">手动组卷</span>
            <span v-else class="badge badge-accent text-base">自动组卷</span>
          </td>
          <td>
            <span v-if="paper.isSealed === '1'" class="badge badge-success text-base">是</span>
            <span v-else class="badge badge-error text-base">否</span>
          </td>
          <td>{{ formatTimestamp(paper.createTime) }}</td>
          <td>{{ formatTimestamp(paper.updatedAt) }}</td>
        </tr>
        </tbody>
      </table>

      <!-- 无数据提示 -->
      <div v-if="papers.length === 0" class="text-gray-500 mt-8">
        暂无试卷数据
      </div>
    </div>
  </div>

  <ManualComposeDialog
      :open="isOpenManualComposeDia"
      :questionTypes="questionTypes"
      @update:open="(val) => { isOpenManualComposeDia = val }"
      @success="getExamPapers()"
  />
  <SmartComposeDialog
      :open="isOpenSmartComposeDia"
      :questionTypes="questionTypes"
      :subjects="subjectList"
      @update:open="(val) => { isOpenSmartComposeDia = val }"
      @success="getExamPapers()"
  />
  <EditExamPaperDialog
      v-if="isOpenEditExamPaperDia"
      :open="isOpenEditExamPaperDia"
      :paper="papers[nowInd]"
      :questionTypes="questionTypes"
      @update:open = "(val) => { isOpenEditExamPaperDia = val }"
      @success="getExamPapers()"
  />
  <dialog v-if="nowInd>=0" id="paperPreviewDialog" class="modal">
    <div class="modal-box max-w-[70vw] w-[70vw]">
      <PaperPreview
          :paper="papers[nowInd]"
          :questionTypes="questionTypes"
      />
    </div>
    <form method="dialog" class="modal-backdrop" @click="nowInd=-1">
      <button>close</button>
    </form>
  </dialog>
  <!-- 右键菜单 -->
  <ul
      v-if="nowInd>=0"
      class="menu bg-base-300 rounded-box fixed"
      :class="isShowMenu ? '' : 'invisible'"
      :style="{left:menuPos.left+'px',top:menuPos.top+'px'}"
  >
    <li @click="isOpenEditExamPaperDia=true"><a>编辑试卷</a></li>
    <li @click="deleteExamPapers"><a>删除试卷</a></li>
    <li onclick="paperPreviewDialog.showModal()"><a>预览试卷</a></li>
    <li @click=""><a>封存</a></li>
  </ul>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import {
  ExamPaperFilter,
  ManualComposeDialog,
  SmartComposeDialog,
  EditExamPaperDialog,
  PaperPreview
} from '../../components'
import { getExamPapersAPI, getQuestionTypeAPI, getSubjectsAPI, deleteExamPapersAPI } from '../../apis'
import { useRequest } from 'vue-hooks-plus'

// --- 数据 ---
const subjectList = ref([])
const questionTypes = ref([])
const papers = ref([])

// --- 对话框状态 ---
const isOpenManualComposeDia = ref<boolean>(false)
const isOpenSmartComposeDia = ref<boolean>(false)
const isOpenEditExamPaperDia = ref<boolean>(false)

// --- 批量操作状态 ---
const isBatchMode = ref(false)
const selectedIds = ref(new Set<number>())

// --- 生命周期 ---
onMounted(() => {
  getExamPapers()
  getQuestionType()
  getSubjects()
})

const isShowMenu = ref<boolean>(false)
const menuPos = ref({
  left: 0,
  top: 0
})

const nowId = ref<number>(-1);
const nowInd = ref<number>(-1);

const openMenu = (event, ind) => {

  nowInd.value = ind
  nowId.value = papers.value[ind].paperId

  isShowMenu.value = true
  menuPos.value.left = event.clientX
  menuPos.value.top = event.clientY

  window.addEventListener('scroll', closeMenu);    // 滚动页面，也要关闭菜单
  window.addEventListener('click', closeMenu);
}

const closeMenu = () => {
  isShowMenu.value = false
  window.removeEventListener('scroll', closeMenu);
}

const changeSealStatus = () => {

}

// --- API 方法 ---
const getQuestionType = () => {
  useRequest(() => getQuestionTypeAPI(), {
    onSuccess(res) {
      if (res['code'] === 200) {
        questionTypes.value = res['data']
      }
    }
  })
}

const getSubjects = () => {
  useRequest(() => getSubjectsAPI(null), {
    onSuccess(res) {
      if (res['code'] === 200) {
        subjectList.value = res['data'].subjects
      }
    }
  })
}

const getExamPapers = () => {
  nowInd.value = -1

  useRequest(() => getExamPapersAPI(), {
    onSuccess(res) {
      if (res['code'] === 200) {
        papers.value = res['data']
        // 如果当前在批量模式，清理已不存在的选中项
        if (isBatchMode.value) {
          const validIds = new Set(papers.value.map(p => p.id))
          selectedIds.value = new Set([...selectedIds.value].filter(id => validIds.has(id)))
        }
      }
    }
  })
}

// --- 工具函数 ---
const formatTimestamp = (timestamp: number): string => {
  const date = new Date(timestamp)
  const pad = (n: number) => n.toString().padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

// --- 批量操作逻辑 ---
const enterBatchMode = () => {
  isBatchMode.value = true
  selectedIds.value = new Set()
}

const exitBatchMode = () => {
  isBatchMode.value = false
  selectedIds.value.clear()
}

const toggleSelect = (id: number) => {
  if (selectedIds.value.has(id)) {
    selectedIds.value.delete(id)
  } else {
    selectedIds.value.add(id)
  }
}

const toggleSelectAll = () => {
  if (selectedIds.value.size === papers.value.length) {
    selectedIds.value.clear()
  } else {
    selectedIds.value = new Set(papers.value.map(p => p.paperId))
  }
}

const deleteExamPapers = () => {
  useRequest(()=>deleteExamPapersAPI([nowId.value]), {
    onSuccess(res) {
      if (res['code'] === 200) {
        getExamPapers()
        alert('删除成功')
      } else {
        alert('删除失败：' + (res || '未知错误'))
      }
    },

    onError(err){
      alert('删除失败：' + (err || '未知错误'))
    }
  })
}

const batchDelete = async () => {
  if (selectedIds.value.size === 0) return

  if (!confirm(`确定要删除选中的 ${selectedIds.value.size} 份试卷吗？此操作不可恢复！`)) {
    return
  }

  useRequest(()=>deleteExamPapersAPI([...selectedIds.value]), {
    onSuccess(res) {
      if (res['code'] === 200) {
        exitBatchMode()
        getExamPapers()
        alert('删除成功')
      } else {
        alert('删除失败：' + (res || '未知错误'))
      }
    },

    onError(err){
      alert('删除失败：' + (err || '未知错误'))
    }
  })
}
</script>