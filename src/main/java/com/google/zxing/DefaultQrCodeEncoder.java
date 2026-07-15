package com.google.zxing;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

import javax.imageio.ImageIO;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import com.google.zxing.exception.QrCodeErrorCode;
import com.google.zxing.exception.QrCodeException;
import com.google.zxing.frame.QrCodeBlockElement;
import com.google.zxing.frame.QrCodeFrame;
import com.google.zxing.frame.QrCodeFrameElement;
import com.google.zxing.frame.QrCodeImageElement;
import com.google.zxing.frame.QrCodeTextElement;
import com.google.zxing.model.QrCodeDecodeRequest;
import com.google.zxing.model.QrCodeImageFormat;
import com.google.zxing.model.QrCodeLogo;
import com.google.zxing.model.QrCodeOutput;
import com.google.zxing.model.QrCodeRequest;
import com.google.zxing.model.QrCodeStyle;

/**
 * 默认 QR Code 编码器，基于 ZXing {@link MultiFormatWriter} 实现。
 *
 * <p>支持：
 * <ul>
 *     <li>PNG 与 SVG 两种输出格式。</li>
 *     <li>渐变色、码眼独立着色、Logo 叠加、外套壳（Frame）装饰。</li>
 *     <li>可选的 {@code selfCheck}，编码后立即调用
 *         {@link DefaultQrCodeDecoder} 反向解码验证内容一致。</li>
 * </ul>
 *
 * <p>安全约束：
 * <ul>
 *     <li>{@link #MAX_DIMENSION}：单边不超过 4096 像素。</li>
 *     <li>{@link #MAX_PIXELS}：总像素数不超过 16,777,216（4096²）。</li>
 *     <li>{@link #MAX_LOGO_RATIO}：Logo 任意边不超过 QR 区域的 20%。</li>
 * </ul>
 * 任何 ZXing 异常都会被翻译为携带稳定 {@link QrCodeErrorCode} 的 {@link QrCodeException}。
 */
public final class DefaultQrCodeEncoder implements QrCodeEncoder {

    /** 单边最大像素。 */
    private static final int MAX_DIMENSION = 4096;
    /** 输出图像最大总像素。 */
    private static final long MAX_PIXELS = 16_777_216L;
    /** Logo 在 QR 区域中所占边长的最大比例。 */
    private static final double MAX_LOGO_RATIO = 0.20D;

    private final MultiFormatWriter writer = new MultiFormatWriter();

    @Override
    public QrCodeOutput encode(QrCodeRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        validateDimensions(request);
        try {
            QrCodeOutput output = request.getFormat() == QrCodeImageFormat.SVG
                    ? renderSvg(request)
                    : renderPng(request);
            if (request.isSelfCheck() && request.getFormat() == QrCodeImageFormat.PNG) {
                selfCheck(request, output);
            }
            return output;
        } catch (QrCodeException ex) {
            throw ex;
        } catch (WriterException ex) {
            throw new QrCodeException(QrCodeErrorCode.QRCODE_CAPACITY_EXCEEDED,
                    "QR code content cannot be encoded with the selected options", ex);
        } catch (IOException ex) {
            throw new QrCodeException(QrCodeErrorCode.QRCODE_RENDER_FAILED, "Failed to render QR code", ex);
        }
    }

