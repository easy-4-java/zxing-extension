package com.google.zxing.model;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.google.zxing.BarcodeFormat;

import lombok.Getter;

/**
 * Immutable one-dimensional barcode encoding request.
 *
 * <p>Defaults:</p>
 * <ul>
 *     <li>Dimensions: {@link #DEFAULT_WIDTH} &times; {@link #DEFAULT_HEIGHT}
 *         (300 &times; 100 pixels);</li>
 *     <li>Margin: 10 pixels;</li>
 *     <li>Charset: UTF-8.</li>
 * </ul>
 *
 * <p>The {@link BarcodeFormat} must be specified explicitly via
 * {@link #builder(String, BarcodeFormat)}.</p>
 *
 * <p>Validation: {@code content} must not be blank; {@code width} and
 * {@code height} must be positive; {@code margin} must be non-negative;
 * {@code format} and {@code charset} must not be {@code null}.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see com.google.zxing.BarCodes
 */
@Getter
public final class BarCodeRequest {

    /** Default output width in pixels. */
    public static final int DEFAULT_WIDTH = 300;
    /** Default output height in pixels. */
    public static final int DEFAULT_HEIGHT = 100;

    private final String content;
    private final BarcodeFormat format;
    private final int width;
    private final int height;
    private final int margin;
    private final Charset charset;

    private BarCodeRequest(Builder builder) {
        if (StringUtils.isBlank(builder.content)) {
            throw new IllegalArgumentException("content must not be blank");
        }
        if (builder.width <= 0 || builder.height <= 0 || builder.margin < 0) {
            throw new IllegalArgumentException("width and height must be positive and margin must not be negative");
        }
        this.content = builder.content;
        this.format = Objects.requireNonNull(builder.format, "format must not be null");
        this.width = builder.width;
        this.height = builder.height;
        this.margin = builder.margin;
        this.charset = Objects.requireNonNull(builder.charset, "charset must not be null");
    }

    /**
     * Creates a new {@link Builder} with the given content and barcode format.
     *
     * @param content the payload to encode
     * @param format  the barcode format; must not be {@code null}
     * @return a new builder instance
     */
    public static Builder builder(String content, BarcodeFormat format) {
        return new Builder(content, format);
    }

    /**
     * Fluent builder for {@link BarCodeRequest} instances.
     */
    public static final class Builder {

        private final String content;
        private final BarcodeFormat format;
        private int width = DEFAULT_WIDTH;
        private int height = DEFAULT_HEIGHT;
        private int margin = 10;
        private Charset charset = StandardCharsets.UTF_8;

        private Builder(String content, BarcodeFormat format) {
            this.content = content;
            this.format = format;
        }

        /**
         * Sets the output dimensions.
         *
         * @param width  the width in pixels; must be positive
         * @param height the height in pixels; must be positive
         * @return this builder
         */
        public Builder size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        /**
         * Sets the quiet-zone margin.
         *
         * @param margin the margin in pixels; must be non-negative
         * @return this builder
         */
        public Builder margin(int margin) {
            this.margin = margin;
            return this;
        }

        /**
         * Sets the character set used for encoding.
         *
         * @param charset the charset; must not be {@code null}
         * @return this builder
         */
        public Builder charset(Charset charset) {
            this.charset = charset;
            return this;
        }

        /**
         * Validates and builds an immutable {@link BarCodeRequest}.
         *
         * @return an immutable request
         */
        public BarCodeRequest build() {
            return new BarCodeRequest(this);
        }
    }
}
