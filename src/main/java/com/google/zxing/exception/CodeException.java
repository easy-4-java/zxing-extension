package com.google.zxing.exception;

/**
 * 运行期异常，用于非 QR（AztEc / 一维条形码）编码和解码中抛出的错误。
 *
 * <p>不携带错误码 —— 调用方只能依赖异常消息与堆栈。
 */
public class CodeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 创建仅含消息的异常。
     *
     * @param message 人类可读描述
     */
    public CodeException(String message) {
        super(message);
    }

    /**
     * 创建带原因的异常。
     *
     * @param message 人类可读描述
     * @param cause   底层原因（通常为 ZXing 或 IO 异常）
     */
    public CodeException(String message, Throwable cause) {
        super(message, cause);
    }
}
