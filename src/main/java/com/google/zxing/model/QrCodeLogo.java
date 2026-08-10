package com.google.zxing.model;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Objects;

import lombok.Getter;

/**
 * Immutable QR Code logo overlay configuration.
 *
 * <p>Defaults:</p>
 * <ul>
 *     <li>{@code padding = 4};</li>
 *     <li>{@code cornerRadius = 8};</li>
 *     <li>{@code backgroundColor = Color.WHITE}.</li>
 * </ul>
 *
 * <p>The logo is subject to a 20% width/height limit relative to the QR
 * region ({@code DefaultQrCodeEncoder#MAX_LOGO_RATIO}).</p>
 *
 * <p>Validation: {@code image} must not be {@code null}; {@code width},
 * {@code height}, {@code padding} and {@code cornerRadius} must be
 * non-negative; {@code backgroundColor} must not be {@code null}.</p>
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see QrCodeRequest
 */
@Getter
public final class QrCodeLogo {

    private final BufferedImage image;
    private final int width;
    private final int height;
    private final int padding;
    private final int cornerRadius;
    private final Color backgroundColor;

    private QrCodeLogo(Builder builder) {
        this.image = Objects.requireNonNull(builder.image, "image must not be null");
        if (builder.width < 0 || builder.height < 0 || builder.padding < 0 || builder.cornerRadius < 0) {
            throw new IllegalArgumentException("logo dimensions, padding and cornerRadius must not be negative");
        }
        this.width = builder.width;
        this.height = builder.height;
        this.padding = builder.padding;
        this.cornerRadius = builder.cornerRadius;
        this.backgroundColor = Objects.requireNonNull(builder.backgroundColor, "backgroundColor must not be null");
    }

    /**
     * Creates a new {@link Builder} with the given logo image.
     *
     * @param image the logo image; must not be {@code null}
     * @return a new builder instance
     */
    public static Builder builder(BufferedImage image) {
        return new Builder(image);
    }

    /**
     * Fluent builder for {@link QrCodeLogo} instances.
     */
    public static final class Builder {

        private final BufferedImage image;
        private int width;
        private int height;
        private int padding = 4;
        private int cornerRadius = 8;
        private Color backgroundColor = Color.WHITE;

        private Builder(BufferedImage image) {
            this.image = image;
        }

        /**
         * Sets the explicit logo dimensions. A value of 0 means the encoder
         * will use the smaller of the original image size and the 20% limit.
         *
         * @param width  the width in pixels; non-negative
         * @param height the height in pixels; non-negative
         * @return this builder
         */
        public Builder size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        /**
         * Sets the padding around the logo.
         *
         * @param padding the padding in pixels; non-negative
         * @return this builder
         */
        public Builder padding(int padding) {
            this.padding = padding;
            return this;
        }

        /**
         * Sets the corner radius of the background rectangle.
         *
         * @param cornerRadius the corner radius in pixels; non-negative
         * @return this builder
         */
        public Builder cornerRadius(int cornerRadius) {
            this.cornerRadius = cornerRadius;
            return this;
        }

        /**
         * Sets the background colour drawn behind the logo.
         *
         * @param backgroundColor the background colour; must not be {@code null}
         * @return this builder
         */
        public Builder backgroundColor(Color backgroundColor) {
            this.backgroundColor = backgroundColor;
            return this;
        }

        /**
         * Validates and builds an immutable {@link QrCodeLogo}.
         *
         * @return an immutable logo configuration
         */
        public QrCodeLogo build() {
            return new QrCodeLogo(this);
        }
    }
}
