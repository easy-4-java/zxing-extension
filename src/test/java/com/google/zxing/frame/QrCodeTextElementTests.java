package com.google.zxing.frame;

import java.awt.Color;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link QrCodeTextElement} 单元测试。
 */
class QrCodeTextElementTests {

    @Test
    void builderUsesSensibleDefaults() {
        QrCodeTextElement element = QrCodeTextElement.builder("hello").build();

        assertThat(element.getText()).isEqualTo("hello");
        assertThat(element.getFontName()).isEqualTo("SansSerif");
        assertThat(element.getFontSize()).isEqualTo(24);
        assertThat(element.getColor()).isEqualTo(Color.BLACK);
        assertThat(element.isBold()).isFalse();
        assertThat(element.getX()).isZero();
        assertThat(element.getY()).isZero();
    }

    @Test
    void builderAppliesAllSetters() {
        QrCodeTextElement element = QrCodeTextElement.builder("hi")
                .bounds(10, 20, 200, 40)
                .zIndex(2)
                .font("Arial", 18, true)
                .color(new Color(50, 60, 70))
                .build();

        assertThat(element.getX()).isEqualTo(10);
        assertThat(element.getY()).isEqualTo(20);
        assertThat(element.getWidth()).isEqualTo(200);
        assertThat(element.getHeight()).isEqualTo(40);
        assertThat(element.getZIndex()).isEqualTo(2);
        assertThat(element.getFontName()).isEqualTo("Arial");
        assertThat(element.getFontSize()).isEqualTo(18);
        assertThat(element.isBold()).isTrue();
        assertThat(element.getColor()).isEqualTo(new Color(50, 60, 70));
    }

    @Test
    void blankFontNameFallsBackToSansSerif() {
        QrCodeTextElement element = QrCodeTextElement.builder("hi")
                .font("  ", 18, false)
                .build();

        assertThat(element.getFontName()).isEqualTo("SansSerif");
    }

    @Test
    void rejectsBlankTextOrNonPositiveFontSize() {
        assertThatThrownBy(() -> QrCodeTextElement.builder("").build())
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> QrCodeTextElement.builder("ok").font("Arial", 0, false).build())
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> QrCodeTextElement.builder("ok").font("Arial", -1, false).build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNullColor() {
        assertThatThrownBy(() -> QrCodeTextElement.builder("ok").color(null).build())
                .isInstanceOf(NullPointerException.class);
    }
}
