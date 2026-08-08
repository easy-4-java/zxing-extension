package com.google.zxing.model;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable encoding output shared across QR, Aztec and one-dimensional
 * barcode paths.
 *
 * <p>Contracts:</p>
 * <ul>
 *     <li>{@link #getBytes()} returns a defensive copy of the internal byte
 *         array on every call.</li>
 *     <li>{@link #writeTo(OutputStream)} does <strong>not</strong> close the
 *         caller-supplied output stream.</li>
 *     <li>{@link #image()} returns {@link Optional#empty()} when no raster
 *         representation is available (e.g. SVG output).</li>
 * </ul>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see QrCodeOutput
 * @see CodeResult
 */
public class CodeOutput {

    /** Encoded bytes; not externally visible. */
    private final byte[] bytes;
    /** MIME type; never {@code null}. */
    private final String mimeType;
    /** Output width in pixels; strictly positive. */
    private final int width;
    /** Output height in pixels; strictly positive. */
    private final int height;
    /** Optional raster image; {@code null} for non-raster formats such as SVG. */
    private final BufferedImage bufferedImage;

    /**
     * Constructs an output instance.
     *
     * @param bytes         the encoded bytes; must not be {@code null}
     * @param mimeType      the MIME type; must not be {@code null}
     * @param width         the width in pixels; must be positive
     * @param height        the height in pixels; must be positive
     * @param bufferedImage optional {@link BufferedImage}; may be {@code null}
     */
    public CodeOutput(byte[] bytes, String mimeType, int width, int height, BufferedImage bufferedImage) {
        this.bytes = Objects.requireNonNull(bytes, "bytes must not be null").clone();
        this.mimeType = Objects.requireNonNull(mimeType, "mimeType must not be null");
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("width and height must be positive");
        }
        this.width = width;
        this.height = height;
        this.bufferedImage = bufferedImage;
    }

    /**
     * Returns a defensive copy of the encoded bytes.
     *
     * @return a new byte array containing the encoded data
     */
    public byte[] getBytes() {
        return bytes.clone();
    }

    /**
     * Returns the MIME type (e.g. {@code "image/png"}).
     *
     * @return the MIME type string
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * Returns the output width in pixels.
     *
     * @return the width
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the output height in pixels.
     *
     * @return the height
     */
    public int getHeight() {
        return height;
    }

    /**
     * Returns the optional raster {@link BufferedImage}.
     *
     * @return an {@link Optional} containing the image, or empty for
     *         non-raster formats such as SVG
     */
    public Optional<BufferedImage> image() {
        return Optional.ofNullable(bufferedImage);
    }

    /**
     * Returns the standard Base64 encoding of the output bytes (no line breaks).
     *
     * @return the Base64-encoded string
     */
    public String base64() {
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * Returns a data URI of the form {@code data:<mime>;base64,...}.
     *
     * @return the data URI string
     */
    public String dataUri() {
        return "data:" + mimeType + ";base64," + base64();
    }

    /**
     * Writes the encoded bytes to the caller-owned output stream. The stream
     * is <strong>not</strong> closed by this method.
     *
     * @param outputStream the target output stream; must not be {@code null}
     * @throws IOException if the underlying write fails
     */
    public void writeTo(OutputStream outputStream) throws IOException {
        Objects.requireNonNull(outputStream, "outputStream must not be null");
        outputStream.write(bytes);
        outputStream.flush();
    }
}
