package com.google.zxing.model;

import java.awt.image.BufferedImage;
import java.util.Objects;

/**
 * 不可变的 QR Code 编码输出，相对 {@link CodeOutput} 增加了 {@link QrCodeImageFormat} 区分。
 */
public final class QrCodeOutput extends CodeOutput {

    private final QrCodeImageFormat format;

    /**
     * 构造 QR 编码输出。
     *
     * @param bytes         编码字节；不能为 {@code null}
     * @param format        输出格式；不能为 {@code null}
     * @param width         像素宽度
     * @param height        像素高度
     * @param bufferedImage SVG 时为 {@code null}
     */
    public QrCodeOutput(byte[] bytes, QrCodeImageFormat format, int width, int height,
            BufferedImage bufferedImage) {
        super(bytes, Objects.requireNonNull(format, "format must not be null").getMimeType(), width, height,
                bufferedImage);
        this.format = format;
    }

    /**
     * @return 输出格式（PNG / SVG）
     */
    public QrCodeImageFormat getFormat() {
        return format;
    }

}
