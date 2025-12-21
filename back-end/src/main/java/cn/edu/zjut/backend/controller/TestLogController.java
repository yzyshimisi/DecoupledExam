// TestLogController.java
package cn.edu.zjut.backend.controller;

import cn.edu.zjut.backend.annotation.LogRecord;
import cn.edu.zjut.backend.util.Response;
import cn.edu.zjut.backend.util.SecurityLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static cn.edu.zjut.backend.annotation.LogRecord.LogType;

@RestController
@RequestMapping("/api/test")
public class TestLogController {
    
    @Autowired
    private SecurityLogger securityLogger;

    @GetMapping("/operation-log")
    @LogRecord(module = "测试模块", action = "操作日志测试", targetType = "功能测试", logType = LogType.OPERATION)
    public Response<String> testOperationLog() {
        return Response.success("操作日志测试成功");
    }
    
    @GetMapping("/security-log")
    public Response<String> testSecurityLog() {
        // 模拟记录一条安全日志
        securityLogger.logSecurityEvent(
            1L, 
            "127.0.0.1", 
            "TEST_EVENT", 
            1, 
            "安全日志测试"
        );
        return Response.success("安全日志测试成功");
    }
    
    @GetMapping("/secure-operation")
    @LogRecord(module = "安全测试", action = "安全操作测试", targetType = "安全功能测试", logType = LogType.SECURITY)
    public Response<String> secureOperation() {
        return Response.success("安全操作测试成功");
    }
    
    @GetMapping("/login-operation")
    @LogRecord(module = "登录测试", action = "登录操作测试", targetType = "登录功能测试", logType = LogType.LOGIN)
    public Response<String> loginOperation() {
        return Response.success("登录操作测试成功");
    }
}