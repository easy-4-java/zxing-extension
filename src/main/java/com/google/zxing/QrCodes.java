package com.google.zxing;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;

import com.google.zxing.model.QrCodeDecodeRequest;
import com.google.zxing.model.QrCodeDecodeResult;
import com.google.zxing.model.QrCodeLogo;
import com.google.zxing.model.QrCodeOutput;
import com.google.zxing.model.QrCodeRequest;
import com.google.zxing.model.QrCodeStyle;

/**
 * Static facade exposing the default {@link QrCodeEncoder} and
 * {@link QrCodeDecoder} implementations, plus a handful of convenience
 * shortcuts for common scenarios (single-colour, colourful, logo overlay).
 *
 * <p>Thread safety: the facade shares a single
 * {@link DefaultQrCodeEncoder} and {@link DefaultQrCodeDecoder} instance; both
 * delegate to ZXing's stateless APIs and hold no mutable state, so concurrent
 * use is safe.</p>
 *
 * <p>The facade does not perform any network I/O, does not load remote URLs,
 * and never holds open resources between calls.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see QrCodeEncoder
 * @see QrCodeDecoder
 */
public final class QrCodes {

    /**
     * Shared default encoder instance.
     */
    private static final QrCodeEncoder ENCODER = new DefaultQrCodeEncoder();

    /**
     * Shared default decoder instance.
     */
    private static final QrCodeDecoder DECODER = new DefaultQrCodeDecoder();

    private QrCodes() {
    }

    /**
     * Returns the shared default {@link QrCodeEncoder}.
     *
     * @return a process-wide singleton {@link DefaultQrCodeEncoder}
     */
    public static QrCodeEncoder encoder() {
        return ENCODER;
    }

    /**
     * Returns the shared default {@link QrCodeDecoder}.
     *
     * @return a process-wide singleton {@link DefaultQrCodeDecoder}
     */
    public static QrCodeDecoder decoder() {
        return DECODER;
    }

    /**
     * Generates a monochrome PNG QR Code using {@link QrCodeRequest}'s default
     * parameters.
     *
     * @param content the payload to encode; must not be {@code null} or blank
     * @return the encoded PNG QR Code
     * @throws com.google.zxing.exception.QrCodeException when encoding fails
     */
    public static QrCodeOutput encode(String content) {
        return ENCODER.encode(QrCodeRequest.builder(content).build());
    }

    /**
     * Generates a colourful gradient PNG QR Code using the
     * {@link QrCodeStyle#colorful()} preset style.
     *
     * @param content the payload to encode; must not be {@code null} or blank
     * @return the encoded PNG QR Code
     * @throws com.google.zxing.exception.QrCodeException when encoding fails
     */
    public static QrCodeOutput colorful(String content) {
        return ENCODER.encode(QrCodeRequest.builder(content)
                .style(QrCodeStyle.colorful())
                .build());
    }

    /**
     * Generates a PNG QR Code with {@code logo} drawn centred over the QR
     * region; the error correction level is automatically raised to
     * {@code H} so the logo can occlude up to ~30&nbsp;% of the modules.
     *
     * @param content the payload to encode; must not be {@code null} or blank
     * @param logo    the centred logo image; must not be {@code null}
     * @return the encoded PNG QR Code
     * @throws com.google.zxing.exception.QrCodeException when encoding fails
     */
    public static QrCodeOutput withLogo(String content, BufferedImage logo) {
        return ENCODER.encode(QrCodeRequest.builder(content)
                .logo(QrCodeLogo.builder(logo).build())
                .build());
    }

    /**
     * Decodes the first QR Code found in a raw encoded image byte array
     * (PNG, JPEG, etc.).
     *
     * @param bytes encoded image bytes; must not be {@code null}
     * @return the first decoded result
     * @throws com.google.zxing.exception.QrCodeException when no QR code is
     *         found or decoding fails
     */
    public static QrCodeDecodeResult decode(byte[] bytes) {
        return DECODER.decodeFirst(QrCodeDecodeRequest.from(bytes).build());
    }

    /**
     * Decodes the first QR Code from a {@link BufferedImage}.
     *
     * @param image the raster image; must not be {@code null}
     * @return the first decoded result
     * @throws com.google.zxing.exception.QrCodeException when no QR code is
     *         found or decoding fails
     */
    public static QrCodeDecodeResult decode(BufferedImage image) {
        return DECODER.decodeFirst(QrCodeDecodeRequest.from(image).build());
    }

    /**
     * Decodes the first QR Code from an image file on disk.
     *
     * @param file the image file; must not be {@code null}
     * @return the first decoded result
     * @throws com.google.zxing.exception.QrCodeException when no QR code is
     *         found, on I/O failure, or when decoding fails
     */
    public static QrCodeDecodeResult decode(File file) {
        return DECODER.decodeFirst(QrCodeDecodeRequest.from(file).build());
    }

    /**
     * Decodes the first QR Code from the image file pointed at by the supplied
     * {@link Path}.
     *
     * @param path the image path; must not be {@code null}
     * @return the first decoded result
     * @throws com.google.zxing.exception.QrCodeException when no QR code is
     *         found, on I/O failure, or when decoding fails
     */
    public static QrCodeDecodeResult decode(Path path) {
        return DECODER.decodeFirst(QrCodeDecodeRequest.from(path).build());
    }

    /**
     * Decodes the first QR Code from an arbitrary {@link InputStream}. The
     * library does <strong>not</strong> close the stream; the caller retains
     * ownership.
     *
     * @param inputStream the byte stream; must not be {@code null}
     * @return the first decoded result
     * @throws com.google.zxing.exception.QrCodeException when no QR code is
     *         found, on I/O failure, or when decoding fails
     */
    public static QrCodeDecodeResult decode(InputStream inputStream) {
        return DECODER.decodeFirst(QrCodeDecodeRequest.from(inputStream).build());
    }
}