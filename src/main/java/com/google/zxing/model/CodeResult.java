package com.google.zxing.model;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.ResultMetadataType;
import com.google.zxing.ResultPoint;

/**
 * Immutable decode result shared across QR, Aztec and one-dimensional
 * barcode paths.
 *
 * <p>Exposes:</p>
 * <ul>
 *     <li>{@link #getText()} &mdash; the decoded text;</li>
 *     <li>{@link #getFormat()} and {@link #getBarcodeFormat()} &mdash; the
 *         barcode format (semantically identical; two naming conventions).</li>
 *     <li>{@link #getRawBytes()}, {@link #getPoints()} and
 *         {@link #getMetadata()} &mdash; raw bytes, result points and
 *         metadata (all defensive copies).</li>
 * </ul>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see QrCodeDecodeResult
 * @see com.google.zxing.AztecCodes
 * @see com.google.zxing.BarCodes
 */
public class CodeResult {

    private final String text;
    private final BarcodeFormat format;
    private final byte[] rawBytes;
    private final ResultPoint[] points;
    private final Map<ResultMetadataType, Object> metadata;

    /**
     * Constructs a decode result.
     *
     * @param text     the decoded text; may be {@code null}
     * @param format   the barcode format; must not be {@code null}
     * @param rawBytes the raw bytes; may be {@code null}
     * @param points   the result point array; may be {@code null} (treated as
     *                 empty array)
     * @param metadata the metadata map; may be {@code null} (treated as empty
     *                 map)
     */
    public CodeResult(String text, BarcodeFormat format, byte[] rawBytes, ResultPoint[] points,
            Map<ResultMetadataType, Object> metadata) {
        this.text = text;
        this.format = Objects.requireNonNull(format, "format must not be null");
        this.rawBytes = Objects.isNull(rawBytes) ? null : rawBytes.clone();
        this.points = Objects.isNull(points) ? new ResultPoint[0] : points.clone();
        Map<ResultMetadataType, Object> copied = new EnumMap<ResultMetadataType, Object>(ResultMetadataType.class);
        if (Objects.nonNull(metadata)) {
            copied.putAll(metadata);
        }
        this.metadata = Collections.unmodifiableMap(copied);
    }

    /**
     * Converts a ZXing native {@link Result} into this project's result type.
     *
     * @param result the ZXing result; must not be {@code null}
     * @return an immutable {@link CodeResult}
     */
    public static CodeResult from(Result result) {
        Objects.requireNonNull(result, "result must not be null");
        return new CodeResult(result.getText(), result.getBarcodeFormat(), result.getRawBytes(),
                result.getResultPoints(), result.getResultMetadata());
    }

    /**
     * Returns the decoded text.
     *
     * @return the decoded text, or {@code null} if not available
     */
    public String getText() {
        return text;
    }

    /**
     * Returns the ZXing {@link BarcodeFormat}.
     *
     * @return the barcode format
     */
    public BarcodeFormat getFormat() {
        return format;
    }

    /**
     * Equivalent to {@link #getFormat()}; retained for naming consistency
     * with ZXing's native {@link Result#getBarcodeFormat()}.
     *
     * @return the barcode format
     */
    public BarcodeFormat getBarcodeFormat() {
        return format;
    }

    /**
     * Returns a defensive copy of the raw bytes.
     *
     * @return the raw bytes, or {@code null} if not available
     */
    public byte[] getRawBytes() {
        return Objects.isNull(rawBytes) ? null : rawBytes.clone();
    }

    /**
     * Returns a defensive copy of the result points.
     *
     * @return the result points (never {@code null})
     */
    public ResultPoint[] getPoints() {
        return points.clone();
    }

    /**
     * Returns an unmodifiable view of the result metadata.
     *
     * @return the metadata map (never {@code null})
     */
    public Map<ResultMetadataType, Object> getMetadata() {
        return metadata;
    }
}
