package com.google.zxing.model;

import java.awt.Color;
import java.util.Objects;

import lombok.Getter;

/**
 * 不可变的 QR Code 颜色样式。
 *
 * <p>字段语义：
 * <ul>
 *     <li>{@code foregroundColor}：数据模块主体颜色；</li>
 *     <li>{@code gradientEndColor}：可空；与前景色一同定义线性渐变；</li>
 *     <li>{@code backgroundColor}：背景色；</li>
 *     <li>{@code eyeColor}：可空；为码眼（三个 finder pattern）独立着色。</li>
 * </ul>
 *
 * <p>{@link #isGradient()} 当渐变色非空且与前景色不同时返回 {@code true}。
 * {@link #colorAt(double)} 在 ratio 越界时会截断到 [0,1]。
 */
@Getter
public final class QrCodeStyle {

    private final Color foregroundColor;
    private final Color gradientEndColor;
    private final Color backgroundColor;
    private final Color eyeColor;

    private QrCodeStyle(Builder builder) {
        this.foregroundColor = Objects.requireNonNull(builder.foregroundColor, "foregroundColor must not be null");
        this.gradientEndColor = builder.gradientEndColor;
        this.backgroundColor = Objects.requireNonNull(builder.backgroundColor, "backgroundColor must not be null");
        this.eyeColor = builder.eyeColor;
    }

    /**
     * @return 内置单色样式（黑前景 + 白背景）
     */
    public static QrCodeStyle monochrome() {
        return builder().build();
    }

    /**
     * @return 内置高对比度彩色渐变样式，附带独立码眼色
     */
    public static QrCodeStyle colorful() {
        return builder()
                .foregroundColor(new Color(0, 122, 98))
                .gradientEndColor(new Color(69, 54, 143))
                .eyeColor(new Color(24, 45, 110))
                .build();
    }

    /**
     * @return 新 Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * @return 是否启用渐变（gradientEndColor 非空且不等于前景色）
     */
    public boolean isGradient() {
        return Objects.nonNull(gradientEndColor) && !foregroundColor.equals(gradientEndColor);
    }

    /**
     * 计算渐变在给定纵向比例上的颜色；ratio 越界会被截断到 [0,1]；
     * 非渐变样式返回 {@code foregroundColor}。
     *
     * @param ratio 比例，0 → 顶部、1 → 底部
     * @return 插值得到的颜色
     */
    public Color colorAt(double ratio) {
        if (!isGradient()) {
            return foregroundColor;
        }
        double bounded = Math.max(0D, Math.min(1D, ratio));
        int red = interpolate(foregroundColor.getRed(), gradientEndColor.getRed(), bounded);
        int green = interpolate(foregroundColor.getGreen(), gradientEndColor.getGreen(), bounded);
        int blue = interpolate(foregroundColor.getBlue(), gradientEndColor.getBlue(), bounded);
        int alpha = interpolate(foregroundColor.getAlpha(), gradientEndColor.getAlpha(), bounded);
        return new Color(red, green, blue, alpha);
    }

    private int interpolate(int start, int end, double ratio) {
        return (int) Math.round(start + (end - start) * ratio);
    }

    /**
     * 颜色样式链式构造器。
     */
    public static final class Builder {

        private Color foregroundColor = Color.BLACK;
        private Color gradientEndColor;
        private Color backgroundColor = Color.WHITE;
        private Color eyeColor;

        private Builder() {
        }

        /**
         * @param foregroundColor 前景色；不能为 {@code null}
         * @return 当前 builder
         */
        public Builder foregroundColor(Color foregroundColor) {
            this.foregroundColor = foregroundColor;
            return this;
        }

        /**
         * @param gradientEndColor 渐变终点色；可为 {@code null}（不渐变）
         * @return 当前 builder
         */
        public Builder gradientEndColor(Color gradientEndColor) {
            this.gradientEndColor = gradientEndColor;
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
         * @param eyeColor 码眼三定位角的颜色；可为 {@code null}
         * @return 当前 builder
         */
        public Builder eyeColor(Color eyeColor) {
            this.eyeColor = eyeColor;
            return this;
        }

        /**
         * @return 不可变样式
         */
        public QrCodeStyle build() {
            return new QrCodeStyle(this);
        }
    }
}
