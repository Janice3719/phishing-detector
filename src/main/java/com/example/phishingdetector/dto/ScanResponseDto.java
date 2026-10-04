package com.example.phishingdetector.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * [스캔 결과 응답 DTO 클래스]
 * - 백엔드(서버)에서 검사한 결과(위험여부, 점수, 메시지, 상세 사유 등)를
 *   하나로 묶어서 프론트엔드(웹 화면)로 전송해 주는 역할을 합니다.
 */
public class ScanResponseDto {

    // @JsonProperty: 자바 변수명을 JSON으로 변환할 때 이름("isSuspicious")을 강제로 고정합니다.
    @JsonProperty("isSuspicious")
    private boolean isSuspicious; // 위험 여부 (true = 위험 / false = 안전)

    private int riskScore;        // 위험도 점수 (0점 ~ 100점)
    private String message;       // 한 줄 요약 메시지
    private String reason;        // ⭐ [신규 추가] 위험/안전 판정의 구체적인 사유
    private String decodedUrl;    // 추출/입력 원본 URL
    private String expandedUrl;   // 단축 URL 해제 후 최종 이동 URL

    // -------------------------------------------------------------------------
    // [생성자]
    // -------------------------------------------------------------------------

    /**
     * 1. 기본 생성자 (JSON 변환 시 Spring/Jackson 라이브러리가 내부적으로 사용)
     */
    public ScanResponseDto() {
    }

    /**
     * 2. 전체 필드 생성자 (ScanServiceImpl에서 검사 결과 데이터를 한 번에 담을 때 사용)
     * 
     * @param isSuspicious 위험 여부 (true/false)
     * @param riskScore    위험도 점수 (0~100)
     * @param message      요약 메시지
     * @param reason       ⭐ 구체적인 판정 사유
     * @param decodedUrl   원본 URL
     * @param expandedUrl  최종 이동 URL
     */
    public ScanResponseDto(boolean isSuspicious, int riskScore, String message, String reason, String decodedUrl, String expandedUrl) {
        this.isSuspicious = isSuspicious;
        this.riskScore = riskScore;
        this.message = message;
        this.reason = reason; // 신규 사유 데이터 저장
        this.decodedUrl = decodedUrl;
        this.expandedUrl = expandedUrl;
    }

    // -------------------------------------------------------------------------
    // [Getter & Setter 메서드] - 외부에서 변수값을 읽어오거나(Get) 저장(Set)할 때 사용
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

    // ⭐ 신규 추가된 reason(사유)의 Getter / Setter
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