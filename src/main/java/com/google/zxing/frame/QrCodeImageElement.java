package com.google.zxing.frame;

import java.awt.image.BufferedImage;
import java.util.Objects;

import lombok.Getter;

/**
 * An arbitrary image element drawn on a {@link QrCodeFrame} canvas.
 *
 * <p>Validation: the {@code image} must not be {@code null};
 * {@code width} and {@code height} must be strictly positive.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see QrCodeFrameElement
 * @see QrCodeFrame
 */
@Getter
public final class QrCodeImageElement implements QrCodeFrameElement {

    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final int zIndex;
    private final BufferedImage image;

    private QrCodeImageElement(Builder builder) {
        this.x = builder.x;
        this.y = builder.y;
        this.width = builder.width;
        this.height = builder.height;
        this.zIndex = builder.zIndex;
        this.image = Objects.requireNonNull(builder.image, "image must not be null");
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("image element width and height must be positive");
        }
    }

    /**
     * Creates a new {@link Builder} for an image element.
     *
     * @param image the {@link BufferedImage} to render; must not be {@code null}
     * @return a new builder instance
     */
    public static Builder builder(BufferedImage image) {
        return new Builder(image);
    }

    /**
     * Fluent builder for {@link QrCodeImageElement} instances.
     */
    public static final class Builder {

        private final BufferedImage image;
        private int x;
        private int y;
        private int width;
        private int height;
        private int zIndex;

        private Builder(BufferedImage image) {
            this.image = image;
        }

        /**
         * Sets the bounding rectangle for this image element.
         *
         * @param x      the left X coordinate in pixels
         * @param y      the top Y coordinate in pixels
         * @param width  the width in pixels; must be positive
         * @param height the height in pixels; must be positive
         * @return this builder
         */
        public Builder bounds(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            return this;
        }

        /**
         * Sets the z-order index; higher values render on top of lower values.
         *
         * @param zIndex the z-order index
         * @return this builder
         */
        public Builder zIndex(int zIndex) {
            this.zIndex = zIndex;
            return this;
        }

        /**
         * Validates and builds an immutable {@link QrCodeImageElement}.
         *
         * @return an immutable image element
         */
        public QrCodeImageElement build() {
            return new QrCodeImageElement(this);
        }
    }
}
