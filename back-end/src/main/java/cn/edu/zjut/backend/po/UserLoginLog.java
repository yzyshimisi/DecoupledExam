// UserLoginLog.java
package cn.edu.zjut.backend.po;

import lombok.Data;
import java.util.Date;

import javax.persistence.*;

@Data
@Entity
@Table(name = "user_login_log")
public class UserLoginLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "login_id")
    private Long loginId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username")
    private String username;

    @Column(name = "login_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date loginTime;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "login_status")
    private Integer loginStatus;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "session_id")
    private String sessionId;
}