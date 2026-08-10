package com.google.zxing.model;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.google.zxing.aztec.encoder.Encoder;

import lombok.Getter;

/**
 * Immutable Aztec barcode encoding request.
 *
 * <p>Defaults:</p>
 * <ul>
 *     <li>Width and height: {@link #DEFAULT_SIZE} (258 pixels);</li>
 *     <li>Error correction percent: {@link #DEFAULT_ERROR_CORRECTION_PERCENT}
 *         (from ZXing {@code Encoder.DEFAULT_EC_PERCENT});</li>
 *     <li>Margin: {@link #DEFAULT_MARGIN} (2 pixels);</li>
 *     <li>Charset: UTF-8.</li>
 * </ul>
 *
 * <p>Validation: {@code content} must not be blank; {@code width} and
 * {@code height} must be positive; {@code errorCorrectionPercent} must be in
 * the range [1, 100]; {@code margin} must be non-negative.</p>
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see com.google.zxing.AztecCodes
 */
@Getter
public final class AztecCodeRequest {

    /** Default output width and height in pixels. */
    public static final int DEFAULT_SIZE = 258;
    /** Default error correction percent; typically the ZXing upstream default. */
    public static final int DEFAULT_ERROR_CORRECTION_PERCENT = Encoder.DEFAULT_EC_PERCENT;
    /** Default quiet-zone margin in pixels. */
    public static final int DEFAULT_MARGIN = 2;

    private final String content;
    private final int width;
    private final int height;
    private final int errorCorrectionPercent;
    private final int margin;
    private final Charset charset;

    private AztecCodeRequest(Builder builder) {
        if (StringUtils.isBlank(builder.content)) {
            throw new IllegalArgumentException("content must not be blank");
        }
        if (builder.width <= 0 || builder.height <= 0) {
            throw new IllegalArgumentException("width and height must be positive");
        }
        if (builder.errorCorrectionPercent <= 0 || builder.errorCorrectionPercent > 100) {
            throw new IllegalArgumentException("errorCorrectionPercent must be between 1 and 100");
        }
        if (builder.margin < 0) {
            throw new IllegalArgumentException("margin must not be negative");
        }
        this.content = builder.content;
        this.width = builder.width;
        this.height = builder.height;
        this.errorCorrectionPercent = builder.errorCorrectionPercent;
        this.margin = builder.margin;
        this.charset = Objects.requireNonNull(builder.charset, "charset must not be null");
    }

    /**
     * Creates a new {@link Builder} with the given content.
     *
     * @param content the payload to encode; must not be {@code null} or blank
     * @return a new builder instance
     */
    public static Builder builder(String content) {
        return new Builder(content);
    }

    /**
     * Fluent builder for {@link AztecCodeRequest} instances.
     */
    public static final class Builder {

        private final String content;
        private int width = DEFAULT_SIZE;
        private int height = DEFAULT_SIZE;
        private int errorCorrectionPercent = DEFAULT_ERROR_CORRECTION_PERCENT;
        private int margin = DEFAULT_MARGIN;
        private Charset charset = StandardCharsets.UTF_8;

        private Builder(String content) {
            this.content = content;
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
         * Sets the error correction percent.
         *
         * @param errorCorrectionPercent the error correction percentage (1-100)
         * @return this builder
         */
        public Builder errorCorrectionPercent(int errorCorrectionPercent) {
            this.errorCorrectionPercent = errorCorrectionPercent;
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
         * Validates and builds an immutable {@link AztecCodeRequest}.
         *
         * @return an immutable request
         */
        public AztecCodeRequest build() {
            return new AztecCodeRequest(this);
        }
    }
}
