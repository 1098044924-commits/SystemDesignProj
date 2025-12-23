package org.example.accounting.service.impl;

import org.example.accounting.service.EmailService;
import org.springframework.stereotype.Service;

@Service
public class LoggingEmailService implements EmailService {
    @Override
    public void sendEmail(String to, String subject, String body) {
        // fallback/no-op logging implementation used when SMTP not configured
        System.out.println("Email send (logged): to=" + to + " subject=" + subject + " body=" + body);
    }
}






