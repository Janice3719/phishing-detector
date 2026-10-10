package com.example.phishingdetector.repository;

import com.example.phishingdetector.entity.ScanHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ScanHistoryRepository extends JpaRepository<ScanHistory, Long> {
    List<ScanHistory> findAllByOrderByIdDesc();

    // 등급별(DANGEROUS, WARNING, SAFE) 검사 건수 카운트
    long countByRiskLevel(String riskLevel);
}