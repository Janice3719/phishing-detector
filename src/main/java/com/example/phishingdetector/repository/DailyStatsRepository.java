package com.example.phishingdetector.repository;

import com.example.phishingdetector.entity.DailyStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;

@Repository
public interface DailyStatsRepository extends JpaRepository<DailyStats, LocalDate> {
    // 기본 CRUD 및 statDate(PK) 기반 조회 자동 제공
}