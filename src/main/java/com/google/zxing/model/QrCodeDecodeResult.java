package com.google.zxing.model;

import java.util.Map;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.ResultMetadataType;
import com.google.zxing.ResultPoint;

/**
 * Immutable QR Code decode result. Semantically equivalent to {@link CodeResult},
 * but statically typed to distinguish the QR subset in the type system.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see CodeResult
 * @see QrCodeDecodeRequest
 */
public final class QrCodeDecodeResult extends CodeResult {

    /**
     * Delegates all arguments to {@link CodeResult}.
     *
     * @param text     the decoded text
     * @param format   the barcode format
     * @param rawBytes the raw bytes
     * @param points   the result points
     * @param metadata the result metadata
     */
    public QrCodeDecodeResult(String text, BarcodeFormat format, byte[] rawBytes, ResultPoint[] points,
            Map<ResultMetadataType, Object> metadata) {
        super(text, format, rawBytes, points, metadata);
    }
}
