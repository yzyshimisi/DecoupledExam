package cn.edu.zjut.backend.util;

import cn.edu.zjut.backend.po.User;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpSession;

/**
 * 用户上下文工具类，用于获取当前登录用户信息
 */
public class UserContext {

    /**
     * 获取当前登录用户ID
     * @return 用户ID，未登录返回null
     */
    public static Long getCurrentUserId() {
        User currentUser = getCurrentUser();
        return currentUser != null ? currentUser.getUserId() : null;
    }

    /**
     * 获取当前登录用户名
     * @return 用户名，未登录返回null
     */
    public static String getCurrentUsername() {
        User currentUser = getCurrentUser();
        return currentUser != null ? currentUser.getUsername() : null;
    }

    /**
     * 获取当前登录用户角色（转换为字符串）
     * @return 角色名称（ADMIN/TEACHER/STUDENT），未登录返回null
     */
    public static String getCurrentUserRole() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return null;
        }
        // 根据User类中的userType转换为角色名称
        switch (currentUser.getUserType()) {
            case 0:
                return "ADMIN";
            case 1:
                return "TEACHER";
            case 2:
                return "STUDENT";
            default:
                return "UNKNOWN";
        }
    }

    /**
     * 获取当前登录用户对象
     * @return User对象，未登录返回null
     */
    public static User getCurrentUser() {
        try {
            // 从当前请求上下文获取HttpSession
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return null;
            }
            HttpSession session = attributes.getRequest().getSession();
            // 从session中获取当前用户（与UserController中的存储键保持一致）
            return (User) session.getAttribute("currentUser");
        } catch (Exception e) {
            // 非Web环境或其他异常时返回null
            return null;
        }
    }
}