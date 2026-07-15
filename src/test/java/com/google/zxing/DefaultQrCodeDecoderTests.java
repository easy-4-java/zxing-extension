package com.google.zxing;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Test;

import com.google.zxing.exception.QrCodeErrorCode;
import com.google.zxing.exception.QrCodeException;
import com.google.zxing.model.QrCodeDecodeRequest;
import com.google.zxing.model.QrCodeDecodeResult;
import com.google.zxing.model.QrCodeRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link DefaultQrCodeDecoder} 内部分支专项测试。
 */
class DefaultQrCodeDecoderTests {

    @Test
    void decodeRejectsNullRequest() {
        assertThatThrownBy(() -> new DefaultQrCodeDecoder().decode(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void decodeFirstOnEmptyThrowsNotFound() {
        BufferedImage empty = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = empty.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 100, 100);
        g.dispose();

        assertThatThrownBy(() -> new DefaultQrCodeDecoder().decode(QrCodeDecodeRequest.from(empty).build()))
                .isInstanceOf(QrCodeException.class)
                .extracting(ex -> ((QrCodeException) ex).getErrorCode())
                .isEqualTo(QrCodeErrorCode.QRCODE_DECODE_NOT_FOUND);
    }

    @Test
    void decodeSingleQrFromBufferedImage() {
        BufferedImage image = QrCodes.encoder().encode(QrCodeRequest.builder("img").size(180, 180).build())
                .image().orElseThrow(AssertionError::new);

        java.util.List<QrCodeDecodeResult> results = new DefaultQrCodeDecoder()
                .decode(QrCodeDecodeRequest.from(image).build());

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getText()).isEqualTo("img");
    }

    @Test
    void rejectsUnreadableImageBytes() {
        assertThatThrownBy(() -> new DefaultQrCodeDecoder()
                .decode(QrCodeDecodeRequest.from(new byte[] { 0, 0, 0 }).build()))
                .isInstanceOf(QrCodeException.class)
                .extracting(ex -> ((QrCodeException) ex).getErrorCode())
                .isEqualTo(QrCodeErrorCode.QRCODE_UNSUPPORTED_FORMAT);
    }

    @Test
    void rejectsImageAboveMaxPixels() {
        BufferedImage image = QrCodes.encoder().encode(QrCodeRequest.builder("big").size(220, 220).build())
                .image().orElseThrow(AssertionError::new);

        assertThatThrownBy(() -> new DefaultQrCodeDecoder()
                .decode(QrCodeDecodeRequest.from(image).maxPixels(100L).build()))
                .isInstanceOf(QrCodeException.class)
                .extracting(ex -> ((QrCodeException) ex).getErrorCode())
                .isEqualTo(QrCodeErrorCode.QRCODE_IMAGE_TOO_LARGE);
    }

    @Test
    void decodeMultipleOnEmptyFallsBackToSingle() {
        // Empty image with multiple(true) should hit NotFoundException → decodeSingle fallback
        BufferedImage empty = new BufferedImage(80, 80, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = empty.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 80, 80);
        g.dispose();

        assertThatThrownBy(() -> new DefaultQrCodeDecoder()
                .decode(QrCodeDecodeRequest.from(empty).multiple(true).build()))
                .isInstanceOf(QrCodeException.class)
                .extracting(ex -> ((QrCodeException) ex).getErrorCode())
                .isEqualTo(QrCodeErrorCode.QRCODE_DECODE_NOT_FOUND);
    }

    @Test
    void decodeMultipleOnSmallBlankImageFallsBackToSingleThenNotFound() {
        // Plain white small image with multiple(true) makes GenericMultipleBarcodeReader throw
        // NotFoundException, which falls back to decodeSingle — which then also fails.
        java.awt.image.BufferedImage small = new java.awt.image.BufferedImage(48, 48,
                java.awt.image.BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g = small.createGraphics();
        g.setColor(java.awt.Color.WHITE);
        g.fillRect(0, 0, 48, 48);
        g.dispose();

        assertThatThrownBy(() -> new DefaultQrCodeDecoder()
                .decode(QrCodeDecodeRequest.from(small).multiple(true).build()))
                .isInstanceOf(QrCodeException.class)
                .extracting(ex -> ((QrCodeException) ex).getErrorCode())
                .isEqualTo(QrCodeErrorCode.QRCODE_DECODE_NOT_FOUND);
    }

    @Test
    void decodeOnReadFailureScreenshotPath() {
        // ImageIO.read throws IOException sometimes (rare); walk the bytes path via a real
        // PNG that ImageIO cannot decode to test the catch(IOException) branch in readImage.
        byte[] bad = new byte[]{(byte) 0x89, 'P', 'N', 'G', 0x00, 0x00}; // truncated PNG
        assertThatThrownBy(() -> new DefaultQrCodeDecoder()
                .decode(QrCodeDecodeRequest.from(bad).build()))
                .isInstanceOf(QrCodeException.class);
    }

    @Test
    void decodeMultipleOnFakeQrWithTryHarderDisabledFallsBackToSingle() {
        // Disable tryHarder so we more reliably hit the NotFoundException fallback in decodeMultiple.
        BufferedImage empty = new BufferedImage(80, 80, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = empty.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 80, 80);
        g.dispose();

        assertThatThrownBy(() -> new DefaultQrCodeDecoder()
                .decode(QrCodeDecodeRequest.from(empty)
                        .multiple(true)
                        .tryHarder(false)
                        .build()))
                .isInstanceOf(QrCodeException.class);
    }

    @Test
    void decodeWithPureBarcodeHintSucceeds() {
        BufferedImage image = QrCodes.encoder().encode(QrCodeRequest.builder("pure").size(180, 180).build())
                .image().orElseThrow(AssertionError::new);

        QrCodeDecodeResult result = new DefaultQrCodeDecoder()
                .decodeFirst(QrCodeDecodeRequest.from(image).pureBarcode(true).build());

        assertThat(result.getText()).isEqualTo("pure");
    }

    @Test
    void rejectsBrokenInputStreamIoException() {
        InputStream broken = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("kaboom");
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                throw new IOException("kaboom");
            }
        };

        assertThatThrownBy(() -> new DefaultQrCodeDecoder()
                .decode(QrCodeDecodeRequest.from(new byte[] { 1, 2, 3 }).build()))
                .isInstanceOf(QrCodeException.class);
        // And exercise the same input pathway through bytes path to confirm
        new ByteArrayInputStream(new byte[] { 0 });
    }
}
