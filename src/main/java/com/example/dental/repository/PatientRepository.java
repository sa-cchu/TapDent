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
    List<Patient> findByDentalClinicDentalIdAndIsDeletedFalse(Long dentalId);

    // 歯科医院ID + 診察番号で患者を検索（論理削除除外）
    Optional<Patient> findByDentalClinicDentalIdAndPatientCodeAndIsDeletedFalse(Long dentalId, String patientCode);

    // アカウントロック解除日時が現在より前（ロック解除済み）の患者を検索
    List<Patient> findByLockedUntillBefore(LocalDateTime now);

    // 歯科医院と電話番号で患者の存在チェック
    boolean existsByDentalClinicAndTel(com.example.dental.entity.DentalClinic clinic, String tel);

    // 歯科医院とメールアドレスで患者を検索
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"role"})
    Optional<Patient> findByDentalClinicAndEmail(com.example.dental.entity.DentalClinic clinic, String email);

    // 歯科医院と ログインID（メールアドレス または 診察券番号）で患者を検索
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"role"})
    @org.springframework.data.jpa.repository.Query("SELECT p FROM Patient p WHERE p.dentalClinic = :clinic AND (p.email = :loginId OR p.patientCode = :loginId)")
    Optional<Patient> findByClinicAndLoginId(@org.springframework.data.repository.query.Param("clinic") com.example.dental.entity.DentalClinic clinic, @org.springframework.data.repository.query.Param("loginId") String loginId);

    // 歯科医院と電話番号で患者を検索（複合重複チェック用）
    Optional<Patient> findByDentalClinicAndTel(com.example.dental.entity.DentalClinic clinic, String tel);
}
