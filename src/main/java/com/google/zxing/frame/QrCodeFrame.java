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

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import lombok.Getter;

/**
 * Imutable outer-frame canvas used to overlay background colours, text and
 * images around a QR Code.
 *
 * <p>Construction contract:</p>
 * <ul>
 *   <li>Elements are sorted ascending by {@code zIndex}; elements sharing the
 *       same {@code zIndex} preserve their insertion order.</li>
 *   <li>Every element must be fully inside the canvas &mdash; otherwise the
 *       builder throws {@link IllegalArgumentException}.</li>
 *   <li>The frame must contain at least one {@link QrCodeBlockElement} so that
 *       a decodable QR Code is always rendered.</li>
 * </ul>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see QrCodeFrameElement
 * @see QrCodeBlockElement
 * @see QrCodeTextElement
 * @see QrCodeImageElement
 */
@Getter
public final class QrCodeFrame {

    /**
     * Canvas width in pixels; strictly positive.
     */
    private final int width;

    /**
     * Canvas height in pixels; strictly positive.
     */
    private final int height;

    /**
     * Solid background colour drawn before any element is composited.
     */
    private final Color backgroundColor;

    /**
     * Unmodifiable, z-order sorted list of frame elements.
     */
    private final List<QrCodeFrameElement> elements;

    private QrCodeFrame(Builder builder) {
        if (builder.width <= 0 || builder.height <= 0) {
            throw new IllegalArgumentException("frame width and height must be positive");
        }
        this.width = builder.width;
        this.height = builder.height;
        this.backgroundColor = Objects.requireNonNull(builder.backgroundColor, "backgroundColor must not be null");
        List<QrCodeFrameElement> ordered = new ArrayList<QrCodeFrameElement>(builder.elements);
        Collections.sort(ordered, Comparator.comparingInt(QrCodeFrameElement::getZIndex));
        boolean hasQrBlock = false;
        for (QrCodeFrameElement element : ordered) {
            validateBounds(element);
            hasQrBlock |= element instanceof QrCodeBlockElement;
        }
        if (!hasQrBlock) {
            throw new IllegalArgumentException("frame must contain at least one QrCodeBlockElement");
        }
        this.elements = Collections.unmodifiableList(ordered);
    }

    /**
     * Creates a new {@link Builder} for the supplied canvas dimensions.
     *
     * @param width  canvas width in pixels; must be positive
     * @param height canvas height in pixels; must be positive
     * @return a new builder instance
     */
    public static Builder builder(int width, int height) {
        return new Builder(width, height);
    }

    /**
     * Verifies that the supplied element is non-{@code null} and lies fully
     * within the canvas.
     *
     * @param element the element to validate; must not be {@code null}
     * @throws IllegalArgumentException if the element is null or out of bounds
     */
    private void validateBounds(QrCodeFrameElement element) {
        Objects.requireNonNull(element, "frame element must not be null");
        if (element.getX() < 0 || element.getY() < 0 || element.getWidth() <= 0 || element.getHeight() <= 0
                || element.getX() + element.getWidth() > width || element.getY() + element.getHeight() > height) {
            throw new IllegalArgumentException("frame element must be fully inside the canvas");
        }
    }

    /**
     * Fluent builder for {@link QrCodeFrame} instances.
     */
    public static final class Builder {

        /**
         * Canvas width in pixels.
         */
        private final int width;

        /**
         * Canvas height in pixels.
         */
        private final int height;

        /**
         * Background colour; defaults to {@link Color#WHITE}.
         */
        private Color backgroundColor = Color.WHITE;

        /**
         * Mutable, in-insertion-order list of frame elements.
         */
        private final List<QrCodeFrameElement> elements = new ArrayList<QrCodeFrameElement>();

        private Builder(int width, int height) {
            this.width = width;
            this.height = height;
        }

        /**
         * Overrides the background colour drawn behind every element.
         *
         * @param backgroundColor background colour; must not be {@code null}
         * @return this builder
         */
        public Builder backgroundColor(Color backgroundColor) {
            this.backgroundColor = backgroundColor;
            return this;
        }

        /**
         * Appends an element to the frame.
         *
         * @param element element to append; must not be {@code null}
         * @return this builder
         */
        public Builder addElement(QrCodeFrameElement element) {
            this.elements.add(element);
            return this;
        }

        /**
         * Validates and freezes the builder state into an immutable frame.
         *
         * @return an immutable {@link QrCodeFrame}
         * @throws IllegalArgumentException if no {@link QrCodeBlockElement}
         *         was added or any element is out of bounds
         */
        public QrCodeFrame build() {
            return new QrCodeFrame(this);
        }
    }
}