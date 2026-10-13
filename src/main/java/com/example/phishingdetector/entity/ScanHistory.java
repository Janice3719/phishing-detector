package com.example.phishingdetector.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * [스캔 이력 엔티티]
 * - ERD 규격에 맞춰 필드명 및 컬럼 매핑을 변경했습니다.
 * - STS Lombok 이슈 방지를 위해 Getter/Setter 및 생성자를 순수 Java 코드로 작성했습니다.
 */
@Entity
@Table(name = "SCAN_HISTORY")
public class ScanHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "scan_id")
    private Long scanId; // 스캔 고유 ID (PK)

    @Column(name = "source_type", length = 50)
    private String sourceType; // 스캔 방식 ('URL', 'QR')

    @Column(name = "original_url", columnDefinition = "TEXT")
    private String originalUrl; // 검사 원본 URL

    @Column(name = "expanded_url", columnDefinition = "TEXT")
    private String expandedUrl; // 최종 이동 URL

    @Column(name = "risk_score")
    private Integer riskScore; // 위험도 점수 (0 ~ 100)

    @Column(name = "risk_level", length = 50)
    private String riskLevel; // SAFE, WARNING, DANGEROUS

    @Column(name = "scan_reason", columnDefinition = "TEXT")
    private String scanReason; // 세부 판정 사유 (탐지 규칙)

    @Column(name = "created_at")
    private LocalDateTime createdAt; // 스캔 일시

    // =========================================================================
    // [생성자]
    // =========================================================================

    // 1. 기본 생성자 (JPA 필수)
    public ScanHistory() {
    }

    // 2. 비즈니스 생성자 (스캔 결과 저장 시 주로 사용)
    public ScanHistory(String sourceType, String originalUrl, String expandedUrl, Integer riskScore, String riskLevel, String scanReason) {
        this.sourceType = sourceType;
        this.originalUrl = originalUrl;
        this.expandedUrl = expandedUrl;
        this.riskScore = riskScore;
        this.riskLevel = riskLevel;
        this.scanReason = scanReason;
    }

    // 3. 전체 필드 생성자
    public ScanHistory(Long scanId, String sourceType, String originalUrl, String expandedUrl, Integer riskScore, String riskLevel, String scanReason, LocalDateTime createdAt) {
        this.scanId = scanId;
        this.sourceType = sourceType;
        this.originalUrl = originalUrl;
        this.expandedUrl = expandedUrl;
        this.riskScore = riskScore;
        this.riskLevel = riskLevel;
        this.scanReason = scanReason;
        this.createdAt = createdAt;
    }

    // DB 저장 전 생성 일시 자동 입력
    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    // =========================================================================
    // [Getter / Setter 메서드]
    // =========================================================================

    public Long getScanId() {
        return scanId;
    }

    public void setScanId(Long scanId) {
        this.scanId = scanId;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public String getExpandedUrl() {
        return expandedUrl;
    }

    public void setExpandedUrl(String expandedUrl) {
        this.expandedUrl = expandedUrl;
    }

    public Integer getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Integer riskScore) {
        this.riskScore = riskScore;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getScanReason() {
        return scanReason;
    }

    public void setScanReason(String scanReason) {
        this.scanReason = scanReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // =========================================================================
    // [하위 호환용 편의 메서드] - 기존 Service 코드와의 연동 에러 방지
    // =========================================================================

    public Long getId() {
        return scanId;
    }

    public String getUrl() {
        return originalUrl;
    }

    public void setUrl(String url) {
        this.originalUrl = url;
    }

    public String getDetectedRules() {
        return scanReason;
    }

    public void setDetectedRules(String detectedRules) {
        this.scanReason = detectedRules;
    }
}