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

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "dentist")
@SQLDelete(sql = "UPDATE dentist SET is_deleted = true WHERE dentist_id=?")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
public class Dentist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dentist_id")
    private Long dentistId;

    // 歯科医院情報との多対一の紐付け（外部キー: dental_id）
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dental_id", nullable = false)
    private DentalClinic dentalClinic;

    @Column(name = "dentist_name", nullable = false, length = 20)
    private String dentistName;

    // TRUE：稼働、FALSE；非稼働（デフォルトTRUE）
    @Column(nullable = false)
    private Boolean status = true;

    // 論理削除フラグ
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;
}
