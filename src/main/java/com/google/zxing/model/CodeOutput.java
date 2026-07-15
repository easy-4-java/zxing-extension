package com.google.zxing.model;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;

/**
 * 不可变的编码结果，是 QR / Aztec / BarCode 共享的输出类型。
 *
 * <p>约定：
 * <ul>
 *     <li>{@link #getBytes()} 每次都返回内部字节数组的防御性拷贝。</li>
 *     <li>{@link #writeTo(OutputStream)} 不会关闭调用方持有的输出流。</li>
 *     <li>{@link #image()} 在仅有 SVG 等无栅格表示时返回 {@link Optional#empty()}。</li>
 * </ul>
 */
public class CodeOutput {

    /** 编码字节；外部不可见。 */
    private final byte[] bytes;
    /** MIME 类型；非空。 */
    private final String mimeType;
    /** 输出宽度（像素），严格大于 0。 */
    private final int width;
    /** 输出高度（像素），严格大于 0。 */
    private final int height;
    /** 可选栅格图像，非 PNG 等栅格格式为 {@code null}。 */
    private final BufferedImage bufferedImage;

    /**
     * 构造输出。
     *
     * @param bytes         编码字节；不能为 {@code null}
     * @param mimeType      MIME 类型；不能为 {@code null}
     * @param width         像素宽度，必须大于 0
     * @param height        像素高度，必须大于 0
     * @param bufferedImage 可选 BufferedImage；可为 {@code null}
     */
    public CodeOutput(byte[] bytes, String mimeType, int width, int height, BufferedImage bufferedImage) {
        this.bytes = Objects.requireNonNull(bytes, "bytes must not be null").clone();
        this.mimeType = Objects.requireNonNull(mimeType, "mimeType must not be null");
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("width and height must be positive");
        }
        this.width = width;
        this.height = height;
        this.bufferedImage = bufferedImage;
    }

    /**
     * @return 内部字节数组的防御性拷贝
     */
    public byte[] getBytes() {
        return bytes.clone();
    }

    /**
     * @return MIME 类型（如 {@code image/png}）
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * @return 像素宽度
     */
    public int getWidth() {
        return width;
    }

    /**
     * @return 像素高度
     */
    public int getHeight() {
        return height;
    }

    /**
     * @return 可选 {@link BufferedImage}；无栅格（如 SVG）时为空
     */
    public Optional<BufferedImage> image() {
        return Optional.ofNullable(bufferedImage);
    }

    /**
     * @return 标准 Base64 编码（无换行）
     */
    public String base64() {
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * @return 形如 {@code data:image/png;base64,...} 的 Data URI
     */
    public String dataUri() {
        return "data:" + mimeType + ";base64," + base64();
    }

    /**
     * 将编码字节写入调用方持有的输出流；不会关闭该流。
     *
     * @param outputStream 目标输出流；不能为 {@code null}
     * @throws IOException 底层写入失败时抛出
     */
    public void writeTo(OutputStream outputStream) throws IOException {
        Objects.requireNonNull(outputStream, "outputStream must not be null");
        outputStream.write(bytes);
        outputStream.flush();
    }
}
