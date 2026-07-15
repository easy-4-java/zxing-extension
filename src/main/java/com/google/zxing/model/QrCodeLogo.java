package com.google.zxing.model;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Objects;

import lombok.Getter;

/**
 * 不可变的 QR Logo 叠加配置。
 *
 * <p>默认值：
 * <ul>
 *     <li>{@code padding = 4}；</li>
 *     <li>{@code cornerRadius = 8}；</li>
 *     <li>{@code backgroundColor = Color.WHITE}。</li>
 * </ul>
 *
 * <p>Logo 在编码时会受到 QR 区域 20% 长宽的限制（{@code DefaultQrCodeEncoder#MAX_LOGO_RATIO}）。
 *
 * <p>校验：image 非空；width/height/padding/cornerRadius 均非负；backgroundColor 非空。
 */
@Getter
public final class QrCodeLogo {

    private final BufferedImage image;
    private final int width;
    private final int height;
    private final int padding;
    private final int cornerRadius;
    private final Color backgroundColor;

    private QrCodeLogo(Builder builder) {
        this.image = Objects.requireNonNull(builder.image, "image must not be null");
        if (builder.width < 0 || builder.height < 0 || builder.padding < 0 || builder.cornerRadius < 0) {
            throw new IllegalArgumentException("logo dimensions, padding and cornerRadius must not be negative");
        }
        this.width = builder.width;
        this.height = builder.height;
        this.padding = builder.padding;
        this.cornerRadius = builder.cornerRadius;
        this.backgroundColor = Objects.requireNonNull(builder.backgroundColor, "backgroundColor must not be null");
    }

    /**
     * @param image Logo 图片；不能为 {@code null}
     * @return 新 Builder
     */
    public static Builder builder(BufferedImage image) {
        return new Builder(image);
    }

    /**
     * Logo 链式构造器。
     */
    public static final class Builder {

        private final BufferedImage image;
        private int width;
        private int height;
        private int padding = 4;
        private int cornerRadius = 8;
        private Color backgroundColor = Color.WHITE;

        private Builder(BufferedImage image) {
            this.image = image;
        }

        /**
         * @param width  宽（像素），可为 0 → 使用原图尺寸与 20% 上限的较小者
         * @param height 高（像素），可为 0
         * @return 当前 builder
         */
        public Builder size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        /**
         * @param padding 四周留白（像素），非负
         * @return 当前 builder
         */
        public Builder padding(int padding) {
            this.padding = padding;
            return this;
        }

        /**
         * @param cornerRadius 圆角半径（像素），非负
         * @return 当前 builder
         */
        public Builder cornerRadius(int cornerRadius) {
            this.cornerRadius = cornerRadius;
            return this;
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
         * @return 不可变 Logo 配置
         */
        public QrCodeLogo build() {
            return new QrCodeLogo(this);
        }
    }
}
