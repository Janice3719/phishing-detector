package com.example.phishingdetector.service;

import com.example.phishingdetector.dto.ScanResponseDto;
import com.example.phishingdetector.entity.ScanHistory; // ★ 추가됨
import org.springframework.web.multipart.MultipartFile;
import java.util.List; // ★ 추가됨

/**
 * [스캔 서비스 인터페이스]
 * - 컨트롤러에서 호출할 서비스 메서드의 규격을 정의합니다.
 */
public interface ScanService {

    // 1. QR 코드 이미지 분석 메서드
    ScanResponseDto scanQrCode(MultipartFile file);

    // 2. URL 직접 입력 분석 메서드
    ScanResponseDto scanUrl(String url);

    // 3. 공통 URL 스캔 처리 메서드 (기존 인터페이스 규격 유지)
    ScanResponseDto processScan(String url);

    // 4. DB에 저장된 검사 이력 목록 조회 메서드 (★ 새 기능)
    List<ScanHistory> getScanHistory();
}