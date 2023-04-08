package org.bot.ua.service.Impl;

import org.bot.ua.dto.MailParams;
import org.bot.ua.service.MailSenderService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
public class MailSenderServiceImpl implements MailSenderService {
    private final JavaMailSender javaMailSender;
    @Value("${spring.mail.username}")
    private String emailFrom;
    @Value("${service.activation.uri}")
    private String activationServiceUri;

    public MailSenderServiceImpl(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @Override
    public void send(MailParams mailParams) {
        var subject = "Account Activation";
        var messageBody = getActivationMailBody(mailParams.getId());
        var emailTo = mailParams.getEmailTo();

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(emailFrom);
            helper.setTo(emailTo);
            helper.setSubject(subject);
            helper.setText(messageBody, true);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }

        javaMailSender.send(mimeMessage);
    }

    private String getActivationMailBody(String id) {
        var message = "<html><body><p style='font-size:14px; line-height:1.5em; margin-bottom: 20px;'>Welcome to FileFusionBot! </p>"
                + "<p style='font-size:14px; line-height:1.5em; margin-bottom: 20px;'>To activate your account, please click on the following link:</p>"
                + "<p style='font-size:14px; line-height:1.5em; margin-bottom: 20px;'><a href='" + activationServiceUri + id + "' style='color: #2196f3;'>Activate Account</a></p>"
                + "</body></html>";
        return message;
    }



}

