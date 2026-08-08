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
 * Default {@link QrCodeEncoder} implementation powered by ZXing's
 * {@link MultiFormatWriter}.
 *
 * <p>Supported features:</p>
 * <ul>
 *   <li>Both PNG (raster) and SVG (vector) output formats.</li>
 *   <li>Foreground gradients, independent finder-eye colouring, logo overlays
 *       and decorative outer {@link QrCodeFrame}s.</li>
 *   <li>An optional {@code selfCheck} flag: when enabled the encoder decodes
 *       its own output via {@link DefaultQrCodeDecoder} to confirm the
 *       rendered content matches the source content.</li>
 * </ul>
 *
 * <p>Safety guards:</p>
 * <ul>
 *   <li>{@link #MAX_DIMENSION}: each side of the output is capped at 4096
 *       pixels.</li>
 *   <li>{@link #MAX_PIXELS}: the total pixel count is capped at 16,777,216
 *       (4096 &times; 4096).</li>
 *   <li>{@link #MAX_LOGO_RATIO}: a logo edge may not exceed 20&nbsp;% of the
 *       QR region's width or height.</li>
 * </ul>
 *
 * <p>Every ZXing / IO exception is translated into a {@link QrCodeException}
 * carrying a stable {@link QrCodeErrorCode}.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see QrCodeEncoder
 * @see QrCodeRequest
 */
public final class DefaultQrCodeEncoder implements QrCodeEncoder {

    /**
     * Maximum width or height of the output image, in pixels.
     */
    private static final int MAX_DIMENSION = 4096;

    /**
     * Maximum total pixel count of the output image.
     */
    private static final long MAX_PIXELS = 16_777_216L;

    /**
     * Maximum ratio of a logo edge relative to the underlying QR region edge.
     */
    private static final double MAX_LOGO_RATIO = 0.20D;

    /**
     * Shared, stateless ZXing writer used for every encode call.
     */
    private final MultiFormatWriter writer = new MultiFormatWriter();

    /**
     * Encodes the supplied {@link QrCodeRequest} into a {@link QrCodeOutput}.
     *
     * @param request the encode request; must not be {@code null}
     * @return an immutable {@link QrCodeOutput} containing the rendered bytes
     * @throws QrCodeException if the request is invalid, exceeds the
     *         configured safety limits, overflows capacity, or fails to render
     */
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

    /**
     * Renders the request as a PNG, optionally compositing the QR code onto a
     * decorative frame.
     *
     * @param request the encode request; must not be {@code null}
     * @return the PNG output
     * @throws WriterException if ZXing fails to produce the bit matrix
     * @throws IOException    on PNG writing failures
     */
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

    /**
     * Renders the bare QR code (no decorative frame) onto an ARGB image.
     *
     * @param request the encode request; must not be {@code null}
     * @param width   target QR width in pixels; must be positive
     * @param height  target QR height in pixels; must be positive
     * @return the rendered image
     * @throws WriterException if ZXing fails to produce the bit matrix
     */
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

    /**
     * Renders the QR code onto a decorative {@link QrCodeFrame} canvas.
     *
     * @param request the encode request; must not be {@code null}
     * @param frame   the frame to render into; must not be {@code null}
     * @return the composited image
     * @throws WriterException if ZXing fails to produce the bit matrix
     */
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

    /**
     * Renders a single text element using the supplied graphics context.
     *
     * @param graphics the target graphics context; must not be {@code null}
     * @param element  the text element to render; must not be {@code null}
     */
    private void drawText(Graphics2D graphics, QrCodeTextElement element) {
        int style = element.isBold() ? Font.BOLD : Font.PLAIN;
        graphics.setFont(new Font(element.getFontName(), style, element.getFontSize()));
        graphics.setColor(element.getColor());
        int baseline = Math.min(element.getY() + element.getHeight(), element.getY() + element.getFontSize());
        graphics.drawString(element.getText(), element.getX(), baseline);
    }

    /**
     * Draws the {@link QrCodeLogo} (with rounded background) onto the centre
     * of the supplied source image, enforcing the {@link #MAX_LOGO_RATIO} limit.
     *
     * @param source the image to overlay the logo onto; must not be {@code null}
     * @param logo   the logo configuration; must not be {@code null}
     * @throws QrCodeException if the logo's requested size exceeds the limit
     */
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

    /**
     * Renders the request as an SVG document, optionally compositing the QR
     * code onto a decorative frame.
     *
     * @param request the encode request; must not be {@code null}
     * @return the SVG output
     * @throws WriterException if ZXing fails to produce the bit matrix
     * @throws IOException    on embedded image writing failures
     */
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

    /**
     * Appends an SVG snippet that renders a single QR code region (optionally
     * with eye colour and logo overlay) at the supplied offset.
     *
     * @param svg     the SVG buffer to append to; must not be {@code null}
     * @param request the encode request; must not be {@code null}
     * @param xOffset horizontal offset in pixels
     * @param yOffset vertical offset in pixels
     * @param width   target QR width in pixels
     * @param height  target QR height in pixels
     * @throws WriterException if ZXing fails to produce the bit matrix
     * @throws IOException    on embedded image writing failures
     */
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

    /**
     * Appends a single SVG {@code <path>} element rendering either the data
     * modules or the finder-eye modules of the matrix.
     *
     * @param svg        the SVG buffer; must not be {@code null}
     * @param matrix     the bit matrix to render; must not be {@code null}
     * @param xOffset    horizontal offset in pixels
     * @param yOffset    vertical offset in pixels
     * @param fill       the SVG fill colour or gradient reference
     * @param finderSize the side length of the finder-eye squares (0 disables detection)
     * @param finderOnly when {@code true} only the finder-eye modules are drawn,
     *                   otherwise only the data modules are drawn
     */
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

    /**
     * Determines the side length of the finder-eye squares by reading the
     * top-left run-length of the enclosing rectangle.
     *
     * @param matrix the bit matrix; must not be {@code null}
     * @return the side length in pixels, or {@code 0} if the matrix is empty
     */
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

    /**
     * Returns whether the pixel at {@code (x, y)} belongs to a finder-eye square.
     *
     * @param matrix     the bit matrix; must not be {@code null}
     * @param x          pixel X coordinate
     * @param y          pixel Y coordinate
     * @param finderSize the side length of the finder-eye squares
     * @return {@code true} when the pixel lies inside one of the three finder-eye
     *         squares
     */
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

    /**
     * Returns whether {@code (x, y)} lies inside a square of the supplied side
     * length whose top-left corner is at {@code (left, top)}.
     *
     * @param x    pixel X coordinate
     * @param y    pixel Y coordinate
     * @param left square left coordinate
     * @param top  square top coordinate
     * @param size square side length
     * @return {@code true} when the pixel is inside the square
     */
    private boolean inSquare(int x, int y, int left, int top, int size) {
        return x >= left && x < left + size && y >= top && y < top + size;
    }

    /**
     * Appends the SVG {@code <defs>} block describing the QR gradient when the
     * supplied style is configured for gradient rendering.
     *
     * @param svg   the SVG buffer; must not be {@code null}
     * @param style the QR style; must not be {@code null}
     */
    private void appendGradientDefinition(StringBuilder svg, QrCodeStyle style) {
        if (style.isGradient()) {
            svg.append("<defs><linearGradient id=\"qr-gradient\" x1=\"0\" y1=\"0\" x2=\"0\" y2=\"1\">")
                    .append("<stop offset=\"0%\" stop-color=\"").append(color(style.getForegroundColor()))
                    .append("\"/><stop offset=\"100%\" stop-color=\"").append(color(style.getGradientEndColor()))
                    .append("\"/></linearGradient></defs>");
        }
    }

    /**
     * Appends an SVG {@code <text>} element rendering a text element.
     *
     * @param svg     the SVG buffer; must not be {@code null}
     * @param element the text element to render; must not be {@code null}
     */
    private void appendSvgText(StringBuilder svg, QrCodeTextElement element) {
        svg.append("<text x=\"").append(element.getX()).append("\" y=\"")
                .append(Math.min(element.getY() + element.getHeight(), element.getY() + element.getFontSize()))
                .append("\" fill=\"").append(color(element.getColor())).append("\" font-family=\"")
                .append(escapeXml(element.getFontName())).append("\" font-size=\"").append(element.getFontSize())
                .append("\" font-weight=\"").append(element.isBold() ? "bold" : "normal").append("\">")
                .append(escapeXml(element.getText())).append("</text>");
    }

    /**
     * Appends an SVG {@code <image>} element wrapping the supplied raster image
     * as a base-64 PNG.
     *
     * @param svg    the SVG buffer; must not be {@code null}
     * @param image  the raster image; must not be {@code null}
     * @param x      element left coordinate
     * @param y      element top coordinate
     * @param width  element width in pixels
     * @param height element height in pixels
     * @throws IOException on PNG writing failures
     */
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

    /**
     * Appends an SVG {@code <rect>} element with the supplied attributes.
     *
     * @param svg   the SVG buffer; must not be {@code null}
     * @param x     left coordinate
     * @param y     top coordinate
     * @param width width in pixels
     * @param height height in pixels
     * @param fill  fill colour (CSS-style)
     */
    private void appendRect(StringBuilder svg, int x, int y, int width, int height, String fill) {
        svg.append("<rect x=\"").append(x).append("\" y=\"").append(y).append("\" width=\"")
                .append(width).append("\" height=\"").append(height).append("\" fill=\"")
                .append(fill).append("\"/>");
    }

    /**
     * Creates the underlying {@link BitMatrix} via ZXing, applying charset,
     * error correction level and margin hints.
     *
     * @param request the encode request; must not be {@code null}
     * @param width   target width in pixels; must be positive
     * @param height  target height in pixels; must be positive
     * @return the generated bit matrix
     * @throws WriterException if ZXing fails to produce the bit matrix
     */
    private BitMatrix createMatrix(QrCodeRequest request, int width, int height) throws WriterException {
        Map<EncodeHintType, Object> hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, request.getCharset().name());
        hints.put(EncodeHintType.ERROR_CORRECTION, request.getErrorCorrectionLevel());
        hints.put(EncodeHintType.MARGIN, request.getMargin());
        return writer.encode(request.getContent(), BarcodeFormat.QR_CODE, width, height, hints);
    }

    /**
     * Validates that the request's output dimensions respect the configured
     * safety caps.
     *
     * @param request the encode request; must not be {@code null}
     * @throws QrCodeException when either dimension or the pixel count exceed
     *         the configured limits
     */
    private void validateDimensions(QrCodeRequest request) {
        int outputWidth = Objects.isNull(request.getFrame()) ? request.getWidth() : request.getFrame().getWidth();
        int outputHeight = Objects.isNull(request.getFrame()) ? request.getHeight() : request.getFrame().getHeight();
        if (outputWidth > MAX_DIMENSION || outputHeight > MAX_DIMENSION
                || (long) outputWidth * (long) outputHeight > MAX_PIXELS) {
            throw new QrCodeException(QrCodeErrorCode.QRCODE_IMAGE_TOO_LARGE,
                    "QR code output exceeds the maximum dimensions or total pixel count");
        }
    }

    /**
     * Decodes the freshly rendered output and verifies that the decoded text
     * matches the original request content.
     *
     * @param request the encode request; must not be {@code null}
     * @param output  the rendered output; must not be {@code null}
     * @throws QrCodeException carrying {@link QrCodeErrorCode#QRCODE_SELF_CHECK_FAILED}
     *         when the round-trip check fails
     */
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

    /**
     * Applies the encoder's standard high-quality rendering hints to the
     * supplied graphics context.
     *
     * @param graphics the graphics context to configure; must not be {@code null}
     */
    private void configureGraphics(Graphics2D graphics) {
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    /**
     * Renders an {@link Color} as a CSS-style hexadecimal string.
     *
     * @param color the colour to format; must not be {@code null}
     * @return a string of the form {@code #rrggbb}
     */
    private String color(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    /**
     * Escapes the five XML special characters in the supplied value.
     *
     * @param value the value to escape; must not be {@code null}
     * @return the XML-safe value
     */
    private String escapeXml(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&apos;");
    }
}