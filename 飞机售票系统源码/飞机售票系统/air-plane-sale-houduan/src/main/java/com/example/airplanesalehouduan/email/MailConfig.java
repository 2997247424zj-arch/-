package com.example.airplanesalehouduan.email;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * 邮件配置类
 * 显式配置JavaMailSender bean，确保邮件功能正常工作
 */
@Configuration
public class MailConfig {

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.163.com");
        mailSender.setPort(465); // 使用SSL端口
        mailSender.setUsername("15828824107@163.com"); // 请替换为您的真实163邮箱
        mailSender.setPassword("VHbUtcZ64wFPwz6a"); // 请替换为您的授权码

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtps"); // 使用smtps协议
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.ssl.trust", "smtp.163.com");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");

        return mailSender;
    }
}
