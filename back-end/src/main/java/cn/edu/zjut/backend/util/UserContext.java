package cn.edu.zjut.backend.util;

import io.jsonwebtoken.Claims;

public class UserContext {

    private static final ThreadLocal<Claims> CLAIMS_HOLDER = new ThreadLocal<>();

    public static void setClaims(Claims claims) {
        clear();
        CLAIMS_HOLDER.set(claims);
    }

    public static Claims getClaims() {
        return CLAIMS_HOLDER.get();
    }

    public static Long getUserId() {
        Claims claims = getClaims();
        return claims != null ?  Long.valueOf(String.valueOf(claims.get("id"))) : null;
    }

    public static String getUsername() {
        Claims claims = getClaims();
        return claims != null ? (String) claims.get("username") : null;
    }

    public static Integer getUserType() {
        Claims claims = getClaims();
        return claims != null ? (Integer) claims.get("userType") : null;
    }

    public static void clear() {
        CLAIMS_HOLDER.remove();     // 必须清理！
    }
}