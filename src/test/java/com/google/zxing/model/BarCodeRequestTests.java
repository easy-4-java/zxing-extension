package com.google.zxing.model;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import com.google.zxing.BarcodeFormat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link BarCodeRequest} 单元测试。
 */
class BarCodeRequestTests {

    @Test
    void defaultsAreAppliedWhenBuilderUntouched() {
        BarCodeRequest request = BarCodeRequest.builder("123", com.google.zxing.BarcodeFormat.CODE_128).build();

        assertThat(request.getContent()).isEqualTo("123");
        assertThat(request.getFormat()).isEqualTo(com.google.zxing.BarcodeFormat.CODE_128);
        assertThat(request.getWidth()).isEqualTo(BarCodeRequest.DEFAULT_WIDTH);
        assertThat(request.getHeight()).isEqualTo(BarCodeRequest.DEFAULT_HEIGHT);
        assertThat(request.getMargin()).isEqualTo(10);
        assertThat(request.getCharset()).isEqualTo(StandardCharsets.UTF_8);
    }

    @Test
    void builderAppliesAllSetters() {
        BarCodeRequest request = BarCodeRequest.builder("ORDER-001", com.google.zxing.BarcodeFormat.EAN_13)
                .size(400, 150)
                .margin(4)
                .charset(StandardCharsets.UTF_16BE)
                .build();

        assertThat(request.getWidth()).isEqualTo(400);
        assertThat(request.getHeight()).isEqualTo(150);
        assertThat(request.getMargin()).isEqualTo(4);
        assertThat(request.getCharset()).isEqualTo(StandardCharsets.UTF_16BE);
        assertThat(request.getFormat()).isEqualTo(com.google.zxing.BarcodeFormat.EAN_13);
    }

    @Test
    void rejectsBlankContent() {
        assertThatThrownBy(() -> BarCodeRequest.builder("", com.google.zxing.BarcodeFormat.CODE_128).build())
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> BarCodeRequest.builder(" ", com.google.zxing.BarcodeFormat.CODE_128).build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNonPositiveDimensionsOrNegativeMargin() {
        assertThatThrownBy(() -> BarCodeRequest.builder("x", com.google.zxing.BarcodeFormat.CODE_128).size(0, 100).build())
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> BarCodeRequest.builder("x", com.google.zxing.BarcodeFormat.CODE_128).size(100, 0).build())
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> BarCodeRequest.builder("x", com.google.zxing.BarcodeFormat.CODE_128).margin(-1).build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNullFormatOrCharset() {
        assertThatThrownBy(() -> BarCodeRequest.builder("x", null).build())
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> BarCodeRequest.builder("x", com.google.zxing.BarcodeFormat.CODE_128).charset(null).build())
                .isInstanceOf(NullPointerException.class);
    }
}
