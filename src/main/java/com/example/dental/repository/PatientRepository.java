package com.example.dental.repository;

import com.example.dental.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    // 歯科医院ID + 診察番号で患者を検索
    Optional<Patient> findByDentalClinicDentalIdAndPatientCode(Long dentalId, String patientCode);

    // 歯科医院IDで患者一覧を取得（論理削除除外）
    List<Patient> findByDentalClinicDentalIdAndDeleteFragFalse(Long dentalId);

    // 歯科医院ID + 診察番号で患者を検索（論理削除除外）
    Optional<Patient> findByDentalClinicDentalIdAndPatientCodeAndDeleteFragFalse(Long dentalId, String patientCode);

    // アカウントロック解除日時が現在より前（ロック解除済み）の患者を検索
    List<Patient> findByLockedUntillBefore(LocalDateTime now);
}
