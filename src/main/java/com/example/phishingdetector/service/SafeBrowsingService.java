package com.example.phishingdetector.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

// Google Safe Browsing 외부 API 검사 서비스
@Service
public class SafeBrowsingService {

    @Value("AIzaSyAjDX_EY6oIUz2Ke90ZnE5AaiDwm3AE1qs")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public boolean checkThreat(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }

        // API 키 미설정 시 피싱/악성코드 테스트 키워드 자체 검사
        if ("YOUR_API_KEY".equals(apiKey)) {
            String lowerUrl = url.toLowerCase();
            return lowerUrl.contains("phishing") || lowerUrl.contains("malware") || lowerUrl.contains("evil") || lowerUrl.contains("test-danger");
        }

        try {
            String apiUrl = "https://safebrowsing.googleapis.com/v4/threatMatches:find?key=" + apiKey;
            Map<String, Object> requestBody = Map.of(
                "client", Map.of("clientId", "phishing-detector", "clientVersion", "1.0.0"),
                "threatInfo", Map.of(
                    "threatTypes", List.of("MALWARE", "SOCIAL_ENGINEERING", "UNWANTED_SOFTWARE"),
                    "platformTypes", List.of("ANY_PLATFORM"),
                    "threatEntryTypes", List.of("URL"),
                    "threatEntries", List.of(Map.of("url", url))
                )
            );
            Map<String, Object> response = restTemplate.postForObject(apiUrl, requestBody, Map.class);
            return response != null && response.containsKey("matches");
        } catch (Exception e) {
            return false;
        }
    }
}