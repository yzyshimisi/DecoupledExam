package cn.edu.zjut.backend.dto;

public class UpdateUserInfoRequest {
    private String username;
    private String realName;
    private String phone;
    private String avatarUrl;

    public UpdateUserInfoRequest() {}

    public UpdateUserInfoRequest(String username, String realName, String phone) {
        this.username = username;
        this.realName = realName;
        this.phone = phone;
        this.avatarUrl = null;
    }

    public UpdateUserInfoRequest(String username, String realName, String phone, String avatarUrl) {
        this.username = username;
        this.realName = realName;
        this.phone = phone;
        this.avatarUrl = avatarUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}