package com.example.dental.entity;

import java.time.LocalDate;
import java.time.LocalTime;

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

import com.example.dental.enums.ExceptionType;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "calendar_exception")
@Getter
@Setter
public class CalendarException {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "calendar_id")
    private Long calendarId;

    // 歯科医院情報との紐付け
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dental_id", nullable = false)
    private DentalClinic dentalClinic;

    // 予定日（日付のみのため LocalDate を採用）
    @Column(name = "target_date", nullable = false)
    private LocalDate targetDate;

    // 例外種別（休診 or 特別診療）
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ExceptionType type = ExceptionType.HOLIDAY;

    // 歯科医師情報との紐付け（特定の医師だけ制限する場合があるため nullable = true）
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dentist_id", nullable = true)
    private Dentist dentist;

    // 開始時刻（時刻のみのため LocalTime を採用、nullable = true）
    @Column(name = "start_at")
    private LocalTime startAt;

    // 終了時刻（時刻のみのため LocalTime を採用、nullable = true）
    @Column(name = "end_at")
    private LocalTime endAt;

    // 休憩開始時刻
    @Column(name = "break_start_at")
    private LocalTime breakStartAt;

    // 休憩終了時刻
    @Column(name = "break_end_at")
    private LocalTime breakEndAt;

    // 終日フラグ
    @Column(name = "is_all_day", nullable = false)
    private Boolean isAllDay = false;
}