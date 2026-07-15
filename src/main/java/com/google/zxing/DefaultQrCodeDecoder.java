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

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.GenericMultipleBarcodeReader;
import com.google.zxing.source.BufferedImageLuminanceSource;

import com.google.zxing.exception.QrCodeErrorCode;
import com.google.zxing.exception.QrCodeException;
import com.google.zxing.model.QrCodeDecodeRequest;
import com.google.zxing.model.QrCodeDecodeResult;

/**
 * 默认 QR Code 解码器，基于 ZXing {@link MultiFormatReader} 实现。
 *
 * <p>特性：
 * <ul>
 *     <li>支持单 QR（{@link QrCodeDecodeRequest#isMultiple()} 为 {@code false}）与多 QR。</li>
 *     <li>多 QR 场景使用 {@link GenericMultipleBarcodeReader}；若 ZXing 未发现多码，
 *         自动降级到单 QR 解码以保证至少返回一条结果。</li>
 *     <li>防御性上限：字节输入不超过 {@link QrCodeDecodeRequest#getMaxInputBytes()}，
 *         像素量不超过 {@link QrCodeDecodeRequest#getMaxPixels()}。</li>
 * </ul>
 * 任何 ZXing {@link ReaderException} 都会被翻译为
 * {@link QrCodeErrorCode#QRCODE_DECODE_NOT_FOUND} 的 {@link QrCodeException}。
 */
public final class DefaultQrCodeDecoder implements QrCodeDecoder {

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

    private Result[] decodeSingle(BufferedImage image, Map<DecodeHintType, Object> hints) throws ReaderException {
        MultiFormatReader reader = new MultiFormatReader();
        try {
            return new Result[] { reader.decode(createBitmap(image), hints) };
        } finally {
            reader.reset();
        }
    }

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

    private BinaryBitmap createBitmap(BufferedImage image) {
        return new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image)));
    }

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

    private void validateImage(QrCodeDecodeRequest request, BufferedImage image) {
        long pixels = (long) image.getWidth() * (long) image.getHeight();
        if (pixels <= 0 || pixels > request.getMaxPixels()) {
            throw new QrCodeException(QrCodeErrorCode.QRCODE_IMAGE_TOO_LARGE,
                    "QR code image exceeds the maximum pixel count");
        }
    }
}
