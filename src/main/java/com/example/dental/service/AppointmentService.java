package com.example.dental.service;

import com.example.dental.entity.*;
import com.example.dental.enums.AppointMethod;
import com.example.dental.enums.AppointmentStatus;
import com.example.dental.enums.ExceptionType;
import com.example.dental.enums.VisitType;
import com.example.dental.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final BusinessHourRepository businessHourRepository;
    private final CalendarExceptionRepository calendarExceptionRepository;
    private final DentalChairRepository dentalChairRepository;
    private final DentistRepository dentistRepository;
    private final ChairTreatmentRepository chairTreatmentRepository;
    private final DentistTreatmentRepository dentistTreatmentRepository;
    private final DentalClinicRepository dentalClinicRepository;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              BusinessHourRepository businessHourRepository,
                              CalendarExceptionRepository calendarExceptionRepository,
                              DentalChairRepository dentalChairRepository,
                              DentistRepository dentistRepository,
                              ChairTreatmentRepository chairTreatmentRepository,
                              DentistTreatmentRepository dentistTreatmentRepository,
                              DentalClinicRepository dentalClinicRepository) {
        this.appointmentRepository = appointmentRepository;
        this.businessHourRepository = businessHourRepository;
        this.calendarExceptionRepository = calendarExceptionRepository;
        this.dentalChairRepository = dentalChairRepository;
        this.dentistRepository = dentistRepository;
        this.chairTreatmentRepository = chairTreatmentRepository;
        this.dentistTreatmentRepository = dentistTreatmentRepository;
        this.dentalClinicRepository = dentalClinicRepository;
    }

    /**
     * 医院全体が休診日かどうかを判定します（予約カレンダーのグレーアウト用）
     */
    public boolean isClinicHoliday(DentalClinic clinic, LocalDate date) {
        Optional<BusinessHour> bhOpt = businessHourRepository.findByDentalClinicDentalIdAndDayOfWeek(clinic.getDentalId(), date.getDayOfWeek());
        List<CalendarException> allExceptions = calendarExceptionRepository.findByDentalClinicAndTargetDateBetween(clinic, date, date);
        List<CalendarException> clinicExceptions = allExceptions.stream().filter(e -> e.getDentist() == null).toList();

        LocalTime openAt = null;
        LocalTime closeAt = null;
        boolean isHoliday = true;
        
        if (bhOpt.isPresent()) {
            isHoliday = bhOpt.get().getRegularHoliday();
            openAt = bhOpt.get().getOpenAt();
            closeAt = bhOpt.get().getCloseAt();
        }

        boolean hasSpecialWithoutHours = false;
        for (CalendarException ex : clinicExceptions) {
            if (ex.getType() == ExceptionType.HOLIDAY) {
                return true; // 休診
            } else if (ex.getType() == ExceptionType.SPECIAL) {
                isHoliday = false; // 特別診療なので休診ではない
                if (ex.getStartAt() != null) openAt = ex.getStartAt();
                else hasSpecialWithoutHours = true;
                if (ex.getEndAt() != null) closeAt = ex.getEndAt();
            }
        }
        
        if (openAt == null && hasSpecialWithoutHours) {
            List<BusinessHour> allHours = businessHourRepository.findByDentalClinicDentalId(clinic.getDentalId());
            Optional<BusinessHour> defaultHour = allHours.stream()
                    .filter(bh -> !bh.getRegularHoliday() && bh.getOpenAt() != null)
                    .findFirst();
            if (defaultHour.isPresent()) {
                openAt = defaultHour.get().getOpenAt();
                closeAt = defaultHour.get().getCloseAt();
            }
        }

        return isHoliday || openAt == null || closeAt == null;
    }

    /**
     * 指定された日付の空き枠を検索します
     */
    public List<LocalTime> getAvailableTimeSlots(DentalClinic clinic, LocalDate date, TreatmentType treatment) {
        List<LocalTime> availableSlots = new ArrayList<>();
        
        // 1. チェアと歯科医師を取得（対象メニューを対応可能なものに絞り込む）
        List<DentalChair> chairs = chairTreatmentRepository.findByTreatmentType(treatment).stream()
                .map(ChairTreatment::getDentalChair)
                .filter(c -> !c.getIsDeleted() && Boolean.TRUE.equals(c.getStatus()))
                .toList();
        List<Dentist> dentists = dentistTreatmentRepository.findByTreatmentType(treatment).stream()
                .map(DentistTreatment::getDentist)
                .filter(d -> !d.getIsDeleted() && Boolean.TRUE.equals(d.getStatus()))
                .toList();
        // 2. 基本の営業時間と例外カレンダーの取得
        Optional<BusinessHour> bhOpt = businessHourRepository.findByDentalClinicDentalIdAndDayOfWeek(clinic.getDentalId(), date.getDayOfWeek());
        List<CalendarException> allExceptions = calendarExceptionRepository.findByDentalClinicAndTargetDateBetween(clinic, date, date);
        
        List<CalendarException> clinicExceptions = allExceptions.stream().filter(e -> e.getDentist() == null).toList();
        List<CalendarException> dentistExceptions = allExceptions.stream().filter(e -> e.getDentist() != null).toList();

        LocalTime openAt = null;
        LocalTime closeAt = null;
        LocalTime breakStart = null;
        LocalTime breakEnd = null;
        boolean isHoliday = true;

        if (bhOpt.isPresent()) {
            BusinessHour bh = bhOpt.get();
            openAt = bh.getOpenAt();
            closeAt = bh.getCloseAt();
            breakStart = bh.getBreakStartAt();
            breakEnd = bh.getBreakEndAt();
            isHoliday = bh.getRegularHoliday();
        }

        // 3. 例外カレンダー（全体）の適用
        boolean hasSpecialWithoutHours = false;
        for (CalendarException ex : clinicExceptions) {
            if (ex.getType() == ExceptionType.HOLIDAY) {
                return availableSlots; // 休診
            } else if (ex.getType() == ExceptionType.SPECIAL) {
                isHoliday = false; // 特別診療なので休診を上書き
                if (ex.getStartAt() != null) {
                    openAt = ex.getStartAt();
                } else {
                    hasSpecialWithoutHours = true;
                }
                if (ex.getEndAt() != null) closeAt = ex.getEndAt();
                if (ex.getBreakStartAt() != null) breakStart = ex.getBreakStartAt();
                if (ex.getBreakEndAt() != null) breakEnd = ex.getBreakEndAt();
            }
        }
        
        // 定休日などで openAt が null のままで、特別診療（時間指定なし）が設定されている場合、
        // 他の通常営業日の営業時間をデフォルトとして採用する
        if (openAt == null && hasSpecialWithoutHours) {
            List<BusinessHour> allHours = businessHourRepository.findByDentalClinicDentalId(clinic.getDentalId());
            Optional<BusinessHour> defaultHour = allHours.stream()
                    .filter(bh -> !bh.getRegularHoliday() && bh.getOpenAt() != null)
                    .findFirst();
            if (defaultHour.isPresent()) {
                openAt = defaultHour.get().getOpenAt();
                closeAt = defaultHour.get().getCloseAt();
                breakStart = defaultHour.get().getBreakStartAt();
                breakEnd = defaultHour.get().getBreakEndAt();
            }
        }

        if (isHoliday || openAt == null || closeAt == null) return availableSlots;
        
        // 歯科医師の個別休診を考慮してキャパシティを減らす
        long dentistHolidayCount = dentistExceptions.stream()
                .filter(ex -> ex.getType() == ExceptionType.HOLIDAY)
                .map(ex -> ex.getDentist().getDentistId())
                .filter(id -> dentists.stream().anyMatch(d -> d.getDentistId().equals(id))) // 対応可能医師のみを対象とする
                .distinct()
                .count();
        
        int availableDentists = Math.max(0, dentists.size() - (int) dentistHolidayCount);
        if (chairs.isEmpty() || availableDentists == 0) {
            return availableSlots;
        }
        int totalCapacity = Math.min(chairs.size(), availableDentists);

        // 4. 指定日の既存予約を取得
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.atTime(LocalTime.MAX);
        List<Appointment> dailyAppointments = appointmentRepository.findByDentalClinicAndStartAtBetween(clinic, dayStart, dayEnd);

        // 5. 空き枠の計算
        int unitMinutes = clinic.getReservationTimeUnit() != null ? clinic.getReservationTimeUnit() : 30;
        int requiredMinutes = treatment.getRequiredMinutes();
        LocalTime current = openAt;

        while (!current.plusMinutes(requiredMinutes).isAfter(closeAt)) {
            LocalTime slotStart = current;
            LocalTime slotEnd = current.plusMinutes(requiredMinutes);

            // 休憩時間の判定
            if (breakStart != null && breakEnd != null) {
                // スロットが休憩時間に被っているか
                boolean overlapsBreak = (slotStart.isBefore(breakEnd) && slotEnd.isAfter(breakStart));
                if (overlapsBreak) {
                    current = current.plusMinutes(unitMinutes);
                    continue;
                }
            }

            // 重複する予約のカウント
            long overlappingCount = dailyAppointments.stream()
                .filter(app -> app.getStatus() != AppointmentStatus.CANCELLED)
                .filter(app -> {
                    LocalTime appStart = app.getStartAt().toLocalTime();
                    LocalTime appEnd = app.getEndAt().toLocalTime();
                    return appStart.isBefore(slotEnd) && appEnd.isAfter(slotStart);
                }).count();

            // 予約枠の空き（チェア数・医師数の少ない方が上限）
            if (overlappingCount < totalCapacity) {
                availableSlots.add(slotStart);
            }

            current = current.plusMinutes(unitMinutes);
        }

        return availableSlots;
    }

    /**
     * 新規予約を登録します（担当医は空いている人を自動割り当て）
     */
    @Transactional
    public Appointment createAppointment(DentalClinic clinic, Patient patient, TreatmentType treatment, 
                                  LocalDate date, LocalTime time, String patientComment, VisitType visitType) {
        
        // 悲観的ロックを取得して他の予約処理と競合しないようにする
        dentalClinicRepository.findByIdForUpdate(clinic.getDentalId())
            .orElseThrow(() -> new IllegalStateException("医院情報が見つかりません"));

        LocalDateTime startAt = LocalDateTime.of(date, time);
        LocalDateTime endAt = startAt.plusMinutes(treatment.getRequiredMinutes());

        // その時間帯の既存予約を取得
        List<Appointment> existingApps = appointmentRepository.findByDentalClinicAndStartAtBetween(clinic, startAt.minusMinutes(1440), startAt.plusMinutes(1440));
        
        List<Appointment> overlappingApps = existingApps.stream()
            .filter(app -> app.getStatus() != AppointmentStatus.CANCELLED)
            .filter(app -> app.getStartAt().isBefore(endAt) && app.getEndAt().isAfter(startAt))
            .toList();

        // チェアの空きを探す（対応可能なチェアのみ）
        List<DentalChair> capableChairs = chairTreatmentRepository.findByTreatmentType(treatment).stream()
                .map(ChairTreatment::getDentalChair)
                .filter(c -> !c.getIsDeleted() && Boolean.TRUE.equals(c.getStatus()))
                .toList();
        List<Long> usedChairIds = overlappingApps.stream().map(a -> a.getDentalChair().getChairId()).toList();
        DentalChair assignedChair = capableChairs.stream()
            .filter(c -> !usedChairIds.contains(c.getChairId()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("利用可能なチェアがありません"));

        // 個別休診の医師IDリスト（当日）
        List<CalendarException> dailyExceptions = calendarExceptionRepository.findByDentalClinicAndTargetDateBetween(clinic, date, date);
        List<Long> holidayDentistIds = dailyExceptions.stream()
            .filter(e -> e.getType() == ExceptionType.HOLIDAY && e.getDentist() != null)
            .map(e -> e.getDentist().getDentistId())
            .toList();

        // 医師の空きを探す（対応可能な医師のみ）
        List<Dentist> capableDentists = dentistTreatmentRepository.findByTreatmentType(treatment).stream()
                .map(DentistTreatment::getDentist)
                .filter(d -> !d.getIsDeleted() && Boolean.TRUE.equals(d.getStatus()))
                .toList();
        List<Long> usedDentistIds = overlappingApps.stream().map(a -> a.getDentist().getDentistId()).toList();
        
        Dentist assignedDentist = capableDentists.stream()
            .filter(d -> !usedDentistIds.contains(d.getDentistId()))
            .filter(d -> !holidayDentistIds.contains(d.getDentistId()))
            // ★可能診療項目が少ない順に並び替え
            .sorted(Comparator.comparingInt(d -> dentistTreatmentRepository.countByDentist(d)))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("対応可能な歯科医師がいません"));

        // 予約の作成
        Appointment appointment = new Appointment();
        appointment.setDentalClinic(clinic);
        appointment.setDentalChair(assignedChair);
        appointment.setDentist(assignedDentist);
        appointment.setPatient(patient);
        appointment.setTreatmentType(treatment);
        appointment.setAppointMethod(AppointMethod.ONLINE);
        appointment.setStartAt(startAt);
        appointment.setEndAt(endAt);
        appointment.setStatus(AppointmentStatus.RESERVED);
        appointment.setVisitType(visitType);
        appointment.setPatientComment(patientComment);
        appointment.setCreatedAt(LocalDateTime.now());
        appointment.setUpdatedAt(LocalDateTime.now());

        return appointmentRepository.save(appointment);
    }

    /**
     * 予約をキャンセルします
     */
    @Transactional
    public void cancelAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("予約が見つかりません"));
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setUpdatedAt(LocalDateTime.now());
        appointmentRepository.save(appointment);
    }

    /**
     * 予約の日時を変更します（担当医・チェアの再割当を含む）
     */
    @Transactional
    public Appointment changeAppointmentTime(Long appointmentId, LocalDate newDate, LocalTime newTime) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("予約が見つかりません"));
        
        DentalClinic clinic = appointment.getDentalClinic();
        TreatmentType treatment = appointment.getTreatmentType();

        // 悲観的ロックを取得して他の予約処理と競合しないようにする
        dentalClinicRepository.findByIdForUpdate(clinic.getDentalId())
            .orElseThrow(() -> new IllegalStateException("医院情報が見つかりません"));

        LocalDateTime startAt = LocalDateTime.of(newDate, newTime);
        LocalDateTime endAt = startAt.plusMinutes(treatment.getRequiredMinutes());

        // その時間帯の既存予約を取得（変更対象の予約自体は除く）
        List<Appointment> existingApps = appointmentRepository.findByDentalClinicAndStartAtBetween(clinic, startAt.minusMinutes(1440), startAt.plusMinutes(1440));
        
        List<Appointment> overlappingApps = existingApps.stream()
            .filter(app -> app.getStatus() != AppointmentStatus.CANCELLED)
            .filter(app -> !app.getAppointmentId().equals(appointmentId)) // 自身を除外
            .filter(app -> app.getStartAt().isBefore(endAt) && app.getEndAt().isAfter(startAt))
            .toList();

        // チェアの空きを探す（対応可能なチェアのみ）
        List<DentalChair> capableChairs = chairTreatmentRepository.findByTreatmentType(treatment).stream()
                .map(ChairTreatment::getDentalChair)
                .filter(c -> !c.getIsDeleted() && Boolean.TRUE.equals(c.getStatus()))
                .toList();
        List<Long> usedChairIds = overlappingApps.stream().map(a -> a.getDentalChair().getChairId()).toList();
        DentalChair assignedChair = capableChairs.stream()
            .filter(c -> !usedChairIds.contains(c.getChairId()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("利用可能なチェアがありません"));

        // 個別休診の医師IDリスト（当日）
        List<CalendarException> dailyExceptions = calendarExceptionRepository.findByDentalClinicAndTargetDateBetween(clinic, newDate, newDate);
        List<Long> holidayDentistIds = dailyExceptions.stream()
            .filter(e -> e.getType() == ExceptionType.HOLIDAY && e.getDentist() != null)
            .map(e -> e.getDentist().getDentistId())
            .toList();

        // 医師の空きを探す（対応可能な医師のみ）
        List<Dentist> capableDentists = dentistTreatmentRepository.findByTreatmentType(treatment).stream()
                .map(DentistTreatment::getDentist)
                .filter(d -> !d.getIsDeleted() && Boolean.TRUE.equals(d.getStatus()))
                .toList();
        List<Long> usedDentistIds = overlappingApps.stream().map(a -> a.getDentist().getDentistId()).toList();
        
        Dentist assignedDentist = capableDentists.stream()
            .filter(d -> !usedDentistIds.contains(d.getDentistId()))
            .filter(d -> !holidayDentistIds.contains(d.getDentistId()))
            // ★可能診療項目が少ない順に並び替え
            .sorted(Comparator.comparingInt(d -> dentistTreatmentRepository.countByDentist(d)))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("対応可能な歯科医師がいません"));

        // 予約の更新
        appointment.setDentalChair(assignedChair);
        appointment.setDentist(assignedDentist);
        appointment.setStartAt(startAt);
        appointment.setEndAt(endAt);
        appointment.setUpdatedAt(LocalDateTime.now());

        return appointmentRepository.save(appointment);
    }
}
