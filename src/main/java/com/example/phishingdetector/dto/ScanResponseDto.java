package com.example.phishingdetector.dto;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * [스캔 결과 응답 DTO 클래스]
 * - 백엔드(서버)에서 검사한 결과(위험여부, 점수, 메시지, 상세 사유 등)를
 *   하나로 묶어서 프론트엔드(웹 화면)로 전송해 주는 역할을 합니다.
 * - Redis 고속 캐싱을 위해 Serializable 인터페이스를 구현합니다.
 */
public class ScanResponseDto implements Serializable { // ★ implements Serializable 추가

    private static final long serialVersionUID = 1L; // ★ 직렬화 버전 ID 추가

    // @JsonProperty: 자바 변수명을 JSON으로 변환할 때 이름("isSuspicious")을 강제로 고정합니다.
    @JsonProperty("isSuspicious")
    private boolean isSuspicious; // 위험 여부 (true = 위험 / false = 안전)

    private int riskScore;        // 위험도 점수 (0점 ~ 100점)
    private String message;       // 한 줄 요약 메시지
    private String reason;        // 위험/안전 판정의 구체적인 사유
    private String decodedUrl;    // 추출/입력 원본 URL
    private String expandedUrl;   // 단축 URL 해제 후 최종 이동 URL

    // -------------------------------------------------------------------------
    // [생성자]
    // -------------------------------------------------------------------------

    /**
     * 1. 기본 생성자 (JSON 변환 및 Redis 직렬화 시 Spring/Jackson 라이브러리가 사용)
     */
    public ScanResponseDto() {
    }

    /**
     * 2. 전체 필드 생성자
     */
    public ScanResponseDto(boolean isSuspicious, int riskScore, String message, String reason, String decodedUrl, String expandedUrl) {
        this.isSuspicious = isSuspicious;
        this.riskScore = riskScore;
        this.message = message;
        this.reason = reason;
        this.decodedUrl = decodedUrl;
        this.expandedUrl = expandedUrl;
    }

    // -------------------------------------------------------------------------
    // [Getter & Setter 메서드]
    // -------------------------------------------------------------------------

    public boolean isSuspicious() {
        return isSuspicious;
    }

    public void setSuspicious(boolean suspicious) {
        isSuspicious = suspicious;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(int riskScore) {
        this.riskScore = riskScore;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getDecodedUrl() {
        return decodedUrl;
    }

    public void setDecodedUrl(String decodedUrl) {
        this.decodedUrl = decodedUrl;
    }

    public String getExpandedUrl() {
        return expandedUrl;
    }

    public void setExpandedUrl(String expandedUrl) {
        this.expandedUrl = expandedUrl;
    }
}