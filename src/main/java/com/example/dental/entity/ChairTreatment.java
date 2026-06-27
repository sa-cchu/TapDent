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
import jakarta.persistence.UniqueConstraint;

import lombok.Getter;
import lombok.Setter;

@Entity
// 複合ユニーク制約（chair_id と treatment_id の組み合わせの重複を防ぐ）を実務的に定義
@Table(
    name = "chair_treatment",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_chair_treatment",
        columnNames = {"chair_id", "treatment_id"}
    )
)
@Getter
@Setter
public class ChairTreatment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ct_id")
    private Long ctId;

    // チェア情報との多対一の紐付け（外部キー: chair_id）
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chair_id", nullable = false)
    private DentalChair dentalChair;

    // 診療メニュー情報との多対一の紐付け（外部キー: treatment_id）
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "treatment_id", nullable = false)
    private TreatmentType treatmentType;
}