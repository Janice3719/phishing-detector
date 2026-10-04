package com.example.phishingdetector.controller;

import com.example.phishingdetector.dto.ScanRequestDto;
import com.example.phishingdetector.dto.ScanResponseDto;
import com.example.phishingdetector.service.ScanService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * [스캔 컨트롤러]
 * - 프론트엔드의 웹 요청(HTTP API)을 가장 먼저 받아 처리하는 컨트롤러 클래스입니다.
 * - URL: http://localhost:8080/api/v1/scan
 */
@RestController
@RequestMapping("/api/v1/scan")
public class ScanController {

    // 비즈니스 로직을 처리하는 ScanService 인터페이스 주입
    private final ScanService scanService;

    // [생성자 주입]
    public ScanController(ScanService scanService) {
        this.scanService = scanService;
    }

    /**
     * [API 1] QR 코드 이미지 파일 업로드 분석 요청 (POST /api/v1/scan/qr)
     * @param file 프론트엔드에서 전송한 QR 이미지 파일
     */
    @PostMapping("/qr")
    public ResponseEntity<ScanResponseDto> analyzeQrCode(@RequestParam("file") MultipartFile file) {
        // [수정] scanService.scanQrCode(file)을 호출하여 QR 분석 수행 (인자 1개)
        ScanResponseDto response = scanService.scanQrCode(file);

        // HTTP 200 OK 상태 코드와 함께 분석 결과를 JSON 형태로 반환
        return ResponseEntity.ok(response);
    }

    /**
     * [API 2] URL 텍스트 직접 입력 분석 요청 (POST /api/v1/scan/url)
     * @param request 프론트엔드에서 JSON Body로 보낸 ScanRequestDto { "url": "..." }
     */
    @PostMapping("/url")
    public ResponseEntity<ScanResponseDto> analyzeUrl(@RequestBody ScanRequestDto request) {
        // [수정] scanService.scanUrl(...)을 호출하여 URL 분석 수행 (인자 1개: request에서 url 추출)
        ScanResponseDto response = scanService.scanUrl(request.getUrl());

        // HTTP 200 OK 상태 코드와 함께 분석 결과를 JSON 형태로 반환
        return ResponseEntity.ok(response);
    }
}