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

import com.example.dental.enums.AppointmentStatus;

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

	// 患者情報との紐付け（仮患者の場合もあるため nullable = true）
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "patient_id", nullable = true)
	private Patient patient;

	// 診療メニュー情報との紐付け
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "treatment_id", nullable = false)
	private TreatmentType treatmentType;

	// 予約方法（TRUE:オンライン予約、FALSE：オフライン予約）
	@Column(name = "appoint_method", nullable = false)
	private Boolean appointMethod;

	@Column(name = "start_at", nullable = false)
	private LocalDateTime startAt;

	@Column(name = "end_at", nullable = false)
	private LocalDateTime endAt;

	// 更新日時
	@Column(name = "update_at", nullable = false)
	private LocalDateTime updateAt;

	// ステータス（Enumをインデックス（数字）としてDBに保存）
	@Enumerated(EnumType.ORDINAL)
	@Column(nullable = false)
	private AppointmentStatus status;
}