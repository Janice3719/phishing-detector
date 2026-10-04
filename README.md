# 🛡️ QishDetector (피싱 & 큐싱 통합 탐지 시스템)

> **QR 코드 스캔 및 악성 URL 분석을 통해 큐싱(Qishing) 위협으로부터 사용자를 보호하는 모바일 웹/백엔드 서비스**

---

## 📌 프로젝트 소개 (Overview)

최근 QR 코드를 악용한 **큐싱(Qishing)** 피싱 범죄가 급증함에 따라, 사용자가 안전하게 QR 및 URL을 검증할 수 있도록 돕는 실시간 탐지 서비스입니다.  
디바이스 자동 감지를 통해 PC 환경에서는 파일 업로드/URL 직접 입력 UI를 제공하고, 모바일 환경에서는 즉시 웹 카메라 스캔 UI를 활성화합니다.

---

## ✨ 주요 기능 (Key Features)

- 📸 **디바이스 자동 감지 & 맞춤 UI**: 접속 기기(`navigator.userAgent`)에 따라 PC/모바일 환경별 전용 UI 동적 제공
- 🔍 **QR 코드 즉시 디코딩**: ZXing 라이브러리를 활용한 QR 이미지 파싱 및 URL 추출
- 🛡️ **복합 위험도 탐지 파이프라인**:
  - **Google Safe Browsing API**: 알려진 악성 URL DB 실시간 조회
  - **Heuristic Rule Engine**: 자체 정의한 위험 룰 기반 패턴 분석 및 위험 점수 산출
- 📱 **모바일 실기기 스캔 지원**: `ngrok` HTTPS 터널링을 통해 아이폰/안드로이드 웹 브라우저 카메라 권한 연동

---

## 🛠️ 기술 스택 (Tech Stack)

### Backend
![Java](https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white)

### Frontend
![HTML5](https://img.shields.io/badge/HTML5-E34F26?style=for-the-badge&logo=html5&logoColor=white)
![CSS3](https://img.shields.io/badge/CSS3-1572B6?style=for-the-badge&logo=css3&logoColor=white)
![JavaScript](https://img.shields.io/badge/JavaScript-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black)

### Environment & Tools
![Git](https://img.shields.io/badge/Git-F05032?style=for-the-badge&logo=git&logoColor=white)
![GitHub](https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white)
![ngrok](https://img.shields.io/badge/ngrok-1F1F1F?style=for-the-badge&logo=ngrok&logoColor=white)

---

## 🔗 API 명세서 (API Specification)

| 기능 | Method | Endpoint | Request | Response |
| :--- | :---: | :--- | :--- | :--- |
| **QR 이미지 분석** | `POST` | `/api/v1/scan/qr` | `MultipartFile` (이미지) | `ScanResponseDto` (JSON) |
| **URL 직접 검증** | `POST` | `/api/v1/scan/url` | `{ "url": "http://..." }` | `ScanResponseDto` (JSON) |

---

## ⚙️ 시스템 흐름도 (Data Flow)
