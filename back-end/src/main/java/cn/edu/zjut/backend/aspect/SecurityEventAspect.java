// SecurityEventAspect.java
package cn.edu.zjut.backend.aspect;

import cn.edu.zjut.backend.util.SecurityLogger;
import cn.edu.zjut.backend.util.UserContext;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Aspect
@Component
public class SecurityEventAspect {

    @Autowired
    private SecurityLogger securityLogger;

    /**
     * 捕获认证异常并记录安全事件
     */
    @AfterThrowing(pointcut = "execution(* cn.edu.zjut.backend.service.UserService.login(..))", throwing = "exception")
    public void logAuthenticationFailure(Exception exception) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String ipAddress = request.getRemoteAddr();

                // 记录安全事件
                securityLogger.logSecurityEvent(
                    null,  // 用户ID未知
                    ipAddress,
                    "AUTHENTICATION_FAILURE",
                    2,  // 中等风险
                    "用户认证失败: " + exception.getMessage()
                );
            }
        } catch (Exception e) {
            // 忽略日志记录过程中的异常，不影响主流程
        }
    }

    /**
     * 捕获权限异常并记录安全事件
     */
    @AfterThrowing(pointcut = "execution(* cn.edu.zjut.backend.controller.*.*(..)) && @annotation(org.springframework.web.bind.annotation.RequestMapping)", throwing = "exception")
    public void logAuthorizationFailure(Exception exception) {
        // 检查是否是权限相关的异常
        if (exception.getMessage() != null && exception.getMessage().contains("权限")) {

            try {
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    String ipAddress = request.getRemoteAddr();
                    Long userId = UserContext.getUserId();
                    String username = UserContext.getUsername();

                    // 记录安全事件
                    securityLogger.logSecurityEvent(
                        userId,
                        ipAddress,
                        "AUTHORIZATION_FAILURE",
                        2,  // 中等风险
                        "用户[" + username + "]权限不足，尝试访问: " + request.getRequestURI()
                    );
                }
            } catch (Exception e) {
                // 忽略日志记录过程中的异常，不影响主流程
            }
        }
    }
}