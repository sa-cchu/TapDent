package com.example.dental.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "token")
@Getter
@Setter
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_id")
    private Long tokenId;

    @Column(name = "token_value", nullable = false, unique = true, length = 255)
    private String tokenValue;

    @Column(name = "dental_id", nullable = false)
    private Long dentalId;

    @Column(nullable = false, length = 20)
    private String name;

    // 生年月日（日付のみのため LocalDate を採用）
    @Column(nullable = false)
    private LocalDate birthday;

    @Column(nullable = false, length = 20)
    private String tel;

    @Column(nullable = false, length = 255)
    private String email;

    // 有効期限（時分秒まで管理するため LocalDateTime を採用）
    @Column(name = "expiry_time", nullable = false)
    private LocalDateTime expiryTime;
}