import { request } from '../axios';

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

// 定义日志类型枚举
enum LogType {
  SYSTEM_OPERATION = 'system-operation',
  SECURITY_EVENT = 'security-event',
  USER_LOGIN = 'user-login',
}

interface SystemOperationLog {
  type: LogType.SYSTEM_OPERATION;
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
  type: LogType.SECURITY_EVENT;
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
  type: LogType.USER_LOGIN;
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

const getSystemOperationLogsAPI = async (params?: LogQueryParams) => {
  // 后端没有专门的系统操作日志API，使用通用日志API并指定type=system
  const extendedParams = { ...params, type: 'system' };
  return request("/api/admin/logs", {
    method: "GET",
    headers: {"Content-Type": "application/json", "Authorization": `Bearer ${localStorage.getItem("token")}`},
    params: extendedParams
  });
};

const getSecurityEventLogsAPI = async (params?: LogQueryParams) => {
  return request("/api/admin/security-events", {
    method: "GET",
    headers: {"Content-Type": "application/json", "Authorization": `Bearer ${localStorage.getItem("token")}`},
    params: params
  });
};

const getUserLoginLogsAPI = async (params?: LogQueryParams) => {
  return request("/api/admin/login-logs", {
    method: "GET",
    headers: {"Content-Type": "application/json", "Authorization": `Bearer ${localStorage.getItem("token")}`},
    params: params
  });
};

export {
  getSystemOperationLogsAPI,
  getSecurityEventLogsAPI,
  getUserLoginLogsAPI,
  LogType,
};

export type {
  SystemOperationLog,
  SecurityEventLog,
  UserLoginLog,
  LogQueryParams,
};