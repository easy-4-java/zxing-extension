package com.google.zxing.model;

import java.awt.image.BufferedImage;
import java.util.Objects;

/**
 * Immutable QR Code encoding output. Extends {@link CodeOutput} with a
 * {@link QrCodeImageFormat} discriminator.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see CodeOutput
 * @see QrCodeImageFormat
 */
public final class QrCodeOutput extends CodeOutput {

    private final QrCodeImageFormat format;

    /**
     * Constructs a QR encoding output.
     *
     * @param bytes         the encoded bytes; must not be {@code null}
     * @param format        the output format; must not be {@code null}
     * @param width         the width in pixels
     * @param height        the height in pixels
     * @param bufferedImage the raster image; {@code null} for SVG output
     */
    public QrCodeOutput(byte[] bytes, QrCodeImageFormat format, int width, int height,
            BufferedImage bufferedImage) {
        super(bytes, Objects.requireNonNull(format, "format must not be null").getMimeType(), width, height,
                bufferedImage);
        this.format = format;
    }

    /**
     * Returns the output format (PNG or SVG).
     *
     * @return the {@link QrCodeImageFormat}
     */
    public QrCodeImageFormat getFormat() {
        return format;
    }

}
