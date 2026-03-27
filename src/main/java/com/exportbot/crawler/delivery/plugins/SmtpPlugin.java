package com.exportbot.crawler.delivery.plugins;

import com.exportbot.crawler.delivery.DeliveryPlugin;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class SmtpPlugin implements DeliveryPlugin {

    private static final Logger logger = LoggerFactory.getLogger(SmtpPlugin.class);

    private JavaMailSender mailSender;
    private String from;
    private String subject;

    @Override
    public String getName() {
        return "smtp";
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initialize(Map<String, Object> options) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(getString(options, "host"));
        sender.setPort(getInt(options, "port", 587));
        sender.setUsername(getString(options, "user"));
        sender.setPassword(getString(options, "pass"));

        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        
        // 根据端口判断是否启用 SSL
        int port = getInt(options, "port", 587);
        if (port == 465) {
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.ssl.trust", getString(options, "host"));
        } else {
            props.put("mail.smtp.starttls.enable", "true");
        }

        this.mailSender = sender;
        this.from = getString(options, "from");
        this.subject = getString(options, "subject", "ProcessOn 图片自动导出通知");
    }

    @Override
    public DeliveryResult deliver(List<String> files, DeliveryMetadata metadata) {
        try {
            // 从 metadata 中获取收件人邮箱列表（支持多个，逗号分隔）
            Object toObj = metadata.variables().get("emailRecipients");
            if (toObj == null) {
                throw new IllegalArgumentException("邮件接收人 (emailRecipients) 不能为空");
            }
            
            List<String> toList = Arrays.stream(String.valueOf(toObj).split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            
            if (toList.isEmpty()) {
                throw new IllegalArgumentException("邮件接收人列表不能为空");
            }

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(from);
            helper.setTo(toList.toArray(new String[0]));
            helper.setSubject(interpolate(subject, metadata));
            helper.setText("您好！\n\n您本次的图片导出任务已完成，请查看附件。");

            for (String filePath : files) {
                File file = new File(filePath);
                if (file.exists()) {
                    helper.addAttachment(file.getName(), new FileSystemResource(file));
                }
            }

            mailSender.send(message);
            logger.info("Email sent successfully to: {}", toList);

            return new DeliveryResult(true, getName(), "Email sent successfully", null);
        } catch (MessagingException e) {
            logger.error("Failed to send email", e);
            return new DeliveryResult(false, getName(), e.getMessage(), null);
        }
    }

    private String interpolate(String text, DeliveryMetadata metadata) {
        if (text == null) return "";
        return text.replace("{{date}}", metadata.timestamp());
    }

    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    private String getString(Map<String, Object> map, String key, String defaultValue) {
        Object value = map.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    private int getInt(Map<String, Object> map, String key, int defaultValue) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException ignored) {}
        }
        return defaultValue;
    }
}
