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
// 複合ユニーク制約（dentist_id と treatment_id の組み合わせの重複を防ぐ）
@Table(
    name = "dentist_treatment",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_dentist_treatment",
        columnNames = {"dentist_id", "treatment_id"}
    )
)
@Getter
@Setter
public class DentistTreatment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dt_id")
    private Long dtId;

    // 歯科医師情報との多対一の紐付け（外部キー: dentist_id）
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dentist_id", nullable = false)
    private Dentist dentist;

    // 診療メニュー情報との多対一の紐付け（外部キー: treatment_id）
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "treatment_id", nullable = false)
    private TreatmentType treatmentType;
}
