package com.example.dental.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminDto {
    private Integer adminId;
    private String loginId;
    private String password;
    private String name;
    private RoleDto role;
}
