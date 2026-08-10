package com.google.zxing;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.imageio.ImageIO;

import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.GenericMultipleBarcodeReader;
import com.google.zxing.source.BufferedImageLuminanceSource;

import com.google.zxing.exception.QrCodeErrorCode;
import com.google.zxing.exception.QrCodeException;
import com.google.zxing.model.QrCodeDecodeRequest;
import com.google.zxing.model.QrCodeDecodeResult;

/**
 * Default {@link QrCodeDecoder} implementation, powered by ZXing's
 * {@link MultiFormatReader}.
 *
 * <p>Highlights:</p>
 * <ul>
 *   <li>Supports single-QR (when {@link QrCodeDecodeRequest#isMultiple()} is
 *       {@code false}) and multi-QR decoding.</li>
 *   <li>For multi-QR requests the
 *       {@link GenericMultipleBarcodeReader} is used. When ZXing does not
 *       report multiple codes, the decoder transparently falls back to single
 *       decoding so that at least one result is returned whenever possible.</li>
 *   <li>Defensive upper bounds are enforced: the byte payload cannot exceed
 *       {@link QrCodeDecodeRequest#getMaxInputBytes()}, and the raster pixel
 *       count cannot exceed {@link QrCodeDecodeRequest#getMaxPixels()}.</li>
 * </ul>
 *
 * <p>Any ZXing {@link ReaderException} is translated into a
 * {@link QrCodeException} carrying the stable
 * {@link QrCodeErrorCode#QRCODE_DECODE_NOT_FOUND} code so callers can branch
 * on the cause without parsing messages.</p>
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see QrCodeDecoder
 * @see QrCodeDecodeRequest
 */
public final class DefaultQrCodeDecoder implements QrCodeDecoder {

    /**
     * Decodes the supplied {@link QrCodeDecodeRequest}.
     *
     * <p>For single-QR requests the returned list contains exactly one element;
     * for multi-QR requests the list contains every successfully decoded QR
     * code in decode order. The returned list is unmodifiable.</p>
     *
     * @param request the decode request; must not be {@code null}
     * @return an unmodifiable list of decoded results (never {@code null})
     * @throws QrCodeException if the request is invalid, the input is too large,
     *         the image format is unsupported, or no QR code can be located
     */
    @Override
    public List<QrCodeDecodeResult> decode(QrCodeDecodeRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        BufferedImage image = readImage(request);
        validateImage(request, image);
        Map<DecodeHintType, Object> hints = createHints(request);
        try {
            Result[] results = request.isMultiple() ? decodeMultiple(image, hints) : decodeSingle(image, hints);
            List<QrCodeDecodeResult> normalized = new ArrayList<QrCodeDecodeResult>(results.length);
            for (Result result : results) {
                normalized.add(new QrCodeDecodeResult(result.getText(), result.getBarcodeFormat(),
                        result.getRawBytes(), result.getResultPoints(), result.getResultMetadata()));
            }
            return Collections.unmodifiableList(normalized);
        } catch (ReaderException ex) {
            throw new QrCodeException(QrCodeErrorCode.QRCODE_DECODE_NOT_FOUND,
                    "No QR code was found in the image", ex);
        }
    }

    /**
     * Decodes a single QR code from the raster image.
     *
     * @param image the raster image; must not be {@code null}
     * @param hints the decode hints; must not be {@code null}
     * @return an array containing exactly one {@link Result}
     * @throws ReaderException if ZXing cannot locate a QR code
     */
    private Result[] decodeSingle(BufferedImage image, Map<DecodeHintType, Object> hints) throws ReaderException {
        MultiFormatReader reader = new MultiFormatReader();
        try {
            return new Result[] { reader.decode(createBitmap(image), hints) };
        } finally {
            reader.reset();
        }
    }

