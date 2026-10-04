let html5QrCode = null;

// 페이지 로드 시 기기 종류(모바일 vs PC) 자동 감지
document.addEventListener("DOMContentLoaded", function() {
    const isMobile = /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent) || window.innerWidth <= 768;
    
    const cameraSection = document.getElementById('cameraSection');
    const fileSection = document.getElementById('fileSection');
    const deviceBadge = document.getElementById('deviceBadge');

    if (isMobile) {
        // 모바일인 경우: 카메라 모드 활성화
        cameraSection.style.display = 'block';
        fileSection.style.display = 'none';
        deviceBadge.innerText = "📱 모바일 접속 모드";
    } else {
        // PC/노트북인 경우: 파일 업로드 모드 활성화
        cameraSection.style.display = 'none';
        fileSection.style.display = 'block';
        deviceBadge.innerText = "💻 PC 접속 모드";
    }
});

// 수동으로 스캔 방식 변경 (카메라 <-> 파일업로드)
function toggleScanMode() {
    const cameraSection = document.getElementById('cameraSection');
    const fileSection = document.getElementById('fileSection');

    if (cameraSection.style.display === 'none') {
        cameraSection.style.display = 'block';
        fileSection.style.display = 'none';
    } else {
        stopCamera();
        cameraSection.style.display = 'none';
        fileSection.style.display = 'block';
    }
}

// [기능 1] URL 직접 입력 검수 처리
async function analyzeUrl() {
    const urlInput = document.getElementById('urlInput').value.trim();
    const resultArea = document.getElementById('resultArea');

    if (!urlInput) {
        alert('검수할 URL을 입력해주세요!');
        return;
    }

    resultArea.innerHTML = '⌛ 입력된 URL을 백엔드로 검증하는 중입니다...';

    try {
        const response = await fetch('/api/v1/scan/url', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ url: urlInput })
        });
        const data = await response.json();
        renderResult(data);
    } catch (error) {
        console.error(error);
        resultArea.innerHTML = `<p class="warning">❌ 서버 통신 오류가 발생했습니다.</p>`;
    }
}

// [기능 2] 이미지 파일 업로드 처리
async function analyzeQrCode() {
    const fileInput = document.getElementById('qrFileInput');
    const resultArea = document.getElementById('resultArea');

    if (!fileInput.files || fileInput.files.length === 0) {
        alert('QR 이미지 파일을 먼저 선택해주세요!');
        return;
    }

    resultArea.innerHTML = '⌛ QR 이미지 파일의 QR 코드를 분석하는 중입니다...';

    const formData = new FormData();
    formData.append('file', fileInput.files[0]);

    try {
        const response = await fetch('/api/v1/scan/qr', {
            method: 'POST',
            body: formData
        });
        const data = await response.json();
        renderResult(data);
    } catch (error) {
        console.error(error);
        resultArea.innerHTML = `<p class="warning">❌ 서버 통신 오류가 발생했습니다.</p>`;
    }
}

// [기능 3] 실시간 카메라 스캔 시작
function startCamera() {
    if (!html5QrCode) {
        html5QrCode = new Html5Qrcode("reader");
    }

    const config = { fps: 10, qrbox: { width: 220, height: 220 } };

    html5QrCode.start(
        { facingMode: "environment" },
        config,
        onScanSuccess
    ).then(() => {
        document.getElementById('startCamBtn').style.display = 'none';
        document.getElementById('stopCamBtn').style.display = 'inline-block';
    }).catch(err => {
        alert('카메라 권한을 허용해 주세요.');
        console.error(err);
    });
}

// 카메라 정지
function stopCamera() {
    if (html5QrCode && html5QrCode.isScanning) {
        html5QrCode.stop().then(() => {
            document.getElementById('startCamBtn').style.display = 'inline-block';
            document.getElementById('stopCamBtn').style.display = 'none';
        });
    }
}

// 카메라 QR 인식 성공 시
async function onScanSuccess(decodedText) {
    stopCamera();

    const resultArea = document.getElementById('resultArea');
    resultArea.innerHTML = '⌛ 스캔된 QR URL을 백엔드로 검증하는 중입니다...';

    try {
        const response = await fetch('/api/v1/scan/url', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ url: decodedText })
        });
        const data = await response.json();
        renderResult(data);
    } catch (error) {
        console.error(error);
        resultArea.innerHTML = `<p class="warning">❌ 서버 통신 오류가 발생했습니다.</p>`;
    }
}

// 공통 결과 화면 출력 함수 (다양한 백엔드 DTO 필드명 호환 처리)
function renderResult(data) {
    console.log("백엔드 응답 데이터:", data); // 개발자 도구 확인용 로그

    const resultArea = document.getElementById('resultArea');
    
    // 1. 위험 여부 판단 (isSuspicious 또는 suspicious 필드 체크)
    const isSuspicious = data.isSuspicious ?? data.suspicious ?? false;
    const statusClass = isSuspicious ? 'warning' : 'safe';
    const statusText = isSuspicious ? '🚨 위험 (의심스러운 URL)' : '✅ 안전 (정상 URL)';

    // 2. 메시지 추출 (message 또는 resultMessage)
    const message = data.message || data.resultMessage || '-';

    // 3. 스캔/입력된 원본 URL 추출
    const rawUrl = data.decodedUrl || data.url || data.originalUrl || data.scannedUrl || '-';

    // 4. 최종 이동 URL 추출
    const finalUrl = data.expandedUrl || data.redirectUrl || data.finalUrl || data.targetUrl || '-';

    resultArea.innerHTML = `
        <p><strong>상태:</strong> <span class="${statusClass}">${statusText}</span></p>
        <p><strong>메시지:</strong> ${message}</p>
        <p><strong>추출/입력 URL:</strong> <code class="url-text">${rawUrl}</code></p>
        <p><strong>최종 이동 URL:</strong> <code class="url-text">${finalUrl}</code></p>
    `;
}