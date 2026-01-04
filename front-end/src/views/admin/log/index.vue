<template>
  <div class="p-4 text-base">
    <div class="flex flex-col gap-3">
      <!-- 筛选栏 -->
      <aside
          v-if="showSidebar"
          class="w-full flex-shrink-0 rounded-box bg-base-100 p-4"
      >
        <div class="space-y-4">
          <h3 class="font-semibold">筛选条件</h3>
          
          <div class="form-control">
            <label class="label">
              <span class="label-text">日志类型</span>
            </label>
            <select 
              v-model="filters.logType" 
              class="select select-bordered"
              @change="handleFilterChange"
            >
              <option value="">全部类型</option>
              <option :value="LogType.SYSTEM_OPERATION">系统操作日志</option>
              <option :value="LogType.SECURITY_EVENT">安全事件日志</option>
              <option :value="LogType.USER_LOGIN">用户登录日志</option>
            </select>
          </div>
          
          <div class="form-control">
            <label class="label">
              <span class="label-text">用户ID</span>
            </label>
            <input
              v-model="filters.userId"
              type="text"
              placeholder="输入用户ID"
              class="input input-bordered"
              @change="handleFilterChange"
            />
          </div>
          
          <div class="form-control">
            <label class="label">
              <span class="label-text">模块</span>
            </label>
            <input
              v-model="filters.module"
              type="text"
              placeholder="输入模块"
              class="input input-bordered"
              @change="handleFilterChange"
            />
          </div>
          
          <div class="form-control">
            <label class="label">
              <span class="label-text">IP地址</span>
            </label>
            <input
              v-model="filters.ipAddress"
              type="text"
              placeholder="输入IP地址"
              class="input input-bordered"
              @change="handleFilterChange"
            />
          </div>
          
          <div class="form-control">
            <label class="label">
              <span class="label-text">时间范围</span>
            </label>
            <div class="flex gap-2">
              <input
                v-model="filters.startDate"
                type="date"
                class="input input-bordered flex-1"
                @change="handleFilterChange"
              />
              <input
                v-model="filters.endDate"
                type="date"
                class="input input-bordered flex-1"
                @change="handleFilterChange"
              />
            </div>
          </div>
          
          <button class="btn btn-outline w-full" @click="resetFilters">
            重置筛选
          </button>
        </div>
      </aside>

      <!-- 主内容区 -->
      <main class="flex-1 flex flex-col items-center">
        <div class="p-6 space-y-6 text-base w-full">
          <!-- 操作区域 -->
          <div class="flex flex-wrap gap-3 items-center">
            <h2 class="text-xl font-bold">系统日志管理</h2>
            
            <div class="flex-1"></div> <!-- 右对齐后面的按钮 -->
            
            <button class="btn btn-secondary" @click="refreshLogs">
              <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"></path>
              </svg>
              刷新
            </button>
          </div>

          <!-- 日志表格 -->
          <div v-show="!isLoading" class="overflow-x-auto rounded-box border">
            <table class="table w-full">
              <thead>
              <tr>
                <th>序号</th>
                <th>时间</th>
                <th v-if="filters.logType !== LogType.USER_LOGIN">用户ID</th>
                <th v-if="filters.logType === LogType.USER_LOGIN">用户名</th>
                <th v-if="filters.logType === LogType.SYSTEM_OPERATION">模块</th>
                <th v-if="filters.logType === LogType.SYSTEM_OPERATION">操作</th>
                <th v-if="filters.logType === LogType.SECURITY_EVENT">事件类型</th>
                <th v-if="filters.logType === LogType.SECURITY_EVENT">风险等级</th>
                <th v-if="filters.logType === LogType.SECURITY_EVENT">是否解决</th>
                <th v-if="filters.logType === LogType.USER_LOGIN">登录状态</th>
                <th>IP地址</th>
                <th>描述</th>
                <th v-if="filters.logType === LogType.SYSTEM_OPERATION">执行时间(ms)</th>
                <th v-if="filters.logType === LogType.SECURITY_EVENT">解决详情</th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="(log, index) in logs" :key="getLogKey(log)" class="hover">
                <td>{{ index + 1 }}</td>
                <td class="whitespace-nowrap">
                  {{ formatDate(getLogTime(log)) }}
                </td>
                <td v-if="log.type !== LogType.USER_LOGIN">{{ log.userId }}</td>
                <td v-if="log.type === LogType.USER_LOGIN">{{ log.username }}</td>
                <td v-if="log.type === LogType.SYSTEM_OPERATION">{{ log.module }}</td>
                <td v-if="log.type === LogType.SYSTEM_OPERATION">{{ log.action }}</td>
                <td v-if="log.type === LogType.SECURITY_EVENT">{{ log.eventType }}</td>
                <td v-if="log.type === LogType.SECURITY_EVENT">
                  <span 
                    class="badge"
                    :class="{
                      'badge-error': log.riskLevel > 5,
                      'badge-warning': log.riskLevel > 2,
                      'badge-success': log.riskLevel <= 2
                    }"
                  >
                    {{ log.riskLevel }}
                  </span>
                </td>
                <td v-if="log.type === LogType.SECURITY_EVENT">
                  <span 
                    class="badge"
                    :class="{
                      'badge-success': log.isResolved === 1,
                      'badge-error': log.isResolved === 0
                    }"
                  >
                    {{ log.isResolved === 1 ? '已解决' : '未解决' }}
                  </span>
                </td>
                <td v-if="log.type === LogType.USER_LOGIN">
                  <span 
                    class="badge"
                    :class="{
                      'badge-success': log.loginStatus === 1,
                      'badge-error': log.loginStatus === 0
                    }"
                  >
                    {{ log.loginStatus === 1 ? '成功' : '失败' }}
                  </span>
                </td>
                <td>{{ log.ipAddress }}</td>
                <td class="max-w-xs truncate" :title="getLogDesc(log)">
                  {{ getLogDesc(log) }}
                </td>
                <td v-if="log.type === LogType.SYSTEM_OPERATION">{{ log.executionTimeMs }}</td>
                <td v-if="log.type === LogType.SECURITY_EVENT">{{ log.resolutionDetail }}</td>
              </tr>
              </tbody>
            </table>
          </div>
          
          <div v-show="isLoading" class="flex gap-3 justify-center">
            <span class="loading loading-ring loading-xs"></span>
            <span class="loading loading-ring loading-sm"></span>
            <span class="loading loading-ring loading-md"></span>
            <span class="loading loading-ring loading-lg"></span>
          </div>

          <div v-if="logs.length === 0" class="text-center py-8 text-gray-500">
            暂无日志数据
          </div>
          
          <div v-else class="flex justify-center items-center gap-4">
            <div class="join">
              <button @click="handlePrevPage" class="join-item btn">«</button>
              <button @dblclick="modifyPageNum" class="join-item btn">Page
                <div v-if="!isModifyPageNum">{{ pageInfo.pageNum }}</div>
                <div v-else>
                  <input
                      v-model="tempPageNum"
                      @blur="overModifyPageNum"
                      @keyup.enter="overModifyPageNum"
                      id="pageNumInput"
                      type="text"
                      placeholder="页码"
                      class="input-xs max-w-10 min-w-4 text-base" />
                </div>
              </button>
              <button @click="handleNextPage" class="join-item btn">»</button>
            </div>
            <span>共 {{ totalPageNum }} 页</span>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, watch } from 'vue'
