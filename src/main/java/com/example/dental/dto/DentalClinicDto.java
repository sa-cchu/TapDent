package com.example.dental.dto;

import com.example.dental.enums.ContractStatusName;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DentalClinicDto {
    private Long dentalId;
    private String loginId;
    private String password;
    private String name;
    private String address;
    private String tel;
    private String mail;
    private ContractStatusName contractStatus;
    private Integer maxReserveMonth;
    private Boolean reservationRestrictions;
    private Integer reservationTimeUnit;
    private Integer roleId;
    private String publicUrlToken;

}
