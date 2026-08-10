package com.google.zxing.model;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import com.google.zxing.exception.QrCodeErrorCode;
import com.google.zxing.exception.QrCodeException;
import lombok.Getter;

/**
 * Immutable QR Code decode request.
 *
 * <p>Input sources (mutually exclusive; at least one must be provided):</p>
 * <ul>
 *     <li>{@link #from(byte[])};</li>
 *     <li>{@link #from(BufferedImage)};</li>
 *     <li>{@link #from(File)} / {@link #from(Path)} &mdash; reads bytes
 *         internally;</li>
 *     <li>{@link #from(InputStream)} &mdash; reads bytes internally; the
 *         library does <strong>not</strong> close the caller's stream.</li>
 * </ul>
 *
 * <p>Defaults: charset = UTF-8; {@code tryHarder = true};
 * {@code alsoInverted = true}; {@code maxInputBytes = 10 MiB};
 * {@code maxPixels = 16,777,216}.</p>
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see QrCodeDecodeResult
 * @see com.google.zxing.QrCodeDecoder
 */
@Getter
public final class QrCodeDecodeRequest {

    private final byte[] bytes;
    private final BufferedImage image;
    private final Charset charset;
    private final boolean multiple;
    private final boolean tryHarder;
    private final boolean alsoInverted;
    private final boolean pureBarcode;
    private final int maxInputBytes;
    private final long maxPixels;

    private QrCodeDecodeRequest(Builder builder) {
        if (Objects.isNull(builder.bytes) && Objects.isNull(builder.image)) {
            throw new IllegalArgumentException("bytes or image must be provided");
        }
        this.bytes = Objects.isNull(builder.bytes) ? null : builder.bytes.clone();
        this.image = builder.image;
        this.charset = Objects.requireNonNull(builder.charset, "charset must not be null");
        this.multiple = builder.multiple;
        this.tryHarder = builder.tryHarder;
        this.alsoInverted = builder.alsoInverted;
        this.pureBarcode = builder.pureBarcode;
        this.maxInputBytes = builder.maxInputBytes;
        this.maxPixels = builder.maxPixels;
    }

    /**
     * Returns a defensive copy of the input bytes.
     *
     * @return the bytes, or {@code null} if an image source was provided
     */
    public byte[] getBytes() {
        return Objects.isNull(bytes) ? null : bytes.clone();
    }

    /**
     * Creates a new {@link Builder} from encoded image bytes.
     *
     * @param bytes the encoded image bytes
     * @return a new builder instance
     */
    public static Builder from(byte[] bytes) {
        return new Builder(bytes, null);
    }

    /**
     * Creates a new {@link Builder} from a {@link BufferedImage}.
     *
     * @param image the raster image
     * @return a new builder instance
     */
    public static Builder from(BufferedImage image) {
        return new Builder(null, image);
    }

    /**
     * Creates a new {@link Builder} from an image {@link File}.
     *
     * @param file the image file; must not be {@code null}
     * @return a new builder instance
     * @throws QrCodeException if the file cannot be read
     */
    public static Builder from(File file) {
        Objects.requireNonNull(file, "file must not be null");
        return from(file.toPath());
    }

    /**
     * Creates a new {@link Builder} from an image {@link Path}.
     *
     * @param path the image path; must not be {@code null}
     * @return a new builder instance
     * @throws QrCodeException if the path cannot be read
     */
    public static Builder from(Path path) {
        Objects.requireNonNull(path, "path must not be null");
        try {
            return from(Files.readAllBytes(path));
        } catch (IOException ex) {
            throw new QrCodeException(QrCodeErrorCode.QRCODE_RENDER_FAILED, "Failed to read QR code image", ex);
        }
    }

    /**
     * Creates a new {@link Builder} from an {@link InputStream}. The library
     * does <strong>not</strong> close the stream.
     *
     * @param inputStream the byte stream; must not be {@code null}
     * @return a new builder instance
     * @throws QrCodeException if reading fails
     */
    public static Builder from(InputStream inputStream) {
        Objects.requireNonNull(inputStream, "inputStream must not be null");
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                if (length > 0) {
                    outputStream.write(buffer, 0, length);
                }
            }
            return from(outputStream.toByteArray());
        } catch (IOException ex) {
            throw new QrCodeException(QrCodeErrorCode.QRCODE_RENDER_FAILED, "Failed to read QR code stream", ex);
        }
    }

    /**
     * Fluent builder for {@link QrCodeDecodeRequest} instances.
     */
    public static final class Builder {

        private final byte[] bytes;
        private final BufferedImage image;
        private Charset charset = StandardCharsets.UTF_8;
        private boolean multiple;
        private boolean tryHarder = true;
        private boolean alsoInverted = true;
        private boolean pureBarcode;
        private int maxInputBytes = 10 * 1024 * 1024;
        private long maxPixels = 16_777_216L;

        private Builder(byte[] bytes, BufferedImage image) {
            this.bytes = bytes;
            this.image = image;
        }

        /**
         * Sets the character set for decoding.
         *
         * @param charset the charset; must not be {@code null}
         * @return this builder
         */
        public Builder charset(Charset charset) {
            this.charset = charset;
            return this;
        }

        /**
         * Enables or disables multi-QR decoding.
         *
         * @param multiple {@code true} to enable multi-QR decoding
         * @return this builder
         */
        public Builder multiple(boolean multiple) {
            this.multiple = multiple;
            return this;
        }

        /**
         * Enables or disables the {@code TRY_HARDER} decode hint.
         *
         * @param tryHarder {@code true} to enable
         * @return this builder
         */
        public Builder tryHarder(boolean tryHarder) {
            this.tryHarder = tryHarder;
            return this;
        }

        /**
         * Enables or disables the {@code ALSO_INVERTED} decode hint.
         *
         * @param alsoInverted {@code true} to enable
         * @return this builder
         */
        public Builder alsoInverted(boolean alsoInverted) {
            this.alsoInverted = alsoInverted;
            return this;
        }

        /**
         * Enables or disables the {@code PURE_BARCODE} decode hint.
         *
         * @param pureBarcode {@code true} to enable
         * @return this builder
         */
        public Builder pureBarcode(boolean pureBarcode) {
            this.pureBarcode = pureBarcode;
            return this;
        }

        /**
         * Sets the maximum input byte size.
         *
         * @param maxInputBytes the byte limit; must be positive
         * @return this builder
         */
        public Builder maxInputBytes(int maxInputBytes) {
            this.maxInputBytes = maxInputBytes;
            return this;
        }

        /**
         * Sets the maximum pixel count for the decoded image.
         *
         * @param maxPixels the pixel limit; must be positive
         * @return this builder
         */
        public Builder maxPixels(long maxPixels) {
            this.maxPixels = maxPixels;
            return this;
        }

        /**
         * Validates and builds an immutable {@link QrCodeDecodeRequest}.
         *
         * @return an immutable request
         * @throws IllegalArgumentException if decode limits are non-positive
         */
        public QrCodeDecodeRequest build() {
            if (maxInputBytes <= 0 || maxPixels <= 0) {
                throw new IllegalArgumentException("decode limits must be positive");
            }
            return new QrCodeDecodeRequest(this);
        }
    }
}
