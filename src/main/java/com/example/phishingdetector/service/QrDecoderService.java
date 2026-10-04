package com.example.phishingdetector.service;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

// QR 코드 이미지 파일에서 실제 URL 문자열을 추출하는 전담 서비스
@Service
public class QrDecoderService {

    // 업로드된 이미지 파일(MultipartFile)을 받아 ZXing 라이브러리로 QR 코드 해독
    public String decodeQrCode(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            // 1. 전달받은 파일을 자바 표준 이미지 객체(BufferedImage)로 변환
            BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
            if (bufferedImage == null) {
                return null;
            }

            // 2. ZXing 인식용 흑백 이진화 비트맵 변환
            LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            // 3. ZXing 디코더 실행 및 QR 코드 내 실제 URL 텍스트 추출
            Result result = new MultiFormatReader().decode(bitmap);
            return result.getText();
        } catch (Exception e) {
            // QR 코드가 인식되지 않거나 파손된 이미지인 경우 null 반환
            return null;
        }
    }
}