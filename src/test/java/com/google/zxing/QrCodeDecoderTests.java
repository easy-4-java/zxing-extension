package com.google.zxing;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.google.zxing.exception.QrCodeErrorCode;
import com.google.zxing.exception.QrCodeException;
import com.google.zxing.model.QrCodeDecodeRequest;
import com.google.zxing.model.QrCodeDecodeResult;
import com.google.zxing.model.QrCodeRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QrCodeDecoderTests {

    @TempDir
    Path tempDirectory;

    @Test
    void decodesMultipleQrCodesFromOneImage() {
        BufferedImage first = QrCodes.encoder().encode(QrCodeRequest.builder("first").size(220, 220).build())
                .image().orElseThrow(AssertionError::new);
        BufferedImage second = QrCodes.encoder().encode(QrCodeRequest.builder("second").size(220, 220).build())
                .image().orElseThrow(AssertionError::new);
        BufferedImage combined = new BufferedImage(480, 240, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = combined.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, combined.getWidth(), combined.getHeight());
        graphics.drawImage(first, 10, 10, null);
        graphics.drawImage(second, 250, 10, null);
        graphics.dispose();

        List<QrCodeDecodeResult> results = QrCodes.decoder()
                .decode(QrCodeDecodeRequest.from(combined).multiple(true).build());

        assertThat(results).extracting(QrCodeDecodeResult::getText).containsExactlyInAnyOrder("first", "second");
    }

    @Test
    void rejectsUnsupportedInput() {
        assertThatThrownBy(() -> QrCodes.decoder().decode(QrCodeDecodeRequest.from(new byte[] { 1, 2, 3 }).build()))
                .isInstanceOf(QrCodeException.class)
                .extracting(ex -> ((QrCodeException) ex).getErrorCode())
                .isEqualTo(QrCodeErrorCode.QRCODE_UNSUPPORTED_FORMAT);
    }

    @Test
    void rejectsOversizedInputBeforeImageDecoding() {
        byte[] bytes = new byte[32];
        assertThatThrownBy(() -> QrCodes.decoder().decode(QrCodeDecodeRequest.from(bytes).maxInputBytes(8).build()))
                .isInstanceOf(QrCodeException.class)
                .extracting(ex -> ((QrCodeException) ex).getErrorCode())
                .isEqualTo(QrCodeErrorCode.QRCODE_IMAGE_TOO_LARGE);
    }

    @Test
    void quickDecodeSupportsCommonInputTypes() throws IOException {
        byte[] bytes = QrCodes.encode("quick-decode").getBytes();
        BufferedImage image = QrCodes.encode("quick-decode").image().orElseThrow(AssertionError::new);
        Path path = tempDirectory.resolve("quick-decode.png");
        Files.write(path, bytes);
        File file = path.toFile();

        assertThat(QrCodes.decode(bytes).getText()).isEqualTo("quick-decode");
        assertThat(QrCodes.decode(image).getText()).isEqualTo("quick-decode");
        assertThat(QrCodes.decode(path).getText()).isEqualTo("quick-decode");
        assertThat(QrCodes.decode(file).getText()).isEqualTo("quick-decode");
        assertThat(QrCodes.decode(new ByteArrayInputStream(bytes)).getText()).isEqualTo("quick-decode");
    }
}
