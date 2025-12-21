package cn.edu.zjut.backend.dto;

public class UpdateUserStatusRequest {
    private Long userId;
    private String status; // "0"表示启用，"1"表示禁用

    public UpdateUserStatusRequest() {}

    public UpdateUserStatusRequest(Long userId, String status) {
        this.userId = userId;
        this.status = status;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}