package com.example.airplanesalehouduan.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * 邮箱服务封装
 * 用于发送邮箱验证码和邮件通知
 */
@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    private final String fromEmail = "your_real_163_email@163.com"; // 请替换为您的真实163邮箱

    // 开发模式：内存缓存验证码（email -> code and timestamp）
    private final java.util.concurrent.ConcurrentHashMap<String, Long> codeTimestamp = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<String, String> codeStore = new java.util.concurrent.ConcurrentHashMap<>();
    // 验证码有效期（毫秒）- 3分钟
    private final long CODE_TTL_MS = 3 * 60 * 1000;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * 发送邮箱验证码
     * @param email 收件邮箱
     * @return true 表示发送请求已成功发出
     */
    public boolean sendVerifyCode(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }

        try {
            // 生成6位数字验证码
            String code = String.format("%06d", (int)(Math.random() * 1000000));

            // 存储验证码到内存缓存
            codeStore.put(email, code);
            codeTimestamp.put(email, System.currentTimeMillis());

            // 发送邮件
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("飞机售票系统 - 邮箱验证码");
            message.setText(String.format("您的验证码是：%s\n\n该验证码将在3分钟后过期，请及时使用。\n\n飞机售票系统", code));

            mailSender.send(message);

            logger.info("[EmailService] 验证码邮件已发送到: {}", email);
            return true;

        } catch (Exception e) {
            logger.error("[EmailService] 发送验证码邮件失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 校验邮箱验证码
     * @param email 邮箱
     * @param code 验证码
     * @return true 则校验通过
     */
    public boolean checkVerifyCode(String email, String code) {
        if (email == null || code == null) {
            return false;
        }

        String cached = codeStore.get(email);
        Long ts = codeTimestamp.get(email);

        if (cached != null && ts != null) {
            if (System.currentTimeMillis() - ts > CODE_TTL_MS) {
                // 验证码过期
                codeStore.remove(email);
                codeTimestamp.remove(email);
                logger.warn("[EmailService] 验证码已过期: {}", email);
                return false;
            }

            boolean isValid = cached.equals(code);
            if (isValid) {
                // 成功校验后移除验证码
                codeStore.remove(email);
                codeTimestamp.remove(email);
                logger.info("[EmailService] 验证码校验成功: {}", email);
            } else {
                logger.warn("[EmailService] 验证码校验失败: {}", email);
            }
            return isValid;
        }

        logger.warn("[EmailService] 未找到验证码缓存: {}", email);
        return false;
    }

    /**
     * 发送通知邮件（如注册成功、订单确认等）
     * @param toEmail 收件邮箱
     * @param subject 邮件主题
     * @param content 邮件内容
     * @return true 表示发送成功
     */
    public boolean sendNotification(String toEmail, String subject, String content) {
        if (toEmail == null || toEmail.isEmpty() || subject == null || content == null) {
            return false;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(content);

            mailSender.send(message);

            logger.info("[EmailService] 通知邮件已发送到: {}", toEmail);
            return true;

        } catch (Exception e) {
            logger.error("[EmailService] 发送通知邮件失败: {}", e.getMessage(), e);
            return false;
        }
    }
}
