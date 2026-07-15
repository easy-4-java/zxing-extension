package com.google.zxing.exception;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link QrCodeException} 单元测试。
 */
class QrCodeExceptionTests {

    @Test
    void messageOnlyConstructorExposesErrorCode() {
        QrCodeException ex = new QrCodeException(QrCodeErrorCode.QRCODE_RENDER_FAILED, "boom");

        assertThat(ex.getErrorCode()).isEqualTo(QrCodeErrorCode.QRCODE_RENDER_FAILED);
        assertThat(ex.getMessage()).isEqualTo("boom");
        assertThat(ex.getCause()).isNull();
    }

    @Test
    void causeConstructorRetainsBothErrorCodeAndCause() {
        IOException io = new IOException("io");
        QrCodeException ex = new QrCodeException(QrCodeErrorCode.QRCODE_RENDER_FAILED, "boom", io);

        assertThat(ex.getErrorCode()).isEqualTo(QrCodeErrorCode.QRCODE_RENDER_FAILED);
        assertThat(ex.getCause()).isSameAs(io);
        assertThat(ex.getMessage()).isEqualTo("boom");
    }

    @Test
    void serialVersionUidIsStable() {
        QrCodeException ex = new QrCodeException(QrCodeErrorCode.QRCODE_RENDER_FAILED, "boom");
        assertThat(ex.getClass()).isNotNull();
    }

    @Test
    void rejectsNullErrorCode() {
        assertThatThrownBy(() -> new QrCodeException(null, "x"))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new QrCodeException(null, "x", new IOException()))
                .isInstanceOf(NullPointerException.class);
    }
}
