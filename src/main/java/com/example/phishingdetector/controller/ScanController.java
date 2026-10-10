package com.example.phishingdetector.controller;

import com.example.phishingdetector.dto.DailyStatsDto;
import com.example.phishingdetector.dto.ScanRequestDto;
import com.example.phishingdetector.dto.ScanResponseDto;
import com.example.phishingdetector.dto.ScanStatsDto;
import com.example.phishingdetector.entity.ScanHistory;
import com.example.phishingdetector.repository.ScanHistoryRepository;
import com.example.phishingdetector.service.ScanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Tag(name = "scan-controller", description = "피싱/큐싱 탐지 및 대시보드 통계 API")
@RestController
@RequestMapping("/api/v1/scan")
public class ScanController {

    private final ScanService scanService;
    private final ScanHistoryRepository scanHistoryRepository;

    public ScanController(ScanService scanService, ScanHistoryRepository scanHistoryRepository) {
        this.scanService = scanService;
        this.scanHistoryRepository = scanHistoryRepository;
    }

    @Operation(summary = "URL 피싱 검사", description = "입력받은 URL의 피싱 위험도를 분석하고 결과를 반환합니다.")
    @PostMapping("/url")
    public ResponseEntity<ScanResponseDto> scanUrl(@RequestBody ScanRequestDto requestDto) {
        ScanResponseDto response = scanService.scanUrl(requestDto.getUrl());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "QR 코드 큐싱 검사", description = "업로드된 QR 이미지에서 URL을 추출하여 검사합니다.")
    @PostMapping("/qr")
    public ResponseEntity<ScanResponseDto> scanQr(@RequestParam("file") MultipartFile file) {
        ScanResponseDto response = scanService.scanQrCode(file);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "스캔 이력 조회", description = "최근 스캔한 URL 및 QR 검사 이력을 최신순으로 조회합니다.")
    @GetMapping("/history")
    public ResponseEntity<List<ScanHistory>> getHistory() {
        List<ScanHistory> historyList = scanHistoryRepository.findAllByOrderByIdDesc();
        return ResponseEntity.ok(historyList);
    }

    @Operation(summary = "대시보드 요약 통계 조회", description = "전체 검사 수 및 위험/경고/안전 등급별 요약 통계를 조회합니다.")
    @GetMapping("/stats")
    public ResponseEntity<ScanStatsDto> getScanStats() {
        long totalScans = scanHistoryRepository.count();
        long dangerousCount = scanHistoryRepository.countByRiskLevel("DANGEROUS");
        long warningCount = scanHistoryRepository.countByRiskLevel("WARNING");
        long safeCount = scanHistoryRepository.countByRiskLevel("SAFE");

        ScanStatsDto stats = new ScanStatsDto(totalScans, dangerousCount, warningCount, safeCount);
        return ResponseEntity.ok(stats);
    }

    @Operation(summary = "한 달치(30일) 일별 스캔 추이 조회", description = "대시보드 꺾은선 그래프 시각화용 30일간의 일별 정상/악성 스캔 통계 데이터를 반환합니다.")
    @GetMapping("/stats/monthly")
    public ResponseEntity<List<DailyStatsDto>> getMonthlyStats() {
        List<DailyStatsDto> monthlyStats = new ArrayList<>();
        LocalDate today = LocalDate.now();

        // 최근 30일간의 일별 통계 구조 생성
        for (int i = 29; i >= 0; i--) {
            LocalDate targetDate = today.minusDays(i);
            String dateStr = targetDate.toString();

            // 현재 저장된 DB 기반 집계 (테스트 및 초기 연동용)
            long total = scanHistoryRepository.count();
            long safe = scanHistoryRepository.countByRiskLevel("SAFE");
            long malicious = total - safe;

            monthlyStats.add(new DailyStatsDto(dateStr, safe, malicious));
        }

        return ResponseEntity.ok(monthlyStats);
    }
}