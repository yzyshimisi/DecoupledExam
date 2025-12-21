package cn.edu.zjut.backend.service;

import cn.edu.zjut.backend.dao.UserDAO;
import cn.edu.zjut.backend.po.User;
import cn.edu.zjut.backend.util.HibernateUtil;
import cn.edu.zjut.backend.util.Jwt;
import cn.edu.zjut.backend.util.PasswordEncoder;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("userServ")
public class UserService {
    
    public Session getSession() {
        Session session = HibernateUtil.getSession();
        if (session == null) {
            throw new RuntimeException("Failed to get Hibernate session");
        }
        return session;
    }

    /**
     * 用户注册 - 普通用户注册（仅能注册学生账号）
     */
    public boolean register(User user) {
        // 普通注册只能注册学生账号
        user.setUserType(2); // 学生账号
        
        // 验证手机号格式
        if (user.getPhone() != null && !isValidPhoneNumber(user.getPhone())) {
            return false; // 手机号格式不正确
        }
        
        // 验证密码是否为空或空字符串
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            return false; // 密码不能为空
        }
        
        return saveUser(user);
    }
    
    /**
     * 验证手机号格式是否正确
     * @param phoneNumber 手机号
     * @return 是否符合中国手机号格式
     */
    private boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return true; // 允许手机号为空
        }
        // 中国手机号正则表达式
        String phonePattern = "^1[3-9]\\d{9}$";
        return phoneNumber.matches(phonePattern);
    }

    /**
     * 管理员注册用户（可以注册教师或学生账号）
     */
    public boolean adminRegister(User user) {
        // 验证密码是否为空或空字符串
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            return false; // 密码不能为空
        }
        
        return saveUser(user);
    }

    /**
     * 保存用户信息的通用方法
     */
    private boolean saveUser(User user) {
        Session session = getSession();
        UserDAO dao = new UserDAO();
        dao.setSession(session);
        Transaction tran = null;
        
        try {
            tran = session.beginTransaction();
            // 检查用户名是否已存在
            if (dao.findByUsername(user.getUsername()) != null) {
                return false; // 用户名已存在
            }
            // 对密码进行加密
            String encodedPassword = PasswordEncoder.encode(user.getPassword());
            user.setPassword(encodedPassword);
            // 设置默认状态为正常
            user.setStatus("0");
            dao.add(user);
            tran.commit();
            return true;
        } catch (Exception e) {
            System.out.println("register user failed " + e);
            if (tran != null) {
                tran.rollback();
            }
            return false;
        } finally {
            HibernateUtil.closeSession();
        }
    }

    /**
     * 用户登录验证
     */
    public String login(String username, String password) {
        Session session = null;
        try {
            session = getSession();
            UserDAO dao = new UserDAO();
            dao.setSession(session);
            
            User user = dao.findByUsername(username);
            // 验证密码是否正确且用户状态正常
            if (user != null && PasswordEncoder.matches(password, user.getPassword()) && user.getStatus().equals("0")) {
                Jwt jwt = new Jwt();
                String token = jwt.generateJwtToken(user.getUserId(), user.getUsername(), user.getUserType());
                return token; // 登录成功，返回用户信息
            }
            return null; // 登录失败
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (session != null) {
                HibernateUtil.closeSession();
            }
        }
    }

    /**
     * 获取用户信息
     */
    public User getUserById(Long userId) {
        Session session = getSession();
        UserDAO dao = new UserDAO();
        dao.setSession(session);
        
        try {
            return dao.findById(userId);
        } finally {
            HibernateUtil.closeSession();
        }
    }

    /**
     * 查询用户列表
     */
    public List<User> getUsers(Integer userType) {
        Session session = getSession();
        UserDAO dao = new UserDAO();
        dao.setSession(session);
        
        try {
            return dao.query(userType);
        } finally {
            HibernateUtil.closeSession();
        }
    }

    /**
     * 更新用户信息
     */
    public boolean updateUser(User user) {
        Session session = getSession();
        UserDAO dao = new UserDAO();
        dao.setSession(session);
        Transaction tran = null;
        
        try {
            tran = session.beginTransaction();
            
            // 检查用户名是否已被其他用户使用（仅在用户名不为空时检查）
            if (user.getUsername() != null && !user.getUsername().isEmpty()) {
                User existingUser = dao.findByUsername(user.getUsername());
                if (existingUser != null && !existingUser.getUserId().equals(user.getUserId())) {
                    // 用户名已存在且不属于当前用户
                    return false;
                }
            }
            
            // 获取数据库中的原用户信息
            User originalUser = dao.findById(user.getUserId());
            if (originalUser == null) {
                return false;
            }
            
            // 只更新请求中提供了值的字段
            if (user.getUsername() != null) {
                originalUser.setUsername(user.getUsername());
            }
            if (user.getRealName() != null) {
                originalUser.setRealName(user.getRealName());
            }
            if (user.getPhone() != null) {
                originalUser.setPhone(user.getPhone());
            }
            if (user.getFaceImg() != null) {
                originalUser.setFaceImg(user.getFaceImg());
            }
            // 不更新userId、userType、password、status、createTime等敏感字段
            
            dao.update(originalUser);
            tran.commit();
            return true;
        } catch (Exception e) {
            System.out.println("update user failed " + e);
            if (tran != null) {
                tran.rollback();
            }
            return false;
        } finally {
            HibernateUtil.closeSession();
        }
    }

    /**
     * 更新用户状态
     */
    public boolean updateUserStatus(Long userId, String status) {
        Session session = getSession();
        UserDAO dao = new UserDAO();
        dao.setSession(session);
        Transaction tran = null;
        
        try {
            tran = session.beginTransaction();
            User user = dao.findById(userId);
            if (user != null) {
                user.setStatus(status);
                dao.update(user);
                tran.commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            System.out.println("update user status failed " + e);
            if (tran != null) {
                tran.rollback();
            }
            return false;
        } finally {
            HibernateUtil.closeSession();
        }
    }

    /**
     * 删除用户
     */
    public boolean deleteUser(Long userId) {
        Session session = getSession();
        UserDAO dao = new UserDAO();
        dao.setSession(session);
        Transaction tran = null;
        
        try {
            tran = session.beginTransaction();
            dao.delete(userId);
            tran.commit();
            return true;
        } catch (Exception e) {
            System.out.println("delete user failed " + e);
            if (tran != null) {
                tran.rollback();
            }
            return false;
        } finally {
            HibernateUtil.closeSession();
        }
    }

    /**
     * 角色模拟登录（管理员可以模拟教师或学生登录）
     */
    public User simulateLogin(Long adminUserId, Integer simulateUserType) {
        Session session = getSession();
        UserDAO dao = new UserDAO();
        dao.setSession(session);
        
        try {
            User currentUser = dao.findById(adminUserId);
            // 检查当前用户是否有权限进行模拟登录（只有管理员可以）
            if (currentUser != null && currentUser.getUserType() == 0) { // 0是管理员
                // 查找系统中可用的对应类型的用户（第一个找到的符合条件的用户）
                List<User> users = dao.query(simulateUserType);
                // 过滤出状态正常的用户
                for (User user : users) {
                    if ("0".equals(user.getStatus())) {
                        return user; // 返回第一个状态正常的用户
                    }
                }
            }
            return null;
        } finally {
            HibernateUtil.closeSession();
        }
    }
    
    /**
     * 修改用户密码
     */
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        // 验证新密码是否符合要求
        if (!isValidPassword(newPassword)) {
            return false;
        }
        
        Session session = getSession();
        UserDAO dao = new UserDAO();
        dao.setSession(session);
        Transaction tran = null;
        
        try {
            tran = session.beginTransaction();
            User user = dao.findById(userId);
            
            // 验证旧密码是否正确
            if (user == null || !PasswordEncoder.matches(oldPassword, user.getPassword())) {
                return false;
            }
            
            // 加密新密码并更新
            String encodedNewPassword = PasswordEncoder.encode(newPassword);
            user.setPassword(encodedNewPassword);
            dao.update(user);
            tran.commit();
            return true;
        } catch (Exception e) {
            System.out.println("change password failed " + e);
            if (tran != null) {
                tran.rollback();
            }
            return false;
        } finally {
            HibernateUtil.closeSession();
        }
    }
    
    /**
     * 验证密码是否符合要求（至少包含字母和数字）
     * @param password 待验证的密码
     * @return 是否符合要求
     */
    private boolean isValidPassword(String password) {
        // 检查密码是否为空
        if (password == null || password.isEmpty()) {
            return false;
        }
        
        // 检查密码是否至少包含一个字母和一个数字
        boolean hasLetter = false;
        boolean hasDigit = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }
            
            // 如果已经同时包含字母和数字，则可以提前退出循环
            if (hasLetter && hasDigit) {
                break;
            }
        }
        
        return hasLetter && hasDigit;
    }
    
    /**
     * 管理员重置用户密码
     * @param userId 要重置密码的用户ID
     * @return 是否重置成功
     */
    public boolean resetPassword(Long userId) {
        Session session = getSession();
        UserDAO dao = new UserDAO();
        dao.setSession(session);
        Transaction tran = null;
        
        try {
            tran = session.beginTransaction();
            User user = dao.findById(userId);
            
            if (user == null) {
                return false;
            }
            
            // 设置初始密码为user123456并加密
            String initialPassword = "user123456";
            String encodedPassword = PasswordEncoder.encode(initialPassword);
            user.setPassword(encodedPassword);
            dao.update(user);
            tran.commit();
            return true;
        } catch (Exception e) {
            System.out.println("reset password failed " + e);
            if (tran != null) {
                tran.rollback();
            }
            return false;
        } finally {
            HibernateUtil.closeSession();
        }
    }
}