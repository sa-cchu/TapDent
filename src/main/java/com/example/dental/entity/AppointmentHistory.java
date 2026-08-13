package com.example.dental.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import com.example.dental.enums.AppointMethod;
import com.example.dental.enums.AppointmentStatus;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "appointments_history")
@Getter
@Setter
public class AppointmentHistory {

    // 主キーカラム名：appointment_id (設計書通り、履歴テーブル自体の自動採番IDとして定義)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "appointment_id")
    private Long appointmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dental_id", nullable = false)
    private DentalClinic dentalClinic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chair_id", nullable = false)
    private DentalChair dentalChair;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dentist_id", nullable = false)
    private Dentist dentist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "treatment_id", nullable = false)
    private TreatmentType treatmentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "appoint_method", nullable = false)
    private AppointMethod appointMethod;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Column(name = "patient_comment", columnDefinition = "TEXT")
    private String patientComment;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.ORDINAL)
    @Column(nullable = false)
    private AppointmentStatus status;

    // アーカイブ日時（いつ履歴に移動したか）
    @Column(name = "archive_at", nullable = false)
    private LocalDateTime archiveAt;
}