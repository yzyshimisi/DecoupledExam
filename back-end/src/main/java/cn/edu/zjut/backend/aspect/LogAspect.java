// LogAspect.java
package cn.edu.zjut.backend.aspect;

import cn.edu.zjut.backend.annotation.LogRecord;
import cn.edu.zjut.backend.dto.LoginRequest;
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
        
        // 添加调试日志
        logger.debug("LogAspect被触发 - 方法: {}.{}, 模块: {}, 操作: {}, 日志类型: {}", 
                signature.getDeclaringType().getSimpleName(), 
                method.getName(), 
                logRecord.module(), 
                logRecord.action(),
                logRecord.logType());

        // 根据日志类型决定使用哪种日志记录方式
        switch (logRecord.logType()) {
            case OPERATION:
                logger.debug("处理操作日志 - 模块: {}, 操作: {}", logRecord.module(), logRecord.action());
                return handleOperationLog(joinPoint, logRecord);
            case SECURITY:
                logger.debug("处理安全日志 - 模块: {}, 操作: {}", logRecord.module(), logRecord.action());
                return handleSecurityLog(joinPoint, logRecord);
            case LOGIN:
                logger.debug("处理登录日志 - 模块: {}, 操作: {}", logRecord.module(), logRecord.action());
                return handleLoginLog(joinPoint, logRecord);
            default:
                logger.debug("处理默认操作日志 - 模块: {}, 操作: {}", logRecord.module(), logRecord.action());
                return handleOperationLog(joinPoint, logRecord);
        }
    }

    private Object handleOperationLog(ProceedingJoinPoint joinPoint, LogRecord logRecord) throws Throwable {
        logger.debug("开始处理操作日志 - 模块: {}, 操作: {}", logRecord.module(), logRecord.action());
        
        // 1. 开始时间
        long startTime = System.currentTimeMillis();
        // 2. 初始化日志对象
        SystemOperationLog log = new SystemOperationLog();
        log.setLogTime(new Date());

        // 3. 获取当前请求和用户信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            log.setIpAddress(IpUtils.getIpAddr(request));
            logger.debug("已设置IP地址: {}", log.getIpAddress());
        } else {
            logger.warn("无法获取请求属性");
        }

        log.setModule(logRecord.module());
        log.setAction(logRecord.action());
        log.setTargetType(logRecord.targetType());
        // 设置可能为null的字段的默认值，防止数据库约束违规
        log.setDescription("");
        log.setTargetId(null);  // 可以为null
        log.setExtraData(null); // 可以为null
        
        logger.debug("已设置日志基本信息 - 模块: {}, 操作: {}, 目标类型: {}, 描述: {}", log.getModule(), log.getAction(), log.getTargetType(), log.getDescription());

        // 4. 获取当前用户信息，如果获取不到则记录为匿名用户
        Long userId = UserContext.getUserId();
        logger.debug("获取到的用户ID: {}", userId);
        
        if (userId != null) {
            log.setUserId(userId);
            // 将Integer类型的userType转换为String类型的userRole
            Integer userType = UserContext.getUserType();
            logger.debug("获取到的用户类型: {}", userType);
            
            if (userType != null) {
                // 根据用户类型数字转换为有意义的字符串
                switch (userType) {
                    case 0:
                        log.setUserRole("管理员");
                        break;
                    case 1:
                        log.setUserRole("教师");
                        break;
                    case 2:
                        log.setUserRole("学生");
                        break;
                    default:
                        log.setUserRole("未知(" + userType + ")");
                        break;
                }
                logger.debug("已设置用户角色: {}", log.getUserRole());
            }
        } else {
            // 如果无法获取用户信息，仍然记录日志但标记为匿名
            logger.warn("无法获取用户信息，记录为匿名操作日志");
        }

        try {
            logger.debug("开始执行目标方法");
            // 5. 执行目标方法
            Object result = joinPoint.proceed();

            // 6. 操作成功
            log.setStatus(1);
            logger.debug("目标方法执行成功，设置日志状态为成功");
            return result;
        } catch (Exception e) {
            logger.error("目标方法执行失败", e);
            // 7. 操作失败
            log.setStatus(0);
            log.setDescription("操作失败: " + e.getMessage());
            logger.error("操作异常", e);
            throw e;
        } finally {
            logger.debug("进入finally块，准备保存日志");
            
            // 8. 计算执行时间
            log.setExecutionTimeMs((int) (System.currentTimeMillis() - startTime));
            logger.debug("已设置执行时间: {}ms", log.getExecutionTimeMs());

            // 9. 保存日志 - 使用try-catch确保不影响主业务流程
            try {
                logger.debug("准备保存操作日志: 模块={}, 操作={}, 用户ID={}, 用户角色={}", 
                    log.getModule(), log.getAction(), log.getUserId(), log.getUserRole());
                // 验证日志对象是否完整
                if (log.getModule() == null || log.getAction() == null) {
                    logger.error("操作日志信息不完整，模块或操作为空");
                } else {
                    logger.debug("调用operationLogService.saveLog方法");
                    operationLogService.saveLog(log);
                    logger.debug("操作日志已保存: {} - {}", log.getModule(), log.getAction());
                }
            } catch (Exception e) {
                logger.error("保存操作日志失败: {}", e.getMessage(), e);
                e.printStackTrace(); // 添加堆栈跟踪
                // 不抛出异常，避免影响主业务流程
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
            log.setUserId(UserContext.getUserId());

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
                logger.debug("准备保存安全事件日志: {}", log.getEventType());
                securityEventLogService.saveLog(log);
                logger.debug("安全事件日志已保存: {}", log.getEventType());
            } catch (Exception e) {
                logger.error("保存安全事件日志失败: {}", e.getMessage(), e);
                // 不抛出异常，避免影响主业务流程
            }
        }
    }

    private Object handleLoginLog(ProceedingJoinPoint joinPoint, LogRecord logRecord) throws Throwable {
        logger.debug("开始处理登录日志 - 模块: {}, 操作: {}", logRecord.module(), logRecord.action());
        
        // 1. 开始时间
        long startTime = System.currentTimeMillis();
        // 2. 初始化日志对象
        UserLoginLog log = new UserLoginLog();
        log.setLoginTime(new Date());

        // 3. 获取当前请求和用户信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            log.setIpAddress(IpUtils.getIpAddr(request));
            log.setUserAgent(request.getHeader("User-Agent"));
            logger.debug("登录日志 - 已设置IP地址: {} 和UserAgent: {}", log.getIpAddress(), log.getUserAgent());
        } else {
            logger.warn("登录日志 - 无法获取请求属性");
        }

        try {
            logger.debug("登录日志 - 开始执行目标方法");
            // 4. 执行目标方法
            Object result = joinPoint.proceed();

            // 5. 登录成功，从ThreadLocal获取用户信息
            log.setLoginStatus(1);
            log.setUserId(UserContext.getUserId());
            log.setUsername(UserContext.getUsername());
            logger.debug("登录日志 - 登录成功，用户ID: {}, 用户名: {}", log.getUserId(), log.getUsername());
            return result;
        } catch (Exception e) {
            logger.error("登录日志 - 目标方法执行失败", e);
            // 6. 登录失败，设置用户名（如果可用）
            log.setLoginStatus(0);
            log.setFailureReason("登录操作失败: " + e.getMessage());
            // 尝试获取登录失败时的用户名
            if (log.getUsername() == null) {
                // 如果无法从UserContext获取用户名（因为登录失败），可以尝试从方法参数获取
                Object[] args = joinPoint.getArgs();
                if (args.length > 0 && args[0] instanceof LoginRequest) {
                    LoginRequest loginRequest = (LoginRequest) args[0];
                    log.setUsername(loginRequest.getUsername());
                }
            }
            logger.error("登录操作异常", e);
            throw e;
        } finally {
            logger.debug("登录日志 - 进入finally块，准备保存日志");
            // 7. 保存日志
            try {
                logger.debug("登录日志 - 准备保存用户登录日志: {}", log.getUsername());
                userLoginLogService.saveLog(log);
                logger.debug("登录日志 - 用户登录日志已保存: {}", log.getUsername());
            } catch (Exception e) {
                logger.error("登录日志 - 保存用户登录日志失败: {}", e.getMessage(), e);
                // 不抛出异常，避免影响主业务流程
            }
            // 清理ThreadLocal，避免内存泄漏
            UserContext.clear();
            logger.debug("登录日志 - 已清理UserContext");
        }
    }
}