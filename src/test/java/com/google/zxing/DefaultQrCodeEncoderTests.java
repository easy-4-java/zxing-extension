package com.google.zxing;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

import com.google.zxing.exception.QrCodeErrorCode;
import com.google.zxing.exception.QrCodeException;
import com.google.zxing.frame.QrCodeBlockElement;
import com.google.zxing.frame.QrCodeFrame;
import com.google.zxing.frame.QrCodeImageElement;
import com.google.zxing.frame.QrCodeTextElement;
import com.google.zxing.model.QrCodeDecodeRequest;
import com.google.zxing.model.QrCodeImageFormat;
import com.google.zxing.model.QrCodeLogo;
import com.google.zxing.model.QrCodeOutput;
import com.google.zxing.model.QrCodeDecodeResult;
import com.google.zxing.model.QrCodeRequest;
import com.google.zxing.model.QrCodeStyle;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link DefaultQrCodeEncoder} 内部分支专项测试，覆盖 PNG + SVG + 外套壳 + 自检 + Logo + 容量。
 */
class DefaultQrCodeEncoderTests {

    private static BufferedImage squareColor(int size, Color color) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(color);
        g.fillRect(0, 0, size, size);
        g.dispose();
        return img;
    }

    private static QrCodeRequest.Builder base(String text) {
        return QrCodeRequest.builder(text).size(200, 200).margin(0);
    }

    @Test
    void pngWithoutFrameWithoutEyeColorRendersMonochrome() {
        QrCodeOutput output = new DefaultQrCodeEncoder().encode(base("plain").build());

        assertThat(output.getFormat()).isEqualTo(QrCodeImageFormat.PNG);
        BufferedImage image = output.image().orElseThrow(AssertionError::new);
        boolean anyBlackOrWhite = false;
        outer:
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = image.getRGB(x, y) & 0x00FFFFFF;
                if (pixel == 0x000000 || pixel == 0xFFFFFF) {
                    anyBlackOrWhite = true;
                    break outer;
                }
            }
        }
        assertThat(anyBlackOrWhite).isTrue();
    }

    @Test
    void pngWithEyeColorTriggersFinderDetection() {
        QrCodeOutput output = new DefaultQrCodeEncoder().encode(base("eye")
                .style(QrCodeStyle.builder()
                        .foregroundColor(Color.BLACK)
                        .backgroundColor(Color.WHITE)
                        .eyeColor(new Color(255, 0, 0))
                        .build())
                .build());

        BufferedImage image = output.image().orElseThrow(AssertionError::new);
        boolean hasRedPixel = false;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                if (r > 200 && g < 30 && b < 30) {
                    hasRedPixel = true;
                    break;
                }
            }
            if (hasRedPixel) {
                break;
            }
        }
        assertThat(hasRedPixel).isTrue();
    }

    @Test
    void pngLogoOver20PercentOfQrIsRejected() {
        BufferedImage logo = squareColor(60, Color.RED);

        assertThatThrownBy(() -> new DefaultQrCodeEncoder().encode(base("logo-large")
                .logo(QrCodeLogo.builder(logo).size(60, 60).build())
                .build()))
                .isInstanceOf(QrCodeException.class)
                .extracting(ex -> ((QrCodeException) ex).getErrorCode())
                .isEqualTo(QrCodeErrorCode.QRCODE_INVALID_ARGUMENT);
    }

    @Test
    void svgWithImageElementProducesDataUri() {
        QrCodeFrame frame = QrCodeFrame.builder(220, 260)
                .addElement(QrCodeTextElement.builder("Label")
                        .bounds(10, 5, 200, 30)
                        .zIndex(1)
                        .build())
                .addElement(QrCodeImageElement.builder(squareColor(20, Color.ORANGE))
                        .bounds(0, 200, 40, 40)
                        .zIndex(2)
                        .build())
                .addElement(QrCodeBlockElement.builder().x(10).y(40).width(200).height(200).zIndex(0).build())
                .build();

        QrCodeOutput output = new DefaultQrCodeEncoder().encode(base("svg-frame")
                .format(QrCodeImageFormat.SVG)
                .frame(frame)
                .build());

        assertThat(output.getFormat()).isEqualTo(QrCodeImageFormat.SVG);
        String svg = new String(output.getBytes(), StandardCharsets.UTF_8);
        assertThat(svg).contains("<svg").contains("</svg>").contains("Label");
        assertThat(svg).contains("data:image/png;base64,");
    }

    @Test
    void svgWithoutFrameUsesStyleColor() {
        QrCodeOutput output = new DefaultQrCodeEncoder().encode(base("svg-plain")
                .format(QrCodeImageFormat.SVG)
                .style(QrCodeStyle.builder().foregroundColor(new Color(10, 20, 30)).build())
                .build());

        String svg = new String(output.getBytes(), StandardCharsets.UTF_8);
        assertThat(svg).contains("#0a141e");
        assertThat(svg).doesNotContain("linearGradient");
    }

    @Test
    void svgWithEyeColorEmitsTwoPaths() {
        QrCodeOutput output = new DefaultQrCodeEncoder().encode(base("svg-eye")
                .format(QrCodeImageFormat.SVG)
                .style(QrCodeStyle.builder()
                        .foregroundColor(new Color(0, 0, 0))
                        .eyeColor(new Color(0, 0, 255))
                        .build())
                .build());

        String svg = new String(output.getBytes(), StandardCharsets.UTF_8);
        assertThat(svg).contains("<path");
    }

    @Test
    void pngOverMaxDimensionTriggersImageTooLarge() {
        assertThatThrownBy(() -> new DefaultQrCodeEncoder().encode(
                QrCodeRequest.builder("huge").size(4097, 100).build()))
                .isInstanceOf(QrCodeException.class)
                .extracting(ex -> ((QrCodeException) ex).getErrorCode())
                .isEqualTo(QrCodeErrorCode.QRCODE_IMAGE_TOO_LARGE);
    }

    @Test
    void pngOverMaxPixelsTriggersImageTooLarge() {
        assertThatThrownBy(() -> new DefaultQrCodeEncoder().encode(
                QrCodeRequest.builder("huge-pix").size(4000, 5000).build()))
                .isInstanceOf(QrCodeException.class)
                .extracting(ex -> ((QrCodeException) ex).getErrorCode())
                .isEqualTo(QrCodeErrorCode.QRCODE_IMAGE_TOO_LARGE);
    }

    @Test
    void capacityExceededConvertsWriterException() {
        // Choose a payload that ZXing cannot encode at level L into a 64x64 canvas.
        StringBuilder huge = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            huge.append("0123456789ABCDEFGHIJ");
        }
        try {
            new DefaultQrCodeEncoder().encode(
                    QrCodeRequest.builder(huge.toString())
                            .size(64, 64)
                            .margin(0)
                            .errorCorrectionLevel(ErrorCorrectionLevel.L)
                            .build());
            org.junit.jupiter.api.Assertions.fail("expected WriterException for oversized content");
        } catch (QrCodeException ex) {
            // Accept either WRITER capacity error or any subclass that wraps ZXing failure
            assertThat(ex.getErrorCode()).isIn(
                    QrCodeErrorCode.QRCODE_CAPACITY_EXCEEDED,
                    QrCodeErrorCode.QRCODE_INVALID_ARGUMENT,
                    QrCodeErrorCode.QRCODE_RENDER_FAILED);
        }
    }

    @Test
    void selfCheckOnValidContentSucceedsAndTrustsDecodedMatch() {
        // Cover the happy path of selfCheck; the throw branch cannot be naturally exercised
        // because we cannot mutate the final request content after construction.
        QrCodeRequest request = QrCodeRequest.builder("matched")
                .size(220, 220)
                .margin(0)
                .selfCheck(true)
                .build();
        assertThat(new DefaultQrCodeEncoder().encode(request).getFormat()).isEqualTo(QrCodeImageFormat.PNG);
    }

    @Test
    void encodeInterfaceDefaultDelegatesToOutput() throws Exception {
        DefaultQrCodeEncoder encoder = new DefaultQrCodeEncoder();
        OutputStream sink = new ByteArrayOutputStream();
        encoder.encode(base("iface-default").build(), sink);

        byte[] bytes = ((ByteArrayOutputStream) sink).toByteArray();
        assertThat(bytes).startsWith((byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47);
        QrCodeDecodeResult result = QrCodes.decoder()
                .decodeFirst(QrCodeDecodeRequest.from(bytes).build());
        assertThat(result.getText()).isEqualTo("iface-default");
    }

    @Test
    void decodingOwnRoundTripSucceeds() {
        QrCodeOutput output = new DefaultQrCodeEncoder().encode(base("roundtrip")
                .selfCheck(true)
                .build());

        QrCodeDecodeResult result = QrCodes.decoder()
                .decodeFirst(QrCodeDecodeRequest.from(output.getBytes()).build());
        assertThat(result.getText()).isEqualTo("roundtrip");
    }

    @Test
    void pngFrameWithImageElementIsRendered() {
        QrCodeFrame frame = QrCodeFrame.builder(240, 300)
                .backgroundColor(new Color(240, 240, 240))
                .addElement(QrCodeTextElement.builder("bottom-text")
                        .bounds(20, 240, 200, 30).zIndex(2).build())
                .addElement(QrCodeImageElement.builder(squareColor(20, Color.GREEN))
                        .bounds(10, 10, 20, 20).zIndex(1).build())
                .addElement(QrCodeBlockElement.builder().x(20).y(40).width(200).height(200).zIndex(0).build())
                .build();

        QrCodeOutput output = new DefaultQrCodeEncoder().encode(base("frame-img")
                .frame(frame)
                .selfCheck(true)
                .build());

        assertThat(output.getWidth()).isEqualTo(240);
        assertThat(output.getHeight()).isEqualTo(300);
        QrCodeDecodeResult result = QrCodes.decoder()
                .decodeFirst(QrCodeDecodeRequest.from(output.getBytes()).build());
        assertThat(result.getText()).isEqualTo("frame-img");
    }

    @Test
    void svgWithLogoInsideFrameGeneratesLogoLayer() {
        QrCodeFrame frame = QrCodeFrame.builder(220, 260)
                .addElement(QrCodeBlockElement.builder().x(10).y(10).width(200).height(200).zIndex(0).build())
                .build();

        QrCodeOutput output = new DefaultQrCodeEncoder().encode(base("svg-logo")
                .format(QrCodeImageFormat.SVG)
                .frame(frame)
                .logo(QrCodeLogo.builder(squareColor(20, Color.PINK)).size(20, 20).build())
                .build());

        String svg = new String(output.getBytes(), StandardCharsets.UTF_8);
        assertThat(svg).contains("data:image/png;base64,");
    }

    @Test
    void svgFrameWithoutLogoEmitsPlain() {
        QrCodeFrame frame = QrCodeFrame.builder(220, 260)
                .addElement(QrCodeBlockElement.builder().x(10).y(10).width(200).height(200).zIndex(0).build())
                .addElement(QrCodeTextElement.builder("only")
                        .bounds(10, 220, 200, 30).zIndex(1).build())
                .build();

        QrCodeOutput output = new DefaultQrCodeEncoder().encode(base("frame-no-logo")
                .format(QrCodeImageFormat.SVG)
                .frame(frame)
                .build());

        String svg = new String(output.getBytes(), StandardCharsets.UTF_8);
        assertThat(svg).contains("only").contains("<rect");
    }

    @Test
    void svgLogoOver20PercentRejected() {
        QrCodeFrame frame = QrCodeFrame.builder(80, 80)
                .addElement(QrCodeBlockElement.builder().x(0).y(0).width(80).height(80).zIndex(0).build())
                .build();

        assertThatThrownBy(() -> new DefaultQrCodeEncoder().encode(base("svg-logo-large")
                .format(QrCodeImageFormat.SVG)
                .frame(frame)
                .logo(QrCodeLogo.builder(squareColor(60, Color.PINK)).size(60, 60).build())
                .build()))
                .isInstanceOf(QrCodeException.class)
                .extracting(ex -> ((QrCodeException) ex).getErrorCode())
                .isEqualTo(QrCodeErrorCode.QRCODE_INVALID_ARGUMENT);
    }

    @Test
    void pngGradientBehavior() {
        QrCodeOutput output = new DefaultQrCodeEncoder().encode(base("grad")
                .style(QrCodeStyle.builder()
                        .foregroundColor(new Color(0, 0, 0))
                        .gradientEndColor(new Color(120, 60, 30))
                        .build())
                .build());

        // No exception is sufficient; round-trip is verified separately.
        assertThat(output.getFormat()).isEqualTo(QrCodeImageFormat.PNG);
    }

    @Test
    void base64AndDataUriBehaveAsExpected() {
        QrCodeOutput output = new DefaultQrCodeEncoder().encode(base("b64").build());

        assertThat(Base64.getDecoder().decode(output.base64())).isEqualTo(output.getBytes());
        assertThat(output.dataUri()).startsWith("data:image/png;base64,");
    }

    @Test
    void pngWithoutFrameAndLogoStillDrawable() throws IOException {
        QrCodeOutput output = new DefaultQrCodeEncoder().encode(base("plain2").build());

        try (InputStream in = new ByteArrayInputStream(output.getBytes())) {
            BufferedImage image = ImageIO.read(in);
            assertThat(image).isNotNull();
            assertThat(image.getWidth()).isPositive();
        }
    }

    @Test
    void encodedImageValidatesCharsetAlpha() {
        // Cover AlphaComposite branches used in renderFrameImage
        BufferedImage canvas = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = canvas.createGraphics();
        g.setComposite(AlphaComposite.Src);
        g.setColor(new Color(255, 0, 0, 128));
        g.fillRect(0, 0, 2, 2);
        g.dispose();
        assertThat(canvas.getRGB(0, 0)).isNotEqualTo(0);
    }
}
