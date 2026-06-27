package com.example.dental.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "news")
@Getter
@Setter
public class News {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "news_id")
    private Long newsId;

    @Column(nullable = false, length = 20)
    private String title;

    // 長文のTEXT型に対応するため、@LobアノテーションとcolumnDefinitionを指定
    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // 公開日時（時分秒まで制御するため LocalDateTime を採用）
    @Column(name = "publish_at", nullable = false)
    private LocalDateTime publishAt;
}