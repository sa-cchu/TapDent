package com.example.dental.repository;

import com.example.dental.entity.AppointmentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppointmentHistoryRepository extends JpaRepository<AppointmentHistory, Long> {
    
    // 患者の最新の予約履歴を1件取得
    java.util.Optional<AppointmentHistory> findFirstByPatientPatientIdOrderByStartAtDesc(Long patientId);
}
