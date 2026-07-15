package com.google.zxing.model;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import com.google.zxing.exception.QrCodeErrorCode;
import com.google.zxing.exception.QrCodeException;
import lombok.Getter;

/**
 * 不可变的 QR Code 解码请求。
 *
 * <p>输入来源（互斥，必传其一）：
 * <ul>
 *     <li>{@link #from(byte[])}；</li>
 *     <li>{@link #from(BufferedImage)}；</li>
 *     <li>{@link #from(File)} / {@link #from(Path)} —— 内部读取字节；</li>
 *     <li>{@link #from(InputStream)} —— 内部读取字节；库不会主动关闭调用方的流。</li>
 * </ul>
 *
 * <p>默认值：charset = UTF-8；{@code tryHarder = true}；{@code alsoInverted = true}；
 * {@code maxInputBytes = 10 MiB}；{@code maxPixels = 16,777,216}。
 */
@Getter
public final class QrCodeDecodeRequest {

    private final byte[] bytes;
    private final BufferedImage image;
    private final Charset charset;
    private final boolean multiple;
    private final boolean tryHarder;
    private final boolean alsoInverted;
    private final boolean pureBarcode;
    private final int maxInputBytes;
    private final long maxPixels;

    private QrCodeDecodeRequest(Builder builder) {
        if (Objects.isNull(builder.bytes) && Objects.isNull(builder.image)) {
            throw new IllegalArgumentException("bytes or image must be provided");
        }
        this.bytes = Objects.isNull(builder.bytes) ? null : builder.bytes.clone();
        this.image = builder.image;
        this.charset = Objects.requireNonNull(builder.charset, "charset must not be null");
        this.multiple = builder.multiple;
        this.tryHarder = builder.tryHarder;
        this.alsoInverted = builder.alsoInverted;
        this.pureBarcode = builder.pureBarcode;
        this.maxInputBytes = builder.maxInputBytes;
        this.maxPixels = builder.maxPixels;
    }

    /**
     * @return 内部字节数组的防御性拷贝
     */
    public byte[] getBytes() {
        return Objects.isNull(bytes) ? null : bytes.clone();
    }

    /**
     * @param bytes 编码字节
     * @return 新 Builder
     */
    public static Builder from(byte[] bytes) {
        return new Builder(bytes, null);
    }

    /**
     * @param image 栅格图像
     * @return 新 Builder
     */
    public static Builder from(BufferedImage image) {
        return new Builder(null, image);
    }

    /**
     * @param file 图像文件
     * @return 新 Builder
     * @throws QrCodeException 当文件无法读取时抛出
     */
    public static Builder from(File file) {
        Objects.requireNonNull(file, "file must not be null");
        return from(file.toPath());
    }

    /**
     * @param path 图像路径
     * @return 新 Builder
     * @throws QrCodeException 当路径无法读取时抛出
     */
    public static Builder from(Path path) {
        Objects.requireNonNull(path, "path must not be null");
        try {
            return from(Files.readAllBytes(path));
        } catch (IOException ex) {
            throw new QrCodeException(QrCodeErrorCode.QRCODE_RENDER_FAILED, "Failed to read QR code image", ex);
        }
    }

    /**
     * @param inputStream 字节流；库不负责关闭
     * @return 新 Builder
     * @throws QrCodeException 当读取失败时抛出
     */
    public static Builder from(InputStream inputStream) {
        Objects.requireNonNull(inputStream, "inputStream must not be null");
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                if (length > 0) {
                    outputStream.write(buffer, 0, length);
                }
            }
            return from(outputStream.toByteArray());
        } catch (IOException ex) {
            throw new QrCodeException(QrCodeErrorCode.QRCODE_RENDER_FAILED, "Failed to read QR code stream", ex);
        }
    }

    /**
     * 请求链式构造器。
     */
    public static final class Builder {

        private final byte[] bytes;
        private final BufferedImage image;
        private Charset charset = StandardCharsets.UTF_8;
        private boolean multiple;
        private boolean tryHarder = true;
        private boolean alsoInverted = true;
        private boolean pureBarcode;
        private int maxInputBytes = 10 * 1024 * 1024;
        private long maxPixels = 16_777_216L;

        private Builder(byte[] bytes, BufferedImage image) {
            this.bytes = bytes;
            this.image = image;
        }

        /**
         * @param charset 字符集；不能为 {@code null}
         * @return 当前 builder
         */
        public Builder charset(Charset charset) {
            this.charset = charset;
            return this;
        }

        /**
         * @param multiple 是否启用多码解析
         * @return 当前 builder
         */
        public Builder multiple(boolean multiple) {
            this.multiple = multiple;
            return this;
        }

        /**
         * @param tryHarder 是否启用 {@code TRY_HARDER} hint
         * @return 当前 builder
         */
        public Builder tryHarder(boolean tryHarder) {
            this.tryHarder = tryHarder;
            return this;
        }

        /**
         * @param alsoInverted 是否启用 {@code ALSO_INVERTED} hint
         * @return 当前 builder
         */
        public Builder alsoInverted(boolean alsoInverted) {
            this.alsoInverted = alsoInverted;
            return this;
        }

        /**
         * @param pureBarcode 是否启用 {@code PURE_BARCODE} hint
         * @return 当前 builder
         */
        public Builder pureBarcode(boolean pureBarcode) {
            this.pureBarcode = pureBarcode;
            return this;
        }

        /**
         * @param maxInputBytes 输入字节上限；必须为正
         * @return 当前 builder
         */
        public Builder maxInputBytes(int maxInputBytes) {
            this.maxInputBytes = maxInputBytes;
            return this;
        }

        /**
         * @param maxPixels 解码图像像素上限；必须为正
         * @return 当前 builder
         */
        public Builder maxPixels(long maxPixels) {
            this.maxPixels = maxPixels;
            return this;
        }

        /**
         * @return 不可变请求
         */
        public QrCodeDecodeRequest build() {
            if (maxInputBytes <= 0 || maxPixels <= 0) {
                throw new IllegalArgumentException("decode limits must be positive");
            }
            return new QrCodeDecodeRequest(this);
        }
    }
}
