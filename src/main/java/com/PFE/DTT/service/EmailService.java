package com.PFE.DTT.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    public void sendVerificationEmail(String to, String verificationCode) throws MessagingException {
        logger.info("Attempting to send verification email to: {}", to);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject("Email Verification");
        helper.setFrom("zaagkhalyl@gmail.com");

        String htmlContent = """
                <!DOCTYPE html>
                               <html lang="en">
                                 <head>
                                   <meta charset="UTF-8" />
                                   <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                                   <title>Verify Email</title>
                                   <style>
                                     body {
                                       margin: 0;
                                       padding: 0;
                                       font-family: 'Segoe UI', sans-serif;
                                       background-color: #f6f6f6;
                                       color: #000;
                                     }
                                     .container {
                                       max-width: 600px;
                                       margin: 0 auto;
                                       background-color: #fff;
                                     }
                                     .logo {
                                       text-align: center;
                                       padding: 30px 0 0;
                                     }
                                     .logo img {
                                       max-width: 180px;
                                     }
                                     .header {
                                       background-color: #003399;
                                       color: #fff;
                                       text-align: center;
                                       padding: 40px 10px 20px;
                                     }
                                     .header h2 {
                                       margin: 0;
                                       font-size: 24px;
                                     }
                                     .verify-code {
                                       background-color: #ffffff;
                                       text-align: center;
                                       padding: 30px 10px 10px;
                                     }
                                     .verify-code p {
                                       font-size: 18px;
                                       font-weight: 500;
                                       margin-bottom: 20px;
                                     }
                                     .verify-code-box {
                                       display: inline-block;
                                       padding: 20px 40px;
                                       font-size: 32px;
                                       font-weight: bold;
                                       background-color: #f16c17;
                                       color: #fff;
                                       border-radius: 6px;
                                       letter-spacing: 6px;
                                       margin-top: 10px;
                                     }
                                     .content {
                                       padding: 30px 20px;
                                       text-align: center;
                                       line-height: 1.8;
                                       font-size: 17px;
                                     }
                                     .footer {
                                       background-color: #e5eaf5;
                                       text-align: center;
                                       padding: 30px 20px;
                                     }
                                     .footer strong {
                                       display: block;
                                       color: #003399;
                                       font-size: 18px;
                                       margin-bottom: 10px;
                                     }
                                     .footer a {
                                       color: #000;
                                       text-decoration: none;
                                     }
                                     .bottom-bar {
                                       background-color: #003399;
                                       text-align: center;
                                       padding: 10px;
                                       color: #fff;
                                       font-size: 14px;
                                     }
                                     .social-icons img {
                                       width: 24px;
                                       margin: 10px 8px 0;
                                     }
                                   </style>
                                 </head>
                                 <body>
                                   <div class="container">
                                     <div class="logo">
                                       <img src="https://upload.wikimedia.org/wikipedia/commons/thumb/f/f4/Leoni_AG_Logo.svg/2560px-Leoni_AG_Logo.svg.png" alt="Company Logo" />
                                     </div>
                
                                     <div class="header">
                                       <div>
                                         <p>THANKS FOR SIGNING UP!</p>
                                         <h2>Verify Your E-mail Address</h2>
                                       </div>
                                     </div>
                
                                     <div class="verify-code">
                                       <p><strong>Use the code below to verify your email address:</strong></p>
                                       <div class="verify-code-box">%s</div>
                                     </div>
                
                                     <div class="content">
                                       <p><strong>You're almost ready to get started.<br />
                                       Enter the above verification code in the app to continue your registration.</strong></p>
                                       <p>Thanks,<br />The Company Team</p>
                                     </div>
                
                                     <div class="footer">
                                       <strong>Get in touch</strong>
                                       <p>+11 111 333 4444<br />
                                       <a href="mailto:info@yourcompany.com">info@yourcompany.com</a></p>
                                       <div class="social-icons">
                                         <a href="#"><img src="https://cdn-icons-png.flaticon.com/512/733/733547.png" alt="Facebook" /></a>
                                         <a href="#"><img src="https://cdn-icons-png.flaticon.com/512/733/733561.png" alt="LinkedIn" /></a>
                                         <a href="#"><img src="https://cdn-icons-png.flaticon.com/512/733/733558.png" alt="Instagram" /></a>
                                         <a href="#"><img src="https://cdn-icons-png.flaticon.com/512/733/733646.png" alt="YouTube" /></a>
                                         <a href="#"><img src="https://cdn-icons-png.flaticon.com/512/732/732200.png" alt="Email" /></a>
                                       </div>
                                     </div>
                
                                     <div class="bottom-bar">
                                       Copyrights &copy; Company All Rights Reserved
                                     </div>
                                   </div>
                                 </body>
                               </html>
                
            """.formatted(verificationCode);

        helper.setText(htmlContent, true);

        try {
            mailSender.send(message);
            logger.info("Verification email sent successfully to: {}", to);
        } catch (Exception e) {
            logger.error("Failed to send verification email to: {}. Error: {}", to, e.getMessage(), e);
            throw e;
        }
    }

    public void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, false); // plain text
            helper.setFrom("zaagkhalyl@gmail.com");

            mailSender.send(message);
            logger.info("Plain email sent successfully to {}", to);
        } catch (Exception e) {
            logger.error("Failed to send plain email to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    public void sendReportCreationEmail(String to, String protocolName, String protocolType,  String createdByFirstName , String createdByLastName ) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setTo(to);
            helper.setSubject("📝 Nouveau rapport assigné à vous");

            String body = String.format(
                    "Bonjour,\n\n" +
                            "Un nouveau rapport intitulé de protocol %s de type %s a été créé par %s %s et vous a été assigné.\n" +
                            "Merci de vérifier et de compléter votre partie dans les délais.\n\n" +
                            "Cordialement,\n" ,
                    protocolName,
                    protocolType,
                    createdByFirstName,
                    createdByLastName

            );

            helper.setText(body, false); // Plain text
            helper.setFrom("zaagkhalyl@gmail.com");

            mailSender.send(message);
            logger.info("Report creation email sent successfully to {}", to);
        } catch (Exception e) {
            logger.error("Failed to send report creation email to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send report creation email", e);
        }
    }

    public void sendReportCompletedEmail(String to, String protocolName, String serialNumber,
                                         String firstName, String lastName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setTo(to);
            helper.setSubject("✅ Rapport complété : " + protocolName);

            String body = String.format(
                    "Bonjour %s %s,\n\n" +
                            "Le rapport \"%s\" (N° série : %s) que vous avez créé est maintenant complété.\n\n" +
                            "Vous pouvez le consulter dans votre tableau de bord.\n\n" +
                            "Cordialement,\n" +
                            "L’équipe DTT.",
                    firstName, lastName, protocolName, serialNumber
            );

            helper.setText(body, false); // Plain text
            helper.setFrom("zaagkhalyl@gmail.com");

            mailSender.send(message);
            logger.info("✅ Report completion email sent to {}", to);
        } catch (Exception e) {
            logger.error("❌ Failed to send report completion email to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send report completion email", e);
        }
    }





}