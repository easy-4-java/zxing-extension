package com.google.zxing;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.exception.CodeException;
import com.google.zxing.model.BarCodeRequest;
import com.google.zxing.model.CodeOutput;
import com.google.zxing.model.CodeResult;
import com.google.zxing.source.BufferedImageLuminanceSource;

/**
 * 一维条形码静态门面。
 *
 * <p>支持以下格式：
 * {@link BarcodeFormat#CODABAR}, {@link BarcodeFormat#CODE_39},
 * {@link BarcodeFormat#CODE_93}, {@link BarcodeFormat#CODE_128},
 * {@link BarcodeFormat#EAN_8}, {@link BarcodeFormat#EAN_13},
 * {@link BarcodeFormat#ITF}, {@link BarcodeFormat#UPC_A},
 * {@link BarcodeFormat#UPC_E}。
 *
 * <p>QR / Aztec / PDF417 等二维格式不被 {@link #encode(BarCodeRequest)} 接受。线程安全：
 * 内部仅调用 ZXing 的无状态 API 与不可变静态集合。
 */
public final class BarCodes {

    private static final MultiFormatWriter WRITER = new MultiFormatWriter();

    /** 一维条形码支持的 ZXing {@link BarcodeFormat} 集合（不可变）。 */
    private static final Set<BarcodeFormat> SUPPORTED_FORMATS = Collections.unmodifiableSet(EnumSet.of(
            BarcodeFormat.CODABAR,
            BarcodeFormat.CODE_39,
            BarcodeFormat.CODE_93,
            BarcodeFormat.CODE_128,
            BarcodeFormat.EAN_8,
            BarcodeFormat.EAN_13,
            BarcodeFormat.ITF,
            BarcodeFormat.UPC_A,
            BarcodeFormat.UPC_E));

    private BarCodes() {
    }

    /**
     * 生成 EAN-13 条形码 PNG，使用 {@link BarCodeRequest} 的默认尺寸。
     *
     * @param content 12 位（自动补全校验位）或 13 位数字内容；不能为 {@code null} 或空白
     * @return 编码后的 PNG
     * @throws com.google.zxing.exception.CodeException 当 ZXing 编码失败时抛出
     */
    public static CodeOutput ean13(String content) {
        return encode(BarCodeRequest.builder(content, BarcodeFormat.EAN_13).build());
    }

    /**
     * 生成受支持的一维条形码 PNG。
     *
     * @param request 编码请求；不能为 {@code null}，其 {@link BarCodeRequest#getFormat()} 必须为支持的格式
     * @return 编码后的 PNG
     * @throws IllegalArgumentException 当请求或格式不受支持时抛出
     * @throws com.google.zxing.exception.CodeException 当 ZXing 编码失败时抛出
     */
    public static CodeOutput encode(BarCodeRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        if (!SUPPORTED_FORMATS.contains(request.getFormat())) {
            throw new IllegalArgumentException("format is not a supported one-dimensional barcode: "
                    + request.getFormat());
        }
        Map<EncodeHintType, Object> hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, request.getCharset().name());
        hints.put(EncodeHintType.MARGIN, request.getMargin());
        try {
            BitMatrix matrix = WRITER.encode(request.getContent(), request.getFormat(),
                    request.getWidth(), request.getHeight(), hints);
            return CodeImageSupport.toPng(matrix);
        } catch (WriterException ex) {
            throw new CodeException("Failed to encode barcode", ex);
        }
    }

    /**
     * 从编码图像字节中解析一维条形码。
     *
     * @param bytes  PNG/JPEG 等编码图像字节；不能为 {@code null}
     * @return 解码结果
     * @throws com.google.zxing.exception.CodeException 当 IO 失败或未发现条形码时抛出
     */
    public static CodeResult decode(byte[] bytes) {
        return decode(CodeImageSupport.read(bytes));
    }

    /**
     * 从 {@link BufferedImage} 中解析一维条形码。
     *
     * @param image 图像；不能为 {@code null}
     * @return 解码结果
     * @throws com.google.zxing.exception.CodeException 当未发现条形码时抛出
     */
    public static CodeResult decode(BufferedImage image) {
        Objects.requireNonNull(image, "image must not be null");
        Map<DecodeHintType, Object> hints = new EnumMap<DecodeHintType, Object>(DecodeHintType.class);
        hints.put(DecodeHintType.POSSIBLE_FORMATS, SUPPORTED_FORMATS);
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        MultiFormatReader reader = new MultiFormatReader();
        try {
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image)));
            return CodeResult.from(reader.decode(bitmap, hints));
        } catch (ReaderException ex) {
            throw new CodeException("No supported barcode was found in the image", ex);
        } finally {
            reader.reset();
        }
    }

    /**
     * 从图像文件中解析一维条形码。
     *
     * @param file 图像文件；不能为 {@code null}
     * @return 解码结果
     * @throws com.google.zxing.exception.CodeException 当 IO 失败或未发现条形码时抛出
     */
    public static CodeResult decode(File file) {
        return decode(CodeImageSupport.read(file));
    }

    /**
     * 从图像 {@link Path} 中解析一维条形码。
     *
     * @param path 图像路径；不能为 {@code null}
     * @return 解码结果
     * @throws com.google.zxing.exception.CodeException 当 IO 失败或未发现条形码时抛出
     */
    public static CodeResult decode(Path path) {
        return decode(CodeImageSupport.read(path));
    }

    /**
     * 从字节流中解析一维条形码。调用方负责流的关闭，库不会主动关闭它。
     *
     * @param inputStream 字节流；不能为 {@code null}
     * @return 解码结果
     * @throws com.google.zxing.exception.CodeException 当 IO 失败或未发现条形码时抛出
     */
    public static CodeResult decode(InputStream inputStream) {
        return decode(CodeImageSupport.read(inputStream));
    }
}
