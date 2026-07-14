package com.google.zxing.frame;

import java.awt.Color;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import lombok.Getter;

/**
 * 外套壳上绘制的文字元素。
 *
 * <p>默认值：{@code fontName = "SansSerif"}；{@code fontSize = 24}；颜色默认黑色；非粗体。
 *
 * <p>校验：text 非空白；fontSize 大于 0；color 非空。
 */
@Getter
public final class QrCodeTextElement implements QrCodeFrameElement {

    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final int zIndex;
    private final String text;
    private final String fontName;
    private final int fontSize;
    private final Color color;
    private final boolean bold;

    private QrCodeTextElement(Builder builder) {
        if (StringUtils.isBlank(builder.text) || builder.fontSize <= 0) {
            throw new IllegalArgumentException("text must not be blank and fontSize must be positive");
        }
        this.x = builder.x;
        this.y = builder.y;
        this.width = builder.width;
        this.height = builder.height;
        this.zIndex = builder.zIndex;
        this.text = builder.text;
        this.fontName = StringUtils.defaultIfBlank(builder.fontName, "SansSerif");
        this.fontSize = builder.fontSize;
        this.color = Objects.requireNonNull(builder.color, "color must not be null");
        this.bold = builder.bold;
    }

    /**
     * @param text 文字内容；不能为 {@code null} 或空白
     * @return 新 Builder
     */
    public static Builder builder(String text) {
        return new Builder(text);
    }

    /**
     * 文字元素链式构造器。
     */
    public static final class Builder {

        private final String text;
        private int x;
        private int y;
        private int width;
        private int height;
        private int zIndex;
        private String fontName = "SansSerif";
        private int fontSize = 24;
        private Color color = Color.BLACK;
        private boolean bold;

        private Builder(String text) {
            this.text = text;
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
         * @param fontName 字体名称
         * @param fontSize 像素大小
         * @param bold     是否粗体
         * @return 当前 builder
         */
        public Builder font(String fontName, int fontSize, boolean bold) {
            this.fontName = fontName;
            this.fontSize = fontSize;
            this.bold = bold;
            return this;
        }

        /**
         * @param color 颜色；不能为 {@code null}
         * @return 当前 builder
         */
        public Builder color(Color color) {
            this.color = color;
            return this;
        }

        /**
         * @return 不可变文字元素
         */
        public QrCodeTextElement build() {
            return new QrCodeTextElement(this);
        }
    }
}
