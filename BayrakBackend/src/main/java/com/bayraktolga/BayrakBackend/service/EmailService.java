package com.bayraktolga.BayrakBackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Async
    public void sendWelcomeEmail(String toEmail, String firstName) {
        String subject = "Bayrak Bank'a Hoş Geldiniz!";
        String body = String.format(
                "Merhaba %s,\n\nBayrak Bank ailesine hoş geldiniz! Hesabınız başarıyla oluşturuldu.\n\nİyi günler dileriz.",
                firstName
        );
        sendEmail(toEmail, subject, body);
    }

    @Async
    public void sendTransferEmail(String toEmail, String firstName, String referenceNo, String amount) {
        String subject = "Para Transferi Bildirimi - " + referenceNo;
        String body = String.format(
                "Merhaba %s,\n\n%s TL tutarındaki transfer işleminiz tamamlanmıştır.\nReferans No: %s\n\nHerhangi bir sorunuz için bize ulaşabilirsiniz.",
                firstName, amount, referenceNo
        );
        sendEmail(toEmail, subject, body);
    }

    @Async
    public void sendPasswordChangedEmail(String toEmail, String firstName) {
        String subject = "Şifreniz Değiştirildi";
        String body = String.format(
                "Merhaba %s,\n\nHesabınızın şifresi başarıyla değiştirildi. Bu işlemi siz yapmadıysanız lütfen hemen bizimle iletişime geçin.",
                firstName
        );
        sendEmail(toEmail, subject, body);
    }

    private void sendEmail(String toEmail, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email gönderildi: {}", toEmail);
        } catch (Exception e) {
            log.error("Email gönderilemedi: {} - Hata: {}", toEmail, e.getMessage());
        }
    }
}
