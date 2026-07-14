package com.google.zxing.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * QR Code 支持的输出格式。
 *
 * <p>{@link #PNG} 使用 {@code javax.imageio.ImageIO} 写出 PNG 字节；
 * {@link #SVG} 由 {@code DefaultQrCodeEncoder} 手写 SVG 文本，无对应 {@link javax.imageio.ImageIO} 写入器。
 */
@Getter
@RequiredArgsConstructor
public enum QrCodeImageFormat {

    /** PNG 位图：{@code imageIoName = "png"}，MIME {@code image/png}。 */
    PNG("png", "image/png"),
    /** SVG 矢量图：{@code imageIoName = "svg"}，MIME {@code image/svg+xml}。 */
    SVG("svg", "image/svg+xml");

    /** ImageIO 格式名（如 {@code "png"}）。 */
    private final String imageIoName;
    /** MIME 类型。 */
    private final String mimeType;
}
