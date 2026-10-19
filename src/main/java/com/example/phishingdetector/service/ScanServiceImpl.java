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

    @Override
    public ScanResponseDto scanQrCode(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return new ScanResponseDto(false, 0, "QR 이미지가 첨부되지 않았습니다.", "파일을 선택해 주세요.", "-", "-");
        }

        // ★ [추가] 이미지 파일(JPG, PNG 등)이 아닌 경우 별도 안내 메시지 반환
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return new ScanResponseDto(
                false, 
                0, 
                "지원하지 않는 파일 형식입니다.", 
                "JPG, PNG, WEBP 등 이미지 형식의 파일만 업로드해 주세요.", 
                "-", 
                "-"
            );
        }

        try {
            String decodedUrl = qrDecoderService.decodeQrCode(file);
            
            if (decodedUrl == null || decodedUrl.trim().isEmpty()) {
                return new ScanResponseDto(false, 0, "QR 코드를 인식할 수 없습니다.", "이미지에서 올바른 URL을 추출하지 못했습니다.", "-", "-");
            }

            return processScanWithTracking(decodedUrl, "QR");

        } catch (Throwable e) {
            return new ScanResponseDto(false, 0, "QR 코드를 인식할 수 없습니다.", "이미지에서 올바른 URL을 추출하지 못했습니다.", "-", "-");
        }
    }

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

    private ScanResponseDto processScanWithTracking(String originalUrl, String sourceType) {
        if (originalUrl == null || originalUrl.trim().isEmpty()) {
            return new ScanResponseDto(false, 0, "유효하지 않은 URL입니다.", "URL이 입력되지 않았습니다.", originalUrl, originalUrl);
        }

        String targetUrl = originalUrl.trim();

        // 1018 수정 내용 : DB 오버플로우 및 시스템 에러 방지를 위한 2,000자 초과 초장문 URL 검증 로직 추가
        if (targetUrl.length() > 2000) {
            return new ScanResponseDto(
                false, 
                0, 
                "입력 가능한 URL 길이를 초과했습니다.", 
                "URL 길이는 최대 2,000자까지 검사할 수 있습니다.", 
                targetUrl.substring(0, 30) + "...", 
                "-"
            );
        }

        // ★ [보완 1] URL 형식이 아닌 비정상 특수문자 및 문자열 1차 검증
        if (!isValidUrlFormat(targetUrl)) {
            return new ScanResponseDto(
                false, 
                0, 
                "유효하지 않은 URL 형식입니다.", 
                "올바른 주소를 입력해 주세요.", 
                originalUrl, 
                originalUrl
            );
        }

        if (!targetUrl.startsWith("http://") && !targetUrl.startsWith("https://")) {
            targetUrl = "http://" + targetUrl;
        }

        String expandedUrl = targetUrl;
        try {
            expandedUrl = urlTrackerService.expandUrl(targetUrl);
        } catch (Exception e) {
            // 추적 실패 시 원본 URL 유지
        }

        return processScanEngine(originalUrl, expandedUrl, sourceType);
    }

    /**
     * ★ [보완 2] URL 및 도메인 정규식 유효성 검증 메서드
     */
    private boolean isValidUrlFormat(String url) {
        String clean = url.trim().toLowerCase();

        // 1) 큰따옴표, 백틱, 괄호 등 비정상 특수문자 단독/범벅 입력 차단
        if (clean.matches(".*[\"'`<>{}|\\[\\]^].*")) {
            return false;
        }

        // 2) http:// 또는 https:// 제거 후 순수 도메인 파트 추출
        String domainPart = clean.replaceFirst("^https?://", "").split("/")[0].split("\\?")[0];

        // 3) 도메인 부분에 최소한 점(.)이 존재하거나 localhost 인지 확인
        if (!domainPart.contains(".") && !domainPart.equals("localhost")) {
            return false;
        }

        // 4) @ 기호가 포함된 경우: google.com@evil.com 형태의 유효한 우회 주소 구조인지 검증
        if (domainPart.contains("@")) {
            String[] parts = domainPart.split("@");
            // @ 앞뒤로 문자열이 존재하고, 실제 접속 대상인 @ 뒷부분이 올바른 도메인 구조(점 포함)여야 함
            if (parts.length != 2 || parts[0].isEmpty() || parts[1].isEmpty() || !parts[1].contains(".")) {
                return false;
            }
        }

        return true;
    }

    @Override
    public ScanResponseDto processScan(String url) {
        return processScanWithTracking(url, "URL");
    }

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

        // [규칙 5] 실제 단축 URL 서비스 사용 여부 (동일 주소 자동 보정 차점 방지)
        if (isShortenedUrl(originalUrl.toLowerCase())) {
            reasons.add("단축 URL을 사용하여 최종 목적지 주소를 은닉했습니다.");
            calculatedRiskScore += 25;
        }

        // [규칙 6] 블랙리스트 / 악성 키워드 검사
        if (isGoogleBlacklisted(lowerUrl)) {
            reasons.add("피싱 및 악성코드 유포 DB/키워드에 등록된 위험 주소입니다.");
            calculatedRiskScore += 50;
        }

        // [규칙 7] @ 기호를 악용한 사용자 인증/도메인 교란 기법
        if (hasUserInfoAtSymbol(lowerUrl)) {
            reasons.add("도메인 앞에 '@' 기호를 삽입하여 정상 사이트 접속으로 착시를 유도하는 주소 교란 패턴입니다.");
            calculatedRiskScore += 45;
        }

        int finalScore = Math.min(calculatedRiskScore, 100);
        boolean isSuspicious = finalScore >= 40;
        String message = isSuspicious 
                ? "위험 사이트(피싱/큐싱 의심)로 판정되었습니다." 
                : "특이사항이 발견되지 않은 안전한 사이트입니다.";

        String formattedReason = formatReasons(reasons);
        String riskLevel = finalScore >= 70 ? "DANGEROUS" : (finalScore >= 40 ? "WARNING" : "SAFE");
        
        ScanHistory history = new ScanHistory();
        history.setSourceType(sourceType);
        history.setOriginalUrl(originalUrl);
        history.setExpandedUrl(expandedUrl);
        history.setRiskScore(finalScore);
        history.setRiskLevel(riskLevel);
        history.setScanReason(formattedReason);

        scanHistoryRepository.save(history);

        return new ScanResponseDto(isSuspicious, finalScore, message, formattedReason, originalUrl, expandedUrl);
    }

    @Override
    public List<ScanHistory> getScanHistory() {
        return scanHistoryRepository.findAllByOrderByScanIdDesc();
    }

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

    private boolean hasUserInfoAtSymbol(String url) {
        String cleanUrl = url.replaceFirst("^https?://", "");
        return cleanUrl.contains("@");
    }
}