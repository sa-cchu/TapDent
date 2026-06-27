package com.example.dental.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class NewsDto {
    private Long newsId;
    private String title;
    private String content;
    private LocalDateTime publishAt;
}
