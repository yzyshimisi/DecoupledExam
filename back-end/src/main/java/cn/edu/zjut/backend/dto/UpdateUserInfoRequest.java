package cn.edu.zjut.backend.dto;

public class UpdateUserInfoRequest {
    private String username;
    private String realName;
    private String phone;

    public UpdateUserInfoRequest() {}

    public UpdateUserInfoRequest(String username, String realName, String phone) {
        this.username = username;
        this.realName = realName;
        this.phone = phone;
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
}