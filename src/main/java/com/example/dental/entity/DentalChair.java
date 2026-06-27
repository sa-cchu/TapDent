package com.example.dental.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "dental_chair")
@Getter
@Setter
public class DentalChair {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chair_id")
    private Long chairId;

    // 歯科医院情報との多対一の紐付け（外部キー: dental_id）
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dental_id", nullable = false)
    private DentalClinic dentalClinic;

    @Column(name = "chair_name", nullable = false, length = 20)
    private String chairName;

    // TRUE：稼働、FALSE；非稼働（デフォルトTRUE）
    @Column(nullable = false)
    private Boolean status = true;
}