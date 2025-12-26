package cn.edu.zjut.backend.service.i;

import cn.edu.zjut.backend.po.SystemOperationLog;
import java.util.List;

public interface SystemOperationLogService {
    void saveLog(SystemOperationLog log);
    List<SystemOperationLog> getLogList(int page, int size, String module, String username);
    List<SystemOperationLog> getLogList(int page, int size, String module, String username, Long userId, String action, String dateFrom, String dateTo);
    Long getTotalCount();
    Long getTotalCount(String module, String username);
    Long getTotalCount(String module, String username, Long userId, String action, String dateFrom, String dateTo);

}