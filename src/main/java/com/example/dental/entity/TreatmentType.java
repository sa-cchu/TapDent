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
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import com.example.dental.enums.TargetPatientType;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "treatment_type")
@SQLDelete(sql = "UPDATE treatment_type SET is_deleted = true WHERE treatment_id = ?")
@Getter
@Setter
public class TreatmentType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "treatment_id")
    private Long treatmentId;

    // 歯科医院情報との多対一の紐付け（外部キー: dental_id）
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dental_id", nullable = false)
    private DentalClinic dentalClinic;

    @Column(name = "treatment_name", nullable = false, length = 20)
    private String treatmentName;

    // 所要時間（分単位）
    @Column(name = "required_minutes", nullable = false)
    private Integer requiredMinutes;

    // 公開状況（TRUE: 公開、FALSE: 非公開（電話予約へ誘導））
    // 設計書の備考「TRUE:FALSEの場合は...」は「TRUE:公開/FALSE:非公開」の意図として初期値TRUEで設定
    @Column(nullable = false)
    private Boolean status = true;

    // 対象患者（初診、既存）
    @Enumerated(EnumType.STRING)
    @Column(name = "target_patient_type", nullable = false)
    private TargetPatientType targetPatientType;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;
}