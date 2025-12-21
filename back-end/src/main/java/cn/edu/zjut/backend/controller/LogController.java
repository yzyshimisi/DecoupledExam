// 管理员日志查询控制器
package cn.edu.zjut.backend.controller;

import cn.edu.zjut.backend.po.SystemOperationLog;
import cn.edu.zjut.backend.service.i.SystemOperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/logs")
public class LogController {

    @Autowired
    private SystemOperationLogService logService;

    @GetMapping
    public Map<String, Object> getLogs(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            String module,
            String username) {

        List<SystemOperationLog> logs = logService.getLogList(page, size, module, username);
        Long total = logService.getTotalCount();

        Map<String, Object> result = new HashMap<>();
        result.put("logs", logs);
        result.put("total", total);
        result.put("pages", (total + size - 1) / size);
        return result;
    }
}