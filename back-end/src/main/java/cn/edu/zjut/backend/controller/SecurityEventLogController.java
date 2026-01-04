// SecurityEventLogController.java
package cn.edu.zjut.backend.controller;

import cn.edu.zjut.backend.po.SecurityEventLog;
import cn.edu.zjut.backend.service.i.SecurityEventLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/security-events")
public class SecurityEventLogController {

    @Autowired
    private SecurityEventLogService logService;

    @GetMapping
    public Map<String, Object> getLogs(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) Integer riskLevel,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String ip,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo) {

        List<SecurityEventLog> logs = logService.getLogList(page, size, eventType, riskLevel, userId, ip, dateFrom, dateTo);
        Long total = logService.getTotalCount(eventType, riskLevel, userId, ip, dateFrom, dateTo);

        Map<String, Object> result = new HashMap<>();
        result.put("logs", logs);
        result.put("total", total);
        result.put("pages", (int) Math.ceil((double) total / size));
        return result;
    }
}