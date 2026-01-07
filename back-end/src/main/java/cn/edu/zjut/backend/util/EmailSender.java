package cn.edu.zjut.backend.util;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.List;
import java.util.Properties;

public class EmailSender {

    // 发件人邮箱
    final static String fromEmail = "3198685096@qq.com";
    // 邮箱密码或授权码（视邮箱服务商要求）
    final static String password = "loxobfumqheedgfd";

    public static boolean sendMail(List<String> recipients, String subject, String content) {

        // 配置 SMTP 服务器属性
        Properties properties = new Properties();
        properties.put("mail.smtp.host", "smtp.qq.com"); // SMTP 主机名（如 Gmail: smtp.gmail.com）
        properties.put("mail.smtp.port", "587"); // SMTP 端口号
        properties.put("mail.smtp.auth", "true"); // 是否需要认证
        properties.put("mail.smtp.starttls.enable", "true"); // 启用 STARTTLS

        // 创建会话
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });

        try {
            // 创建邮件内容
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail)); // 设置发件人

            // 群发
            Address[] addresses = new Address[recipients.size()];
            for (int i = 0; i < recipients.size(); i++) {
                addresses[i] = new InternetAddress(recipients.get(i));
            }

            message.setRecipients(Message.RecipientType.TO, addresses);

            message.setSubject(subject); // 设置邮件主题
            message.setText(content); // 设置邮件正文

            // 发送邮件
            Transport.send(message);
            System.out.println("邮件发送成功！");
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return false;
    }
}