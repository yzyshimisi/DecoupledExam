// UserLoginLogService.java
package cn.edu.zjut.backend.service.i;

import cn.edu.zjut.backend.po.UserLoginLog;
import java.util.List;

public interface UserLoginLogService {
    void saveLog(UserLoginLog log);
    List<UserLoginLog> getLogList(int page, int size, String username, Integer loginStatus);
    List<UserLoginLog> getLogList(int page, int size, String username, Integer loginStatus, Long userId, String ip, String dateFrom, String dateTo);
    Long getTotalCount();
    Long getTotalCount(String username, Integer loginStatus);
    Long getTotalCount(String username, Integer loginStatus, Long userId, String ip, String dateFrom, String dateTo);
}