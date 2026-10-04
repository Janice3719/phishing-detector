package com.example.phishingdetector.dto;

// [프론트엔드 -> 백엔드] URL 직접 검수 요청 시 전달받는 데이터 객체
public class ScanRequestDto {

    // 프론트엔드에서 전송한 검사 대상 URL 문자열
    private String url;

    // [기본 생성자] Spring이 JSON 데이터를 Java 객체로 자율 변환할 때 필요합니다.
    public ScanRequestDto() {
    }

    // [매개변수 생성자]
    public ScanRequestDto(String url) {
        this.url = url;
    }

    // [Getter 메서드] ScanController의 requestDto.getUrl() 호출 시 실행되어 url 값을 반환합니다.
    public String getUrl() {
        return url;
    }

    // [Setter 메서드] url 변수에 값을 입력 저장합니다.
    public void setUrl(String url) {
        this.url = url;
    }
}