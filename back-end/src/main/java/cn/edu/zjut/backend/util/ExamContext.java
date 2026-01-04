package cn.edu.zjut.backend.util;

import io.jsonwebtoken.Claims;

public class ExamContext {

    private static final ThreadLocal<Claims> CLAIMS_HOLDER = new ThreadLocal<>();

    /**
     * 设置考试上下文的 Claims（通常从 JWT 解析而来）
     */
    public static void setClaims(Claims claims) {
        clear();
        CLAIMS_HOLDER.set(claims);
    }

    /**
     * 获取当前线程的 Claims
     */
    public static Claims getClaims() {
        return CLAIMS_HOLDER.get();
    }

    /**
     * 获取学生ID（studentId）
     */
    public static Long getStudentId() {
        Claims claims = getClaims();
        return claims != null ? Long.valueOf(String.valueOf(claims.get("studentId"))) : null;
    }

    /**
     * 获取考试ID（examId）
     */
    public static Long getExamId() {
        Claims claims = getClaims();
        return claims != null ? Long.valueOf(String.valueOf(claims.get("examId"))) : null;
    }

    /**
     * 清理当前线程的上下文（非常重要！防止内存泄漏）
     */
    public static void clear() {
        CLAIMS_HOLDER.remove();
    }
}