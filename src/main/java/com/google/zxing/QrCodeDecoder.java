package com.google.zxing;

import java.util.List;

import com.google.zxing.model.QrCodeDecodeRequest;
import com.google.zxing.model.QrCodeDecodeResult;

/**
 * 框架无关的 QR Code 解码器接口。
 *
 * <p>实现需遵循语义：
 * <ul>
 *     <li>{@link #decode(QrCodeDecodeRequest)} 返回不可变列表（即使是单码场景）。</li>
 *     <li>{@link #decodeFirst(QrCodeDecodeRequest)} 在没有 QR 时抛出 {@code QrCodeException} 而不是返回空。</li>
 * </ul>
 */
public interface QrCodeDecoder {

    /**
     * 解码给定的输入，单码返回长度为 1 的列表，多码场景返回按解码顺序排列的结果。
     *
     * @param request 解码请求；不能为 {@code null}
     * @return 不可变的结果列表
     * @throws com.google.zxing.exception.QrCodeException 参数无效、未找到 QR 或图像超大时抛出
     */
    List<QrCodeDecodeResult> decode(QrCodeDecodeRequest request);

    /**
     * 解码并返回首条记录，等价于 {@code decode(request).get(0)}。
     *
     * @param request 解码请求；不能为 {@code null}
     * @return 第一条解码结果
     * @throws com.google.zxing.exception.QrCodeException 当没有 QR 时抛出（替代空列表的索引越界）
     */
    default QrCodeDecodeResult decodeFirst(QrCodeDecodeRequest request) {
        return decode(request).get(0);
    }
}
