package com.google.zxing.frame;

import java.awt.Color;
import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link QrCodeFrame} 单元测试，覆盖排序、边界校验与强制 BlockElement 校验。
 */
class QrCodeFrameTests {

    private static QrCodeBlockElement block(int x, int y, int w, int h, int z) {
        return QrCodeBlockElement.builder().x(x).y(y).width(w).height(h).zIndex(z).build();
    }

    private static QrCodeTextElement text(int x, int y, int w, int h, int z) {
        return QrCodeTextElement.builder("hello").bounds(x, y, w, h).zIndex(z).build();
    }

    private static QrCodeImageElement image(int x, int y, int w, int h, int z) {
        BufferedImage img = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        return QrCodeImageElement.builder(img).bounds(x, y, w, h).zIndex(z).build();
    }

    @Test
    void elementsAreSortedByZIndex() {
        QrCodeFrame frame = QrCodeFrame.builder(300, 300)
                .addElement(text(10, 10, 50, 20, 5))
                .addElement(image(10, 40, 50, 50, 1))
                .addElement(block(0, 0, 100, 100, 3))
                .build();

        assertThat(frame.getElements()).extracting(QrCodeFrameElement::getZIndex)
                .containsExactly(1, 3, 5);
        assertThat(frame.getElements().get(0)).isInstanceOf(QrCodeImageElement.class);
        assertThat(frame.getElements().get(1)).isInstanceOf(QrCodeBlockElement.class);
        assertThat(frame.getElements().get(2)).isInstanceOf(QrCodeTextElement.class);
        assertThat(frame.getBackgroundColor()).isEqualTo(Color.WHITE);
        assertThat(frame.getElements()).isUnmodifiable();
    }

    @Test
    void builderCanOverrideBackgroundColor() {
        QrCodeFrame frame = QrCodeFrame.builder(100, 100)
                .backgroundColor(new Color(20, 30, 40))
                .addElement(block(0, 0, 100, 100, 0))
                .build();

        assertThat(frame.getBackgroundColor()).isEqualTo(new Color(20, 30, 40));
    }

    @Test
    void rejectsZeroOrNegativeCanvas() {
        assertThatThrownBy(() -> QrCodeFrame.builder(0, 100).build())
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> QrCodeFrame.builder(100, 0).build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNullBackgroundColor() {
        QrCodeFrame.Builder builder = QrCodeFrame.builder(100, 100)
                .backgroundColor(null)
                .addElement(block(0, 0, 100, 100, 0));
        assertThatThrownBy(builder::build).isInstanceOf(NullPointerException.class);
    }

    @Test
    void requiresAtLeastOneBlockElement() {
        assertThatThrownBy(() -> QrCodeFrame.builder(100, 100)
                .addElement(text(0, 0, 50, 20, 0))
                .build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsElementOutsideCanvas() {
        assertThatThrownBy(() -> QrCodeFrame.builder(100, 100)
                .addElement(block(50, 50, 60, 50, 0))
                .build())
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> QrCodeFrame.builder(100, 100)
                .addElement(block(-1, 0, 50, 50, 0))
                .build())
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> QrCodeFrame.builder(100, 100)
                .addElement(block(0, 0, 0, 50, 0))
                .build())
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> QrCodeFrame.builder(100, 100)
                .addElement(block(0, 0, 50, 0, 0))
                .build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNullElement() {
        QrCodeFrame.Builder builder = QrCodeFrame.builder(100, 100)
                .addElement(block(0, 0, 100, 100, 0))
                .addElement(null);
        assertThatThrownBy(builder::build).isInstanceOf(NullPointerException.class);
    }

    @Test
    void stableOrderingWhenSameZIndex() {
        // Same zIndex keeps insertion order (Comparator does not reorder them).
        QrCodeFrame frame = QrCodeFrame.builder(200, 200)
                .addElement(text(10, 10, 40, 20, 5))
                .addElement(text(60, 10, 40, 20, 5))
                .addElement(block(0, 0, 200, 200, 5))
                .build();
        assertThat(frame.getElements()).hasSize(3);
        assertThat(frame.getElements().get(0)).isInstanceOf(QrCodeTextElement.class);
        assertThat(frame.getElements().get(1)).isInstanceOf(QrCodeTextElement.class);
        assertThat(frame.getElements().get(2)).isInstanceOf(QrCodeBlockElement.class);
    }

    @Test
    void rejectsZeroOrNegativeHeightOnImageElement() throws Exception {
        BufferedImage img = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        // Build a valid element first, then mutate its width via reflection to a non-positive value
        // and inject it into the frame so validateBounds(width<=0) trips during Frame construction.
        QrCodeImageElement manual = QrCodeImageElement.builder(img).bounds(0, 0, 10, 10).build();
        java.lang.reflect.Field widthField = QrCodeImageElement.class.getDeclaredField("width");
        widthField.setAccessible(true);
        widthField.setInt(manual, 0);
        QrCodeFrame.Builder builder = QrCodeFrame.builder(50, 50)
                .addElement(block(0, 0, 50, 50, 0))
                .addElement(manual);
        assertThatThrownBy(builder::build).isInstanceOf(IllegalArgumentException.class);

        // Likewise mutate height = 0
        QrCodeImageElement manual2 = QrCodeImageElement.builder(img).bounds(0, 0, 10, 10).build();
        java.lang.reflect.Field heightField = QrCodeImageElement.class.getDeclaredField("height");
        heightField.setAccessible(true);
        heightField.setInt(manual2, 0);
        QrCodeFrame.Builder builder2 = QrCodeFrame.builder(50, 50)
                .addElement(block(0, 0, 50, 50, 0))
                .addElement(manual2);
        assertThatThrownBy(builder2::build).isInstanceOf(IllegalArgumentException.class);
    }
}
