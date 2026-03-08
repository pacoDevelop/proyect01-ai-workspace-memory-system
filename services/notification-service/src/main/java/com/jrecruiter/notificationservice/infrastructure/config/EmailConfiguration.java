package com.jrecruiter.notificationservice.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

import java.util.Properties;

/**
 * Email Configuration: SMTP + Thymeleaf template engine setup
 * 
 * Properties required in application-{profile}.yml:
 * - mail.smtp.host
 * - mail.smtp.port
 * - mail.smtp.username
 * - mail.smtp.password
 * - mail.smtp.auth
 * - mail.smtp.starttls.enable
 * - mail.smtp.starttls.required
 * - notification.email.from
 * - notification.email.from-name
 * 
 * @author GitHub Copilot / TASK-018
 */
@Configuration
public class EmailConfiguration {
    
    /**
     * Configure JavaMailSender for SMTP
     */
    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        
        // SMTP properties (from application-{profile}.yml)
        mailSender.setHost("${mail.smtp.host}");
        mailSender.setPort(587); // or 25, 465 depending on provider
        mailSender.setUsername("${mail.smtp.username}");
        mailSender.setPassword("${mail.smtp.password}");
        
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "5000");
        props.put("mail.smtp.writetimeout", "5000");
        props.put("mail.smtp.socketFactory.port", "587");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");
        
        return mailSender;
    }
    
    /**
     * Configure Thymeleaf template resolver for email templates
     */
    @Bean
    public ITemplateResolver emailTemplateResolver() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(true);
        resolver.setCacheTTLMs(3600000L); // 1 hour cache
        resolver.setOrder(1);
        return resolver;
    }
    
    /**
     * Configure Thymeleaf template engine for email
     */
    @Bean
    public SpringTemplateEngine templateEngine() {
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.addTemplateResolver(emailTemplateResolver());
        engine.setEnableSpringELCompiler(true);
        return engine;
    }
}
