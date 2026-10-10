package com.example.phishingdetector.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * [스캔 이력 엔티티]
 * - 검사된 URL, 위험 점수, 위험 등급, 탐지 사유 및 생성 일시를 DB(SCAN_HISTORY)에 저장합니다.
 * - STS Lombok 이슈 방지를 위해 Getter/Setter 및 생성자를 직접 작성했습니다.
 */
@Entity
@Table(name = "SCAN_HISTORY")
public class ScanHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String url;          // 검사한 URL

    private int riskScore;       // 위험 점수 (0 ~ 100)

    private String riskLevel;    // SAFE, WARNING, DANGEROUS

    @Column(length = 1000)
    private String detectedRules;// 탐지된 휴리스틱 룰 목록 (사유)

    private LocalDateTime createdAt; // 검사 일시

    // 1. 기본 생성자 (JPA 필수)
    public ScanHistory() {
    }

    // 2. 비즈니스 생성자 (스캔 결과 저장 시 사용)
    public ScanHistory(String url, int riskScore, String riskLevel, String detectedRules) {
        this.url = url;
        this.riskScore = riskScore;
        this.riskLevel = riskLevel;
        this.detectedRules = detectedRules;
    }

    // 3. 전체 필드 생성자
    public ScanHistory(Long id, String url, int riskScore, String riskLevel, String detectedRules, LocalDateTime createdAt) {
        this.id = id;
        this.url = url;
        this.riskScore = riskScore;
        this.riskLevel = riskLevel;
        this.detectedRules = detectedRules;
        this.createdAt = createdAt;
    }

    // DB 저장 전 생성 일시 자동 입력
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // =========================================================================
    // [Getter / Setter 메서드] - Jackson JSON 직렬화 및 데이터 접근용
    // =========================================================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(int riskScore) {
        this.riskScore = riskScore;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getDetectedRules() {
        return detectedRules;
    }

    public void setDetectedRules(String detectedRules) {
        this.detectedRules = detectedRules;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}