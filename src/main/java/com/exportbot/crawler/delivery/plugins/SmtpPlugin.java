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
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class SmtpPlugin implements DeliveryPlugin {

    private static final Logger logger = LoggerFactory.getLogger(SmtpPlugin.class);

    private JavaMailSender mailSender;
    private String from;
    private List<String> to;
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
        props.put("mail.smtp.starttls.enable", "true");

        this.mailSender = sender;
        this.from = getString(options, "from");
        this.to = (List<String>) options.get("to");
        this.subject = getString(options, "subject", "Export - {{date}}");
    }

    @Override
    public DeliveryResult deliver(List<String> files, DeliveryMetadata metadata) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(from);
            helper.setTo(to.toArray(new String[0]));
            helper.setSubject(interpolate(subject, metadata));
            helper.setText("Exported files attached.");

            for (String filePath : files) {
                File file = new File(filePath);
                if (file.exists()) {
                    helper.addAttachment(file.getName(), new FileSystemResource(file));
                }
            }

            mailSender.send(message);
            logger.info("Email sent successfully to: {}", to);

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
