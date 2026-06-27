package com.example.dental.dto;

import com.example.dental.enums.ContractStatusName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContractStatusDto {
    private Integer statusId;
    private ContractStatusName statusName;
}
