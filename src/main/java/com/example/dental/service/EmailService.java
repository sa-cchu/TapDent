package com.example.dental.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.dental.entity.Appointment;
import java.time.format.DateTimeFormatter;
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

    /**
     * 予約時の認証コードを記載したメールを送信します
     */
    @Async
    public void sendReservationVerificationCode(String toEmail, String passcode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("【TapDent】ご予約のメール認証コード");
        message.setText("ご予約を確定するための認証コードは以下の通りです。\n\n" +
                "【 " + passcode + " 】\n\n" +
                "※この認証コードの有効期限は送信後15分間です。\n" +
                "予約画面に戻り、上記コードを入力して予約を完了してください。\n" +
                "ご自身で予約を行っていない場合は、このメールを破棄してください。");

        try {
            mailSender.send(message);
            logger.info("Reservation verification email successfully sent to {}", toEmail);
        } catch (Exception e) {
            logger.error("Failed to send email. Falling back to console output. Error: {}", e.getMessage());
            System.out.println("====== [MAIL LOG (Local Fallback: Reservation Verify)] ======");
            System.out.println("To: " + toEmail);
            System.out.println("Subject: " + message.getSubject());
            System.out.println("Passcode: " + passcode);
            System.out.println("=============================================================");
        }
    /**
     * 予約完了メールを送信します
     */
    @Async
    public void sendReservationCompleteEmail(String toEmail, Appointment appointment) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("【TapDent】ご予約完了のお知らせ");

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        String text = "この度はご予約ありがとうございます。\n以下の内容で予約が確定いたしました。\n\n" +
                "■ご予約内容\n" +
                "・医院名: " + appointment.getDentalClinic().getName() + "\n" +
                "・日時: " + appointment.getStartAt().format(dateFormatter) + " " + appointment.getStartAt().format(timeFormatter) + " ～\n" +
                "・メニュー: " + appointment.getTreatmentType().getTreatmentName() + "\n\n" +
                "ご来院の際は、保険証と本人確認書類をお持ちください。\n" +
                "予約の変更やキャンセルは、お早めにダッシュボードからお願いいたします。";

        message.setText(text);

        try {
            mailSender.send(message);
            logger.info("Reservation complete email successfully sent to {}", toEmail);
        } catch (Exception e) {
            logger.error("Failed to send complete email. Error: {}", e.getMessage());
            System.out.println("====== [MAIL LOG (Reservation Complete)] ======");
            System.out.println("To: " + toEmail);
            System.out.println("Subject: " + message.getSubject());
            System.out.println("Text: \n" + text);
            System.out.println("===============================================");
        }
    }
}
