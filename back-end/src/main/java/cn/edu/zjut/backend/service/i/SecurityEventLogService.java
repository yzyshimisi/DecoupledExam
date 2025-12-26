// SecurityEventLogService.java
package cn.edu.zjut.backend.service.i;

import cn.edu.zjut.backend.po.SecurityEventLog;
import java.util.List;

public interface SecurityEventLogService {
    void saveLog(SecurityEventLog log);
    List<SecurityEventLog> getLogList(int page, int size, String eventType, Integer riskLevel);
    List<SecurityEventLog> getLogList(int page, int size, String eventType, Integer riskLevel, Long userId, String ip, String dateFrom, String dateTo);
    Long getTotalCount();
    Long getTotalCount(String eventType, Integer riskLevel);
    Long getTotalCount(String eventType, Integer riskLevel, Long userId, String ip, String dateFrom, String dateTo);
}