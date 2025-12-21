package cn.edu.zjut.backend.app;

import cn.edu.zjut.backend.dao.UserDAO;
import cn.edu.zjut.backend.po.User;
import cn.edu.zjut.backend.service.UserService;
import cn.edu.zjut.backend.util.HibernateUtil;
import cn.edu.zjut.backend.util.PasswordEncoder;
import org.hibernate.Session;

public class LoginDebugTest {
    public static void main(String[] args) {
        // 测试数据库连接和用户查询
        testDatabaseConnection();
        
        // 测试密码编码和验证
        testPasswordEncoder();
        
        // 测试完整登录流程
        testLoginProcess();
    }
    
    private static void testDatabaseConnection() {
        System.out.println("=== 测试数据库连接 ===");
        try {
            Session session = HibernateUtil.getSession();
            if (session != null) {
                System.out.println("数据库连接成功");
                
                UserDAO dao = new UserDAO();
                dao.setSession(session);
                
                // 尝试查询用户
                User user = dao.findByUsername("testuser");
                System.out.println("查询用户结果: " + user);
            } else {
                System.out.println("数据库连接失败");
            }
        } catch (Exception e) {
            System.err.println("数据库连接测试失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            HibernateUtil.closeSession();
        }
    }
    
    private static void testPasswordEncoder() {
        System.out.println("\n=== 测试密码编码和验证 ===");
        try {
            String rawPassword = "test123";
            String encodedPassword = PasswordEncoder.encode(rawPassword);
            System.out.println("原始密码: " + rawPassword);
            System.out.println("编码后密码: " + encodedPassword);
            
            boolean match1 = PasswordEncoder.matches(rawPassword, encodedPassword);
            System.out.println("密码匹配结果 (正确密码): " + match1);
            
            boolean match2 = PasswordEncoder.matches("wrongpass", encodedPassword);
            System.out.println("密码匹配结果 (错误密码): " + match2);
        } catch (Exception e) {
            System.err.println("密码编码测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void testLoginProcess() {
        System.out.println("\n=== 测试登录流程 ===");
        UserService userService = new UserService();
        
        try {
            // 这里替换成你实际的用户名和密码
            String username = "testuser";
            String password = "test123";
            
            String token = userService.login(username, password);
            System.out.println("登录结果 token: " + token);
            
            if (token != null) {
                System.out.println("登录成功");
            } else {
                System.out.println("登录失败");
            }
        } catch (Exception e) {
            System.err.println("登录流程测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}