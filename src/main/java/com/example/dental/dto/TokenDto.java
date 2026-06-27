package com.example.dental.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class TokenDto {
    private Long tokenId;
    private String tokenValue;
    private Long dentalId;
    private String name;
    private LocalDate birthday;
    private String tel;
    private String email;
    private LocalDateTime expiryTime;
}
