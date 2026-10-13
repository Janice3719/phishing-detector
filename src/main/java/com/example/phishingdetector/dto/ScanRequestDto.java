package com.example.phishingdetector.dto;

public class ScanRequestDto {
    private String url;

    // 1. 기본 생성자 (Jackson JSON 변환용 필수)
    public ScanRequestDto() {
    }

    public ScanRequestDto(String url) {
        this.url = url;
    }

    // 2. Getter & Setter (필수)
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}