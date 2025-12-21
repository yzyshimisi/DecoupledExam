package cn.edu.zjut.backend.dto;

public class SimulateLoginRequest {
    private Integer simulateUserType; // 1=教师, 2=学生

    public SimulateLoginRequest() {}

    public SimulateLoginRequest(Integer simulateUserType) {
        this.simulateUserType = simulateUserType;
    }

    public Integer getSimulateUserType() {
        return simulateUserType;
    }

    public void setSimulateUserType(Integer simulateUserType) {
        this.simulateUserType = simulateUserType;
    }
}