package com.google.zxing.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link QrCodeImageFormat} 枚举值单元测试。
 */
class QrCodeImageFormatTests {

    @Test
    void pngHasExpectedImageIoNameAndMimeType() {
        assertThat(QrCodeImageFormat.PNG.getImageIoName()).isEqualTo("png");
        assertThat(QrCodeImageFormat.PNG.getMimeType()).isEqualTo("image/png");
    }

    @Test
    void svgHasExpectedImageIoNameAndMimeType() {
        assertThat(QrCodeImageFormat.SVG.getImageIoName()).isEqualTo("svg");
        assertThat(QrCodeImageFormat.SVG.getMimeType()).isEqualTo("image/svg+xml");
    }

    @Test
    void enumValuesAreExactlyTwo() {
        assertThat(QrCodeImageFormat.values()).hasSize(2);
        assertThat(QrCodeImageFormat.valueOf("PNG")).isEqualTo(QrCodeImageFormat.PNG);
        assertThat(QrCodeImageFormat.valueOf("SVG")).isEqualTo(QrCodeImageFormat.SVG);
    }
}
