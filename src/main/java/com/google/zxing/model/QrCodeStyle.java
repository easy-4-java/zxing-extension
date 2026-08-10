package com.google.zxing.model;

import java.awt.Color;
import java.util.Objects;

import lombok.Getter;

/**
 * Immutable QR Code colour style.
 *
 * <p>Fields:</p>
 * <ul>
 *     <li>{@code foregroundColor} &mdash; the main data-module colour;</li>
 *     <li>{@code gradientEndColor} &mdash; nullable; defines a linear gradient
 *         together with the foreground colour;</li>
 *     <li>{@code backgroundColor} &mdash; the background colour;</li>
 *     <li>{@code eyeColor} &mdash; nullable; colours the three finder-pattern
 *         (eye) squares independently.</li>
 * </ul>
 *
 * <p>{@link #isGradient()} returns {@code true} when the gradient-end colour
 * is non-{@code null} and differs from the foreground colour.
 * {@link #colorAt(double)} clamps the ratio to [0, 1] before interpolating;
 * for non-gradient styles it always returns {@code foregroundColor}.</p>
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see QrCodeRequest
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
     * Returns a built-in monochrome style (black foreground, white background).
     *
     * @return a monochrome {@link QrCodeStyle}
     */
    public static QrCodeStyle monochrome() {
        return builder().build();
    }

    /**
     * Returns a built-in high-contrast colourful gradient style with
     * independent eye colour.
     *
     * @return a colourful {@link QrCodeStyle}
     */
    public static QrCodeStyle colorful() {
        return builder()
                .foregroundColor(new Color(0, 122, 98))
                .gradientEndColor(new Color(69, 54, 143))
                .eyeColor(new Color(24, 45, 110))
                .build();
    }

    /**
     * Creates a new {@link Builder}.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns whether gradient rendering is enabled (gradient-end colour is
     * non-{@code null} and differs from the foreground colour).
     *
     * @return {@code true} if gradient is active
     */
    public boolean isGradient() {
        return Objects.nonNull(gradientEndColor) && !foregroundColor.equals(gradientEndColor);
    }

    /**
     * Computes the interpolated colour at the given vertical ratio.
     * The ratio is clamped to [0, 1]; for non-gradient styles the
     * foreground colour is always returned.
     *
     * @param ratio the vertical ratio (0 = top, 1 = bottom)
     * @return the interpolated {@link Color}
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
     * Fluent builder for {@link QrCodeStyle} instances.
     */
    public static final class Builder {

        private Color foregroundColor = Color.BLACK;
        private Color gradientEndColor;
        private Color backgroundColor = Color.WHITE;
        private Color eyeColor;

        private Builder() {
        }

        /**
         * Sets the foreground (data-module) colour.
         *
         * @param foregroundColor the foreground colour; must not be {@code null}
         * @return this builder
         */
        public Builder foregroundColor(Color foregroundColor) {
            this.foregroundColor = foregroundColor;
            return this;
        }

        /**
         * Sets the gradient-end colour. A value of {@code null} disables
         * gradient rendering.
         *
         * @param gradientEndColor the gradient-end colour; may be {@code null}
         * @return this builder
         */
        public Builder gradientEndColor(Color gradientEndColor) {
            this.gradientEndColor = gradientEndColor;
            return this;
        }

        /**
         * Sets the background colour.
         *
         * @param backgroundColor the background colour; must not be {@code null}
         * @return this builder
         */
        public Builder backgroundColor(Color backgroundColor) {
            this.backgroundColor = backgroundColor;
            return this;
        }

        /**
         * Sets the finder-pattern (eye) colour.
         *
         * @param eyeColor the eye colour; may be {@code null}
         * @return this builder
         */
        public Builder eyeColor(Color eyeColor) {
            this.eyeColor = eyeColor;
            return this;
        }

        /**
         * Validates and builds an immutable {@link QrCodeStyle}.
         *
         * @return an immutable style
         */
        public QrCodeStyle build() {
            return new QrCodeStyle(this);
        }
    }
}
