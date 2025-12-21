package cn.edu.zjut.backend.app;

import cn.edu.zjut.backend.po.User;
import cn.edu.zjut.backend.service.UserService;
import cn.edu.zjut.backend.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;

public class UserRegisterTest {
    public static void main(String[] args) {
        // 先清理之前的测试数据
        cleanupTestData();
        
        // 测试普通用户注册（只能注册学生账号）
        testNormalUserRegistration();
        
        // 测试管理员注册教师账号
        testAdminRegisterTeacher();
    }

    /**
     * 清理测试数据
     */
    private static void cleanupTestData() {
        System.out.println("=== 清理测试数据 ===");
        
        Session session = HibernateUtil.getSession();
        try {
            // 删除之前测试的用户数据
            String hql = "DELETE FROM User WHERE username LIKE 'student_test_%' OR username LIKE 'teacher_test_%'";
            Query query = session.createQuery(hql);
            int deletedCount = query.executeUpdate();
            System.out.println("已清理 " + deletedCount + " 条测试数据");
        } catch (Exception e) {
            System.out.println("清理测试数据时出错: " + e.getMessage());
        } finally {
            HibernateUtil.closeSession();
        }
    }

    /**
     * 测试普通用户注册（只能注册学生账号）
     */
    private static void testNormalUserRegistration() {
        System.out.println("\n=== 测试普通用户注册（学生账号） ===");
        
        UserService userService = new UserService();
        
        // 创建用户信息
        User user = new User();
        user.setUsername("student_test_001");
        user.setPassword("password123");
        user.setRealName("张三");
        user.setPhone("13800138001");
        // 注意：这里不设置userType，默认会被设为学生账号
        
        // 注册用户
        boolean result = userService.register(user);
        
        if (result) {
            System.out.println("学生用户注册成功！");
        } else {
            System.out.println("学生用户注册失败，可能是用户名已存在！");
        }
    }

    /**
     * 测试管理员注册教师账号
     */
    private static void testAdminRegisterTeacher() {
        System.out.println("\n=== 测试管理员注册教师账号 ===");
        
        UserService userService = new UserService();
        
        // 创建教师用户信息
        User teacher = new User();
        teacher.setUsername("teacher_test_001");
        teacher.setPassword("teacher123");
        teacher.setRealName("李老师");
        teacher.setPhone("13900139001");
        teacher.setUserType(1); // 设置为教师账号
        
        // 管理员注册用户（可以指定账号类型）
        boolean result = userService.adminRegister(teacher);
        
        if (result) {
            System.out.println("教师用户注册成功！");
        } else {
            System.out.println("教师用户注册失败，可能是用户名已存在！");
        }
        
        // 关闭Hibernate会话工厂
        HibernateUtil.getSessionFactory().close();
    }
}