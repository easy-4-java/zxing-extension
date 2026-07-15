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
 * 非 QR 路径（AztEc / BarCode）共享的图像输入/输出支持类，包私有。
 *
 * <p>该类的所有静态方法均为包私有，所有 IO 异常都会被包装为 {@link CodeException}；
 * 调用方传入的 {@link InputStream} 不会被关闭（仅 {@link #read(Path)} 与
 * {@link #toPng(BitMatrix)} 创建自己的内部流）。
 */
final class CodeImageSupport {

    private static final String PNG_FORMAT = "png";
    private static final String PNG_MIME_TYPE = "image/png";

    private CodeImageSupport() {
    }

    /**
     * 将 {@link BitMatrix} 渲染为 PNG 格式的 {@link CodeOutput}。
     *
     * @param matrix ZXing 生成的位图矩阵；不能为 {@code null}
     * @return 编码后的 PNG 输出
     * @throws CodeException 当没有可用的 PNG 写入器或渲染失败时抛出
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
     * 从字节数组读取 {@link BufferedImage}。
     *
     * @param bytes 编码字节；不能为 {@code null}
     * @return 解码后的图像
     * @throws CodeException 当输入不是合法的栅格图像时抛出
     */
    static BufferedImage read(byte[] bytes) {
        Objects.requireNonNull(bytes, "bytes must not be null");
        return read(new ByteArrayInputStream(bytes));
    }

    /**
     * 从 {@link File} 读取 {@link BufferedImage}。
     *
     * @param file 文件；不能为 {@code null}
     * @return 解码后的图像
     * @throws CodeException 当 IO 失败或非合法图像时抛出
     */
    static BufferedImage read(File file) {
        Objects.requireNonNull(file, "file must not be null");
        return read(file.toPath());
    }

    /**
     * 从 {@link Path} 读取 {@link BufferedImage}。库内部会打开并关闭流。
     *
     * @param path 文件路径；不能为 {@code null}
     * @return 解码后的图像
     * @throws CodeException 当 IO 失败或非合法图像时抛出
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
     * 从调用方持有的 {@link InputStream} 读取 {@link BufferedImage}。库不会关闭该流。
     *
     * @param inputStream 字节流；不能为 {@code null}
     * @return 解码后的图像
     * @throws CodeException 当 IO 失败或非合法图像时抛出
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
