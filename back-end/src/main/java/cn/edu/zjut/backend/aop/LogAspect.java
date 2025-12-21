// LogAspect.java
package cn.edu.zjut.backend.aop;

import cn.edu.zjut.backend.annotation.LogRecord;
import cn.edu.zjut.backend.po.SystemOperationLog;
import cn.edu.zjut.backend.service.i.SystemOperationLogService;
import cn.edu.zjut.backend.service.i.SecurityEventLogService;
import cn.edu.zjut.backend.service.i.UserLoginLogService;
import cn.edu.zjut.backend.po.SecurityEventLog;
import cn.edu.zjut.backend.po.UserLoginLog;
import cn.edu.zjut.backend.util.IpUtils;
import cn.edu.zjut.backend.util.UserContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Date;

@Aspect
@Component
public class LogAspect {
    private static final Logger logger = LoggerFactory.getLogger(LogAspect.class);

    @Autowired
    private SystemOperationLogService operationLogService;

    @Autowired
    private SecurityEventLogService securityEventLogService;

    @Autowired
    private UserLoginLogService userLoginLogService;

    @Around("@annotation(cn.edu.zjut.backend.annotation.LogRecord)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取注解信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        LogRecord logRecord = method.getAnnotation(LogRecord.class);

        // 根据日志类型决定使用哪种日志记录方式
        switch (logRecord.logType()) {
            case OPERATION:
                return handleOperationLog(joinPoint, logRecord);
            case SECURITY:
                return handleSecurityLog(joinPoint, logRecord);
            case LOGIN:
                return handleLoginLog(joinPoint, logRecord);
            default:
                return handleOperationLog(joinPoint, logRecord);
        }
    }

    private Object handleOperationLog(ProceedingJoinPoint joinPoint, LogRecord logRecord) throws Throwable {
        // 1. 开始时间
        long startTime = System.currentTimeMillis();
        // 2. 初始化日志对象
        SystemOperationLog log = new SystemOperationLog();
        log.setLogTime(new Date());

        try {
            // 3. 获取当前请求和用户信息
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                log.setIpAddress(IpUtils.getIpAddr(request));
            }

            log.setModule(logRecord.module());
            log.setAction(logRecord.action());
            log.setTargetType(logRecord.targetType());

            // 4. 获取当前用户信息
            log.setUserId(UserContext.getCurrentUserId());

            log.setUserRole(UserContext.getCurrentUserRole());

            // 5. 执行目标方法
            Object result = joinPoint.proceed();

            // 6. 操作成功
            log.setStatus(1);
            return result;
        } catch (Exception e) {
            // 7. 操作失败
            log.setStatus(0);
            log.setDescription("操作失败: " + e.getMessage());
            logger.error("操作异常", e);
            throw e;
        } finally {
            // 8. 计算执行时间
            log.setExecutionTimeMs((int) (System.currentTimeMillis() - startTime));

            // 9. 保存日志
            try {
                operationLogService.saveLog(log);
            } catch (Exception e) {
                logger.error("保存操作日志失败", e);
            }
        }
    }

    private Object handleSecurityLog(ProceedingJoinPoint joinPoint, LogRecord logRecord) throws Throwable {
        // 1. 开始时间
        long startTime = System.currentTimeMillis();
        // 2. 初始化日志对象
        SecurityEventLog log = new SecurityEventLog();
        log.setEventTime(new Date());

        try {
            // 3. 获取当前请求和用户信息
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                log.setIpAddress(IpUtils.getIpAddr(request));
            }

            log.setEventType(logRecord.module() + ":" + logRecord.action());
            log.setRiskLevel(2); // 默认中等风险
            log.setDescription("安全操作");

            // 4. 获取当前用户信息
            log.setUserId(UserContext.getCurrentUserId());

            // 5. 执行目标方法
            Object result = joinPoint.proceed();

            // 6. 操作成功
            log.setIsResolved(1);
            return result;
        } catch (Exception e) {
            // 7. 操作失败
            log.setIsResolved(0);
            log.setDescription("安全操作失败: " + e.getMessage());
            logger.error("安全操作异常", e);
            throw e;
        } finally {
            // 8. 保存日志
            try {
                securityEventLogService.saveLog(log);
            } catch (Exception e) {
                logger.error("保存安全事件日志失败", e);
            }
        }
    }

    private Object handleLoginLog(ProceedingJoinPoint joinPoint, LogRecord logRecord) throws Throwable {
        // 1. 开始时间
        long startTime = System.currentTimeMillis();
        // 2. 初始化日志对象
        UserLoginLog log = new UserLoginLog();
        log.setLoginTime(new Date());

        try {
            // 3. 获取当前请求和用户信息
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                log.setIpAddress(IpUtils.getIpAddr(request));
                log.setUserAgent(request.getHeader("User-Agent"));
            }

            log.setUsername(UserContext.getCurrentUsername());

            // 4. 执行目标方法
            Object result = joinPoint.proceed();

            // 5. 登录成功
            log.setLoginStatus(1);
            log.setUserId(UserContext.getCurrentUserId());
            return result;
        } catch (Exception e) {
            // 6. 登录失败
            log.setLoginStatus(0);
            log.setFailureReason("登录操作失败: " + e.getMessage());
            logger.error("登录操作异常", e);
            throw e;
        } finally {
            // 7. 保存日志
            try {
                userLoginLogService.saveLog(log);
            } catch (Exception e) {
                logger.error("保存用户登录日志失败", e);
            }
        }
    }
}