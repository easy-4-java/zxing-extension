package com.google.zxing;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.jupiter.api.Test;

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import com.google.zxing.frame.QrCodeBlockElement;
import com.google.zxing.frame.QrCodeFrame;
import com.google.zxing.frame.QrCodeTextElement;
import com.google.zxing.exception.QrCodeErrorCode;
import com.google.zxing.exception.QrCodeException;
import com.google.zxing.model.QrCodeDecodeRequest;
import com.google.zxing.model.QrCodeImageFormat;
import com.google.zxing.model.QrCodeLogo;
import com.google.zxing.model.QrCodeOutput;
import com.google.zxing.model.QrCodeRequest;
import com.google.zxing.model.QrCodeStyle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QrCodeEncoderTests {

    private static final String CONTENT = "https://example.com/二维码?source=zxing-extension";

    @Test
    void pngBase64AndDataUriRoundTrip() {
        QrCodeOutput output = QrCodes.encoder().encode(QrCodeRequest.builder(CONTENT)
                .size(320, 320)
                .errorCorrectionLevel(ErrorCorrectionLevel.H)
                .selfCheck(true)
                .build());

        assertThat(output.getFormat()).isEqualTo(QrCodeImageFormat.PNG);
        assertThat(output.getBytes()).startsWith((byte) 0x89, (byte) 0x50, (byte) 0x4e, (byte) 0x47);
        assertThat(Base64.getDecoder().decode(output.base64())).isEqualTo(output.getBytes());
        assertThat(output.dataUri()).startsWith("data:image/png;base64,");
        assertThat(QrCodes.decoder().decodeFirst(QrCodeDecodeRequest.from(output.getBytes()).build()).getText())
                .isEqualTo(CONTENT);
    }

    @Test
    void colorfulQuickEntryPointRoundTrips() {
        QrCodeOutput output = QrCodes.colorful(CONTENT);

        assertThat(output.getFormat()).isEqualTo(QrCodeImageFormat.PNG);
        assertThat(output.dataUri()).startsWith("data:image/png;base64,");
        assertThat(output.image()).isPresent();
        assertThat(QrCodeStyle.colorful().isGradient()).isTrue();
        assertThat(QrCodes.decoder().decodeFirst(QrCodeDecodeRequest.from(output.getBytes()).build()).getText())
                .isEqualTo(CONTENT);
    }

    @Test
    void monochromeAndLogoQuickEntryPointsRoundTrip() {
        BufferedImage logo = new BufferedImage(40, 40, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = logo.createGraphics();
        graphics.setColor(Color.BLUE);
        graphics.fillRect(0, 0, logo.getWidth(), logo.getHeight());
        graphics.dispose();

        assertThat(QrCodes.decode(QrCodes.encode(CONTENT).getBytes()).getText()).isEqualTo(CONTENT);
        assertThat(QrCodes.decode(QrCodes.withLogo(CONTENT, logo).getBytes()).getText()).isEqualTo(CONTENT);
    }

    @Test
    void unicodeRoundTripsAtEveryErrorCorrectionLevel() {
        for (ErrorCorrectionLevel level : ErrorCorrectionLevel.values()) {
            QrCodeOutput output = QrCodes.encoder().encode(QrCodeRequest.builder("中文-العربية-" + level)
                    .size(300, 300)
                    .errorCorrectionLevel(level)
                    .build());

            assertThat(QrCodes.decoder().decodeFirst(QrCodeDecodeRequest.from(output.getBytes()).build()).getText())
                    .isEqualTo("中文-العربية-" + level);
        }
    }

    @Test
    void rejectsCanvasBeyondDimensionLimit() {
        assertThatThrownBy(() -> QrCodes.encoder().encode(QrCodeRequest.builder("oversized")
                        .size(4097, 256)
                        .build()))
                .isInstanceOf(QrCodeException.class)
                .extracting(exception -> ((QrCodeException) exception).getErrorCode())
                .isEqualTo(QrCodeErrorCode.QRCODE_IMAGE_TOO_LARGE);
    }

    @Test
    void gradientLogoAndOuterFrameRemainDecodable() {
        BufferedImage logoImage = new BufferedImage(80, 40, BufferedImage.TYPE_INT_ARGB);
        Graphics2D logoGraphics = logoImage.createGraphics();
        logoGraphics.setColor(new Color(30, 100, 210));
        logoGraphics.fillRect(0, 0, 80, 40);
        logoGraphics.dispose();

        QrCodeFrame frame = QrCodeFrame.builder(420, 520)
                .backgroundColor(new Color(245, 247, 250))
                .addElement(QrCodeTextElement.builder("扫码查看详情")
                        .bounds(120, 28, 240, 42)
                        .font("SansSerif", 28, true)
                        .color(new Color(30, 41, 59))
                        .build())
                .addElement(QrCodeBlockElement.builder().x(50).y(100).width(320).height(320).zIndex(1).build())
                .addElement(QrCodeTextElement.builder("Powered by zxing-extension")
                        .bounds(82, 455, 300, 30)
                        .font("SansSerif", 18, false)
                        .color(Color.DARK_GRAY)
                        .zIndex(2)
                        .build())
                .build();

        QrCodeOutput output = QrCodes.encoder().encode(QrCodeRequest.builder(CONTENT)
                .size(320, 320)
                .style(QrCodeStyle.builder()
                        .foregroundColor(new Color(0, 122, 98))
                        .gradientEndColor(new Color(69, 54, 143))
                        .eyeColor(new Color(24, 45, 110))
                        .build())
                .logo(QrCodeLogo.builder(logoImage).size(56, 28).build())
                .frame(frame)
                .selfCheck(true)
                .build());

        assertThat(output.getWidth()).isEqualTo(420);
        assertThat(output.getHeight()).isEqualTo(520);
        assertThat(QrCodes.decoder().decodeFirst(QrCodeDecodeRequest.from(output.getBytes()).build()).getText())
                .isEqualTo(CONTENT);
    }

    @Test
    void svgSupportsGradientAndFrame() {
        QrCodeFrame frame = QrCodeFrame.builder(360, 420)
                .addElement(QrCodeTextElement.builder("QR & <safe>")
                        .bounds(100, 20, 220, 36).font("SansSerif", 24, true).build())
                .addElement(QrCodeBlockElement.builder().x(40).y(80).width(280).height(280).zIndex(1).build())
                .build();

        QrCodeOutput output = QrCodes.encoder().encode(QrCodeRequest.builder(CONTENT)
                .format(QrCodeImageFormat.SVG)
                .style(QrCodeStyle.builder().foregroundColor(Color.BLUE).gradientEndColor(Color.RED)
                        .eyeColor(Color.BLACK).build())
                .frame(frame)
                .build());

        String svg = new String(output.getBytes(), StandardCharsets.UTF_8);
        assertThat(output.getFormat()).isEqualTo(QrCodeImageFormat.SVG);
        assertThat(output.image()).isEmpty();
        assertThat(svg).contains("<svg", "linearGradient", "QR &amp; &lt;safe&gt;", "<path");
        assertThat(output.dataUri()).startsWith("data:image/svg+xml;base64,");
    }
}
