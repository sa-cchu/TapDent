package com.example.dental.service;
import java.time.LocalDate;
import java.time.LocalTime;
import com.example.dental.enums.VisitType;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.dental.dto.ReservationFormDto;
import com.example.dental.entity.Appointment;
import com.example.dental.entity.CalendarException;
import com.example.dental.entity.ChairTreatment;
import com.example.dental.entity.DentalChair;
import com.example.dental.entity.DentalClinic;
import com.example.dental.entity.Dentist;
import com.example.dental.entity.DentistTreatment;
import com.example.dental.entity.Patient;
import com.example.dental.entity.Token;
import com.example.dental.entity.TreatmentType;
import com.example.dental.enums.AppointmentStatus;
import com.example.dental.enums.ExceptionType;
import com.example.dental.repository.AppointmentRepository;
import com.example.dental.repository.CalendarExceptionRepository;
import com.example.dental.repository.ChairTreatmentRepository;
import com.example.dental.repository.DentalChairRepository;
import com.example.dental.repository.DentalClinicRepository;
import com.example.dental.repository.DentistRepository;
import com.example.dental.repository.DentistTreatmentRepository;
import com.example.dental.repository.PatientRepository;
import com.example.dental.repository.TokenRepository;
import com.example.dental.repository.TreatmentTypeRepository;

@Service
public class ClinicReservationService {

    private final AppointmentRepository appointmentRepository;
    private final DentalClinicRepository dentalClinicRepository;
    private final PatientRepository patientRepository;
    private final TokenRepository tokenRepository;
    private final TreatmentTypeRepository treatmentTypeRepository;
    private final DentalChairRepository dentalChairRepository;
    private final DentistRepository dentistRepository;
    private final ChairTreatmentRepository chairTreatmentRepository;
    private final DentistTreatmentRepository dentistTreatmentRepository;
    private final CalendarExceptionRepository calendarExceptionRepository;

    public ClinicReservationService(
            AppointmentRepository appointmentRepository,
            DentalClinicRepository dentalClinicRepository,
            PatientRepository patientRepository,
            TokenRepository tokenRepository,
            TreatmentTypeRepository treatmentTypeRepository,
            DentalChairRepository dentalChairRepository,
            DentistRepository dentistRepository,
            ChairTreatmentRepository chairTreatmentRepository,
            DentistTreatmentRepository dentistTreatmentRepository,
            CalendarExceptionRepository calendarExceptionRepository) {
        this.appointmentRepository = appointmentRepository;
        this.dentalClinicRepository = dentalClinicRepository;
        this.patientRepository = patientRepository;
        this.tokenRepository = tokenRepository;
        this.treatmentTypeRepository = treatmentTypeRepository;
        this.dentalChairRepository = dentalChairRepository;
        this.dentistRepository = dentistRepository;
        this.chairTreatmentRepository = chairTreatmentRepository;
        this.dentistTreatmentRepository = dentistTreatmentRepository;
        this.calendarExceptionRepository = calendarExceptionRepository;
    }

