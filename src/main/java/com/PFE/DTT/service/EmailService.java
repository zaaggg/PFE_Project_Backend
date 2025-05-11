package com.PFE.DTT.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    public void sendVerificationEmail(String to, String verificationCode) throws MessagingException {
        logger.info("Sending verification email to {}", to);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(to);
        helper.setSubject("Email Verification");
        helper.setFrom("zaagkhalyl@gmail.com");

        String html = getStyledEmailTemplate("EMAIL VERIFICATION", "Verify Your E-mail Address", verificationCode, "Use the code below to verify your email address:", null, null);
        helper.setText(html, true);

        mailSender.send(message);
    }

    public void sendReportCreationEmail(String to, String protocolName, String protocolType, String firstName, String lastName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(to);
            helper.setSubject("📝 Nouveau rapport assigné à vous");
            helper.setFrom("zaagkhalyl@gmail.com");

            String html = getStyledEmailTemplate("NEW REPORT ASSIGNED", "You Have a New Report", protocolName, "Protocol:", protocolType, String.format("Created by: %s %s", firstName, lastName));
            helper.setText(html, true);

            mailSender.send(message);
        } catch (Exception e) {
            logger.error("Failed to send report creation email to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send report creation email", e);
        }
    }

    public void sendReportCompletedEmail(String to, String protocolName, String serialNumber, String firstName, String lastName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(to);
            helper.setSubject("✅ Rapport complété : " + protocolName);
            helper.setFrom("zaagkhalyl@gmail.com");

            String completedInfo = String.format("Rapport '%s'\nN° série : %s\nCréé par : %s %s", protocolName, serialNumber, firstName, lastName);
            String html = getStyledEmailTemplate("REPORT COMPLETED", "Report Fully Completed", completedInfo, "Details:", null, "You can now view the report in your dashboard.");
            helper.setText(html, true);

            mailSender.send(message);
        } catch (Exception e) {
            logger.error("Failed to send report completion email to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send report completion email", e);
        }
    }

    public void sendUserCreationEmail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setFrom("zaagkhalyl@gmail.com");

            String html = getStyledEmailTemplate(subject.toUpperCase(), subject, body, "Message:", null, null);
            helper.setText(html, true);

            mailSender.send(message);
            logger.info("HTML styled email sent successfully to {}", to);
        } catch (Exception e) {
            logger.error("Failed to send styled email to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send styled email", e);
        }
    }

    private String getStyledEmailTemplate(String bannerText, String title, String mainBox, String label, String subLabel, String contentNote) {
        return String.format("""
            <html><head><style>
              body { margin: 0; padding: 0; font-family: 'Segoe UI', sans-serif; background-color: #f6f6f6; color: #000; }
              .container { max-width: 600px; margin: 0 auto; background-color: #fff; }
              .logo { text-align: center; padding: 30px 0 0; }
              .logo img { max-width: 180px; }
              .header { background-color: #003399; color: #fff; text-align: center; padding: 40px 10px 20px; }
              .header h2 { margin: 0; font-size: 28px; }
              .verify-code { background-color: #ffffff; text-align: center; padding: 30px 10px 10px; }
              .verify-code p { font-size: 20px; font-weight: 600; margin-bottom: 20px; }
              .verify-code-box { display: inline-block; padding: 22px 44px; font-size: 20px; font-weight: bold; background-color: #f16c17; color: #fff; border-radius: 6px; letter-spacing: 1px; margin-top: 10px; }
              .content { padding: 30px 20px; text-align: center; line-height: 1.8; font-size: 17px; }
              .footer { background-color: #e5eaf5; text-align: center; padding: 30px 20px; }
              .footer strong { display: block; color: #003399; font-size: 18px; margin-bottom: 10px; }
              .footer a { color: #000; text-decoration: none; }
              .bottom-bar { background-color: #003399; text-align: center; padding: 10px; color: #fff; font-size: 14px; }
              .social-icons img { width: 24px; margin: 10px 8px 0; }
            </style></head><body>
            <div class='container'>
              <div class='logo'>
                <img src='https://upload.wikimedia.org/wikipedia/commons/thumb/f/f4/Leoni_AG_Logo.svg/2560px-Leoni_AG_Logo.svg.png' alt='Company Logo' />
              </div>
              <div class='header'>
                <p style='font-size: 20px; font-weight: bold'>%s</p>
                <h2>%s</h2>
              </div>
              <div class='verify-code'>
                <p><strong>%s</strong></p>
                <div class='verify-code-box'>%s</div>
                %s
              </div>
              <div class='content'>
                <p><strong>%s</strong></p>
                <p>Thanks,<br />The Company Team</p>
              </div>
              <div class='footer'>
                <strong>Get in touch</strong>
                <p>+11 111 333 4444<br />
                <a href='mailto:info@yourcompany.com'>info@yourcompany.com</a></p>
                <div class='social-icons'>
                  <a href='#'><img src='https://cdn-icons-png.flaticon.com/512/733/733547.png' alt='Facebook' /></a>
                  <a href='#'><img src='https://cdn-icons-png.flaticon.com/512/733/733561.png' alt='LinkedIn' /></a>
                  <a href='#'><img src='https://cdn-icons-png.flaticon.com/512/733/733558.png' alt='Instagram' /></a>
                  <a href='#'><img src='https://cdn-icons-png.flaticon.com/512/733/733646.png' alt='YouTube' /></a>
                  <a href='#'><img src='https://cdn-icons-png.flaticon.com/512/732/732200.png' alt='Email' /></a>
                </div>
              </div>
              <div class='bottom-bar'>
                Copyrights &copy; Company All Rights Reserved
              </div>
            </div>
            </body></html>
        """, bannerText, title, label, mainBox, subLabel == null ? "" : "<p><strong>" + subLabel + "</strong></p>", contentNote == null ? "You can continue using the app." : contentNote);
    }
}
