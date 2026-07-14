package com.google.zxing.exception;

import java.util.Objects;

import lombok.Getter;

/**
 * 运行期异常，携带稳定的 {@link QrCodeErrorCode}，用于 QR Code 引擎的所有错误路径。
 *
 * <p>{@link QrCodeErrorCode} 与调用方契约绑定，不依赖异常消息内容，因此即使本地化文案变更也不会破坏 API。
 */
@Getter
public class QrCodeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** 稳定错误码；不为 {@code null}。 */
    private final QrCodeErrorCode errorCode;

    /**
     * 创建仅含消息的异常。
     *
     * @param errorCode 不能为 {@code null}
     * @param message   人类可读的错误描述
     */
    public QrCodeException(QrCodeErrorCode errorCode, String message) {
        super(message);
        this.errorCode = Objects.requireNonNull(errorCode, "errorCode must not be null");
    }

    /**
     * 创建带原因的异常。
     *
     * @param errorCode 不能为 {@code null}
     * @param message   人类可读的错误描述
     * @param cause     底层原因；通常为 ZXing 上游异常
     */
    public QrCodeException(QrCodeErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = Objects.requireNonNull(errorCode, "errorCode must not be null");
    }
}
