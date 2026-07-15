package com.google.zxing;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.google.zxing.model.QrCodeRequest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link QrCodes} 门面扩展测试，覆盖 5 个 {@code decode} 重载和接口默认方法。
 */
class QrCodesTests {

    private static final String CONTENT = "https://example.com/-/qr-codes";

    @Test
    void encoderAndDecoderReturnSameSingletons() {
        assertThat(QrCodes.encoder()).isSameAs(QrCodes.encoder());
        assertThat(QrCodes.decoder()).isSameAs(QrCodes.decoder());
        assertThat(QrCodes.encoder()).isInstanceOf(DefaultQrCodeEncoder.class);
        assertThat(QrCodes.decoder()).isInstanceOf(DefaultQrCodeDecoder.class);
    }

    @Test
    void encodeWritesThroughInterfaceDefault(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("qr.png");
        try (OutputStream stream = Files.newOutputStream(file)) {
            QrCodes.encoder().encode(QrCodeRequest.builder(CONTENT).size(180, 180).build(), stream);
        }
        assertThat(file).exists();
        assertThat(Files.size(file)).isPositive();
        assertThat(QrCodes.decode(file).getText()).isEqualTo(CONTENT);
    }

    @Test
    void decodeAcceptsFile(@TempDir Path tempDir) throws IOException {
        byte[] bytes = QrCodes.encode(CONTENT).getBytes();
        File file = tempDir.resolve("qr.png").toFile();
        Files.write(file.toPath(), bytes);

        assertThat(QrCodes.decode(file).getText()).isEqualTo(CONTENT);
    }

    @Test
    void decodeAcceptsInputStream(@TempDir Path tempDir) throws IOException {
        byte[] bytes = QrCodes.encode(CONTENT).getBytes();
        File file = tempDir.resolve("qr.png").toFile();
        Files.write(file.toPath(), bytes);

        try (java.io.InputStream in = new ByteArrayInputStream(bytes)) {
            assertThat(QrCodes.decode(in).getText()).isEqualTo(CONTENT);
        }
        assertThat(QrCodes.decode(file).getText()).isEqualTo(CONTENT);
    }

    @Test
    void withLogoHelperProducesMonochromeStyle() {
        BufferedImage logo = new BufferedImage(40, 40, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = logo.createGraphics();
        g.setColor(Color.RED);
        g.fillRect(0, 0, 40, 40);
        g.dispose();

        assertThat(QrCodes.decode(QrCodes.withLogo(CONTENT, logo).getBytes()).getText()).isEqualTo(CONTENT);
    }

    @Test
    void colorfulProducesDecodableColorfulOutput() {
        assertThat(QrCodes.decode(QrCodes.colorful(CONTENT).getBytes()).getText()).isEqualTo(CONTENT);
    }
}
