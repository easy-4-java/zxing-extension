package com.google.zxing.frame;

import java.awt.image.BufferedImage;
import java.util.Objects;

import lombok.Getter;

/**
 * 外套壳上绘制的任意图片元素。
 *
 * <p>校验：image 非空；width/height 大于 0。
 */
@Getter
public final class QrCodeImageElement implements QrCodeFrameElement {

    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final int zIndex;
    private final BufferedImage image;

    private QrCodeImageElement(Builder builder) {
        this.x = builder.x;
        this.y = builder.y;
        this.width = builder.width;
        this.height = builder.height;
        this.zIndex = builder.zIndex;
        this.image = Objects.requireNonNull(builder.image, "image must not be null");
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("image element width and height must be positive");
        }
    }

    /**
     * @param image 任意 BufferedImage；不能为 {@code null}
     * @return 新 Builder
     */
    public static Builder builder(BufferedImage image) {
        return new Builder(image);
    }

    /**
     * 图片元素链式构造器。
     */
    public static final class Builder {

        private final BufferedImage image;
        private int x;
        private int y;
        private int width;
        private int height;
        private int zIndex;

        private Builder(BufferedImage image) {
            this.image = image;
        }

        /**
         * @param x      左上角 X
         * @param y      左上角 Y
         * @param width  像素宽度
         * @param height 像素高度
         * @return 当前 builder
         */
        public Builder bounds(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            return this;
        }

        /**
         * @param zIndex zIndex；越大越在上层
         * @return 当前 builder
         */
        public Builder zIndex(int zIndex) {
            this.zIndex = zIndex;
            return this;
        }

        /**
         * @return 不可变图片元素
         */
        public QrCodeImageElement build() {
            return new QrCodeImageElement(this);
        }
    }
}
