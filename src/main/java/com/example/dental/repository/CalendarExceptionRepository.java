package com.example.dental.repository;

import com.example.dental.entity.CalendarException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CalendarExceptionRepository extends JpaRepository<CalendarException, Long> {
}