    /**
     * Decodes multiple QR codes, falling back to single-code decoding if the
     * multiple-code reader reports nothing.
     *
     * @param image the raster image; must not be {@code null}
     * @param hints the decode hints; must not be {@code null}
     * @return an array of decoded {@link Result}s (never {@code null})
     * @throws ReaderException if neither multi nor single decoding succeeds
     */
    private Result[] decodeMultiple(BufferedImage image, Map<DecodeHintType, Object> hints) throws ReaderException {
        MultiFormatReader reader = new MultiFormatReader();
        try {
            return new GenericMultipleBarcodeReader(reader).decodeMultiple(createBitmap(image), hints);
        } catch (NotFoundException ex) {
            return decodeSingle(image, hints);
        } finally {
            reader.reset();
        }
    }

    /**
     * Wraps the supplied image in a ZXing {@link BinaryBitmap} using a
     * {@link HybridBinarizer} backed by {@link BufferedImageLuminanceSource}.
     *
     * @param image the raster image; must not be {@code null}
     * @return a fresh {@link BinaryBitmap}
     */
    private BinaryBitmap createBitmap(BufferedImage image) {
        return new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image)));
    }

    /**
     * Builds the {@link DecodeHintType} hint map for the given request, copying
     * charset, possible formats and the optional
     * {@code TRY_HARDER}/{@code ALSO_INVERTED}/{@code PURE_BARCODE} flags.
     *
     * @param request the decode request; must not be {@code null}
     * @return a mutable hint map
     */
    private Map<DecodeHintType, Object> createHints(QrCodeDecodeRequest request) {
        Map<DecodeHintType, Object> hints = new EnumMap<DecodeHintType, Object>(DecodeHintType.class);
        hints.put(DecodeHintType.CHARACTER_SET, request.getCharset().name());
        hints.put(DecodeHintType.POSSIBLE_FORMATS, Collections.singletonList(BarcodeFormat.QR_CODE));
        if (request.isTryHarder()) {
            hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        }
        if (request.isAlsoInverted()) {
            hints.put(DecodeHintType.ALSO_INVERTED, Boolean.TRUE);
        }
        if (request.isPureBarcode()) {
            hints.put(DecodeHintType.PURE_BARCODE, Boolean.TRUE);
        }
        return hints;
    }

    /**
     * Loads the request's image bytes into a {@link BufferedImage}, honouring
     * the configured byte-size limit.
     *
     * @param request the decode request; must not be {@code null}
     * @return the decoded raster image
     * @throws QrCodeException on I/O failure or when the input exceeds the
     *         configured byte limit
     */
    private BufferedImage readImage(QrCodeDecodeRequest request) {
        if (Objects.nonNull(request.getImage())) {
            return request.getImage();
        }
        byte[] bytes = request.getBytes();
        if (bytes.length > request.getMaxInputBytes()) {
            throw new QrCodeException(QrCodeErrorCode.QRCODE_IMAGE_TOO_LARGE,
                    "QR code input exceeds the maximum byte size");
        }
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
            if (Objects.isNull(image)) {
                throw new QrCodeException(QrCodeErrorCode.QRCODE_UNSUPPORTED_FORMAT,
                        "Input is not a supported raster image");
            }
            return image;
        } catch (IOException ex) {
            throw new QrCodeException(QrCodeErrorCode.QRCODE_UNSUPPORTED_FORMAT,
                    "Failed to read QR code image", ex);
        }
    }

    /**
     * Verifies that the raster image's pixel count fits within the configured
     * upper bound.
     *
     * @param request the decode request; must not be {@code null}
     * @param image   the raster image; must not be {@code null}
     * @throws QrCodeException when the pixel count exceeds the configured limit
     */
    private void validateImage(QrCodeDecodeRequest request, BufferedImage image) {
        long pixels = (long) image.getWidth() * (long) image.getHeight();
        if (pixels <= 0 || pixels > request.getMaxPixels()) {
            throw new QrCodeException(QrCodeErrorCode.QRCODE_IMAGE_TOO_LARGE,
                    "QR code image exceeds the maximum pixel count");
        }
    }
}