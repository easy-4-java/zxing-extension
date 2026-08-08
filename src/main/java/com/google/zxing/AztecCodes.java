package com.google.zxing;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;

import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.exception.CodeException;
import com.google.zxing.model.AztecCodeRequest;
import com.google.zxing.model.CodeOutput;
import com.google.zxing.model.CodeResult;
import com.google.zxing.source.BufferedImageLuminanceSource;
import com.google.zxing.source.MatrixToImageWriter;

/**
 * Static facade providing encode / decode entry points for the Aztec 2-D barcode
 * symbology, built on top of ZXing's {@link MultiFormatWriter} and
 * {@link MultiFormatReader}.
 *
 * <p>Unlike QR Codes, Aztec codes expose a single continuous
 * {@code errorCorrectionPercent} integer (1-100) instead of the L / M / Q / H
 * discrete levels. The facade translates that single value into ZXing's
 * {@link EncodeHintType#ERROR_CORRECTION} hint verbatim and lets ZXing drive
 * the actual encoding / decoding.</p>
 *
 * <p>Thread safety: this class only delegates to ZXing's stateless APIs and
 * keeps a single static {@link MultiFormatWriter}; concurrent calls are safe.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see AztecCodeRequest
 * @see com.google.zxing.MultiFormatWriter
 */
public final class AztecCodes {

    /**
     * Shared, stateless ZXing writer used for every encode call.
     */
    private static final MultiFormatWriter WRITER = new MultiFormatWriter();

    private AztecCodes() {
    }

    /**
     * Encodes {@code content} as an Aztec barcode PNG using a default
     * {@link AztecCodeRequest}.
     *
     * @param content the payload to encode; must not be {@code null} or blank
     * @return the encoded PNG wrapped in a {@link CodeOutput}
     * @throws com.google.zxing.exception.CodeException if ZXing fails to encode
     */
    public static CodeOutput encode(String content) {
        return encode(AztecCodeRequest.builder(content).build());
    }

    /**
     * Encodes an explicit {@link AztecCodeRequest} as an Aztec barcode PNG,
     * reserving a {@code margin} pixel quiet zone around the module area.
     *
     * @param request the encode request; must not be {@code null}
     * @return the encoded PNG wrapped in a {@link CodeOutput}
     * @throws com.google.zxing.exception.CodeException if ZXing fails to encode
     */
    public static CodeOutput encode(AztecCodeRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        Map<EncodeHintType, Object> hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, request.getCharset().name());
        hints.put(EncodeHintType.ERROR_CORRECTION, request.getErrorCorrectionPercent());
        try {
            BitMatrix matrix = WRITER.encode(request.getContent(), BarcodeFormat.AZTEC,
                    request.getWidth(), request.getHeight(), hints);
            if (request.getMargin() > 0) {
                matrix = MatrixToImageWriter.updateBit(matrix, request.getMargin());
            }
            return CodeImageSupport.toPng(matrix);
        } catch (WriterException ex) {
            throw new CodeException("Failed to encode Aztec code", ex);
        }
    }

    /**
     * Decodes an Aztec code from a raw encoded image byte array (PNG, JPEG, etc.).
     *
     * @param bytes encoded image bytes; must not be {@code null}
     * @return the decode result
     * @throws com.google.zxing.exception.CodeException if the image cannot be read
     *         or no Aztec code is found
     */
    public static CodeResult decode(byte[] bytes) {
        return decode(CodeImageSupport.read(bytes));
    }

    /**
     * Decodes an Aztec code from a {@link BufferedImage}.
     *
     * @param image the raster image; must not be {@code null}
     * @return the decode result
     * @throws com.google.zxing.exception.CodeException if no Aztec code is found
     */
    public static CodeResult decode(BufferedImage image) {
        Objects.requireNonNull(image, "image must not be null");
        Map<DecodeHintType, Object> hints = new EnumMap<DecodeHintType, Object>(DecodeHintType.class);
        hints.put(DecodeHintType.POSSIBLE_FORMATS, EnumSet.of(BarcodeFormat.AZTEC));
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        MultiFormatReader reader = new MultiFormatReader();
        try {
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image)));
            return CodeResult.from(reader.decode(bitmap, hints));
        } catch (ReaderException ex) {
            throw new CodeException("No Aztec code was found in the image", ex);
        } finally {
            reader.reset();
        }
    }

    /**
     * Decodes an Aztec code from an image file on disk.
     *
     * @param file the image file; must not be {@code null}
     * @return the decode result
     * @throws com.google.zxing.exception.CodeException on I/O failure or if no
     *         Aztec code is found
     */
    public static CodeResult decode(File file) {
        return decode(CodeImageSupport.read(file));
    }

    /**
     * Decodes an Aztec code from a {@link Path} pointing at an image file.
     *
     * @param path the image path; must not be {@code null}
     * @return the decode result
     * @throws com.google.zxing.exception.CodeException on I/O failure or if no
     *         Aztec code is found
     */
    public static CodeResult decode(Path path) {
        return decode(CodeImageSupport.read(path));
    }

    /**
     * Decodes an Aztec code from an arbitrary {@link InputStream}. The library
     * does <strong>not</strong> close the stream; the caller retains ownership.
     *
     * @param inputStream the byte stream; must not be {@code null}
     * @return the decode result
     * @throws com.google.zxing.exception.CodeException on I/O failure or if no
     *         Aztec code is found
     */
    public static CodeResult decode(InputStream inputStream) {
        return decode(CodeImageSupport.read(inputStream));
    }
}