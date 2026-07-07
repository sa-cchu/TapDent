package com.example.dental.repository;

import com.example.dental.entity.BusinessHour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.Optional;

@Repository
public interface BusinessHourRepository extends JpaRepository<BusinessHour, Long> {
    Optional<BusinessHour> findByDentalClinicDentalIdAndDayOfWeek(Long dentalId, DayOfWeek dayOfWeek);
    java.util.List<BusinessHour> findByDentalClinicDentalId(Long dentalId);
}
