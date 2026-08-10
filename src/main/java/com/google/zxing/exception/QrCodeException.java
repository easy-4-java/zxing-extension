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

import java.util.Objects;

import lombok.Getter;

/**
 * Unchecked exception that carries a stable {@link QrCodeErrorCode} for every
 * failure path exposed by the QR Code engine.
 *
 * <p>{@link QrCodeErrorCode} is part of the public contract; the exception
 * message is purely informational and may be localised without breaking
 * consumers.</p>
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see QrCodeErrorCode
 */
@Getter
public class QrCodeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Stable error code; never {@code null}.
     */
    private final QrCodeErrorCode errorCode;

    /**
     * Creates a new exception with the given error code and message.
     *
     * @param errorCode the stable error code; must not be {@code null}
     * @param message   a human-readable description of the failure
     */
    public QrCodeException(QrCodeErrorCode errorCode, String message) {
        super(message);
        this.errorCode = Objects.requireNonNull(errorCode, "errorCode must not be null");
    }

    /**
     * Creates a new exception that wraps a lower-level cause.
     *
     * @param errorCode the stable error code; must not be {@code null}
     * @param message   a human-readable description of the failure
     * @param cause     the underlying cause; typically a ZXing exception
     */
    public QrCodeException(QrCodeErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = Objects.requireNonNull(errorCode, "errorCode must not be null");
    }
}