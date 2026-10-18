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

@Service
public class QrDecoderService {

    public String decodeQrCode(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
            if (bufferedImage == null) {
                return null;
            }

            LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            Result result = new MultiFormatReader().decode(bitmap);
            return result.getText();
        } catch (Throwable e) {
        	System.err.println("QR 디코딩 예외 발생 : "+e.getMessage());
            // ★ Exception 대신 Throwable을 사용하여 OutOfMemoryError 등 모든 최상위 에러까지 안전 포획
            return null;
        }
    }
}