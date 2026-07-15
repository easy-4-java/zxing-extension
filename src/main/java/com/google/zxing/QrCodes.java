package com.google.zxing;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;

import com.google.zxing.model.QrCodeDecodeRequest;
import com.google.zxing.model.QrCodeDecodeResult;
import com.google.zxing.model.QrCodeLogo;
import com.google.zxing.model.QrCodeOutput;
import com.google.zxing.model.QrCodeRequest;
import com.google.zxing.model.QrCodeStyle;

/**
 * QR Code 静态门面。
 *
 * <p>对外暴露 {@link QrCodeEncoder} 与 {@link QrCodeDecoder} 的默认实现，并提供若干
 * 快捷方法（如 {@link #encode(String)}、{@link #colorful(String)}、{@link #withLogo(String, BufferedImage)}）
 * 用于常见场景。线程安全：内部使用单例的 {@link DefaultQrCodeEncoder} /
 * {@link DefaultQrCodeDecoder}。该门面不进行远程 URL 加载，不持有任何状态。
 */
public final class QrCodes {

    private static final QrCodeEncoder ENCODER = new DefaultQrCodeEncoder();
    private static final QrCodeDecoder DECODER = new DefaultQrCodeDecoder();

    private QrCodes() {
    }

    /**
     * 返回默认 QR 编码器（{@link DefaultQrCodeEncoder} 单例）。
     *
     * @return 不变量共享的默认编码器
     */
    public static QrCodeEncoder encoder() {
        return ENCODER;
    }

    /**
     * 返回默认 QR 解码器（{@link DefaultQrCodeDecoder} 单例）。
     *
     * @return 不变量共享的默认解码器
     */
    public static QrCodeDecoder decoder() {
        return DECODER;
    }

    /**
     * 生成单色 PNG 二维码，使用 {@link QrCodeRequest} 的默认值。
     *
     * @param content 要编码的内容；不能为 {@code null} 或空白
     * @return 编码后的 PNG 二维码
     * @throws com.google.zxing.exception.QrCodeException 编码失败时抛出
     */
    public static QrCodeOutput encode(String content) {
        return ENCODER.encode(QrCodeRequest.builder(content).build());
    }

    /**
     * 生成彩色渐变 PNG 二维码，使用 {@link QrCodeStyle#colorful()} 默认风格。
     *
     * @param content 要编码的内容；不能为 {@code null} 或空白
     * @return 编码后的 PNG 二维码
     * @throws com.google.zxing.exception.QrCodeException 编码失败时抛出
     */
    public static QrCodeOutput colorful(String content) {
        return ENCODER.encode(QrCodeRequest.builder(content)
                .style(QrCodeStyle.colorful())
                .build());
    }

    /**
     * 生成 PNG 二维码并将给定图片居中放置为 Logo；自动将纠错级别上调到 {@code H}。
     *
     * @param content 要编码的内容；不能为 {@code null} 或空白
     * @param logo    居中绘制的 Logo 图片；不能为 {@code null}
     * @return 编码后的 PNG 二维码
     * @throws com.google.zxing.exception.QrCodeException 编码失败时抛出
     */
    public static QrCodeOutput withLogo(String content, BufferedImage logo) {
        return ENCODER.encode(QrCodeRequest.builder(content)
                .logo(QrCodeLogo.builder(logo).build())
                .build());
    }

    /**
     * 从编码图像字节中解析第一条 QR 记录。
     *
     * @param bytes  PNG/JPEG 等编码图像字节；不能为 {@code null}
     * @return 解析得到的首条记录
     * @throws com.google.zxing.exception.QrCodeException 未找到或解码失败时抛出
     */
    public static QrCodeDecodeResult decode(byte[] bytes) {
        return DECODER.decodeFirst(QrCodeDecodeRequest.from(bytes).build());
    }

    /**
     * 从 {@link BufferedImage} 中解析第一条 QR 记录。
     *
     * @param image 图像；不能为 {@code null}
     * @return 解析得到的首条记录
     * @throws com.google.zxing.exception.QrCodeException 未找到或解码失败时抛出
     */
    public static QrCodeDecodeResult decode(BufferedImage image) {
        return DECODER.decodeFirst(QrCodeDecodeRequest.from(image).build());
    }

    /**
     * 从图像文件中解析第一条 QR 记录。
     *
     * @param file PNG/JPEG 等图像文件；不能为 {@code null}
     * @return 解析得到的首条记录
     * @throws com.google.zxing.exception.QrCodeException 未找到或解码失败时抛出
     */
    public static QrCodeDecodeResult decode(File file) {
        return DECODER.decodeFirst(QrCodeDecodeRequest.from(file).build());
    }

    /**
     * 从 {@link Path} 指向的图像文件中解析第一条 QR 记录。
     *
     * @param path 图像路径；不能为 {@code null}
     * @return 解析得到的首条记录
     * @throws com.google.zxing.exception.QrCodeException 未找到、IO 失败或解码失败时抛出
     */
    public static QrCodeDecodeResult decode(Path path) {
        return DECODER.decodeFirst(QrCodeDecodeRequest.from(path).build());
    }

    /**
     * 从字节流中解析第一条 QR 记录。调用方负责流的关闭，库不会主动关闭它。
     *
     * @param inputStream 字节流；不能为 {@code null}
     * @return 解析得到的首条记录
     * @throws com.google.zxing.exception.QrCodeException 未找到、IO 失败或解码失败时抛出
     */
    public static QrCodeDecodeResult decode(InputStream inputStream) {
        return DECODER.decodeFirst(QrCodeDecodeRequest.from(inputStream).build());
    }
}
