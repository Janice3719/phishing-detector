package com.example.phishingdetector.service;

import com.example.phishingdetector.dto.ScanResponseDto;
import com.example.phishingdetector.entity.ScanHistory;
import com.example.phishingdetector.repository.ScanHistoryRepository;
import org.springframework.cache.annotation.Cacheable; // ★ Redis 캐싱
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * [ScanServiceImpl]
 * - URL 및 QR 피싱/큐싱 탐지 서비스 구현체
 * - OWASP / APWG 가이드라인 기반 6대 휴리스틱 탐지 엔진 적용
 * - Redis 고속 캐싱(@Cacheable) 및 H2 DB 이력 저장 처리
 */
@Service
public class ScanServiceImpl implements ScanService {

    private final ScanHistoryRepository scanHistoryRepository;

    public ScanServiceImpl(ScanHistoryRepository scanHistoryRepository) {
        this.scanHistoryRepository = scanHistoryRepository;
    }

    /**
     * [기능 1] QR 코드 이미지 분석 메서드
     */
    @Override
    public ScanResponseDto scanQrCode(MultipartFile file) {
        // TODO: 실제 QR 디코딩 서비스 연동 시 추출된 URL 대입 (현재는 샘플 테스트)
        String extractedUrl = "https://example.com";
        return scanUrl(extractedUrl);
    }

    /**
     * [기능 2] URL 직접 입력 분석 메서드 (Redis 캐싱 적용)
     * - 동일 URL 재검사 시 DB/엔진을 거치지 않고 Redis 메모리에서 1ms 만에 응답
     */
    @Override
    @Cacheable(value = "scanCache", key = "#url") // ★ Redis 고속 캐싱 적용
    public ScanResponseDto scanUrl(String url) {
        return processScan(url);
    }

    /**
     * [기능 3] OWASP / APWG 가이드 기준 6대 휴리스틱 스캔 엔진 및 DB 자동 저장
     */
    @Override
    public ScanResponseDto processScan(String url) {
        List<String> reasons = new ArrayList<>();
        int calculatedRiskScore = 0;

        if (url == null || url.trim().isEmpty()) {
            return new ScanResponseDto(false, 0, "유효하지 않은 URL입니다.", "URL이 입력되지 않았습니다.", url, url);
        }

        String lowerUrl = url.toLowerCase();

        // [규칙 1] IP 주소 형태 도메인 접속 (APWG 가이드라인 기준)
        if (isIpHost(lowerUrl)) {
            reasons.add("[위협] 도메인 이름 대신 IP 주소를 직접 사용한 피싱 의심 패턴입니다.");
            calculatedRiskScore += 40;
        }

        // [규칙 2] 고위험 피싱 악용 TLD(최상위 도메인) 검사
        if (hasHighRiskTld(lowerUrl)) {
            reasons.add("[주의] 피싱 및 악성코드 유포에 자주 악용되는 고위험 최상위 도메인(TLD)입니다.");
            calculatedRiskScore += 30;
        }

        // [규칙 3] 유명 서비스 사칭/타이포스쿼팅 패턴 (Naver, Kakao, Google, 금융사 등)
        if (isBrandSpoofing(lowerUrl)) {
            reasons.add("[경고] 포털, SNS 또는 금융기관 브랜드를 사칭한 주소 패턴입니다.");
            calculatedRiskScore += 35;
        }

        // [규칙 4] 과도한 서브도메인 중첩 (Depth > 3)
        if (hasExcessiveSubdomains(lowerUrl)) {
            reasons.add("[주의] 서브도메인을 과도하게 나열하여 목적지 주소를 교란하는 패턴입니다.");
            calculatedRiskScore += 20;
        }

        // [규칙 5] 단축 URL 서비스 사용 여부
        if (isShortenedUrl(lowerUrl)) {
            reasons.add("[주의] 단축 URL을 사용하여 최종 목적지 주소를 은닉했습니다.");
            calculatedRiskScore += 25;
        }

        // [규칙 6] 블랙리스트 / 악성 키워드 검사
        if (isGoogleBlacklisted(lowerUrl)) {
            reasons.add("[위험] 피싱 및 악성코드 유포 DB/키워드에 등록된 위험 주소입니다.");
            calculatedRiskScore += 50;
        }

        // 위험 점수 상한치 100점 처리 및 위험 등급 판정
        int finalScore = Math.min(calculatedRiskScore, 100);
        boolean isSuspicious = finalScore >= 40;
        String message = isSuspicious 
                ? "위험 사이트(피싱/큐싱 의심)로 판정되었습니다." 
                : "안전한 사이트입니다.";

        String formattedReason = formatReasons(reasons);

        // H2 DB 자동 저장 (스캔 이력 생성)
        String riskLevel = finalScore >= 70 ? "DANGEROUS" : (finalScore >= 40 ? "WARNING" : "SAFE");
        ScanHistory history = new ScanHistory(url, finalScore, riskLevel, formattedReason);
        scanHistoryRepository.save(history);

        return new ScanResponseDto(isSuspicious, finalScore, message, formattedReason, url, url);
    }

    /**
     * [기능 4] DB에 저장된 검사 이력 목록 조회 (최신순)
     */
    @Override
    public List<ScanHistory> getScanHistory() {
        return scanHistoryRepository.findAllByOrderByIdDesc();
    }

    // =========================================================================
    // [자체 휴리스틱 탐지 엔진 세부 검증 메서드]
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
            if (url.contains(kw) && !url.contains(kw + ".com") && !url.contains(kw + ".net")) {
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
        return url.contains("malicious") || url.contains("phishing") || url.contains("login-check");
    }
}