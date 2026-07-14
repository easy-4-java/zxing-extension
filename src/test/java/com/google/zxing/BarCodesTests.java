package com.google.zxing;

import java.io.ByteArrayInputStream;
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
}
