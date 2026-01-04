package cn.edu.zjut.backend.aspect;

import cn.edu.zjut.backend.util.ExamContext;
import cn.edu.zjut.backend.util.Jwt;
import cn.edu.zjut.backend.util.UserContext;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Objects;

@Aspect
@Order(1)
@Component
public class ExamAuthHandler {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private static final List<String> EXAM_AUTH_PATHS = List.of(
            "/api/exam/{examId}/exam-settings",
            "/api/examAnswer",
            "/api/exam/violation"
    );

    @Pointcut("execution(* cn.edu.zjut.backend.controller..*(..))")
    public void examControllerMethods() {}

    @Around("examControllerMethods()")
    public Object authenticateExamToken(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new IllegalStateException("Request attributes not available");
        }

        HttpServletRequest request = attributes.getRequest();
        HttpServletResponse response = attributes.getResponse();

        String uri = request.getRequestURI();

        Integer userType = UserContext.getUserType();

        if (!(requiresExamAuth(uri) && userType == 2)) {    // 学生用户访问这些接口，需要进行examToken鉴权
            // ✅ 如果不在目标列表中，直接返回
            return joinPoint.proceed();
        }

        String examToken = request.getHeader("Exam-Token");

        if (examToken == null || examToken.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":402,\"message\":\"Exam-Token missing\"}");
            return null;
        }

        try {
            Jwt jwt = new Jwt();
            Claims claims = jwt.validateJwt(examToken);
            if (claims == null) {
                throw new Exception("Invalid examToken");
            }

            // 设置考试上下文
            ExamContext.setClaims(claims);
            request.setAttribute("examClaims", claims);

            // 放行
            return joinPoint.proceed();

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"code\":402,\"message\":\"Invalid exam token\"}");
            return null;
        }
    }

    private boolean requiresExamAuth(String uri) {
        for (String pattern : EXAM_AUTH_PATHS) {
            if (PATH_MATCHER.match(pattern, uri)) {
                return true;
            }
        }
        return false;
    }
}
