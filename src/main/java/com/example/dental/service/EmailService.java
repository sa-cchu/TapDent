package com.example.dental.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * 4桁のランダムなパスコードを生成します
     */
    public String generatePasscode() {
        Random random = new Random();
        int code = 1000 + random.nextInt(9000);
        return String.valueOf(code);
    }

    /**
     * パスコードを記載したメールを送信します
     */
    public void sendPasscode(String toEmail, String passcode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("【TapDent】ログイン情報の変更パスコード");
        message.setText("ログインID/パスワードを変更するためのパスコードは以下の通りです。\n\n" +
                passcode + "\n\n" +
                "このパスコードは現在のセッションでのみ有効です。\n" +
                "設定画面に入力し、更新を完了してください。");

        try {
            mailSender.send(message);
            logger.info("Passcode email successfully sent to {}", toEmail);
        } catch (Exception e) {
            // ローカル環境等でMailHogなどのSMTPが起動していない場合のフォールバックとしてログに出力
            logger.error("Failed to send email. Falling back to console output. Error: {}", e.getMessage());
            System.out.println("====== [MAIL LOG (Local Fallback)] ======");
            System.out.println("To: " + toEmail);
            System.out.println("Subject: " + message.getSubject());
            System.out.println("Passcode: " + passcode);
            System.out.println("=========================================");
        }
    }
}