    @Transactional
    public Appointment createClinicReservation(DentalClinic clinic, ReservationFormDto dto) {
        // 悲観的ロックを取得して他の予約処理と競合しないようにする
        dentalClinicRepository.findByIdForUpdate(clinic.getDentalId())
            .orElseThrow(() -> new IllegalStateException("医院情報が見つかりません"));

        TreatmentType treatment = treatmentTypeRepository.findById(dto.getTreatmentId())
            .orElseThrow(() -> new IllegalArgumentException("診療メニューが見つかりません"));

        LocalDateTime startAt = LocalDateTime.of(dto.getReservationDate(), dto.getReservationTime());
        LocalDateTime endAt = startAt.plusMinutes(treatment.getRequiredMinutes());

        // その時間帯の既存予約を取得
        List<Appointment> existingApps = appointmentRepository.findByDentalClinicAndStartAtBetween(
            clinic, startAt.minusMinutes(1440), startAt.plusMinutes(1440));
        
        List<Appointment> overlappingApps = existingApps.stream()
            .filter(app -> app.getStatus() != AppointmentStatus.CANCELLED)
            .filter(app -> app.getStartAt().isBefore(endAt) && app.getEndAt().isAfter(startAt))
            .toList();

        // --- チェアの決定 ---
        DentalChair assignedChair = null;
        if (dto.getChairId() != null) {
            assignedChair = dentalChairRepository.findById(dto.getChairId())
                .orElseThrow(() -> new IllegalArgumentException("選択されたチェアが見つかりません"));
            
            // 重複チェック
            final Long selectedChairId = dto.getChairId();
            boolean isChairUsed = overlappingApps.stream()
                .anyMatch(a -> a.getDentalChair().getChairId().equals(selectedChairId));
            if (isChairUsed) {
                throw new IllegalStateException("選択されたチェアはその時間帯にすでに予約が入っています");
            }
        } else {
            // 自動振り分け
            List<DentalChair> capableChairs = chairTreatmentRepository.findByTreatmentType(treatment).stream()
                    .map(ChairTreatment::getDentalChair)
                    .filter(c -> !c.getIsDeleted() && Boolean.TRUE.equals(c.getStatus()))
                    .toList();
            List<Long> usedChairIds = overlappingApps.stream().map(a -> a.getDentalChair().getChairId()).toList();
            assignedChair = capableChairs.stream()
                .filter(c -> !usedChairIds.contains(c.getChairId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("利用可能なチェアがありません"));
        }

        // --- 担当医の決定 ---
        Dentist assignedDentist = null;
        if (dto.getDentistId() != null) {
            assignedDentist = dentistRepository.findById(dto.getDentistId())
                .orElseThrow(() -> new IllegalArgumentException("選択された歯科医師が見つかりません"));
            
            // 重複チェック
            final Long selectedDentistId = dto.getDentistId();
            boolean isDentistUsed = overlappingApps.stream()
                .anyMatch(a -> a.getDentist().getDentistId().equals(selectedDentistId));
            if (isDentistUsed) {
                throw new IllegalStateException("選択された歯科医師はその時間帯にすでに予約が入っています");
            }
            
            // 休日チェック
            List<CalendarException> dailyExceptions = calendarExceptionRepository.findByDentalClinicAndTargetDateBetween(clinic, dto.getReservationDate(), dto.getReservationDate());
            boolean isDentistHoliday = dailyExceptions.stream()
                .anyMatch(e -> e.getType() == ExceptionType.HOLIDAY && e.getDentist() != null && e.getDentist().getDentistId().equals(selectedDentistId));
            if (isDentistHoliday) {
                throw new IllegalStateException("選択された歯科医師はその日は休診です");
            }
        } else {
            // 自動振り分け
            List<CalendarException> dailyExceptions = calendarExceptionRepository.findByDentalClinicAndTargetDateBetween(clinic, dto.getReservationDate(), dto.getReservationDate());
            List<Long> holidayDentistIds = dailyExceptions.stream()
                .filter(e -> e.getType() == ExceptionType.HOLIDAY && e.getDentist() != null)
                .map(e -> e.getDentist().getDentistId())
                .toList();

            List<Dentist> capableDentists = dentistTreatmentRepository.findByTreatmentType(treatment).stream()
                    .map(DentistTreatment::getDentist)
                    .filter(d -> !d.getIsDeleted() && Boolean.TRUE.equals(d.getStatus()))
                    .toList();
            List<Long> usedDentistIds = overlappingApps.stream().map(a -> a.getDentist().getDentistId()).toList();
            
            assignedDentist = capableDentists.stream()
                .filter(d -> !usedDentistIds.contains(d.getDentistId()))
                .filter(d -> !holidayDentistIds.contains(d.getDentistId()))
                // 可能診療項目が少ない順に並び替え
                .sorted(Comparator.comparingInt(d -> dentistTreatmentRepository.countByDentist(d)))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("対応可能な歯科医師がいません"));
        }

        // --- 患者またはトークンの設定 ---
        Patient patient = null;
        Token token = null;

        if (dto.getPatientId() != null) {
            patient = patientRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new IllegalArgumentException("患者が見つかりません"));
        } else {
            // アカウント（Patientレコード）が未登録・未選択の場合はTokenに保存する
            token = new Token();
            token.setName(dto.getName());
            token.setNameKana(dto.getNameKana());
            token.setTell(dto.getTel());
            token = tokenRepository.save(token);
        }

        // --- 予約の作成 ---
        Appointment appointment = new Appointment();
        appointment.setDentalClinic(clinic);
        appointment.setDentalChair(assignedChair);
        appointment.setDentist(assignedDentist);
        appointment.setPatient(patient);
        appointment.setToken(token);
        appointment.setTreatmentType(treatment);
        appointment.setAppointMethod(dto.getAppointMethod());
        appointment.setStartAt(startAt);
        appointment.setEndAt(endAt);
        appointment.setStatus(AppointmentStatus.RESERVED);
        appointment.setVisitType(dto.getVisitType());
        appointment.setPatientComment(dto.getPatientComment());
        appointment.setCreatedAt(LocalDateTime.now());
        appointment.setUpdatedAt(LocalDateTime.now());

        return appointmentRepository.save(appointment);
    }
}
