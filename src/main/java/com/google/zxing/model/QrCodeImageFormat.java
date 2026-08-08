package com.google.zxing.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Supported output formats for QR Code encoding.
 *
 * <p>{@link #PNG} uses {@code javax.imageio.ImageIO} to write PNG bytes;
 * {@link #SVG} is hand-written by {@code DefaultQrCodeEncoder} and has no
 * corresponding {@link javax.imageio.ImageIO} writer.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see QrCodeOutput
 */
@Getter
@RequiredArgsConstructor
public enum QrCodeImageFormat {

    /** PNG raster format: {@code imageIoName = "png"}, MIME {@code "image/png"}. */
    PNG("png", "image/png"),
    /** SVG vector format: {@code imageIoName = "svg"}, MIME {@code "image/svg+xml"}. */
    SVG("svg", "image/svg+xml");

    /** The ImageIO format name (e.g. {@code "png"}). */
    private final String imageIoName;
    /** The MIME type (e.g. {@code "image/png"}). */
    private final String mimeType;
}
