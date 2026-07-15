package com.google.zxing;

import java.io.IOException;
import java.io.OutputStream;

import com.google.zxing.model.QrCodeOutput;
import com.google.zxing.model.QrCodeRequest;

/**
 * 框架无关的 QR Code 编码器接口。
 *
 * <p>实现需遵循语义：
 * <ul>
 *     <li>{@link #encode(QrCodeRequest)} 必须返回不可变的 {@link QrCodeOutput}。</li>
 *     <li>默认的 {@link #encode(QrCodeRequest, OutputStream)} 不会关闭调用方传入的流。</li>
 * </ul>
 */
public interface QrCodeEncoder {

    /**
     * 根据请求生成对应的编码输出。
     *
     * @param request 编码请求；不能为 {@code null}
     * @return 不可变的编码结果
     * @throws com.google.zxing.exception.QrCodeException 参数无效、超容量或渲染失败时抛出
     */
    QrCodeOutput encode(QrCodeRequest request);

    /**
     * 便捷方法：将 {@link #encode(QrCodeRequest)} 的输出写入流。该方法不会关闭调用方传入的流。
     *
     * @param request     编码请求
     * @param outputStream 调用方持有的输出流
     * @throws IOException 当底层写入失败时抛出
     */
    default void encode(QrCodeRequest request, OutputStream outputStream) throws IOException {
        encode(request).writeTo(outputStream);
    }
}
