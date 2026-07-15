package com.google.zxing.model;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import com.google.zxing.frame.QrCodeBlockElement;
import com.google.zxing.frame.QrCodeFrame;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link QrCodeRequest} 单元测试，覆盖 Builder 全部链式 setter 与构造器校验分支。
 */
class QrCodeRequestTests {

    @Test
    void builderWithContentOnlyUsesDefaults() {
        QrCodeRequest request = QrCodeRequest.builder("hello").build();

        assertThat(request.getContent()).isEqualTo("hello");
        assertThat(request.getWidth()).isEqualTo(QrCodeRequest.DEFAULT_SIZE);
        assertThat(request.getHeight()).isEqualTo(QrCodeRequest.DEFAULT_SIZE);
        assertThat(request.getMargin()).isEqualTo(2);
        assertThat(request.getCharset()).isEqualTo(StandardCharsets.UTF_8);
        assertThat(request.getErrorCorrectionLevel()).isEqualTo(ErrorCorrectionLevel.M);
        assertThat(request.getFormat()).isEqualTo(QrCodeImageFormat.PNG);
        assertThat(request.getStyle()).isNotNull();
        assertThat(request.getLogo()).isNull();
        assertThat(request.getFrame()).isNull();
        assertThat(request.isSelfCheck()).isFalse();
    }

    @Test
    void builderAppliesEverySetter() {
        Charset charset = StandardCharsets.ISO_8859_1;
        QrCodeStyle style = QrCodeStyle.colorful();
        QrCodeLogo logo = QrCodeLogo.builder(new java.awt.image.BufferedImage(8, 8, java.awt.image.BufferedImage.TYPE_INT_RGB)).build();
        QrCodeFrame frame = QrCodeFrame.builder(200, 200)
                .addElement(QrCodeBlockElement.builder().x(0).y(0).width(200).height(200).zIndex(0).build())
                .build();

        QrCodeRequest request = QrCodeRequest.builder("data")
                .size(123, 456)
                .margin(7)
                .charset(charset)
                .errorCorrectionLevel(ErrorCorrectionLevel.H)
                .format(QrCodeImageFormat.SVG)
                .style(style)
                .logo(logo)
                .frame(frame)
                .selfCheck(true)
                .build();

        assertThat(request.getWidth()).isEqualTo(123);
        assertThat(request.getHeight()).isEqualTo(456);
        assertThat(request.getMargin()).isEqualTo(7);
        assertThat(request.getCharset()).isEqualTo(charset);
        assertThat(request.getErrorCorrectionLevel()).isEqualTo(ErrorCorrectionLevel.H);
        assertThat(request.getFormat()).isEqualTo(QrCodeImageFormat.SVG);
        assertThat(request.getStyle()).isSameAs(style);
        assertThat(request.getLogo()).isSameAs(logo);
        assertThat(request.getFrame()).isSameAs(frame);
        assertThat(request.isSelfCheck()).isTrue();
    }

    @Test
    void logoForcesErrorCorrectionLevelH() {
        QrCodeLogo logo = QrCodeLogo.builder(new java.awt.image.BufferedImage(8, 8, java.awt.image.BufferedImage.TYPE_INT_RGB)).build();

        QrCodeRequest request = QrCodeRequest.builder("data")
                .errorCorrectionLevel(ErrorCorrectionLevel.L)
                .logo(logo)
                .build();

        assertThat(request.getErrorCorrectionLevel()).isEqualTo(ErrorCorrectionLevel.H);
    }

    @Test
    void settingLogoToNullKeepsExistingErrorCorrectionLevel() {
        QrCodeLogo logo = QrCodeLogo.builder(new java.awt.image.BufferedImage(8, 8, java.awt.image.BufferedImage.TYPE_INT_RGB)).build();

        QrCodeRequest request = QrCodeRequest.builder("data")
                .logo(logo)
                .logo(null)
                .build();

        assertThat(request.getErrorCorrectionLevel()).isEqualTo(ErrorCorrectionLevel.H);
        assertThat(request.getLogo()).isNull();
    }

    @Test
    void rejectsBlankContent() {
        assertThatThrownBy(() -> QrCodeRequest.builder(" ").build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNonPositiveWidthOrHeightOrNegativeMargin() {
        assertThatThrownBy(() -> QrCodeRequest.builder("x").size(0, 100).build())
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> QrCodeRequest.builder("x").size(100, 0).build())
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> QrCodeRequest.builder("x").margin(-1).build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void buildRejectsNullForRequiredFields() {
        QrCodeRequest.Builder builder = QrCodeRequest.builder("x")
                .charset(null);
        assertThatThrownBy(builder::build).isInstanceOf(IllegalArgumentException.class);

        QrCodeRequest.Builder builder2 = QrCodeRequest.builder("x")
                .errorCorrectionLevel(null);
        assertThatThrownBy(builder2::build).isInstanceOf(IllegalArgumentException.class);

        QrCodeRequest.Builder builder3 = QrCodeRequest.builder("x")
                .format(null);
        assertThatThrownBy(builder3::build).isInstanceOf(IllegalArgumentException.class);

        QrCodeRequest.Builder builder4 = QrCodeRequest.builder("x")
                .style(null);
        assertThatThrownBy(builder4::build).isInstanceOf(IllegalArgumentException.class);
    }
}
