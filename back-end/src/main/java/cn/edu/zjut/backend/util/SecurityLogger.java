// SecurityLogger.java
package cn.edu.zjut.backend.util;

import cn.edu.zjut.backend.po.SecurityEventLog;
import cn.edu.zjut.backend.service.i.SecurityEventLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class SecurityLogger {
    
    @Autowired
    private SecurityEventLogService securityEventLogService;
    
    /**
     * 记录安全事件日志
     * @param userId 用户ID
     * @param ipAddress IP地址
     * @param eventType 事件类型
     * @param riskLevel 风险等级 (1低 2中 3高 4紧急)
     * @param description 事件描述
     */
    public void logSecurityEvent(Long userId, String ipAddress, String eventType, 
                                Integer riskLevel, String description) {
        SecurityEventLog log = new SecurityEventLog();
        log.setEventTime(new Date());
        log.setUserId(userId);
        log.setIpAddress(ipAddress);
        log.setEventType(eventType);
        log.setRiskLevel(riskLevel);
        log.setDescription(description);
        log.setIsResolved(0); // 默认未处理
        
        securityEventLogService.saveLog(log);
    }
    
    /**
     * 记录安全事件日志（带额外数据）
     * @param userId 用户ID
     * @param ipAddress IP地址
     * @param eventType 事件类型
     * @param riskLevel 风险等级 (1低 2中 3高 4紧急)
     * @param description 事件描述
     * @param extraData 额外数据
     */
    public void logSecurityEvent(Long userId, String ipAddress, String eventType,
                                Integer riskLevel, String description, String extraData) {
        SecurityEventLog log = new SecurityEventLog();
        log.setEventTime(new Date());
        log.setUserId(userId);
        log.setIpAddress(ipAddress);
        log.setEventType(eventType);
        log.setRiskLevel(riskLevel);
        log.setDescription(description);
        log.setIsResolved(0); // 默认未处理
        log.setExtraData(extraData);
        
        securityEventLogService.saveLog(log);
    }
}