import { useRequest } from "vue-hooks-plus";
import { 
  getSystemOperationLogsAPI, 
  getSecurityEventLogsAPI, 
  getUserLoginLogsAPI,
  LogType
} from '../../../apis/Server/getLogsAPI';
import { request } from '../../../apis/axios'; // 导入request方法

// 2. 给每个日志接口添加 type 字段（核心：用于TS类型收窄）
interface SystemOperationLog {
  type: LogType.SYSTEM_OPERATION; // 新增标识
  logId: number;
  logTime: string;
  userId: number;
  userRole: string;
  module: string;
  action: string;
  targetType: string;
  targetId: string;
  description: string;
  ipAddress: string;
  status: number;
  executionTimeMs: number;
  extraData: string;
}

interface SecurityEventLog {
  type: LogType.SECURITY_EVENT; // 新增标识
  eventId: number;
  eventTime: string;
  userId: number;
  ipAddress: string;
  eventType: string;
  riskLevel: number;
  description: string;
  isResolved: number;
  resolutionDetail: string;
  extraData: string;
}

interface UserLoginLog {
  type: LogType.USER_LOGIN; // 新增标识
  loginId: number;
  userId: number;
  username: string;
  loginTime: string;
  ipAddress: string;
  userAgent: string;
  loginStatus: number;
  failureReason: string;
  sessionId: string;
}

