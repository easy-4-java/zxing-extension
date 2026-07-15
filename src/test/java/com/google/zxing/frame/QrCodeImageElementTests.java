package com.google.zxing.frame;

import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link QrCodeImageElement} 单元测试。
 */
class QrCodeImageElementTests {

    @Test
    void builderAppliesAllSetters() {
        BufferedImage img = new BufferedImage(40, 30, BufferedImage.TYPE_INT_RGB);
        QrCodeImageElement element = QrCodeImageElement.builder(img)
                .bounds(5, 10, 80, 60)
                .zIndex(7)
                .build();

        assertThat(element.getImage()).isSameAs(img);
        assertThat(element.getX()).isEqualTo(5);
        assertThat(element.getY()).isEqualTo(10);
        assertThat(element.getWidth()).isEqualTo(80);
        assertThat(element.getHeight()).isEqualTo(60);
        assertThat(element.getZIndex()).isEqualTo(7);
    }

    @Test
    void rejectsNullImage() {
        assertThatThrownBy(() -> QrCodeImageElement.builder(null).build())
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void rejectsNonPositiveDimensions() {
        BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        assertThatThrownBy(() -> QrCodeImageElement.builder(img).bounds(0, 0, 0, 10).build())
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> QrCodeImageElement.builder(img).bounds(0, 0, 10, 0).build())
                .isInstanceOf(IllegalArgumentException.class);
    }
}
