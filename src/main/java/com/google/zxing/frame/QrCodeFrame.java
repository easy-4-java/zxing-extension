package com.google.zxing.frame;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import lombok.Getter;

/**
 * 不可变的外套壳画布，用于在 QR Code 周围叠加背景色、文字与图片。
 *
 * <p>约定：
 * <ul>
 *     <li>元素按 {@code zIndex} 升序排列；同 zIndex 保持添加顺序。</li>
 *     <li>每个元素必须完全在画布内，否则构造失败。</li>
 *     <li>至少包含一个 {@link QrCodeBlockElement}，否则无 QR 可被解码。</li>
 * </ul>
 */
@Getter
public final class QrCodeFrame {

    private final int width;
    private final int height;
    private final Color backgroundColor;
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
     * @param width  画布宽（像素），必须大于 0
     * @param height 画布高（像素），必须大于 0
     * @return 新 Builder
     */
    public static Builder builder(int width, int height) {
        return new Builder(width, height);
    }

    private void validateBounds(QrCodeFrameElement element) {
        Objects.requireNonNull(element, "frame element must not be null");
        if (element.getX() < 0 || element.getY() < 0 || element.getWidth() <= 0 || element.getHeight() <= 0
                || element.getX() + element.getWidth() > width || element.getY() + element.getHeight() > height) {
            throw new IllegalArgumentException("frame element must be fully inside the canvas");
        }
    }

    /**
     * 外套壳链式构造器。
     */
    public static final class Builder {

        private final int width;
        private final int height;
        private Color backgroundColor = Color.WHITE;
        private final List<QrCodeFrameElement> elements = new ArrayList<QrCodeFrameElement>();

        private Builder(int width, int height) {
            this.width = width;
            this.height = height;
        }

        /**
         * @param backgroundColor 背景色；不能为 {@code null}
         * @return 当前 builder
         */
        public Builder backgroundColor(Color backgroundColor) {
            this.backgroundColor = backgroundColor;
            return this;
        }

        /**
         * @param element 待添加元素；不能为 {@code null}
         * @return 当前 builder
         */
        public Builder addElement(QrCodeFrameElement element) {
            this.elements.add(element);
            return this;
        }

        /**
         * @return 不可变外套壳
         */
        public QrCodeFrame build() {
            return new QrCodeFrame(this);
        }
    }
}