interface LogQueryParams {
  pageNum?: number;
  pageSize?: number;
  startDate?: string;
  endDate?: string;
  userId?: string;
  module?: string;
  action?: string;
  ipAddress?: string;
  status?: number; // 1成功 0失败
}

// 3. 同步更新筛选条件的类型
interface LogFilters {
  logType: LogType | ''; // 改用枚举约束
  userId: string;
  module: string;
  ipAddress: string;
  startDate: string;
  endDate: string;
}

interface PageInfo {
  pageNum: number;
  pageSize: 10;
}

// 4. 联合类型不变（但每个成员已有唯一type字段）
type UnifiedLog = SystemOperationLog | SecurityEventLog | UserLoginLog;

// 筛选状态
const filters = ref<LogFilters>({
  logType: '',
  userId: '',
  module: '',
  ipAddress: '',
  startDate: '',
  endDate: '',
});

const pageInfo = ref<PageInfo>({
  pageNum: 1,
  pageSize: 10
});

const logs = ref<UnifiedLog[]>([]);
const totalPageNum = ref<number>(1);
const isLoading = ref<boolean>(false);
const isModifyPageNum = ref(false);
const tempPageNum = ref<number>();

// 响应式侧边栏
const showSidebar = ref(window.innerWidth >= 1024);

// 监听筛选条件变化
watch(filters, () => {
  pageInfo.value.pageNum = 1;
  getLogs();
}, { deep: true });

onMounted(() => {
  getLogs();
});

const getLogs = async () => {
  console.log('开始获取日志...'); // 调试信息
  isLoading.value = true;
  
  try {
    let response;
    const params = {
      page: pageInfo.value.pageNum,  // 后端使用 page 而不是 pageNum
      size: pageInfo.value.pageSize, // 后端使用 size 而不是 pageSize
      type: filters.value.logType || undefined, // 使用后端支持的type参数
      userId: filters.value.userId || undefined,
      module: filters.value.module || undefined,
      ip: filters.value.ipAddress || undefined,
      dateFrom: filters.value.startDate || undefined,
      dateTo: filters.value.endDate || undefined,
    };

    console.log('请求参数:', params); // 调试信息

    // 当没有选择特定类型时，获取所有类型的日志摘要
    if (filters.value.logType === '') {
      console.log('请求所有类型日志摘要');
      // 使用后端通用日志API，不传递type参数以获取摘要信息
      delete params.type; // 不传递type参数以获取所有类型摘要
      response = await request('/api/admin/logs', {
        method: "GET",
        headers: {"Content-Type": "application/json", "Authorization": `Bearer ${localStorage.getItem("token")}`},
        params: params
      });
      
      // 检查响应是否为摘要信息
      if (response && response.logType === 'summary' && response.summary) {
        // 显示摘要信息而不是具体日志
        logs.value = []; // 不显示具体日志条目
        
        // 创建摘要信息显示
        const summaryLog = {
          type: LogType.SYSTEM_OPERATION, // 使用系统操作日志类型作为摘要显示
          logId: 0,
          logTime: new Date().toISOString(),
          userId: 0,
          userRole: '',
          module: '日志摘要',
          action: '统计信息',
          targetType: '',
          targetId: '',
          description: `总日志数: ${response.summary.totalLogs}, 系统操作: ${response.summary.systemLogs}, 安全事件: ${response.summary.securityLogs}, 用户登录: ${response.summary.loginLogs}`,
          ipAddress: '',
          status: 1,
          executionTimeMs: 0,
          extraData: JSON.stringify(response.summary)
        };
        
        logs.value = [summaryLog];
        totalPageNum.value = 1;
      } else {
        logs.value = [];
        totalPageNum.value = 1;
      }
    } else {
      let currentLogType: LogType;
      switch (filters.value.logType) {
        case LogType.SYSTEM_OPERATION:
          console.log('请求系统操作日志');
          // 使用通用API并指定type=system
          params.type = 'system';
          response = await request('/api/admin/logs', {
            method: "GET",
            headers: {"Content-Type": "application/json", "Authorization": `Bearer ${localStorage.getItem("token")}`},
            params: params
          });
          currentLogType = LogType.SYSTEM_OPERATION;
          break;
        case LogType.SECURITY_EVENT:
          console.log('请求安全事件日志');
          response = await getSecurityEventLogsAPI({
            page: params.page,
            size: params.size,
            dateFrom: params.dateFrom,
            dateTo: params.dateTo,
            userId: params.userId,
            ip: params.ip,
          });
          currentLogType = LogType.SECURITY_EVENT;
          break;
        case LogType.USER_LOGIN:
          console.log('请求用户登录日志');
          response = await getUserLoginLogsAPI({
            page: params.page,
            size: params.size,
            dateFrom: params.dateFrom,
            dateTo: params.dateTo,
            userId: params.userId,
            ip: params.ip,
          });
          currentLogType = LogType.USER_LOGIN;
          break;
        default:
          console.log('未知日志类型');
          response = null;
          currentLogType = LogType.SYSTEM_OPERATION;
          break;
      }
      
      console.log('API响应:', response); // 调试信息

      if (response) {
        let rawLogs: any[] = [];
        if (response.code === 200 && response.data) {
          rawLogs = response.data.logs || response.data || [];
          totalPageNum.value = response.data.pages || response.data.totalPages || 1;
        } else if (typeof response === 'object' && response.logs) {
          rawLogs = response.logs || [];
          totalPageNum.value = response.pages || 1;
        } else if (Array.isArray(response)) {
          rawLogs = response;
          totalPageNum.value = 1;
        } else {
          rawLogs = response || [];
          totalPageNum.value = 1;
        }
        
        // 单一类型日志，直接添加type标识
        logs.value = rawLogs.map(log => ({ ...log, type: currentLogType }));
      } else {
        logs.value = [];
        totalPageNum.value = 1;
      }
    }
    
    console.log('处理后的日志数据:', logs.value); // 调试信息
  } catch (error) {
    console.error('获取日志失败:', error);
    logs.value = [];
    totalPageNum.value = 1;
  } finally {
    isLoading.value = false;
  }
};

