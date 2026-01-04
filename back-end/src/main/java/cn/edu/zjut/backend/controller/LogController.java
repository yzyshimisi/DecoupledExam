// 管理员日志查询控制器
package cn.edu.zjut.backend.controller;

import cn.edu.zjut.backend.po.SystemOperationLog;
import cn.edu.zjut.backend.po.SecurityEventLog;
import cn.edu.zjut.backend.po.UserLoginLog;
import cn.edu.zjut.backend.service.i.SystemOperationLogService;
import cn.edu.zjut.backend.service.i.SecurityEventLogService;
import cn.edu.zjut.backend.service.i.UserLoginLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/logs")
public class LogController {

    @Autowired
    private SystemOperationLogService systemLogService;
    
    @Autowired
    private SecurityEventLogService securityLogService;
    
    @Autowired
    private UserLoginLogService userLoginLogService;

    @GetMapping
    public Map<String, Object> getLogs(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "type", required = false) String logType,
            @RequestParam(name = "module", required = false) String module,
            @RequestParam(name = "username", required = false) String username,
            @RequestParam(name = "eventType", required = false) String eventType,
            @RequestParam(name = "loginStatus", required = false) Integer loginStatus,
            @RequestParam(name = "userId", required = false) Long userId,
            @RequestParam(name = "ip", required = false) String ip,
            @RequestParam(name = "action", required = false) String action,
            @RequestParam(name = "dateFrom", required = false) String dateFrom,
            @RequestParam(name = "dateTo", required = false) String dateTo) {

        Map<String, Object> result = new HashMap<>();
        result.put("page", page);
        result.put("size", size);
        result.put("type", logType);

        // 根据日志类型查询不同的日志表
        switch (logType != null ? logType.toLowerCase() : "all") {
            case "system":
                List<SystemOperationLog> systemLogs = systemLogService.getLogList(page, size, module, username, userId, action, dateFrom, dateTo);
                Long systemTotal = systemLogService.getTotalCount(module, username, userId, action, dateFrom, dateTo);
                result.put("logs", systemLogs);
                result.put("total", systemTotal);
                result.put("pages", (int) Math.ceil((double) systemTotal / size));
                result.put("logType", "system");
                break;
            case "security":
                List<SecurityEventLog> securityLogs = securityLogService.getLogList(page, size, eventType, null);
                Long securityTotal = securityLogService.getTotalCount(eventType, null);
                result.put("logs", securityLogs);
                result.put("total", securityTotal);
                result.put("pages", (int) Math.ceil((double) securityTotal / size));
                result.put("logType", "security");
                break;
            case "login":
                List<UserLoginLog> loginLogs = userLoginLogService.getLogList(page, size, username, loginStatus, userId, ip, dateFrom, dateTo);
                Long loginTotal = userLoginLogService.getTotalCount(username, loginStatus, userId, ip, dateFrom, dateTo);
                result.put("logs", loginLogs);
                result.put("total", loginTotal);
                result.put("pages", (int) Math.ceil((double) loginTotal / size));
                result.put("logType", "login");
                break;
            case "all":
            default:
                // 返回所有类型的日志统计信息
                Long systemCount = systemLogService.getTotalCount(null, null, null, null, null, null);
                Long securityCount = securityLogService.getTotalCount(null, null);
                Long loginCount = userLoginLogService.getTotalCount(null, null, null, null, null, null);
                
                Map<String, Object> summary = new HashMap<>();
                summary.put("systemLogs", systemCount);
                summary.put("securityLogs", securityCount);
                summary.put("loginLogs", loginCount);
                summary.put("totalLogs", systemCount + securityCount + loginCount);
                
                result.put("summary", summary);
                result.put("logType", "summary");
                break;
        }

        return result;
    }
}