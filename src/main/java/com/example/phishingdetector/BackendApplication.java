package com.example.phishingdetector;

import jakarta.annotation.PostConstruct; // ★ 추가 (Spring Boot 3.x)
import java.util.TimeZone;                // ★ 추가

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class BackendApplication {

    // ★ 추가: 서버 구동 시 기본 타임존을 한국 시간(KST)으로 설정
    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    }

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}