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
 * Unchecked exception raised by the non-QR code paths (Aztec and one-dimensional
 * barcodes) for both encoding and decoding failures.
 *
 * <p>The exception intentionally does <strong>not</strong> carry a stable error
 * code; callers should branch on the exception type and inspect the message
 * and cause for details.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see QrCodeException
 */
public class CodeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new exception with the given message and no cause.
     *
     * @param message a human-readable description of the failure
     */
    public CodeException(String message) {
        super(message);
    }

    /**
     * Creates a new exception that wraps a lower-level cause (typically a
     * ZXing or I/O exception).
     *
     * @param message a human-readable description of the failure
     * @param cause   the underlying cause
     */
    public CodeException(String message, Throwable cause) {
        super(message, cause);
    }
}