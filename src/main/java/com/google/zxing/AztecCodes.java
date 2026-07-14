package com.google.zxing;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;

import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.exception.CodeException;
import com.google.zxing.model.AztecCodeRequest;
import com.google.zxing.model.CodeOutput;
import com.google.zxing.model.CodeResult;
import com.google.zxing.source.BufferedImageLuminanceSource;
import com.google.zxing.source.MatrixToImageWriter;

/**
 * Aztec 码静态门面。
 *
 * <p>提供生成与解码的快捷入口，使用 ZXing 的 {@link MultiFormatWriter} /
 * {@link MultiFormatReader}。纠错参数 {@code errorCorrectionPercent} 取值 1-100，
 * 与 QR Code 的 L/M/Q/H 语义不同。线程安全：内部仅调用 ZXing 的无状态 API。
 */
public final class AztecCodes {

    private static final MultiFormatWriter WRITER = new MultiFormatWriter();

    private AztecCodes() {
    }

    /**
     * 使用 {@link AztecCodeRequest} 默认值生成 Aztec 码 PNG。
     *
     * @param content 要编码的内容；不能为 {@code null} 或空白
     * @return 编码后的 PNG
     * @throws com.google.zxing.exception.CodeException 当 ZXing 编码失败时抛出
     */
    public static CodeOutput encode(String content) {
        return encode(AztecCodeRequest.builder(content).build());
    }

    /**
     * 根据显式请求生成 Aztec 码 PNG。会在输入区四周留出 {@code request.getMargin()}
     * 像素的 quiet zone。
     *
     * @param request 编码请求；不能为 {@code null}
     * @return 编码后的 PNG
     * @throws com.google.zxing.exception.CodeException 当 ZXing 编码失败时抛出
     */
    public static CodeOutput encode(AztecCodeRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        Map<EncodeHintType, Object> hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, request.getCharset().name());
        hints.put(EncodeHintType.ERROR_CORRECTION, request.getErrorCorrectionPercent());
        try {
            BitMatrix matrix = WRITER.encode(request.getContent(), BarcodeFormat.AZTEC,
                    request.getWidth(), request.getHeight(), hints);
            if (request.getMargin() > 0) {
                matrix = MatrixToImageWriter.updateBit(matrix, request.getMargin());
            }
            return CodeImageSupport.toPng(matrix);
        } catch (WriterException ex) {
            throw new CodeException("Failed to encode Aztec code", ex);
        }
    }

    /**
     * 从编码图像字节中解析 Aztec 码。
     *
     * @param bytes  PNG/JPEG 等编码图像字节；不能为 {@code null}
     * @return 解码结果
     * @throws com.google.zxing.exception.CodeException 当 IO 失败或未发现 Aztec 码时抛出
     */
    public static CodeResult decode(byte[] bytes) {
        return decode(CodeImageSupport.read(bytes));
    }

    /**
     * 从 {@link BufferedImage} 中解析 Aztec 码。
     *
     * @param image 图像；不能为 {@code null}
     * @return 解码结果
     * @throws com.google.zxing.exception.CodeException 当未发现 Aztec 码时抛出
     */
    public static CodeResult decode(BufferedImage image) {
        Objects.requireNonNull(image, "image must not be null");
        Map<DecodeHintType, Object> hints = new EnumMap<DecodeHintType, Object>(DecodeHintType.class);
        hints.put(DecodeHintType.POSSIBLE_FORMATS, EnumSet.of(BarcodeFormat.AZTEC));
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        MultiFormatReader reader = new MultiFormatReader();
        try {
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image)));
            return CodeResult.from(reader.decode(bitmap, hints));
        } catch (ReaderException ex) {
            throw new CodeException("No Aztec code was found in the image", ex);
        } finally {
            reader.reset();
        }
    }

    /**
     * 从图像文件中解析 Aztec 码。
     *
     * @param file 图像文件；不能为 {@code null}
     * @return 解码结果
     * @throws com.google.zxing.exception.CodeException 当 IO 失败或未发现 Aztec 码时抛出
     */
    public static CodeResult decode(File file) {
        return decode(CodeImageSupport.read(file));
    }

    /**
     * 从图像 {@link Path} 中解析 Aztec 码。
     *
     * @param path 图像路径；不能为 {@code null}
     * @return 解码结果
     * @throws com.google.zxing.exception.CodeException 当 IO 失败或未发现 Aztec 码时抛出
     */
    public static CodeResult decode(Path path) {
        return decode(CodeImageSupport.read(path));
    }

    /**
     * 从字节流中解析 Aztec 码。调用方负责流的关闭，库不会主动关闭它。
     *
     * @param inputStream 字节流；不能为 {@code null}
     * @return 解码结果
     * @throws com.google.zxing.exception.CodeException 当 IO 失败或未发现 Aztec 码时抛出
     */
    public static CodeResult decode(InputStream inputStream) {
        return decode(CodeImageSupport.read(inputStream));
    }
}
