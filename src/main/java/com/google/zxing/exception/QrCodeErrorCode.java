/*
 * Copyright (c) 2018-present, easy-4-java (https://github.com/easy-4-java).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.zxing.exception;

/**
 * Stable error classification exposed by the QR Code engine.
 *
 * <p>Each constant carries an unambiguous meaning:</p>
 * <ul>
 *   <li>{@link #QRCODE_INVALID_ARGUMENT}: the caller supplied an illegal
 *       argument (for example a logo that exceeds 20&nbsp;% of the QR region).</li>
 *   <li>{@link #QRCODE_CAPACITY_EXCEEDED}: the content overflows the chosen
 *       error correction level's capacity.</li>
 *   <li>{@link #QRCODE_UNSUPPORTED_FORMAT}: the input format is not recognised
 *       by ImageIO or ZXing.</li>
 *   <li>{@link #QRCODE_DECODE_NOT_FOUND}: no QR code could be located in the
 *       supplied image.</li>
 *   <li>{@link #QRCODE_IMAGE_TOO_LARGE}: the pixel count or byte payload
 *       exceeds the defensive upper bound.</li>
 *   <li>{@link #QRCODE_RENDER_FAILED}: rendering a PNG / SVG, or reading an
 *       image file, failed.</li>
 *   <li>{@link #QRCODE_SELF_CHECK_FAILED}: the post-encode round-trip decode
 *       did not match the original content.</li>
 * </ul>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see QrCodeException
 */
public enum QrCodeErrorCode {

    /** The caller supplied an illegal argument. */
    QRCODE_INVALID_ARGUMENT,
    /** The content exceeds the chosen error correction capacity. */
    QRCODE_CAPACITY_EXCEEDED,
    /** The input image format is not supported. */
    QRCODE_UNSUPPORTED_FORMAT,
    /** No QR code was found in the supplied image. */
    QRCODE_DECODE_NOT_FOUND,
    /** The input bytes or raster exceed the configured safety limits. */
    QRCODE_IMAGE_TOO_LARGE,
    /** Rendering or image reading failed. */
    QRCODE_RENDER_FAILED,
    /** The self-check round-trip decode did not match the source content. */
    QRCODE_SELF_CHECK_FAILED
}