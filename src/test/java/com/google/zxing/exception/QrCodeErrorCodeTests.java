package com.google.zxing.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link QrCodeErrorCode} 枚举值单元测试。
 */
class QrCodeErrorCodeTests {

    @Test
    void allEnumValuesExist() {
        assertThat(QrCodeErrorCode.values()).containsExactly(
                QrCodeErrorCode.QRCODE_INVALID_ARGUMENT,
                QrCodeErrorCode.QRCODE_CAPACITY_EXCEEDED,
                QrCodeErrorCode.QRCODE_UNSUPPORTED_FORMAT,
                QrCodeErrorCode.QRCODE_DECODE_NOT_FOUND,
                QrCodeErrorCode.QRCODE_IMAGE_TOO_LARGE,
                QrCodeErrorCode.QRCODE_RENDER_FAILED,
                QrCodeErrorCode.QRCODE_SELF_CHECK_FAILED);
    }

    @Test
    void valueOfWorksForEachName() {
        for (QrCodeErrorCode code : QrCodeErrorCode.values()) {
            assertThat(QrCodeErrorCode.valueOf(code.name())).isSameAs(code);
        }
    }
}
