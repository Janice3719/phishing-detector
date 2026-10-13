package com.example.phishingdetector.service;

import com.example.phishingdetector.dto.ScanResponseDto;
import com.example.phishingdetector.entity.ScanHistory;
import com.example.phishingdetector.repository.ScanHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * [ScanServiceImpl]
 * - URL 및 QR 피싱/큐싱 탐지 서비스 구현체
 * - QrDecoderService 및 UrlTrackerService 연동을 통한 실데이터 검수
 * - OWASP / APWG 가이드라인 기반 6대 휴리스틱 탐지 엔진 적용 및 ERD 기반 DB 이력 저장
 */
@Service
public class ScanServiceImpl implements ScanService {

    private final ScanHistoryRepository scanHistoryRepository;
    private final QrDecoderService qrDecoderService;
    private final UrlTrackerService urlTrackerService;

    public ScanServiceImpl(ScanHistoryRepository scanHistoryRepository,
                           QrDecoderService qrDecoderService,
                           UrlTrackerService urlTrackerService) {
        this.scanHistoryRepository = scanHistoryRepository;
        this.qrDecoderService = qrDecoderService;
        this.urlTrackerService = urlTrackerService;
    }

    /**
     * [기능 1] 실제 QR 코드 이미지 디코딩 및 분석 ('QR' 타입)
     */
    @Override
    public ScanResponseDto scanQrCode(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return new ScanResponseDto(false, 0, "QR 이미지가 첨부되지 않았습니다.", "파일을 선택해 주세요.", "-", "-");
        }

