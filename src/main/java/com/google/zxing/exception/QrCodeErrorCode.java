package com.google.zxing.exception;

/**
 * QR Code 引擎对外暴露的稳定错误分类。
 *
 * <p>每种值的语义：
 * <ul>
 *     <li>{@link #QRCODE_INVALID_ARGUMENT}：调用方参数非法（如 Logo 超出 20%）。</li>
 *     <li>{@link #QRCODE_CAPACITY_EXCEEDED}：内容超过所选纠错级别的容量。</li>
 *     <li>{@link #QRCODE_UNSUPPORTED_FORMAT}：输入格式不被 ImageIO/ZXing 识别。</li>
 *     <li>{@link #QRCODE_DECODE_NOT_FOUND}：图像中未发现任何 QR。</li>
 *     <li>{@link #QRCODE_IMAGE_TOO_LARGE}：像素量或字节数超出防御性上限。</li>
 *     <li>{@link #QRCODE_RENDER_FAILED}：渲染 PNG / SVG 或读取文件失败。</li>
 *     <li>{@link #QRCODE_SELF_CHECK_FAILED}：编码后反向解码与原始内容不一致。</li>
 * </ul>
 */
public enum QrCodeErrorCode {

    /** 参数非法。 */
    QRCODE_INVALID_ARGUMENT,
    /** 内容超过容量。 */
    QRCODE_CAPACITY_EXCEEDED,
    /** 不支持的格式。 */
    QRCODE_UNSUPPORTED_FORMAT,
    /** 未发现 QR 记录。 */
    QRCODE_DECODE_NOT_FOUND,
    /** 图像过大或字节超限。 */
    QRCODE_IMAGE_TOO_LARGE,
    /** 渲染失败。 */
    QRCODE_RENDER_FAILED,
    /** 自检失败。 */
    QRCODE_SELF_CHECK_FAILED
}
