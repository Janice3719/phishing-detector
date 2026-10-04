package com.example.phishingdetector.service;

import com.example.phishingdetector.dto.ScanResponseDto;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * [ScanServiceImpl]
 * ScanService 인터페이스의 구현체로,
 * QR 및 URL 검사를 수행하고 ScanResponseDto 객체로 결과를 반환합니다.
 */
@Service
public class ScanServiceImpl implements ScanService {

    /**
     * [기능 1] QR 코드 이미지 분석 메서드 (ScanService 인터페이스 구현)
     * 
     * @param file 업로드된 QR 코드 이미지 파일
     * @return ScanResponseDto 분석 결과
     */
    @Override
    public ScanResponseDto scanQrCode(MultipartFile file) {
        // TODO: 실제 QR 디코딩 서비스 연결 위치 (예: QrDecoderService)
        String extractedUrl = "https://example.com"; 
        return scanUrl(extractedUrl);
    }

    /**
     * [기능 2] URL 검수 메서드 1 (ScanService 인터페이스의 scanUrl 구현)
     * 
     * @param url 검사 대상 URL
     * @return ScanResponseDto 분석 결과
     */
    @Override
    public ScanResponseDto scanUrl(String url) {
        return processScan(url);
    }

    /**
     * [기능 3] URL 검수 메인 로직 처리 메서드 (processScan)
     * 
     * @param url 검사 대상 URL
     * @return ScanResponseDto 분석 결과
     */
    @Override
    public ScanResponseDto processScan(String url) {
        // 1. 검출된 위험 사유 목록을 저장할 리스트
        List<String> reasons = new ArrayList<>();

        // -------------------------------------------------------------------------
        // [위험 요소 검사 로직]
        // 문장 어미: '~입니다.', '~했습니다.' / 특정 브랜드명(네이버/비틀리 등) 제외
        // -------------------------------------------------------------------------

        // (1) 구글 Safe Browsing / 블랙리스트 DB 등록 여부 검사
        if (isGoogleBlacklisted(url)) {
            reasons.add("구글 피싱 및 악성코드 블랙리스트 DB에 등록된 위험 사이트입니다.");
        }

        // (2) 단축 URL 사용 여부 검사
        if (isShortenedUrl(url)) {
            reasons.add("단축 URL을 사용하여 최종 목적지 주소를 숨겼습니다.");
        }

        // (3) IP 주소 형태 도메인 접속 여부 검사
        if (isIpHost(url)) {
            reasons.add("도메인 이름 대신 IP 주소를 직접 사용한 의심스러운 URL입니다.");
        }

        // -------------------------------------------------------------------------
        // [결과 데이터 생성 및 DTO 반환]
        // -------------------------------------------------------------------------

        boolean isSuspicious = !reasons.isEmpty();
        int riskScore = isSuspicious ? 100 : 0;
        String message = isSuspicious 
                ? "위험한 사이트(피싱 의심)로 판정되었습니다." 
                : "안전한 사이트입니다.";

        // 다중 사유 포맷팅 (사유가 여러 개일 때 '1. --- \n 2. ---' 형태로 변환)
        String formattedReason = formatReasons(reasons);

        // ScanResponseDto 전체 필드 생성자 호출
        return new ScanResponseDto(
                isSuspicious,     // boolean isSuspicious
                riskScore,        // int riskScore
                message,          // String message
                formattedReason,  // String reason
                url,              // String decodedUrl
                url               // String expandedUrl
        );
    }

    /**
     * [판정 사유 포맷팅 헬퍼 메서드]
     * - 사유가 없을 때: 기본 안내 문구 반환
     * - 사유가 1개일 때: 문장만 반환
     * - 사유가 2개 이상일 때: '1. 사유1 \n 2. 사유2' 형태로 조합
     */
    private String formatReasons(List<String> reasons) {
        if (reasons == null || reasons.isEmpty()) {
            return "특이사항이 발견되지 않은 안전한 URL입니다.";
        }
        if (reasons.size() == 1) {
            return reasons.get(0);
        }
        return IntStream.range(0, reasons.size())
                .mapToObj(i -> (i + 1) + ". " + reasons.get(i))
                .collect(Collectors.joining("\n"));
    }

    // =========================================================================
    // [세부 검증 로직 예시 메서드]
    // =========================================================================

    private boolean isGoogleBlacklisted(String url) {
        return url != null && url.contains("malicious");
    }

    private boolean isShortenedUrl(String url) {
        return url != null && (url.contains("bit.ly") || url.contains("me2.do") || url.contains("tinyurl"));
    }

    private boolean isIpHost(String url) {
        return url != null && url.matches(".*//\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}.*");
    }
}