package com.example.dental.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.example.dental.enums.ContractStatusName;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "contract_statuses")
@Getter
@Setter
public class ContractStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "status_id")
    private Integer statusId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_name", nullable = false, unique = true, length = 10)
    private ContractStatusName statusName;
}
