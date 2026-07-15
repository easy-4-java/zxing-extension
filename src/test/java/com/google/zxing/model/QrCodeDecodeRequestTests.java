package com.google.zxing.model;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.google.zxing.exception.QrCodeErrorCode;
import com.google.zxing.exception.QrCodeException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link QrCodeDecodeRequest} 单元测试，覆盖 5 个 {@code from(...)} 重载与 Builder 行为。
 */
class QrCodeDecodeRequestTests {

    private static byte[] samplePngBytes() throws Exception {
        BufferedImage image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB);
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        javax.imageio.ImageIO.write(image, "png", out);
        return out.toByteArray();
    }

    @Test
    void fromByteArrayStripsBytesAndRetainsConfig() throws Exception {
        byte[] bytes = samplePngBytes();
        QrCodeDecodeRequest request = QrCodeDecodeRequest.from(bytes)
                .multiple(true)
                .tryHarder(false)
                .alsoInverted(false)
                .pureBarcode(true)
                .charset(StandardCharsets.US_ASCII)
                .maxInputBytes(2048)
                .maxPixels(1_000_000L)
                .build();

        assertThat(request.getBytes()).isEqualTo(bytes);
        assertThat(request.getBytes()).isNotSameAs(bytes);
        assertThat(request.isMultiple()).isTrue();
        assertThat(request.isTryHarder()).isFalse();
        assertThat(request.isAlsoInverted()).isFalse();
        assertThat(request.isPureBarcode()).isTrue();
        assertThat(request.getCharset()).isEqualTo(StandardCharsets.US_ASCII);
        assertThat(request.getMaxInputBytes()).isEqualTo(2048);
        assertThat(request.getMaxPixels()).isEqualTo(1_000_000L);
    }

    @Test
    void fromBufferedImageHasNullBytes() {
        BufferedImage image = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);
        QrCodeDecodeRequest request = QrCodeDecodeRequest.from(image).build();

        assertThat(request.getBytes()).isNull();
        assertThat(request.getImage()).isSameAs(image);
    }

    @Test
    void fromPathLoadsBytes(@TempDir Path tempDir) throws Exception {
        byte[] bytes = samplePngBytes();
        Path path = tempDir.resolve("qr.png");
        Files.write(path, bytes);

        QrCodeDecodeRequest request = QrCodeDecodeRequest.from(path).build();

        assertThat(request.getBytes()).isEqualTo(bytes);
    }

    @Test
    void fromFileLoadsBytes(@TempDir Path tempDir) throws Exception {
        byte[] bytes = samplePngBytes();
        File file = tempDir.resolve("qr.png").toFile();
        Files.write(file.toPath(), bytes);

        QrCodeDecodeRequest request = QrCodeDecodeRequest.from(file).build();

        assertThat(request.getBytes()).isEqualTo(bytes);
    }

    @Test
    void fromInputStreamDoesNotClose(@TempDir Path tempDir) throws Exception {
        byte[] bytes = samplePngBytes();
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);

        QrCodeDecodeRequest request = QrCodeDecodeRequest.from(stream).build();

        assertThat(request.getBytes()).isEqualTo(bytes);
        assertThat(stream).isNotNull();
    }

    @Test
    void fromInputStreamPropagatesIOException() {
        java.io.InputStream broken = new java.io.InputStream() {
            @Override
            public int read() throws java.io.IOException {
                throw new java.io.IOException("stream kaboom");
            }

            @Override
            public int read(byte[] b, int off, int len) throws java.io.IOException {
                throw new java.io.IOException("stream kaboom");
            }
        };

        assertThatThrownBy(() -> QrCodeDecodeRequest.from(broken))
                .isInstanceOf(QrCodeException.class)
                .extracting(ex -> ((QrCodeException) ex).getErrorCode())
                .isEqualTo(QrCodeErrorCode.QRCODE_RENDER_FAILED);
    }

    @Test
    void fromPathPropagatesIoError(@TempDir Path tempDir) {
        Path missing = tempDir.resolve("does-not-exist.png");
        assertThatThrownBy(() -> QrCodeDecodeRequest.from(missing))
                .isInstanceOf(QrCodeException.class)
                .extracting(ex -> ((QrCodeException) ex).getErrorCode())
                .isEqualTo(QrCodeErrorCode.QRCODE_RENDER_FAILED);
    }

    @Test
    void fromMissingFilePropagatesIoError(@TempDir Path tempDir) {
        File missing = tempDir.resolve("missing.png").toFile();
        assertThatThrownBy(() -> QrCodeDecodeRequest.from(missing))
                .isInstanceOf(QrCodeException.class)
                .extracting(ex -> ((QrCodeException) ex).getErrorCode())
                .isEqualTo(QrCodeErrorCode.QRCODE_RENDER_FAILED);
    }

    @Test
    void fromNullInputsRejected() {
        assertThatThrownBy(() -> QrCodeDecodeRequest.from((byte[]) null).build())
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> QrCodeDecodeRequest.from((File) null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> QrCodeDecodeRequest.from((Path) null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> QrCodeDecodeRequest.from((java.io.InputStream) null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void builderRejectsMissingSource() throws Exception {
        // Use reflection to construct the private Builder with both bytes and image null,
        // which is the only configuration where build() rejects "bytes or image must be provided".
        Class<?> builderClass = Class.forName("com.google.zxing.model.QrCodeDecodeRequest$Builder");
        java.lang.reflect.Constructor<?> ctor = builderClass.getDeclaredConstructor(byte[].class, java.awt.image.BufferedImage.class);
        ctor.setAccessible(true);
        Object builder = ctor.newInstance(null, null);
        java.lang.reflect.Method build = builderClass.getMethod("build");
        assertThatThrownBy(() -> build.invoke(builder))
                .hasCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void builderRejectsNonPositiveLimits() {
        assertThatThrownBy(() -> QrCodeDecodeRequest.from(new byte[]{1}).maxInputBytes(0).build())
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> QrCodeDecodeRequest.from(new byte[]{1}).maxPixels(0).build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void builderRequiresCharset() {
        assertThatThrownBy(() -> QrCodeDecodeRequest.from(new byte[]{1}).charset(null).build())
                .isInstanceOf(NullPointerException.class);
    }
}