// 重置筛选条件
const resetFilters = () => {
  filters.value = {
    logType: '',
    userId: '',
    module: '',
    ipAddress: '',
    startDate: '',
    endDate: '',
  };
  pageInfo.value.pageNum = 1;
  getLogs();
};

// 刷新日志
const refreshLogs = () => {
  getLogs();
};

// 处理筛选条件变化
const handleFilterChange = () => {
  pageInfo.value.pageNum = 1;
  getLogs();
};

// 分页相关方法
const handlePrevPage = () => {
  if (pageInfo.value.pageNum > 1) {
    pageInfo.value.pageNum--;
    getLogs();
  }
};

const handleNextPage = () => {
  if (pageInfo.value.pageNum < totalPageNum.value) {
    pageInfo.value.pageNum++;
    getLogs();
  }
};

const modifyPageNum = () => {
  tempPageNum.value = pageInfo.value.pageNum;
  isModifyPageNum.value = true;
  nextTick(() => {
    const pageNumInput = document.getElementById('pageNumInput') as HTMLInputElement;
    if (pageNumInput) {
      pageNumInput.focus();
      pageNumInput.setSelectionRange(pageNumInput.value.length, pageNumInput.value.length);
    }
  });
};

const overModifyPageNum = () => {
  isModifyPageNum.value = false;
  if (tempPageNum.value && tempPageNum.value >= 1 && tempPageNum.value <= totalPageNum.value) {
    pageInfo.value.pageNum = tempPageNum.value;
    getLogs();
  }
};

// 新增：获取日志唯一key
const getLogKey = (log: UnifiedLog) => {
  switch (log.type) {
    case LogType.SYSTEM_OPERATION:
      return log.logId;
    case LogType.SECURITY_EVENT:
      return log.eventId;
    case LogType.USER_LOGIN:
      return log.loginId;
  }
};

// 新增：获取日志时间
const getLogTime = (log: UnifiedLog) => {
  switch (log.type) {
    case LogType.SYSTEM_OPERATION:
      return log.logTime;
    case LogType.SECURITY_EVENT:
      return log.eventTime;
    case LogType.USER_LOGIN:
      return log.loginTime;
  }
};

// 新增：获取日志描述
const getLogDesc = (log: UnifiedLog) => {
  switch (log.type) {
    case LogType.SYSTEM_OPERATION:
      return log.description;
    case LogType.SECURITY_EVENT:
      return log.resolutionDetail || log.description;
    case LogType.USER_LOGIN:
      return log.failureReason;
  }
};

// 格式化日期
const formatDate = (isoString: string): string => {
  if (!isoString) return '';
  const date = new Date(isoString);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  });
};
</script>