        try {
            // 1. QrDecoderService를 사용하여 QR 이미지에서 URL 추출
            String decodedUrl = qrDecoderService.decodeQrCode(file);
            
            if (decodedUrl == null || decodedUrl.trim().isEmpty()) {
                return new ScanResponseDto(false, 0, "QR 코드를 인식할 수 없습니다.", "이미지에서 올바른 URL을 추출하지 못했습니다.", "-", "-");
            }

            // 2. 추출된 URL을 바탕으로 스캔 진행 ('QR' 스캔 방식)
            return processScanWithTracking(decodedUrl, "QR");

        } catch (Exception e) {
            return new ScanResponseDto(false, 0, "QR 분석 중 오류가 발생했습니다.", e.getMessage(), "-", "-");
        }
    }

    /**
     * [기능 2] URL 직접 입력 분석 ('URL' 타입)
     */
    @Override
    public ScanResponseDto scanUrl(String url) {
        try {
            return processScanWithTracking(url, "URL");
        } catch (Exception e) {
            e.printStackTrace();
            String safeUrl = (url != null && !url.trim().isEmpty()) ? url : "-";
            return new ScanResponseDto(
                false, 
                0, 
                "URL 분석 중 오류가 발생했습니다.", 
                e.getMessage() != null ? e.getMessage() : "서버 내부 분석 예외", 
                safeUrl, 
                safeUrl
            );
        }
    }

    /**
     * [기능 3] 단축 URL 추적 및 OWASP/APWG 기반 휴리스틱 탐지
     */
    private ScanResponseDto processScanWithTracking(String originalUrl, String sourceType) {
        if (originalUrl == null || originalUrl.trim().isEmpty()) {
            return new ScanResponseDto(false, 0, "유효하지 않은 URL입니다.", "URL이 입력되지 않았습니다.", originalUrl, originalUrl);
        }

        String targetUrl = originalUrl.trim();
        
        // http:// 또는 https:// 스키마 보정
        if (!targetUrl.startsWith("http://") && !targetUrl.startsWith("https://")) {
            targetUrl = "http://" + targetUrl;
        }

        // 1. UrlTrackerService로 단축 URL 추적하여 최종 destination URL 도출
        String expandedUrl = targetUrl;
        try {
            expandedUrl = urlTrackerService.expandUrl(targetUrl);
        } catch (Exception e) {
            // 추적 실패 시 원본 URL 유지
        }

        // 2. 최종 이동 URL 기반 탐지 엔진 실행 (sourceType 포함)
        return processScanEngine(originalUrl, expandedUrl, sourceType);
    }

    @Override
    public ScanResponseDto processScan(String url) {
        return processScanWithTracking(url, "URL");
    }

    /**
     * [기능 4] 6대 휴리스틱 스캔 엔진 및 ERD 규격 DB 자동 저장
     */
    private ScanResponseDto processScanEngine(String originalUrl, String expandedUrl, String sourceType) {
        List<String> reasons = new ArrayList<>();
        int calculatedRiskScore = 0;

        String lowerUrl = expandedUrl.toLowerCase();

        // [규칙 1] IP 주소 형태 도메인 접속
        if (isIpHost(lowerUrl)) {
            reasons.add("도메인 이름 대신 IP 주소를 직접 사용한 피싱 의심 패턴입니다.");
            calculatedRiskScore += 40;
        }

        // [규칙 2] 고위험 피싱 악용 TLD 검사
        if (hasHighRiskTld(lowerUrl)) {
            reasons.add("피싱 및 악성코드 유포에 자주 악용되는 고위험 최상위 도메인(TLD)입니다.");
            calculatedRiskScore += 30;
        }

        // [규칙 3] 유명 서비스 사칭/타이포스쿼팅 패턴
        if (isBrandSpoofing(lowerUrl)) {
            reasons.add("포털, SNS 또는 금융기관 브랜드를 사칭한 주소 패턴입니다.");
            calculatedRiskScore += 35;
        }

        // [규칙 4] 과도한 서브도메인 중첩 (Depth > 3)
        if (hasExcessiveSubdomains(lowerUrl)) {
            reasons.add("서브도메인을 과도하게 나열하여 목적지 주소를 교란하는 패턴입니다.");
            calculatedRiskScore += 20;
        }

        // [규칙 5] 단축 URL 서비스 사용 여부
        if (!originalUrl.equals(expandedUrl) || isShortenedUrl(originalUrl.toLowerCase())) {
            reasons.add("단축 URL을 사용하여 최종 목적지 주소를 은닉했습니다.");
            calculatedRiskScore += 25;
        }

        // [규칙 6] 블랙리스트 / 악성 키워드 검사
        if (isGoogleBlacklisted(lowerUrl)) {
            reasons.add("피싱 및 악성코드 유포 DB/키워드에 등록된 위험 주소입니다.");
            calculatedRiskScore += 50;
        }

        int finalScore = Math.min(calculatedRiskScore, 100);
        boolean isSuspicious = finalScore >= 40;
        String message = isSuspicious 
                ? "위험 사이트(피싱/큐싱 의심)로 판정되었습니다." 
                : "특이사항이 발견되지 않은 안전한 사이트입니다.";

        String formattedReason = formatReasons(reasons);

        // ERD 스펙에 맞춘 DB 저장 (ScanHistory)
        String riskLevel = finalScore >= 70 ? "DANGEROUS" : (finalScore >= 40 ? "WARNING" : "SAFE");
        
        ScanHistory history = new ScanHistory();
        history.setSourceType(sourceType);       // 'URL' 또는 'QR'
        history.setOriginalUrl(originalUrl);     // 검사 원본 URL
        history.setExpandedUrl(expandedUrl);     // 최종 이동 URL
        history.setRiskScore(finalScore);        // 위험 점수
        history.setRiskLevel(riskLevel);         // SAFE, WARNING, DANGEROUS
        history.setScanReason(formattedReason);  // 세부 판정 사유

        scanHistoryRepository.save(history);

        return new ScanResponseDto(isSuspicious, finalScore, message, formattedReason, originalUrl, expandedUrl);
    }

    @Override
    public List<ScanHistory> getScanHistory() {
        return scanHistoryRepository.findAllByOrderByScanIdDesc();
    }

    // =========================================================================
    // [휴리스틱 검증 세부 메서드]
    // =========================================================================

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

    private boolean isIpHost(String url) {
        return url.matches(".*//\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}.*");
    }

    private boolean hasHighRiskTld(String url) {
        String[] highRiskTlds = {".top", ".xyz", ".biz", ".cc", ".monster", ".fit", ".tk", ".work", ".click", ".gq", ".ml"};
        for (String tld : highRiskTlds) {
            if (url.contains(tld + "/") || url.endsWith(tld)) return true;
        }
        return false;
    }

    private boolean isBrandSpoofing(String url) {
        String[] keywords = {"naver", "kakao", "daum", "google", "shinhan", "kbstar", "woori", "hana", "pass"};
        for (String kw : keywords) {
            if (url.contains(kw) && !url.contains(kw + ".com") && !url.contains(kw + ".net") && !url.contains(kw + ".co.kr")) {
                return true;
            }
        }
        return false;
    }

    private boolean hasExcessiveSubdomains(String url) {
        String domainPart = url.replaceFirst("^https?://", "").split("/")[0];
        long dotCount = domainPart.chars().filter(ch -> ch == '.').count();
        return dotCount >= 4;
    }

    private boolean isShortenedUrl(String url) {
        String[] shorteners = {"bit.ly", "tinyurl.com", "me2.do", "t.co", "is.gd", "kakaotalk.at", "cut.ly", "url.kr"};
        for (String s : shorteners) {
            if (url.contains(s)) return true;
        }
        return false;
    }

    private boolean isGoogleBlacklisted(String url) {
        return url.contains("malicious") || url.contains("phishing") || url.contains("login-check") || url.contains("security-check");
    }
}