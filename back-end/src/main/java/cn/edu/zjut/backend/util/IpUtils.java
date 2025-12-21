package cn.edu.zjut.backend.util;

import jakarta.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class IpUtils {

    /**
     * 获取客户端真实IP地址（完善版）
     * 处理代理、负载均衡及本地环境场景
     *
     * @param request HttpServletRequest对象
     * @return 客户端IP地址，异常时返回"unknown"
     */
    public static String getClientIpAddress(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }

        String ip = null;
        // 代理相关头信息（按优先级排序）
        String[] headers = {
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_CLIENT_IP",
                "HTTP_X_FORWARDED_FOR"
        };

        for (String header : headers) {
            ip = request.getHeader(header);
            if (isValidIp(ip)) {
                // 处理多IP场景（取第一个有效IP）
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }

        // 从请求直接获取IP
        ip = request.getRemoteAddr();
        // 处理本地回环地址
        if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
            try {
                ip = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                ip = "127.0.0.1"; // 本地地址获取失败时默认值
            }
        }

        return isValidIp(ip) ? ip : "unknown";
    }

    /**
     * 简化调用别名（兼容原getIpAddr调用习惯）
     */
    public static String getIpAddr(HttpServletRequest request) {
        return getClientIpAddress(request);
    }

    /**
     * 判断IP地址是否有效（非空且非unknown）
     */
    private static boolean isValidIp(String ip) {
        return ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip);
    }

    /**
     * 判断IP地址是否有效（IPv4格式校验）
     */
    public static boolean isValidIpv4(String ip) {
        if (!isValidIp(ip)) {
            return false;
        }

        String[] parts = ip.split("\\.");
        if (parts.length != 4) {
            return false;
        }

        try {
            for (String part : parts) {
                int num = Integer.parseInt(part);
                if (num < 0 || num > 255) {
                    return false;
                }
                // 禁止前导零（如"01"）
                if (part.length() > 1 && part.startsWith("0")) {
                    return false;
                }
            }
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }

    /**
     * 判断是否为内网IP地址
     */
    public static boolean isInternalIp(String ip) {
        if (!isValidIpv4(ip)) {
            return false;
        }

        String[] parts = ip.split("\\.");
        int first = Integer.parseInt(parts[0]);
        int second = Integer.parseInt(parts[1]);

        // 10.x.x.x
        if (first == 10) {
            return true;
        }
        // 172.16.x.x - 172.31.x.x
        if (first == 172 && second >= 16 && second <= 31) {
            return true;
        }
        // 192.168.x.x
        if (first == 192 && second == 168) {
            return true;
        }
        // 127.x.x.x（回环地址）
        if (first == 127) {
            return true;
        }

        return false;
    }
}