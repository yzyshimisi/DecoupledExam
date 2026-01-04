package cn.edu.zjut.backend.aspect;

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
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.List;

@Aspect
@Component
@Order(1)
public class AuthHandler {

    /**
     * 白名单列表
     * 存放不需要身份验证的 URI 路径
     */
    private static final List<String> WHITE_LIST = List.of(
            "/api/user/register",
            "/api/user/login",
            "/api/user/login-face"
    );

    @Pointcut("execution(* cn.edu.zjut.backend.controller..*(..))")
    public void controllerMethods() {}

    @Around("controllerMethods()")
    public Object authenticate(ProceedingJoinPoint joinPoint) throws Throwable {

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        HttpServletRequest request = attributes.getRequest();
        HttpServletResponse response = attributes.getResponse();

        String uri = request.getRequestURI();
        System.out.println(uri);

        // 白名单路径（不拦截）
        if (isWhiteListed(uri)) {
            // ✅ 如果在白名单，直接放行，执行原方法
            return joinPoint.proceed();
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // 直接返回错误，不执行 Controller
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"Token missing or invalid\"}");
            return null; // 中断执行
        }

        String token = authHeader.substring(7);     // 去掉 "Bearer "
        try {
            // 解析 token
            Jwt jwt = new Jwt();
            Claims claims = jwt.validateJwt(token);

            if(claims==null){
                throw new Exception("Token invalid");
            }

            UserContext.setClaims(claims);
            // 同时将claims设置到request属性中，以供Controller使用
            request.setAttribute("claims", claims);

            // ✅ 放行，执行 Controller
            return joinPoint.proceed();
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"code\":401,\"message\":\"Invalid token\"}");
            return null;
        }
    }

    private boolean isWhiteListed(String uri) {
        // 使用 Java 8 Stream 的 anyMatch 进行高效匹配
        return WHITE_LIST.stream().anyMatch(uri::equals);
    }
}
