package com.google.zxing;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.google.zxing.exception.CodeException;
import com.google.zxing.model.BarCodeRequest;
import com.google.zxing.model.CodeOutput;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BarCodesTests {

    private static final String EAN13 = "6901234567892";

    @TempDir
    Path tempDirectory;

    @Test
    void ean13QuickEntryPointRoundTrips() {
        CodeOutput output = BarCodes.ean13(EAN13);

        assertThat(output.dataUri()).startsWith("data:image/png;base64,");
        assertThat(BarCodes.decode(output.getBytes()).getText()).isEqualTo(EAN13);
        assertThat(BarCodes.decode(output.image().orElseThrow(AssertionError::new)).getText()).isEqualTo(EAN13);
    }

    @Test
    void explicitCode128RequestRoundTrips() {
        CodeOutput output = BarCodes.encode(BarCodeRequest.builder("CODE-128-123", BarcodeFormat.CODE_128)
                .size(360, 120)
                .margin(8)
                .build());

        assertThat(BarCodes.decode(output.getBytes()).getBarcodeFormat()).isEqualTo(BarcodeFormat.CODE_128);
        assertThat(BarCodes.decode(output.getBytes()).getText()).isEqualTo("CODE-128-123");
    }

    @Test
    void decodeSupportsFilePathAndStream() throws IOException {
        CodeOutput output = BarCodes.ean13(EAN13);
        Path path = tempDirectory.resolve("ean13.png");
        Files.write(path, output.getBytes());

        assertThat(BarCodes.decode(path).getText()).isEqualTo(EAN13);
        assertThat(BarCodes.decode(path.toFile()).getText()).isEqualTo(EAN13);
        assertThat(BarCodes.decode(new ByteArrayInputStream(output.getBytes())).getText()).isEqualTo(EAN13);
    }

    @Test
    void rejectsTwoDimensionalFormatAndInvalidImage() {
        assertThatThrownBy(() -> BarCodes.encode(BarCodeRequest.builder("qr", BarcodeFormat.QR_CODE).build()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> BarCodes.decode(new byte[] { 1, 2, 3 }))
                .isInstanceOf(CodeException.class);
    }

    @Test
    void decodeOnBlankImageThrows() {
        java.awt.image.BufferedImage blank =
                new java.awt.image.BufferedImage(80, 80, java.awt.image.BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g = blank.createGraphics();
        g.setColor(java.awt.Color.WHITE);
        g.fillRect(0, 0, 80, 80);
        g.dispose();
        assertThatThrownBy(() -> BarCodes.decode(blank))
                .isInstanceOf(CodeException.class);
    }

    @Test
    void rejectsUnsupportedFormatsPdf417AztecDataMatrix() {
        for (BarcodeFormat format : new BarcodeFormat[] {
                BarcodeFormat.PDF_417, BarcodeFormat.AZTEC, BarcodeFormat.DATA_MATRIX, BarcodeFormat.MAXICODE
        }) {
            assertThatThrownBy(() -> BarCodes.encode(BarCodeRequest.builder("data", format).build()))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    void rejectsNullRequest() {
        assertThatThrownBy(() -> BarCodes.encode(null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> BarCodes.decode((BufferedImage) null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void rejectsUnreadableBytes() {
        assertThatThrownBy(() -> BarCodes.decode(new byte[] { 7, 7, 7 }))
                .isInstanceOf(CodeException.class);
    }

    @Test
    void encodeAndDecodeAcrossAllSupportedFormats() {
        for (BarcodeFormat format : new BarcodeFormat[] {
                BarcodeFormat.CODABAR, BarcodeFormat.CODE_39, BarcodeFormat.CODE_93,
                BarcodeFormat.CODE_128, BarcodeFormat.EAN_8, BarcodeFormat.EAN_13,
                BarcodeFormat.ITF, BarcodeFormat.UPC_A
        }) {
            String content = pickContent(format);
            CodeOutput output = BarCodes.encode(BarCodeRequest.builder(content, format)
                    .size(220, 100).margin(4).build());
            assertThat(BarCodes.decode(output.getBytes()).getBarcodeFormat()).isEqualTo(format);
            assertThat(BarCodes.decode(output.getBytes())).isNotNull();
        }
    }

    private static String pickContent(BarcodeFormat format) {
        if (format == BarcodeFormat.EAN_13) {
            return "590123412345";
        }
        if (format == BarcodeFormat.EAN_8) {
            return "96385074";
        }
        if (format == BarcodeFormat.UPC_A) {
            return "42510075714";
        }
        if (format == BarcodeFormat.ITF) {
            return "12345678901231";
        }
        return "1234567890";
    }
}
