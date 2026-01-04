package cn.edu.zjut.backend.util;

import cn.edu.zjut.backend.dao.ExamDAO;
import cn.edu.zjut.backend.po.Exam;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.hibernate.Session;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Jwt {
    private final static SignatureAlgorithm SIGNATURE_ALGORITHM = SignatureAlgorithm.HS256;
    private final static String secret = "DecoupledExam_MustBeLongEnoughToBeSecure123";
    private final static Long access_token_expiration = 7200L;
    private final static String jwt_iss = "DecoupledExam";
    private final static String subject = "DecoupledExam";

    public String generateExamToken(Long studentId, Long examId, Long expirationSeconds) {

        if (expirationSeconds == null || expirationSeconds <= 0) {
            expirationSeconds = 7200L; // 默认 2 小时（可根据需求调整）
        }

        Map<String, Object> map = new HashMap<>();
        map.put("alg", "HS256");
        map.put("typ", "JWT");

        Map<String, Object> claims = new HashMap<>();

        // 核心考试会话信息
        claims.put("studentId", studentId);   // 学生ID
        claims.put("examId", examId);         // 考试ID

        claims.put("iss", jwt_iss + "/exam"); // 区分 issuer
        claims.put("type", "exam");           // 可选：便于调试区分 token 类型

        SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder() // 这里其实就是new一个JwtBuilder，设置jwt的body
                .setHeader(map)         // 头部信息
                .setClaims(claims)      // 载荷信息
                .setId(UUID.randomUUID().toString()) // 设置jti(JWT ID)：是JWT的唯一标识，从而回避重放攻击。
                .setIssuedAt(new Date())       // 设置iat: jwt的签发时间
                .setExpiration(new Date(System.currentTimeMillis() + expirationSeconds * 1000)) // 设置exp：jwt过期时间
                .setSubject(subject)    //设置sub：代表这个jwt所面向的用户，所有人
                .signWith(SIGNATURE_ALGORITHM, secret)//设置签名：通过签名算法和秘钥生成签名
                .compact(); // 开始压缩为xxxxx.yyyyy.zzzzz 格式的jwt token
    }

    public String generateJwtToken(Long id, String username, int userType, String faceImg) {
        Map<String, Object> map = new HashMap<>();
        map.put("alg", "HS256");
        map.put("typ", "JWT");

        Map<String, Object> claims = new HashMap<>();
        claims.put("id", id);
        claims.put("username", username);
        claims.put("userType", userType);
        // 添加人脸识别基准照片URL信息
        claims.put("faceImg", faceImg);
        // 添加是否上传人脸照片的标识
        claims.put("hasFaceImg", faceImg != null && !faceImg.isEmpty());

        claims.put("iss", jwt_iss);

        SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder() // 这里其实就是new一个JwtBuilder，设置jwt的body
                .setHeader(map)         // 头部信息
                .setClaims(claims)      // 载荷信息
                .setId(UUID.randomUUID().toString()) // 设置jti(JWT ID)：是JWT的唯一标识，从而回避重放攻击。
                .setIssuedAt(new Date())       // 设置iat: jwt的签发时间
                .setExpiration(new Date(System.currentTimeMillis() + access_token_expiration * 1000)) // 设置exp：jwt过期时间
                .setSubject(subject)    //设置sub：代表这个jwt所面向的用户，所有人
                .signWith(SIGNATURE_ALGORITHM, secret)//设置签名：通过签名算法和秘钥生成签名
                .compact(); // 开始压缩为xxxxx.yyyyy.zzzzz 格式的jwt token
    }
    
    // 重载方法，保持向后兼容
    public String generateJwtToken(Long id, String username, int userType) {
        return generateJwtToken(id, username, userType, null);
    }

    private Claims getClaimsFromJwt(String jwt) {
        SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        try {
            return Jwts.parser().setSigningKey(secret).parseClaimsJws(jwt).getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Claims validateJwt(String jwtToken) {

        if (jwtToken.split("\\.").length != 3) {    // 验证基本结构
            System.out.println("Invalid JWT format");
            return null;
        }

        Claims claims = getClaimsFromJwt(jwtToken);
        if (claims == null){    // 验证签名
            System.out.println("Invalid JWT");
            return null;
        }

        Date expiration = claims.getExpiration();
        if (expiration.before(new Date())) {
            System.out.println("JWT expired");
            return null;
        }

        return claims;
    }
}