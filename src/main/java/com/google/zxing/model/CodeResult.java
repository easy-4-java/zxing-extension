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
 * 不可变的解码结果抽象，是 QR / Aztec / BarCode 共享的结果类型。
 *
 * <p>暴露：
 * <ul>
 *     <li>{@link #getText()}：原始文本；</li>
 *     <li>{@link #getFormat()} 与 {@link #getBarcodeFormat()}：格式（同语义，两种命名）。</li>
 *     <li>{@link #getRawBytes()}、{@link #getPoints()} 与 {@link #getMetadata()}：原始字节、定位点、元数据（均为防御性拷贝）。</li>
 * </ul>
 */
public class CodeResult {

    private final String text;
    private final BarcodeFormat format;
    private final byte[] rawBytes;
    private final ResultPoint[] points;
    private final Map<ResultMetadataType, Object> metadata;

    /**
     * 构造解码结果。
     *
     * @param text     解码文本，可为 {@code null}
     * @param format   格式；不能为 {@code null}
     * @param rawBytes 原始字节，可为 {@code null}
     * @param points   定位点数组，可为 {@code null}（{@code null} → 视为空数组）
     * @param metadata 元数据映射，可为 {@code null}（{@code null} → 视为空映射）
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
     * 从 ZXing 原生 {@link Result} 转换为当前项目结果。
     *
     * @param result ZXing 结果；不能为 {@code null}
     * @return 不可变结果
     */
    public static CodeResult from(Result result) {
        Objects.requireNonNull(result, "result must not be null");
        return new CodeResult(result.getText(), result.getBarcodeFormat(), result.getRawBytes(),
                result.getResultPoints(), result.getResultMetadata());
    }

    /**
     * @return 解码得到的文本
     */
    public String getText() {
        return text;
    }

    /**
     * @return ZXing {@link BarcodeFormat}
     */
    public BarcodeFormat getFormat() {
        return format;
    }

    /**
     * 与 {@link #getFormat()} 等价；为了与 ZXing 原生 {@link Result#getBarcodeFormat()} 命名保持一致。
     *
     * @return ZXing {@link BarcodeFormat}
     */
    public BarcodeFormat getBarcodeFormat() {
        return format;
    }

    /**
     * @return 原始字节，未设置时为 {@code null}
     */
    public byte[] getRawBytes() {
        return Objects.isNull(rawBytes) ? null : rawBytes.clone();
    }

    /**
     * @return 定位点的防御性拷贝
     */
    public ResultPoint[] getPoints() {
        return points.clone();
    }

    /**
     * @return 不可变元数据视图
     */
    public Map<ResultMetadataType, Object> getMetadata() {
        return metadata;
    }
}
