package com.example.phishingdetector.service;

import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.net.URL;

// 단축 URL 및 리다이렉트를 추적하여 최종 도달 URL을 찾아내는 전담 서비스
@Service
public class UrlTrackerService {

    public String expandUrl(String shortUrl) {
        if (shortUrl == null || shortUrl.isBlank()) {
            return shortUrl;
        }

        String currentUrl = shortUrl;
        try {
            // 프로토콜이 없는 경우 기본 https:// 추가
            if (!currentUrl.startsWith("http://") && !currentUrl.startsWith("https://")) {
                currentUrl = "https://" + currentUrl;
            }

            // 301/302 리다이렉션 추적
            while (true) {
                URL url = new URL(currentUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setInstanceFollowRedirects(false);
                connection.setConnectTimeout(3000);
                connection.setReadTimeout(3000);
                connection.connect();

                int responseCode = connection.getResponseCode();
                if (responseCode >= 300 && responseCode < 400) {
                    String redirectUrl = connection.getHeaderField("Location");
                    if (redirectUrl != null && !redirectUrl.isBlank()) {
                        currentUrl = redirectUrl;
                        continue;
                    }
                }
                break;
            }
        } catch (Exception e) {
            // 연결 에러 시 원래 URL 유지
        }
        return currentUrl;
    }
}