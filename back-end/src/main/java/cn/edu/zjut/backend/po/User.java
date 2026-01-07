package cn.edu.zjut.backend.po;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username", length = 50, nullable = false, unique = true)
    private String username;

    @Column(name = "password", length = 100, nullable = false)
    private String password;

    @Column(name = "real_name", length = 50)
    private String realName;

    @Column(name = "avatar_url", length = 255)
    private String avatarUrl;

    @Column(name = "user_type", nullable = false, columnDefinition = "INT(4) DEFAULT 2")
    private Integer userType = 2; // 0=管理员, 1=教师, 2=学生

    @Column(name = "face_img", length = 255)
    private String faceImg;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "email", length = 100, unique = true)
    private String email;

    @Column(name = "status", length = 1, nullable = false, columnDefinition = "CHAR(1) DEFAULT '0'")
    private String status = "0"; // 0正常 1停用

    @Column(name = "create_time", nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private Date createTime = new Date();

    // 构造函数
    public User() {}

    public User(Long userId, String username, String password, String realName,
                Integer userType, String faceImg, String phone, String status, Date createTime) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.realName = realName;
        this.avatarUrl = null; // 默认值
        this.userType = userType;
        this.faceImg = faceImg;
        this.phone = phone;
        this.email = null; // 默认值
        this.status = status;
        this.createTime = createTime;
    }

    public User(Long userId, String username, String password, String realName, String email,
                Integer userType, String faceImg, String phone, String status, Date createTime) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.realName = realName;
        this.avatarUrl = null; // 默认值
        this.email = email;
        this.userType = userType;
        this.faceImg = faceImg;
        this.phone = phone;
        this.status = status;
        this.createTime = createTime;
    }

    // Getter和Setter方法
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public Integer getUserType() {
        return userType;
    }

    public void setUserType(Integer userType) {
        this.userType = userType;
    }

    public String getFaceImg() {
        return faceImg;
    }

    public void setFaceImg(String faceImg) {
        this.faceImg = faceImg;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", realName='" + realName + '\'' +
                ", userType=" + userType +
                ", faceImg='" + faceImg + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", status='" + status + '\'' +
                ", createTime=" + createTime +
                '}';
    }
}
