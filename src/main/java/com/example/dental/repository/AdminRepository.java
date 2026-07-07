package com.example.dental.repository;

import com.example.dental.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Integer> {
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"role"})
    Optional<Admin> findByLoginId(String loginId);
}
