package com.google.zxing.model;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link AztecCodeRequest} 单元测试。
 */
class AztecCodeRequestTests {

    @Test
    void defaultsAreAppliedWhenBuilderUntouched() {
        AztecCodeRequest request = AztecCodeRequest.builder("data").build();

        assertThat(request.getContent()).isEqualTo("data");
        assertThat(request.getWidth()).isEqualTo(AztecCodeRequest.DEFAULT_SIZE);
        assertThat(request.getHeight()).isEqualTo(AztecCodeRequest.DEFAULT_SIZE);
        assertThat(request.getErrorCorrectionPercent()).isEqualTo(AztecCodeRequest.DEFAULT_ERROR_CORRECTION_PERCENT);
        assertThat(request.getMargin()).isEqualTo(AztecCodeRequest.DEFAULT_MARGIN);
        assertThat(request.getCharset()).isEqualTo(StandardCharsets.UTF_8);
    }

    @Test
    void builderAppliesAllSetters() {
        AztecCodeRequest request = AztecCodeRequest.builder("AZ")
                .size(300, 250)
                .errorCorrectionPercent(75)
                .margin(8)
                .charset(StandardCharsets.US_ASCII)
                .build();

        assertThat(request.getWidth()).isEqualTo(300);
        assertThat(request.getHeight()).isEqualTo(250);
        assertThat(request.getErrorCorrectionPercent()).isEqualTo(75);
        assertThat(request.getMargin()).isEqualTo(8);
        assertThat(request.getCharset()).isEqualTo(StandardCharsets.US_ASCII);
    }

    @Test
    void rejectsBlankContent() {
        assertThatThrownBy(() -> AztecCodeRequest.builder("").build())
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> AztecCodeRequest.builder(" ").build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNonPositiveDimensions() {
        assertThatThrownBy(() -> AztecCodeRequest.builder("x").size(0, 100).build())
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> AztecCodeRequest.builder("x").size(100, -1).build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsInvalidErrorCorrectionPercent() {
        assertThatThrownBy(() -> AztecCodeRequest.builder("x").errorCorrectionPercent(0).build())
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> AztecCodeRequest.builder("x").errorCorrectionPercent(101).build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNegativeMargin() {
        assertThatThrownBy(() -> AztecCodeRequest.builder("x").margin(-1).build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNullCharset() {
        assertThatThrownBy(() -> AztecCodeRequest.builder("x").charset(null).build())
                .isInstanceOf(NullPointerException.class);
    }
}
