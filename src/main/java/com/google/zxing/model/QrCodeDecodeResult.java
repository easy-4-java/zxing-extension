package com.google.zxing.model;

import java.util.Map;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.ResultMetadataType;
import com.google.zxing.ResultPoint;

/**
 * 不可变的 QR Code 解码结果。语义上与 {@link CodeResult} 等价，仅在静态类型系统中区分 QR 子集。
 */
public final class QrCodeDecodeResult extends CodeResult {

    /**
     * 透传给 {@link CodeResult}。
     *
     * @param text     文本
     * @param format   格式
     * @param rawBytes 原始字节
     * @param points   定位点
     * @param metadata 元数据
     */
    public QrCodeDecodeResult(String text, BarcodeFormat format, byte[] rawBytes, ResultPoint[] points,
            Map<ResultMetadataType, Object> metadata) {
        super(text, format, rawBytes, points, metadata);
    }
}
