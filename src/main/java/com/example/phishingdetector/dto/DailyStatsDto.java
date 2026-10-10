package com.example.phishingdetector.dto;

import java.io.Serializable;

/**
 * [대시보드 일별 통계 DTO]
 * - 최근 30일간의 일별 정상(SAFE) 및 악성/위험(DANGEROUS/WARNING) 스캔 건수를 전달합니다.
 * - 프론트엔드 꺾은선 그래프(Line Chart) 연동에 사용됩니다.
 */
public class DailyStatsDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String date;         // 날짜 (형식: YYYY-MM-DD)
    private long safeCount;      // 정상 URL 스캔 건수
    private long maliciousCount; // 악성/위험 URL 스캔 건수

    // -------------------------------------------------------------------------
    // [생성자]
    // -------------------------------------------------------------------------

    /**
     * 1. 기본 생성자
     */
    public DailyStatsDto() {
    }

    /**
     * 2. 전체 필드 생성자
     */
    public DailyStatsDto(String date, long safeCount, long maliciousCount) {
        this.date = date;
        this.safeCount = safeCount;
        this.maliciousCount = maliciousCount;
    }

    // -------------------------------------------------------------------------
    // [Getter & Setter 메서드]
    // -------------------------------------------------------------------------

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public long getSafeCount() {
        return safeCount;
    }

    public void setSafeCount(long safeCount) {
        this.safeCount = safeCount;
    }

    public long getMaliciousCount() {
        return maliciousCount;
    }

    public void setMaliciousCount(long maliciousCount) {
        this.maliciousCount = maliciousCount;
    }
}