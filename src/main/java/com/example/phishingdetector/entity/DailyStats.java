package com.example.phishingdetector.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * [일별 통계 엔티티]
 * - ERD 규격의 DAILY_STATS 테이블과 1:1 매핑됩니다.
 * - 통계 기준 날짜(stat_date, YYYY-MM-DD)를 기본키(PK)로 사용합니다.
 */
@Entity
@Table(name = "DAILY_STATS")
public class DailyStats {

    @Id
    @Column(name = "stat_date")
    private LocalDate statDate; // 통계 기준 날짜 (YYYY-MM-DD) [PK]

    @Column(name = "total_scans")
    private Integer totalScans = 0; // 당일 총 스캔 건수

    @Column(name = "safe_count")
    private Integer safeCount = 0; // 정상(SAFE) 건수

    @Column(name = "warning_count")
    private Integer warningCount = 0; // 경고(WARNING) 건수

    @Column(name = "dangerous_count")
    private Integer dangerousCount = 0; // 위험(DANGEROUS) 건수

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // 최종 집계 일시

    // =========================================================================
    // [생성자]
    // =========================================================================

    // 1. 기본 생성자 (JPA 필수)
    public DailyStats() {
    }

    // 2. 날짜 지정 생성자
    public DailyStats(LocalDate statDate) {
        this.statDate = statDate;
        this.totalScans = 0;
        this.safeCount = 0;
        this.warningCount = 0;
        this.dangerousCount = 0;
    }

    // 3. 전체 필드 생성자
    public DailyStats(LocalDate statDate, Integer totalScans, Integer safeCount, Integer warningCount, Integer dangerousCount, LocalDateTime updatedAt) {
        this.statDate = statDate;
        this.totalScans = totalScans;
        this.safeCount = safeCount;
        this.warningCount = warningCount;
        this.dangerousCount = dangerousCount;
        this.updatedAt = updatedAt;
    }

    // DB 저장 및 수정 시 최종 집계 일시 자동 업데이트
    @PrePersist
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // =========================================================================
    // [Getter / Setter 메서드]
    // =========================================================================

    public LocalDate getStatDate() {
        return statDate;
    }

    public void setStatDate(LocalDate statDate) {
        this.statDate = statDate;
    }

    public Integer getTotalScans() {
        return totalScans;
    }

    public void setTotalScans(Integer totalScans) {
        this.totalScans = totalScans;
    }

    public Integer getSafeCount() {
        return safeCount;
    }

    public void setSafeCount(Integer safeCount) {
        this.safeCount = safeCount;
    }

    public Integer getWarningCount() {
        return warningCount;
    }

    public void setWarningCount(Integer warningCount) {
        this.warningCount = warningCount;
    }

    public Integer getDangerousCount() {
        return dangerousCount;
    }

    public void setDangerousCount(Integer dangerousCount) {
        this.dangerousCount = dangerousCount;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}