    private QrCodeOutput renderPng(QrCodeRequest request) throws WriterException, IOException {
        BufferedImage image;
        if (Objects.isNull(request.getFrame())) {
            image = renderQrImage(request, request.getWidth(), request.getHeight());
        } else {
            image = renderFrameImage(request, request.getFrame());
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        if (!ImageIO.write(image, QrCodeImageFormat.PNG.getImageIoName(), outputStream)) {
            throw new QrCodeException(QrCodeErrorCode.QRCODE_UNSUPPORTED_FORMAT,
                    "No ImageIO writer is available for PNG");
        }
        return new QrCodeOutput(outputStream.toByteArray(), QrCodeImageFormat.PNG,
                image.getWidth(), image.getHeight(), image);
    }

    private BufferedImage renderQrImage(QrCodeRequest request, int width, int height) throws WriterException {
        BitMatrix matrix = createMatrix(request, width, height);
        BufferedImage image = new BufferedImage(matrix.getWidth(), matrix.getHeight(), BufferedImage.TYPE_INT_ARGB);
        int finderSize = Objects.nonNull(request.getStyle().getEyeColor()) ? finderSize(matrix) : 0;
        for (int y = 0; y < matrix.getHeight(); y++) {
            double ratio = matrix.getHeight() <= 1 ? 0D : (double) y / (double) (matrix.getHeight() - 1);
            int foreground = request.getStyle().colorAt(ratio).getRGB();
            int background = request.getStyle().getBackgroundColor().getRGB();
            for (int x = 0; x < matrix.getWidth(); x++) {
                int pixel = matrix.get(x, y) && isFinderPixel(matrix, x, y, finderSize)
                        && Objects.nonNull(request.getStyle().getEyeColor())
                        ? request.getStyle().getEyeColor().getRGB()
                        : matrix.get(x, y) ? foreground : background;
                image.setRGB(x, y, pixel);
            }
        }
        if (Objects.nonNull(request.getLogo())) {
            drawLogo(image, request.getLogo());
        }
        return image;
    }

    private BufferedImage renderFrameImage(QrCodeRequest request, QrCodeFrame frame) throws WriterException {
        BufferedImage canvas = new BufferedImage(frame.getWidth(), frame.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = canvas.createGraphics();
        try {
            configureGraphics(graphics);
            graphics.setComposite(AlphaComposite.Src);
            graphics.setColor(frame.getBackgroundColor());
            graphics.fillRect(0, 0, frame.getWidth(), frame.getHeight());
            graphics.setComposite(AlphaComposite.SrcOver);
            for (QrCodeFrameElement element : frame.getElements()) {
                if (element instanceof QrCodeBlockElement) {
                    BufferedImage qrImage = renderQrImage(request, element.getWidth(), element.getHeight());
                    graphics.drawImage(qrImage, element.getX(), element.getY(), element.getWidth(), element.getHeight(), null);
                } else if (element instanceof QrCodeImageElement) {
                    QrCodeImageElement imageElement = (QrCodeImageElement) element;
                    graphics.drawImage(imageElement.getImage(), imageElement.getX(), imageElement.getY(),
                            imageElement.getWidth(), imageElement.getHeight(), null);
                } else if (element instanceof QrCodeTextElement) {
                    drawText(graphics, (QrCodeTextElement) element);
                }
            }
        } finally {
            graphics.dispose();
        }
        return canvas;
    }

    private void drawText(Graphics2D graphics, QrCodeTextElement element) {
        int style = element.isBold() ? Font.BOLD : Font.PLAIN;
        graphics.setFont(new Font(element.getFontName(), style, element.getFontSize()));
        graphics.setColor(element.getColor());
        int baseline = Math.min(element.getY() + element.getHeight(), element.getY() + element.getFontSize());
        graphics.drawString(element.getText(), element.getX(), baseline);
    }

    private void drawLogo(BufferedImage source, QrCodeLogo logo) {
        int maxWidth = Math.max(1, (int) Math.floor(source.getWidth() * MAX_LOGO_RATIO));
        int maxHeight = Math.max(1, (int) Math.floor(source.getHeight() * MAX_LOGO_RATIO));
        int width = logo.getWidth() > 0 ? logo.getWidth() : Math.min(maxWidth, logo.getImage().getWidth());
        int height = logo.getHeight() > 0 ? logo.getHeight() : Math.min(maxHeight, logo.getImage().getHeight());
        if (width > maxWidth || height > maxHeight) {
            throw new QrCodeException(QrCodeErrorCode.QRCODE_INVALID_ARGUMENT,
                    "logo width and height must not exceed 20% of the QR code dimensions");
        }
        int x = (source.getWidth() - width) / 2;
        int y = (source.getHeight() - height) / 2;
        Graphics2D graphics = source.createGraphics();
        try {
            configureGraphics(graphics);
            int padding = logo.getPadding();
            graphics.setColor(logo.getBackgroundColor());
            graphics.fillRoundRect(x - padding, y - padding, width + padding * 2, height + padding * 2,
                    logo.getCornerRadius(), logo.getCornerRadius());
            graphics.drawImage(logo.getImage(), x, y, width, height, null);
        } finally {
            graphics.dispose();
        }
    }

    private QrCodeOutput renderSvg(QrCodeRequest request) throws WriterException, IOException {
        int canvasWidth = Objects.isNull(request.getFrame()) ? request.getWidth() : request.getFrame().getWidth();
        int canvasHeight = Objects.isNull(request.getFrame()) ? request.getHeight() : request.getFrame().getHeight();
        StringBuilder svg = new StringBuilder(8192);
        svg.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
                .append("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"").append(canvasWidth)
                .append("\" height=\"").append(canvasHeight).append("\" viewBox=\"0 0 ")
                .append(canvasWidth).append(' ').append(canvasHeight).append("\">");
        appendGradientDefinition(svg, request.getStyle());
        Color canvasBackground = Objects.isNull(request.getFrame())
                ? request.getStyle().getBackgroundColor() : request.getFrame().getBackgroundColor();
        appendRect(svg, 0, 0, canvasWidth, canvasHeight, color(canvasBackground));
        if (Objects.isNull(request.getFrame())) {
            appendSvgQr(svg, request, 0, 0, request.getWidth(), request.getHeight());
        } else {
            for (QrCodeFrameElement element : request.getFrame().getElements()) {
                if (element instanceof QrCodeBlockElement) {
                    appendSvgQr(svg, request, element.getX(), element.getY(), element.getWidth(), element.getHeight());
                } else if (element instanceof QrCodeTextElement) {
                    appendSvgText(svg, (QrCodeTextElement) element);
                } else if (element instanceof QrCodeImageElement) {
                    QrCodeImageElement imageElement = (QrCodeImageElement) element;
                    appendSvgImage(svg, imageElement.getImage(), imageElement.getX(), imageElement.getY(),
                            imageElement.getWidth(), imageElement.getHeight());
                }
            }
        }
        svg.append("</svg>");
        byte[] bytes = svg.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return new QrCodeOutput(bytes, QrCodeImageFormat.SVG, canvasWidth, canvasHeight, null);
    }

    private void appendSvgQr(StringBuilder svg, QrCodeRequest request, int xOffset, int yOffset, int width,
            int height) throws WriterException, IOException {
        BitMatrix matrix = createMatrix(request, width, height);
        appendRect(svg, xOffset, yOffset, width, height, color(request.getStyle().getBackgroundColor()));
        String fill = request.getStyle().isGradient() ? "url(#qr-gradient)" : color(request.getStyle().getForegroundColor());
        int finderSize = Objects.nonNull(request.getStyle().getEyeColor()) ? finderSize(matrix) : 0;
        appendSvgMatrixPath(svg, matrix, xOffset, yOffset, fill, finderSize, false);
        if (Objects.nonNull(request.getStyle().getEyeColor())) {
            appendSvgMatrixPath(svg, matrix, xOffset, yOffset, color(request.getStyle().getEyeColor()), finderSize, true);
        }
        if (Objects.nonNull(request.getLogo())) {
            QrCodeLogo logo = request.getLogo();
            int maxWidth = Math.max(1, (int) Math.floor(width * MAX_LOGO_RATIO));
            int maxHeight = Math.max(1, (int) Math.floor(height * MAX_LOGO_RATIO));
            int logoWidth = logo.getWidth() > 0 ? logo.getWidth() : Math.min(maxWidth, logo.getImage().getWidth());
            int logoHeight = logo.getHeight() > 0 ? logo.getHeight() : Math.min(maxHeight, logo.getImage().getHeight());
            if (logoWidth > maxWidth || logoHeight > maxHeight) {
                throw new QrCodeException(QrCodeErrorCode.QRCODE_INVALID_ARGUMENT,
                        "logo width and height must not exceed 20% of the QR code dimensions");
            }
            int logoX = xOffset + (width - logoWidth) / 2;
            int logoY = yOffset + (height - logoHeight) / 2;
            int padding = logo.getPadding();
            appendRect(svg, logoX - padding, logoY - padding, logoWidth + padding * 2,
                    logoHeight + padding * 2, color(logo.getBackgroundColor()));
            appendSvgImage(svg, logo.getImage(), logoX, logoY, logoWidth, logoHeight);
        }
    }

    private void appendSvgMatrixPath(StringBuilder svg, BitMatrix matrix, int xOffset, int yOffset,
            String fill, int finderSize, boolean finderOnly) {
        svg.append("<path fill=\"").append(fill).append("\" d=\"");
        for (int y = 0; y < matrix.getHeight(); y++) {
            int x = 0;
            while (x < matrix.getWidth()) {
                boolean selected = matrix.get(x, y)
                        && (isFinderPixel(matrix, x, y, finderSize) == finderOnly);
                if (!selected) {
                    x++;
                    continue;
                }
                int start = x;
                while (x < matrix.getWidth() && matrix.get(x, y)
                        && (isFinderPixel(matrix, x, y, finderSize) == finderOnly)) {
                    x++;
                }
                svg.append('M').append(xOffset + start).append(' ').append(yOffset + y)
                        .append('h').append(x - start).append("v1h-").append(x - start).append('z');
            }
        }
        svg.append("\"/>");
    }

    private int finderSize(BitMatrix matrix) {
        int[] rectangle = matrix.getEnclosingRectangle();
        if (Objects.isNull(rectangle)) {
            return 0;
        }
        int x = rectangle[0];
        int y = rectangle[1];
        int run = 0;
        while (x + run < matrix.getWidth() && matrix.get(x + run, y)) {
            run++;
        }
        return run;
    }

    private boolean isFinderPixel(BitMatrix matrix, int x, int y, int finderSize) {
        if (finderSize <= 0) {
            return false;
        }
        int[] rectangle = matrix.getEnclosingRectangle();
        if (Objects.isNull(rectangle)) {
            return false;
        }
        int left = rectangle[0];
        int top = rectangle[1];
        int right = left + rectangle[2] - finderSize;
        int bottom = top + rectangle[3] - finderSize;
        return inSquare(x, y, left, top, finderSize)
                || inSquare(x, y, right, top, finderSize)
                || inSquare(x, y, left, bottom, finderSize);
    }

    private boolean inSquare(int x, int y, int left, int top, int size) {
        return x >= left && x < left + size && y >= top && y < top + size;
    }

    private void appendGradientDefinition(StringBuilder svg, QrCodeStyle style) {
        if (style.isGradient()) {
            svg.append("<defs><linearGradient id=\"qr-gradient\" x1=\"0\" y1=\"0\" x2=\"0\" y2=\"1\">")
                    .append("<stop offset=\"0%\" stop-color=\"").append(color(style.getForegroundColor()))
                    .append("\"/><stop offset=\"100%\" stop-color=\"").append(color(style.getGradientEndColor()))
                    .append("\"/></linearGradient></defs>");
        }
    }

    private void appendSvgText(StringBuilder svg, QrCodeTextElement element) {
        svg.append("<text x=\"").append(element.getX()).append("\" y=\"")
                .append(Math.min(element.getY() + element.getHeight(), element.getY() + element.getFontSize()))
                .append("\" fill=\"").append(color(element.getColor())).append("\" font-family=\"")
                .append(escapeXml(element.getFontName())).append("\" font-size=\"").append(element.getFontSize())
                .append("\" font-weight=\"").append(element.isBold() ? "bold" : "normal").append("\">")
                .append(escapeXml(element.getText())).append("</text>");
    }

    private void appendSvgImage(StringBuilder svg, BufferedImage image, int x, int y, int width, int height)
            throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        if (!ImageIO.write(image, "png", outputStream)) {
            throw new QrCodeException(QrCodeErrorCode.QRCODE_UNSUPPORTED_FORMAT,
                    "No ImageIO writer is available for embedded PNG images");
        }
        svg.append("<image x=\"").append(x).append("\" y=\"").append(y).append("\" width=\"")
                .append(width).append("\" height=\"").append(height)
                .append("\" href=\"data:image/png;base64,")
                .append(Base64.getEncoder().encodeToString(outputStream.toByteArray())).append("\"/>");
    }

    private void appendRect(StringBuilder svg, int x, int y, int width, int height, String fill) {
        svg.append("<rect x=\"").append(x).append("\" y=\"").append(y).append("\" width=\"")
                .append(width).append("\" height=\"").append(height).append("\" fill=\"")
                .append(fill).append("\"/>");
    }

    private BitMatrix createMatrix(QrCodeRequest request, int width, int height) throws WriterException {
        Map<EncodeHintType, Object> hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, request.getCharset().name());
        hints.put(EncodeHintType.ERROR_CORRECTION, request.getErrorCorrectionLevel());
        hints.put(EncodeHintType.MARGIN, request.getMargin());
        return writer.encode(request.getContent(), BarcodeFormat.QR_CODE, width, height, hints);
    }

    private void validateDimensions(QrCodeRequest request) {
        int outputWidth = Objects.isNull(request.getFrame()) ? request.getWidth() : request.getFrame().getWidth();
        int outputHeight = Objects.isNull(request.getFrame()) ? request.getHeight() : request.getFrame().getHeight();
        if (outputWidth > MAX_DIMENSION || outputHeight > MAX_DIMENSION
                || (long) outputWidth * (long) outputHeight > MAX_PIXELS) {
            throw new QrCodeException(QrCodeErrorCode.QRCODE_IMAGE_TOO_LARGE,
                    "QR code output exceeds the maximum dimensions or total pixel count");
        }
    }

    private void selfCheck(QrCodeRequest request, QrCodeOutput output) {
        try {
            String decoded = new DefaultQrCodeDecoder().decodeFirst(QrCodeDecodeRequest.from(output.getBytes()).build())
                    .getText();
            if (!request.getContent().equals(decoded)) {
                throw new QrCodeException(QrCodeErrorCode.QRCODE_SELF_CHECK_FAILED,
                        "Rendered QR code content did not match the source content");
            }
        } catch (QrCodeException ex) {
            if (ex.getErrorCode() == QrCodeErrorCode.QRCODE_SELF_CHECK_FAILED) {
                throw ex;
            }
            throw new QrCodeException(QrCodeErrorCode.QRCODE_SELF_CHECK_FAILED,
                    "Rendered QR code could not be decoded", ex);
        }
    }

    private void configureGraphics(Graphics2D graphics) {
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    private String color(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    private String escapeXml(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&apos;");
    }
}
