// SecurityEventLog.java
package cn.edu.zjut.backend.po;

import lombok.Data;
import java.util.Date;

import javax.persistence.*;

@Data
@Entity
@Table(name = "security_event_log")
public class SecurityEventLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long eventId;

    @Column(name = "event_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date eventTime;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "event_type")
    private String eventType;

    @Column(name = "risk_level")
    private Integer riskLevel;

    @Column(name = "description")
    private String description;

    @Column(name = "is_resolved")
    private Integer isResolved;

    @Column(name = "resolution_detail")
    private String resolutionDetail;

    @Column(name = "extra_data")
    private String extraData;
}