package com.example.dental.dto;

import com.example.dental.enums.RoleName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleDto {
    private Integer roleId;
    private RoleName roleName;
}
