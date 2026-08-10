package com.google.zxing.model;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import com.google.zxing.frame.QrCodeFrame;
import lombok.Getter;

/**
 * Immutable QR Code encoding request.
 *
 * <p>Created via {@link #builder(String)}. Defaults:</p>
 * <ul>
 *     <li>Dimensions: {@link #DEFAULT_SIZE} (256 pixels);</li>
 *     <li>Margin: 2;</li>
 *     <li>Charset: {@link StandardCharsets#UTF_8};</li>
 *     <li>Error correction: {@link ErrorCorrectionLevel#M};</li>
 *     <li>Format: {@link QrCodeImageFormat#PNG};</li>
 *     <li>Style: {@link QrCodeStyle#monochrome()};</li>
 *     <li>Logo: {@code null}; calling {@link Builder#logo(QrCodeLogo)}
 *         automatically upgrades error correction to
 *         {@link ErrorCorrectionLevel#H} to preserve redundancy.</li>
 *     <li>Self-check: disabled by default.</li>
 * </ul>
 *
 * <p>Validation:</p>
 * <ul>
 *     <li>{@code content} must not be blank;</li>
 *     <li>{@code width} and {@code height} must be positive;
 *         {@code margin} must be non-negative;</li>
 *     <li>{@code build()} verifies that {@code charset},
 *         {@code errorCorrectionLevel}, {@code format} and {@code style}
 *         are not {@code null}.</li>
 * </ul>
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see QrCodeOutput
 * @see com.google.zxing.QrCodeEncoder
 */
@Getter
public final class QrCodeRequest {

    /** Default output width and height in pixels. */
    public static final int DEFAULT_SIZE = 256;

    private final String content;
    private final int width;
    private final int height;
    private final int margin;
    private final Charset charset;
    private final ErrorCorrectionLevel errorCorrectionLevel;
    private final QrCodeImageFormat format;
    private final QrCodeStyle style;
    private final QrCodeLogo logo;
    private final QrCodeFrame frame;
    private final boolean selfCheck;

    private QrCodeRequest(Builder builder) {
        if (StringUtils.isBlank(builder.content)) {
            throw new IllegalArgumentException("content must not be blank");
        }
        if (builder.width <= 0 || builder.height <= 0 || builder.margin < 0) {
            throw new IllegalArgumentException("width and height must be positive and margin must not be negative");
        }
        this.content = builder.content;
        this.width = builder.width;
        this.height = builder.height;
        this.margin = builder.margin;
        this.charset = builder.charset;
        this.errorCorrectionLevel = builder.errorCorrectionLevel;
        this.format = builder.format;
        this.style = builder.style;
        this.logo = builder.logo;
        this.frame = builder.frame;
        this.selfCheck = builder.selfCheck;
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
     * Fluent builder for {@link QrCodeRequest} instances.
     */
    public static final class Builder {

        private final String content;
        private int width = DEFAULT_SIZE;
        private int height = DEFAULT_SIZE;
        private int margin = 2;
        private Charset charset = StandardCharsets.UTF_8;
        private ErrorCorrectionLevel errorCorrectionLevel = ErrorCorrectionLevel.M;
        private QrCodeImageFormat format = QrCodeImageFormat.PNG;
        private QrCodeStyle style = QrCodeStyle.monochrome();
        private QrCodeLogo logo;
        private QrCodeFrame frame;
        private boolean selfCheck;

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
         * Sets the character set for encoding.
         *
         * @param charset the charset; must not be {@code null}
         * @return this builder
         */
        public Builder charset(Charset charset) {
            this.charset = charset;
            return this;
        }

        /**
         * Sets the error correction level.
         *
         * @param errorCorrectionLevel the level; must not be {@code null}
         * @return this builder
         */
        public Builder errorCorrectionLevel(ErrorCorrectionLevel errorCorrectionLevel) {
            this.errorCorrectionLevel = errorCorrectionLevel;
            return this;
        }

        /**
         * Sets the output format.
         *
         * @param format the format; must not be {@code null}
         * @return this builder
         */
        public Builder format(QrCodeImageFormat format) {
            this.format = format;
            return this;
        }

        /**
         * Sets the colour style.
         *
         * @param style the style; must not be {@code null}
         * @return this builder
         */
        public Builder style(QrCodeStyle style) {
            this.style = style;
            return this;
        }

        /**
         * Sets the logo. When a non-{@code null} logo is provided, the error
         * correction level is automatically upgraded to
         * {@link ErrorCorrectionLevel#H} to preserve redundancy.
         *
         * @param logo the logo; may be {@code null}
         * @return this builder
         */
        public Builder logo(QrCodeLogo logo) {
            this.logo = logo;
            if (Objects.nonNull(logo)) {
                this.errorCorrectionLevel = ErrorCorrectionLevel.H;
            }
            return this;
        }

        /**
         * Sets an optional decorative outer frame.
         *
         * @param frame the frame; may be {@code null}
         * @return this builder
         */
        public Builder frame(QrCodeFrame frame) {
            this.frame = frame;
            return this;
        }

        /**
         * Enables or disables self-check (round-trip decode after encoding).
         *
         * @param selfCheck {@code true} to enable self-check
         * @return this builder
         */
        public Builder selfCheck(boolean selfCheck) {
            this.selfCheck = selfCheck;
            return this;
        }

        /**
         * Validates and builds an immutable {@link QrCodeRequest}.
         *
         * @return an immutable request
         * @throws IllegalArgumentException if any required field is {@code null}
         */
        public QrCodeRequest build() {
            if (Objects.isNull(charset) || Objects.isNull(errorCorrectionLevel)
                    || Objects.isNull(format) || Objects.isNull(style)) {
                throw new IllegalArgumentException("charset, errorCorrectionLevel, format and style must not be null");
            }
            return new QrCodeRequest(this);
        }
    }
}
