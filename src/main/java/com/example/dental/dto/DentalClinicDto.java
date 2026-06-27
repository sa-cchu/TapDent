package com.example.dental.dto;

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
    private ContractStatusDto contractStatus;
    private Integer maxReserveMonth;
    private Boolean reservationRestrictions;
    private Integer roleId;
    private String publicUrlToken;
}
