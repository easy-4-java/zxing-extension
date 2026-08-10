package com.google.zxing;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.exception.CodeException;
import com.google.zxing.model.BarCodeRequest;
import com.google.zxing.model.CodeOutput;
import com.google.zxing.model.CodeResult;
import com.google.zxing.source.BufferedImageLuminanceSource;

/**
 * Static facade providing encode / decode entry points for one-dimensional
 * barcode symbologies, built on top of ZXing's {@link MultiFormatWriter} and
 * {@link MultiFormatReader}.
 *
 * <p>The supported formats are:</p>
 * <ul>
 *   <li>{@link BarcodeFormat#CODABAR}</li>
 *   <li>{@link BarcodeFormat#CODE_39}</li>
 *   <li>{@link BarcodeFormat#CODE_93}</li>
 *   <li>{@link BarcodeFormat#CODE_128}</li>
 *   <li>{@link BarcodeFormat#EAN_8}</li>
 *   <li>{@link BarcodeFormat#EAN_13}</li>
 *   <li>{@link BarcodeFormat#ITF}</li>
 *   <li>{@link BarcodeFormat#UPC_A}</li>
 *   <li>{@link BarcodeFormat#UPC_E}</li>
 * </ul>
 *
 * <p>Two-dimensional formats such as {@code QR_CODE}, {@code AZTEC},
 * {@code PDF_417} and {@code DATA_MATRIX} are rejected by
 * {@link #encode(BarCodeRequest)}; callers must use
 * {@code QrCodes} or {@code AztecCodes} instead.</p>
 *
 * <p>Thread safety: the facade only delegates to ZXing's stateless APIs and
 * exposes an immutable {@link Set} of supported formats; concurrent calls are
 * safe.</p>
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see BarCodeRequest
 * @see com.google.zxing.MultiFormatWriter
 */
public final class BarCodes {

    /**
     * Shared, stateless ZXing writer used for every encode call.
     */
    private static final MultiFormatWriter WRITER = new MultiFormatWriter();

    /**
     * Immutable set of one-dimensional barcode formats accepted by this facade.
     */
    private static final Set<BarcodeFormat> SUPPORTED_FORMATS = Collections.unmodifiableSet(EnumSet.of(
            BarcodeFormat.CODABAR,
            BarcodeFormat.CODE_39,
            BarcodeFormat.CODE_93,
            BarcodeFormat.CODE_128,
            BarcodeFormat.EAN_8,
            BarcodeFormat.EAN_13,
            BarcodeFormat.ITF,
            BarcodeFormat.UPC_A,
            BarcodeFormat.UPC_E));

    private BarCodes() {
    }

    /**
     * Generates an EAN-13 barcode PNG using {@link BarCodeRequest}'s default
     * dimensions.
     *
     * @param content the 12-digit payload (the check digit will be auto-appended)
     *                or a 13-digit payload already containing the check digit;
     *                must not be {@code null} or blank
     * @return the encoded PNG
     * @throws com.google.zxing.exception.CodeException if ZXing fails to encode
     */
    public static CodeOutput ean13(String content) {
        return encode(BarCodeRequest.builder(content, BarcodeFormat.EAN_13).build());
    }

    /**
     * Generates a PNG for any of the supported one-dimensional barcode formats.
     *
     * @param request the encode request; must not be {@code null} and its
     *                {@link BarCodeRequest#getFormat()} must be one of the
     *                supported formats
     * @return the encoded PNG
     * @throws IllegalArgumentException if the request or its format is unsupported
     * @throws com.google.zxing.exception.CodeException if ZXing fails to encode
     */
    public static CodeOutput encode(BarCodeRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        if (!SUPPORTED_FORMATS.contains(request.getFormat())) {
            throw new IllegalArgumentException("format is not a supported one-dimensional barcode: "
                    + request.getFormat());
        }
        Map<EncodeHintType, Object> hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, request.getCharset().name());
        hints.put(EncodeHintType.MARGIN, request.getMargin());
        try {
            BitMatrix matrix = WRITER.encode(request.getContent(), request.getFormat(),
                    request.getWidth(), request.getHeight(), hints);
            return CodeImageSupport.toPng(matrix);
        } catch (WriterException ex) {
            throw new CodeException("Failed to encode barcode", ex);
        }
    }

    /**
     * Decodes a one-dimensional barcode from a raw encoded image byte array
     * (PNG, JPEG, etc.).
     *
     * @param bytes encoded image bytes; must not be {@code null}
     * @return the decode result
     * @throws com.google.zxing.exception.CodeException on I/O failure or if no
     *         supported barcode is found
     */
    public static CodeResult decode(byte[] bytes) {
        return decode(CodeImageSupport.read(bytes));
    }

    /**
     * Decodes a one-dimensional barcode from a {@link BufferedImage}.
     *
     * @param image the raster image; must not be {@code null}
     * @return the decode result
     * @throws com.google.zxing.exception.CodeException if no supported barcode
     *         is found
     */
    public static CodeResult decode(BufferedImage image) {
        Objects.requireNonNull(image, "image must not be null");
        Map<DecodeHintType, Object> hints = new EnumMap<DecodeHintType, Object>(DecodeHintType.class);
        hints.put(DecodeHintType.POSSIBLE_FORMATS, SUPPORTED_FORMATS);
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        MultiFormatReader reader = new MultiFormatReader();
        try {
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image)));
            return CodeResult.from(reader.decode(bitmap, hints));
        } catch (ReaderException ex) {
            throw new CodeException("No supported barcode was found in the image", ex);
        } finally {
            reader.reset();
        }
    }

    /**
     * Decodes a one-dimensional barcode from an image file on disk.
     *
     * @param file the image file; must not be {@code null}
     * @return the decode result
     * @throws com.google.zxing.exception.CodeException on I/O failure or if no
     *         supported barcode is found
     */
    public static CodeResult decode(File file) {
        return decode(CodeImageSupport.read(file));
    }

    /**
     * Decodes a one-dimensional barcode from a {@link Path} pointing at an
     * image file.
     *
     * @param path the image path; must not be {@code null}
     * @return the decode result
     * @throws com.google.zxing.exception.CodeException on I/O failure or if no
     *         supported barcode is found
     */
    public static CodeResult decode(Path path) {
        return decode(CodeImageSupport.read(path));
    }

    /**
     * Decodes a one-dimensional barcode from an arbitrary {@link InputStream}.
     * The library does <strong>not</strong> close the stream; the caller
     * retains ownership.
     *
     * @param inputStream the byte stream; must not be {@code null}
     * @return the decode result
     * @throws com.google.zxing.exception.CodeException on I/O failure or if no
     *         supported barcode is found
     */
    public static CodeResult decode(InputStream inputStream) {
        return decode(CodeImageSupport.read(inputStream));
    }
}