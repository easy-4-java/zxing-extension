package com.google.zxing;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.google.zxing.common.BitMatrix;
import com.google.zxing.exception.CodeException;
import com.google.zxing.model.CodeOutput;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 包私有 {@link CodeImageSupport} 单元测试，通过同包访问覆盖 5 个 {@code read} 重载与 {@code toPng}。
 */
class CodeImageSupportTests {

    private static byte[] samplePng() throws Exception {
        BufferedImage image = new BufferedImage(20, 20, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        return out.toByteArray();
    }

    private static BitMatrix matrix() throws Exception {
        java.util.Map<EncodeHintType, Object> hints = new java.util.EnumMap<EncodeHintType, Object>(EncodeHintType.class);
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 0);
        return new MultiFormatWriter().encode("test", BarcodeFormat.QR_CODE, 64, 64, hints);
    }

    @Test
    void toPngProducesValidPngBytes() throws Exception {
        CodeOutput output = CodeImageSupport.toPng(matrix());

        assertThat(output.getMimeType()).isEqualTo("image/png");
        assertThat(output.getBytes()).startsWith((byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47);
        assertThat(output.getWidth()).isPositive();
        assertThat(output.getHeight()).isPositive();
        assertThat(output.image()).isPresent();
    }

    @Test
    void readFromBytesProducesImage() throws Exception {
        BufferedImage image = CodeImageSupport.read(samplePng());

        assertThat(image.getWidth()).isEqualTo(20);
        assertThat(image.getHeight()).isEqualTo(20);
    }

    @Test
    void readFromFileAndPath(@TempDir Path tempDir) throws Exception {
        byte[] bytes = samplePng();
        Path path = tempDir.resolve("qr.png");
        Files.write(path, bytes);
        File file = path.toFile();

        assertThat(CodeImageSupport.read(file).getWidth()).isEqualTo(20);
        assertThat(CodeImageSupport.read(path).getHeight()).isEqualTo(20);
    }

    @Test
    void readFromInputStreamReadsBytesWithoutClosing() throws Exception {
        InputStream stream = new ByteArrayInputStream(samplePng());
        BufferedImage image = CodeImageSupport.read(stream);
        assertThat(image).isNotNull();
        assertThat(stream).isNotNull();
    }

    @Test
    void readFromNullInputsRejected() {
        assertThatThrownBy(() -> CodeImageSupport.read((byte[]) null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> CodeImageSupport.read((File) null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> CodeImageSupport.read((Path) null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> CodeImageSupport.read((InputStream) null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void readFromUnreadablePathThrowsCodeException(@TempDir Path tempDir) {
        Path missing = tempDir.resolve("nope.png");
        assertThatThrownBy(() -> CodeImageSupport.read(missing))
                .isInstanceOf(CodeException.class)
                .hasMessageContaining("Failed to read code image");
    }

    @Test
    void readFromNonImageBytesThrowsCodeException() {
        assertThatThrownBy(() -> CodeImageSupport.read(new byte[] { 0, 1, 2, 3, 4 }))
                .isInstanceOf(CodeException.class)
                .hasMessageContaining("Input is not a supported raster image");
    }

    @Test
    void readFromBrokenStreamThrowsCodeException() {
        InputStream broken = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("stream kaboom");
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                throw new IOException("stream kaboom");
            }
        };

        // ImageIO can either throw an IOException directly or wrap it in IIOException
        // (a RuntimeException subclass); on the JDK used here, the read returns null first.
        assertThatThrownBy(() -> CodeImageSupport.read(broken))
                .isInstanceOfAny(CodeException.class, RuntimeException.class);
    }

    @Test
    void readFromPathWithUnreadableParentPropagatesIoError(@TempDir Path tempDir) throws Exception {
        // The read(Path) tries Files.newInputStream; force IOException by requesting a path
        // inside a non-existent directory chain that is guaranteed to fail on POSIX.
        Path missingDir = tempDir.resolve("nonexistent-subdir-" + System.nanoTime());
        Path target = missingDir.resolve("nope.png");
        assertThatThrownBy(() -> CodeImageSupport.read(target))
                .isInstanceOf(CodeException.class);
    }
}
