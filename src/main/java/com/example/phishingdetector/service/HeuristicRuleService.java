package com.example.phishingdetector.service;

import org.springframework.stereotype.Service;

// URL의 패턴을 분석하여 실제 위험도 점수(0~100점)를 계산하는 서비스
@Service
public class HeuristicRuleService {

    public int calculateRiskScore(String url) {
        if (url == null || url.isBlank()) {
            return 0;
        }

        int score = 0;
        String lowerUrl = url.toLowerCase();

        // [규칙 1] IP 주소 형태 직접 접속 (+35점)
        if (lowerUrl.matches(".*//\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}.*")) {
            score += 35;
        }

        // [규칙 2] 보안 취약 HTTP 접속 (+20점)
        if (lowerUrl.startsWith("http://")) {
            score += 20;
        }

        // [규칙 3] 의심스러운 단축 URL 서비스 (+20점)
        if (lowerUrl.contains("bit.ly") || lowerUrl.contains("tinyurl.com") || lowerUrl.contains("site.naver.com")) {
            score += 20;
        }

        // [규칙 4] 계정 탈취/피싱 의심 키워드 (+30점)
        if (lowerUrl.contains("login") || lowerUrl.contains("account") || lowerUrl.contains("verify") || lowerUrl.contains("bank") || lowerUrl.contains("pay")) {
            score += 30;
        }

        // [규칙 5] 악성 앱 다운로드 확장자 (.apk, .exe) (+40점)
        if (lowerUrl.contains(".apk") || lowerUrl.contains(".exe")) {
            score += 40;
        }

        return Math.min(score, 100);
    }
}