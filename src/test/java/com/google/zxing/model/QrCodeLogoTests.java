package com.google.zxing.model;

import java.awt.Color;
import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link QrCodeLogo} 单元测试。
 */
class QrCodeLogoTests {

    private static BufferedImage image(int w, int h) {
        return new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    }

    @Test
    void builderUsesSensibleDefaults() {
        QrCodeLogo logo = QrCodeLogo.builder(image(10, 10)).build();

        assertThat(logo.getImage()).isNotNull();
        assertThat(logo.getWidth()).isZero();
        assertThat(logo.getHeight()).isZero();
        assertThat(logo.getPadding()).isEqualTo(4);
        assertThat(logo.getCornerRadius()).isEqualTo(8);
        assertThat(logo.getBackgroundColor()).isEqualTo(Color.WHITE);
    }

    @Test
    void builderAppliesAllSetters() {
        QrCodeLogo logo = QrCodeLogo.builder(image(20, 30))
                .size(40, 50)
                .padding(12)
                .cornerRadius(6)
                .backgroundColor(Color.CYAN)
                .build();

        assertThat(logo.getWidth()).isEqualTo(40);
        assertThat(logo.getHeight()).isEqualTo(50);
        assertThat(logo.getPadding()).isEqualTo(12);
        assertThat(logo.getCornerRadius()).isEqualTo(6);
        assertThat(logo.getBackgroundColor()).isEqualTo(Color.CYAN);
    }

    @Test
    void builderRequiresImage() {
        assertThatThrownBy(() -> QrCodeLogo.builder(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void builderRejectsNegativeDimensions() {
        assertThatThrownBy(() -> QrCodeLogo.builder(image(10, 10)).padding(-1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> QrCodeLogo.builder(image(10, 10)).cornerRadius(-1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void builderRejectsNegativeSize() {
        assertThatThrownBy(() -> QrCodeLogo.builder(image(10, 10)).size(-1, 1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> QrCodeLogo.builder(image(10, 10)).size(1, -1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void builderRejectsNullBackgroundColor() {
        assertThatThrownBy(() -> QrCodeLogo.builder(image(10, 10)).backgroundColor(null))
                .isInstanceOf(NullPointerException.class);
    }
}
