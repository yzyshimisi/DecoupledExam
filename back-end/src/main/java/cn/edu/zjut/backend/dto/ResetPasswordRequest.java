package cn.edu.zjut.backend.dto;

public class ResetPasswordRequest {
    private Long userId;

    public ResetPasswordRequest() {}

    public ResetPasswordRequest(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}