package com.example.phishingdetector.repository;

import com.example.phishingdetector.entity.ScanHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ScanHistoryRepository extends JpaRepository<ScanHistory, Long> {

    // scanId 기준 내림차순 정렬 (기존 findAllByOrderByIdDesc 대체)
    List<ScanHistory> findAllByOrderByScanIdDesc();

    // 등급별(DANGEROUS, WARNING, SAFE) 검사 건수 카운트
    long countByRiskLevel(String riskLevel);
}