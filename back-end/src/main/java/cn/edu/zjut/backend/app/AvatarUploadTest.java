package cn.edu.zjut.backend.app;

import cn.edu.zjut.backend.po.User;
import cn.edu.zjut.backend.service.UserService;
import cn.edu.zjut.backend.util.HibernateUtil;

public class AvatarUploadTest {
    public static void main(String[] args) {
        UserService userService = new UserService();
        
        // 测试获取用户信息，确认avatarUrl字段存在
        User user = new User();
        user.setAvatarUrl("/uploads/avatars/avatar_1_123456789.jpg");
        System.out.println("Test avatar URL: " + user.getAvatarUrl());
        
        // 关闭Hibernate会话工厂
        HibernateUtil.getSessionFactory().close();
    }
}