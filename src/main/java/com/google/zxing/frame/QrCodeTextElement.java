package com.google.zxing.frame;

import java.awt.Color;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import lombok.Getter;

/**
 * A text element drawn on a {@link QrCodeFrame} canvas.
 *
 * <p>Defaults: {@code fontName = "SansSerif"}; {@code fontSize = 24};
 * {@code color = Color.BLACK}; {@code bold = false}.</p>
 *
 * <p>Validation: {@code text} must not be blank; {@code fontSize} must be
 * positive; {@code color} must not be {@code null}.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see QrCodeFrameElement
 * @see QrCodeFrame
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
     * Creates a new {@link Builder} for a text element.
     *
     * @param text the text content; must not be {@code null} or blank
     * @return a new builder instance
     */
    public static Builder builder(String text) {
        return new Builder(text);
    }

    /**
     * Fluent builder for {@link QrCodeTextElement} instances.
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
         * Sets the bounding rectangle for this text element.
         *
         * @param x      the left X coordinate in pixels
         * @param y      the top Y coordinate in pixels
         * @param width  the width in pixels
         * @param height the height in pixels
         * @return this builder
         */
        public Builder bounds(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            return this;
        }

        /**
         * Sets the z-order index; higher values render on top of lower values.
         *
         * @param zIndex the z-order index
         * @return this builder
         */
        public Builder zIndex(int zIndex) {
            this.zIndex = zIndex;
            return this;
        }

        /**
         * Sets the font properties for this text element.
         *
         * @param fontName the font family name; blank values fall back to {@code "SansSerif"}
         * @param fontSize the font size in pixels; must be positive
         * @param bold     whether to render in bold
         * @return this builder
         */
        public Builder font(String fontName, int fontSize, boolean bold) {
            this.fontName = fontName;
            this.fontSize = fontSize;
            this.bold = bold;
            return this;
        }

        /**
         * Sets the text colour.
         *
         * @param color the text colour; must not be {@code null}
         * @return this builder
         */
        public Builder color(Color color) {
            this.color = color;
            return this;
        }

        /**
         * Validates and builds an immutable {@link QrCodeTextElement}.
         *
         * @return an immutable text element
         */
        public QrCodeTextElement build() {
            return new QrCodeTextElement(this);
        }
    }
}
