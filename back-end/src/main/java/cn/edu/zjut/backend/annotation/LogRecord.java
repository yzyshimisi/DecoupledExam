// LogRecord.java
package cn.edu.zjut.backend.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogRecord {
    String module(); // 功能模块
    String action(); // 具体操作
    String targetType() default ""; // 操作对象类型
    LogType logType() default LogType.OPERATION; // 日志类型

    enum LogType {
        OPERATION,  // 系统操作日志
        SECURITY,   // 安全日志
        LOGIN       // 登录日志
    }
}