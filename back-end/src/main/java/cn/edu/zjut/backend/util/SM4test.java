package cn.edu.zjut.backend.util;
import cn.edu.zjut.backend.util.sm4.SM4Utils;

public class SM4test {

    private static final String SECRET_KEY = "DecoupledExamKey"; // 与你 PasswordEncoder 中一致

    public static void main(String[] args) {
        String rawPassword = "gly123456"; // 要加密的明文密码

        try {
            // 使用你现有的 SM4Utils.encrypt 方法加密
            String encryptedPassword = SM4Utils.encrypt(SECRET_KEY, rawPassword);

            System.out.println("管理员用户名: admin");
            System.out.println("明文密码: " + rawPassword);
            System.out.println("加密后密码（Base64）: " + encryptedPassword);

            // 直接输出的完整 INSERT 语句
            System.out.println("\n=== 直接复制下面的 SQL 到数据库执行 ===");
            System.out.println("INSERT INTO `user` (");
            System.out.println("  `username`, `password`, `real_name`, `user_type`, `status`, `create_time`");
            System.out.println(") VALUES (");
            System.out.println("  'admin',");
            System.out.println("  '" + encryptedPassword + "',");
            System.out.println("  '系统管理员',");
            System.out.println("  0,  -- 0=管理员");
            System.out.println("  '0',  -- 状态正常");
            System.out.println("  NOW()");
            System.out.println(");");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}