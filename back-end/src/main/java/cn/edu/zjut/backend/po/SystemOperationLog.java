// SystemOperationLog.java
package cn.edu.zjut.backend.po;

import lombok.Data;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.*;

@Data
@Entity
@Table(name = "system_operation_log")
public class SystemOperationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @Column(name = "log_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date logTime;

    @Column(name = "user_id")
    private Long userId;
    

    @Column(name = "user_role")
    private String userRole;  // 从 Integer 改为 String，与数据库 VARCHAR(20) 匹配

    @Column(name = "module")
    private String module;

    @Column(name = "action")
    private String action;

    @Column(name = "target_type")
    private String targetType;

    @Column(name = "target_id")
    private String targetId;

    @Column(name = "description")
    private String description;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "status")
    private Integer status; // 1成功 0失败

    @Column(name = "execution_time_ms")
    private Integer executionTimeMs;

    @Column(name = "extra_data")
    private String extraData;
}