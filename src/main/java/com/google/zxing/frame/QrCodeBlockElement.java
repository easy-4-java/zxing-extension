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
package com.google.zxing.frame;

import lombok.Builder;
import lombok.Getter;

/**
 * Frame element describing where a QR Code module should be rendered on the
 * outer {@link QrCodeFrame} canvas.
 *
 * <p>Instances are produced via Lombok's {@code @Builder}; see
 * {@link QrCodeFrameElement} for the meaning of each accessor.</p>
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see QrCodeFrameElement
 * @see QrCodeFrame
 */
@Getter
@Builder
public final class QrCodeBlockElement implements QrCodeFrameElement {

    /**
     * Left coordinate in pixels (non-negative).
     */
    private final int x;

    /**
     * Top coordinate in pixels (non-negative).
     */
    private final int y;

    /**
     * Width in pixels (strictly positive).
     */
    private final int width;

    /**
     * Height in pixels (strictly positive).
     */
    private final int height;

    /**
     * Z-order; higher values render on top of lower values.
     */
    private final int zIndex;
}