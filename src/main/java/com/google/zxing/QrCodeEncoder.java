package com.google.zxing;

import java.io.IOException;
import java.io.OutputStream;

import com.google.zxing.model.QrCodeOutput;
import com.google.zxing.model.QrCodeRequest;

/**
 * Framework-agnostic QR Code encoder SPI.
 *
 * <p>Implementations must honour the following contract:</p>
 * <ul>
 *   <li>{@link #encode(QrCodeRequest)} returns an <strong>immutable</strong>
 *       {@link QrCodeOutput}.</li>
 *   <li>The default {@link #encode(QrCodeRequest, OutputStream)} does not
 *       close the caller-supplied stream.</li>
 * </ul>
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see QrCodeRequest
 * @see QrCodeOutput
 */
public interface QrCodeEncoder {

    /**
     * Encodes the supplied request into a {@link QrCodeOutput}.
     *
     * @param request the encode request; must not be {@code null}
     * @return an immutable, non-{@code null} encode result
     * @throws com.google.zxing.exception.QrCodeException when the request is
     *         invalid, content exceeds capacity, or rendering fails
     */
    QrCodeOutput encode(QrCodeRequest request);

    /**
     * Convenience method: writes the bytes of {@link #encode(QrCodeRequest)}
     * into the supplied stream. The stream is not closed by this method.
     *
     * @param request      the encode request
     * @param outputStream caller-owned output stream; must not be {@code null}
     * @throws IOException when the underlying write fails
     */
    default void encode(QrCodeRequest request, OutputStream outputStream) throws IOException {
        encode(request).writeTo(outputStream);
    }
}