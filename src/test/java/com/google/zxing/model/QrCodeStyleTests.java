package com.google.zxing.model;

import java.awt.Color;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link QrCodeStyle} 单元测试，覆盖工厂、{@code isGradient()}、{@code colorAt(double)} 与构造器校验。
 */
class QrCodeStyleTests {

    @Test
    void monochromeHasBlackForegroundAndWhiteBackground() {
        QrCodeStyle style = QrCodeStyle.monochrome();

        assertThat(style.getForegroundColor()).isEqualTo(Color.BLACK);
        assertThat(style.getBackgroundColor()).isEqualTo(Color.WHITE);
        assertThat(style.getGradientEndColor()).isNull();
        assertThat(style.getEyeColor()).isNull();
        assertThat(style.isGradient()).isFalse();
    }

    @Test
    void colorfulEnablesGradientAndHasEyeColor() {
        QrCodeStyle style = QrCodeStyle.colorful();

        assertThat(style.isGradient()).isTrue();
        assertThat(style.getEyeColor()).isNotNull();
    }

    @Test
    void equalForegroundAndGradientEndNotConsideredGradient() {
        QrCodeStyle style = QrCodeStyle.builder()
                .foregroundColor(Color.RED)
                .gradientEndColor(Color.RED)
                .build();

        assertThat(style.isGradient()).isFalse();
        assertThat(style.colorAt(0.5)).isEqualTo(Color.RED);
    }

    @Test
    void nonGradientColorAtAlwaysReturnsForeground() {
        QrCodeStyle style = QrCodeStyle.builder().foregroundColor(Color.GREEN).build();

        assertThat(style.colorAt(0.0)).isEqualTo(Color.GREEN);
        assertThat(style.colorAt(0.5)).isEqualTo(Color.GREEN);
        assertThat(style.colorAt(1.0)).isEqualTo(Color.GREEN);
        assertThat(style.colorAt(-5)).isEqualTo(Color.GREEN);
        assertThat(style.colorAt(99)).isEqualTo(Color.GREEN);
    }

    @Test
    void gradientColorInterpolatesAtEdgesAndClipsOutOfRange() {
        Color start = new Color(0, 0, 0);
        Color end = new Color(200, 100, 50);
        QrCodeStyle style = QrCodeStyle.builder()
                .foregroundColor(start)
                .gradientEndColor(end)
                .build();

        Color zero = style.colorAt(0D);
        assertThat(zero.getRed()).isZero();
        assertThat(zero.getGreen()).isZero();
        assertThat(zero.getBlue()).isZero();

        Color full = style.colorAt(1D);
        assertThat(full.getRed()).isEqualTo(200);
        assertThat(full.getGreen()).isEqualTo(100);
        assertThat(full.getBlue()).isEqualTo(50);

        Color clippedLow = style.colorAt(-2D);
        assertThat(clippedLow.getRed()).isZero();
        Color clippedHigh = style.colorAt(2D);
        assertThat(clippedHigh.getRed()).isEqualTo(200);

        Color middle = style.colorAt(0.5);
        assertThat(middle.getRed()).isBetween(90, 110);
        assertThat(middle.getGreen()).isBetween(45, 55);
        assertThat(middle.getBlue()).isBetween(20, 30);
    }

    @Test
    void builderAppliesAllSetters() {
        Color fg = new Color(10, 20, 30);
        Color bg = new Color(40, 50, 60);
        Color end = new Color(70, 80, 90);
        Color eye = new Color(100, 110, 120);

        QrCodeStyle style = QrCodeStyle.builder()
                .foregroundColor(fg)
                .backgroundColor(bg)
                .gradientEndColor(end)
                .eyeColor(eye)
                .build();

        assertThat(style.getForegroundColor()).isEqualTo(fg);
        assertThat(style.getBackgroundColor()).isEqualTo(bg);
        assertThat(style.getGradientEndColor()).isEqualTo(end);
        assertThat(style.getEyeColor()).isEqualTo(eye);
    }

    @Test
    void eyeColorAloneDoesNotImplyGradient() {
        QrCodeStyle style = QrCodeStyle.builder().eyeColor(Color.ORANGE).build();

        assertThat(style.isGradient()).isFalse();
        assertThat(style.colorAt(0.5)).isEqualTo(Color.BLACK);
    }

    @Test
    void builderRejectsNullForegroundOrBackground() {
        assertThatThrownBy(() -> QrCodeStyle.builder().foregroundColor(null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> QrCodeStyle.builder().backgroundColor(null))
                .isInstanceOf(NullPointerException.class);
    }
}
