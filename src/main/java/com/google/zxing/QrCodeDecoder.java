package com.google.zxing;

import java.util.List;

import com.google.zxing.model.QrCodeDecodeRequest;
import com.google.zxing.model.QrCodeDecodeResult;

/**
 * Framework-agnostic QR Code decoder SPI.
 *
 * <p>Implementations must honour the following contract:</p>
 * <ul>
 *   <li>{@link #decode(QrCodeDecodeRequest)} returns an <strong>unmodifiable</strong>
 *       list &mdash; even in the single-QR case where the list has exactly one
 *       element.</li>
 *   <li>{@link #decodeFirst(QrCodeDecodeRequest)} throws
 *       {@code QrCodeException} when no QR code can be found rather than
 *       returning {@code null} or letting the caller deal with index-out-of-bounds
 *       on an empty list.</li>
 * </ul>
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see QrCodeDecodeRequest
 * @see QrCodeDecodeResult
 */
public interface QrCodeDecoder {

    /**
     * Decodes the supplied input.
     *
     * <p>For single-QR requests the returned list contains exactly one element;
     * for multi-QR requests the list contains every successfully decoded QR
     * code in decode order.</p>
     *
     * @param request the decode request; must not be {@code null}
     * @return an unmodifiable, non-{@code null} list of decode results
     * @throws com.google.zxing.exception.QrCodeException when the request is
     *         invalid, no QR code can be located, or the input exceeds the
     *         configured safety limits
     */
    List<QrCodeDecodeResult> decode(QrCodeDecodeRequest request);

    /**
     * Decodes and returns the first result; shorthand for
     * {@code decode(request).get(0)}.
     *
     * @param request the decode request; must not be {@code null}
     * @return the first decode result
     * @throws com.google.zxing.exception.QrCodeException when no QR code can be
     *         located
     */
    default QrCodeDecodeResult decodeFirst(QrCodeDecodeRequest request) {
        return decode(request).get(0);
    }
}