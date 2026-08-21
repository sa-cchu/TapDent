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
import com.example.dental.enums.VisitType;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "appointments")
@Getter
@Setter
public class Appointment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "appointment_id")
	private Long appointmentId;

	// 歯科医院情報との紐付け
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "dental_id", nullable = false)
	private DentalClinic dentalClinic;

	// チェア情報との紐付け
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "chair_id", nullable = false)
	private DentalChair dentalChair;

	// 歯科医師との紐付け
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "dentist_id", nullable = false)
	private Dentist dentist;

	// 患者情報との紐付け（必須）
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "patient_id", nullable = false)
	private Patient patient;

	// 診療メニュー情報との紐付け
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "treatment_id", nullable = false)
	private TreatmentType treatmentType;

	// 予約方法
	@Enumerated(EnumType.STRING)
	@Column(name = "appoint_method", nullable = false)
	private AppointMethod appointMethod;

	@Column(name = "start_at", nullable = false)
	private LocalDateTime startAt;

	@Column(name = "end_at", nullable = false)
	private LocalDateTime endAt;

	// 予約時のコメント
	@Column(name = "patient_comment", columnDefinition = "TEXT")
	private String patientComment;

	// 作成日時
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	// 更新日時
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	// ステータス（Enumをインデックス（数字）としてDBに保存）
	@Enumerated(EnumType.ORDINAL)
	@Column(nullable = false)
	private AppointmentStatus status;

	// 初診・再診フラグ
	@Enumerated(EnumType.STRING)
	@Column(name = "visit_type", nullable = false)
	private VisitType visitType;
}