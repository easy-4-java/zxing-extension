package com.google.zxing;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import javax.imageio.ImageIO;

import com.google.zxing.common.BitMatrix;
import com.google.zxing.exception.CodeException;
import com.google.zxing.model.CodeOutput;
import com.google.zxing.source.MatrixToImageWriter;

/**
 * Shared image input / output helper for the non-QR code paths (Aztec and
 * one-dimensional barcodes).
 *
 * <p>The class is package-private and all of its methods are static. Every
 * {@link IOException} is wrapped as a {@link CodeException} so that the public
 * facades ({@link AztecCodes}, {@link BarCodes}) only need to deal with a
 * single exception type.</p>
 *
 * <p>The caller-supplied {@link InputStream} passed to {@link #read(InputStream)}
 * is <strong>not</strong> closed by this class &mdash; only the internal
 * streams opened by {@link #read(Path)} and {@link #toPng(BitMatrix)} are
 * closed automatically via try-with-resources.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see CodeException
 */
final class CodeImageSupport {

    /**
     * ImageIO format name used for PNG rendering.
     */
    private static final String PNG_FORMAT = "png";

    /**
     * Standard MIME type associated with PNG output.
     */
    private static final String PNG_MIME_TYPE = "image/png";

    private CodeImageSupport() {
    }

    /**
     * Renders a {@link BitMatrix} as a PNG-encoded {@link CodeOutput}.
     *
     * @param matrix the bit matrix produced by ZXing; must not be {@code null}
     * @return a PNG-encoded output containing the rendered image
     * @throws CodeException if no PNG writer is registered or rendering fails
     */
    static CodeOutput toPng(BitMatrix matrix) {
        BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            if (!ImageIO.write(image, PNG_FORMAT, outputStream)) {
                throw new CodeException("No ImageIO writer is available for PNG");
            }
            return new CodeOutput(outputStream.toByteArray(), PNG_MIME_TYPE, image.getWidth(), image.getHeight(), image);
        } catch (IOException ex) {
            throw new CodeException("Failed to render code image", ex);
        }
    }

    /**
     * Reads a {@link BufferedImage} from a raw byte array (PNG, JPEG, etc.).
     *
     * @param bytes the encoded image bytes; must not be {@code null}
     * @return the decoded raster image
     * @throws CodeException if the bytes do not represent a supported raster image
     */
    static BufferedImage read(byte[] bytes) {
        Objects.requireNonNull(bytes, "bytes must not be null");
        return read(new ByteArrayInputStream(bytes));
    }

    /**
     * Reads a {@link BufferedImage} from a {@link File}.
     *
     * @param file the image file; must not be {@code null}
     * @return the decoded raster image
     * @throws CodeException on I/O failure or if the file is not a supported image
     */
    static BufferedImage read(File file) {
        Objects.requireNonNull(file, "file must not be null");
        return read(file.toPath());
    }

    /**
     * Reads a {@link BufferedImage} from a {@link Path}; the library opens and
     * closes its own stream internally.
     *
     * @param path the file path; must not be {@code null}
     * @return the decoded raster image
     * @throws CodeException on I/O failure or if the file is not a supported image
     */
    static BufferedImage read(Path path) {
        Objects.requireNonNull(path, "path must not be null");
        try (InputStream inputStream = Files.newInputStream(path)) {
            return read(inputStream);
        } catch (IOException ex) {
            throw new CodeException("Failed to read code image", ex);
        }
    }

    /**
     * Reads a {@link BufferedImage} from a caller-owned {@link InputStream}.
     * The stream is <strong>not</strong> closed by this method.
     *
     * @param inputStream the byte stream; must not be {@code null}
     * @return the decoded raster image
     * @throws CodeException on I/O failure or if the stream does not produce a
     *         supported raster image
     */
    static BufferedImage read(InputStream inputStream) {
        Objects.requireNonNull(inputStream, "inputStream must not be null");
        try {
            BufferedImage image = ImageIO.read(inputStream);
            if (Objects.isNull(image)) {
                throw new CodeException("Input is not a supported raster image");
            }
            return image;
        } catch (IOException ex) {
            throw new CodeException("Failed to read code image", ex);
        }
    }
}