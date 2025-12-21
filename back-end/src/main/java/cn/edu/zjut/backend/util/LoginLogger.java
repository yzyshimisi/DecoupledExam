// LoginLogger.java
package cn.edu.zjut.backend.util;

import cn.edu.zjut.backend.po.UserLoginLog;
import cn.edu.zjut.backend.service.i.UserLoginLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class LoginLogger {
    
    @Autowired
    private UserLoginLogService userLoginLogService;
    
    /**
     * 记录用户登录日志
     * @param userId 用户ID
     * @param username 用户名
     * @param ipAddress 登录IP
     * @param userAgent 浏览器信息
     * @param loginStatus 登录状态 (1成功 0失败)
     * @param failureReason 失败原因（登录失败时填写）
     * @param sessionId 会话ID
     */
    public void logUserLogin(Long userId, String username, String ipAddress, 
                           String userAgent, Integer loginStatus, String failureReason, 
                           String sessionId) {
        UserLoginLog log = new UserLoginLog();
        log.setLoginTime(new Date());
        log.setUserId(userId);
        log.setUsername(username);
        log.setIpAddress(ipAddress);
        log.setUserAgent(userAgent);
        log.setLoginStatus(loginStatus);
        log.setFailureReason(failureReason);
        log.setSessionId(sessionId);
        
        userLoginLogService.saveLog(log);
    }
    
    /**
     * 记录用户登录成功日志
     */
    public void logLoginSuccess(Long userId, String username, String ipAddress, 
                              String userAgent, String sessionId) {
        logUserLogin(userId, username, ipAddress, userAgent, 1, null, sessionId);
    }
    
    /**
     * 记录用户登录失败日志
     */
    public void logLoginFailure(String username, String ipAddress, 
                              String userAgent, String failureReason) {
        logUserLogin(null, username, ipAddress, userAgent, 0, failureReason, null);
    }
}