package com.google.zxing.model;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import com.google.zxing.frame.QrCodeFrame;
import lombok.Getter;

/**
 * 不可变的 QR Code 编码请求。
 *
 * <p>通过 {@link #builder(String)} 创建；下列为默认值：
 * <ul>
 *     <li>宽高：{@link #DEFAULT_SIZE}（256 像素）；</li>
 *     <li>margin：2；</li>
 *     <li>charset：{@link StandardCharsets#UTF_8}；</li>
 *     <li>纠错：{@link ErrorCorrectionLevel#M}；</li>
 *     <li>格式：{@link QrCodeImageFormat#PNG}；</li>
 *     <li>样式：{@link QrCodeStyle#monochrome()}；</li>
 *     <li>Logo：{@code null}；调用 {@link Builder#logo(QrCodeLogo)} 自动将纠错升级到
 *         {@link ErrorCorrectionLevel#H} 以保留容错空间。</li>
 *     <li>自检：默认关闭。</li>
 * </ul>
 *
 * <p>构造器会校验：
 * <ul>
 *     <li>content 非空白；</li>
 *     <li>width/height 大于 0、margin 非负；</li>
 *     <li>build() 检查 charset / errorCorrectionLevel / format / style 均非 {@code null}。</li>
 * </ul>
 */
@Getter
public final class QrCodeRequest {

    /** 默认输出宽高（像素）。 */
    public static final int DEFAULT_SIZE = 256;

    private final String content;
    private final int width;
    private final int height;
    private final int margin;
    private final Charset charset;
    private final ErrorCorrectionLevel errorCorrectionLevel;
    private final QrCodeImageFormat format;
    private final QrCodeStyle style;
    private final QrCodeLogo logo;
    private final QrCodeFrame frame;
    private final boolean selfCheck;

    private QrCodeRequest(Builder builder) {
        if (StringUtils.isBlank(builder.content)) {
            throw new IllegalArgumentException("content must not be blank");
        }
        if (builder.width <= 0 || builder.height <= 0 || builder.margin < 0) {
            throw new IllegalArgumentException("width and height must be positive and margin must not be negative");
        }
        this.content = builder.content;
        this.width = builder.width;
        this.height = builder.height;
        this.margin = builder.margin;
        this.charset = builder.charset;
        this.errorCorrectionLevel = builder.errorCorrectionLevel;
        this.format = builder.format;
        this.style = builder.style;
        this.logo = builder.logo;
        this.frame = builder.frame;
        this.selfCheck = builder.selfCheck;
    }

    /**
     * @param content 待编码内容；不能为 {@code null} 或空白
     * @return 新 Builder
     */
    public static Builder builder(String content) {
        return new Builder(content);
    }

    /**
     * QR Code 请求的链式构造器。
     */
    public static final class Builder {

        private final String content;
        private int width = DEFAULT_SIZE;
        private int height = DEFAULT_SIZE;
        private int margin = 2;
        private Charset charset = StandardCharsets.UTF_8;
        private ErrorCorrectionLevel errorCorrectionLevel = ErrorCorrectionLevel.M;
        private QrCodeImageFormat format = QrCodeImageFormat.PNG;
        private QrCodeStyle style = QrCodeStyle.monochrome();
        private QrCodeLogo logo;
        private QrCodeFrame frame;
        private boolean selfCheck;

        private Builder(String content) {
            this.content = content;
        }

        /**
         * @param width  宽（像素），必须大于 0
         * @param height 高（像素），必须大于 0
         * @return 当前 builder
         */
        public Builder size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        /**
         * @param margin 边距（像素），必须大于等于 0
         * @return 当前 builder
         */
        public Builder margin(int margin) {
            this.margin = margin;
            return this;
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
         * @param errorCorrectionLevel 纠错级别；不能为 {@code null}
         * @return 当前 builder
         */
        public Builder errorCorrectionLevel(ErrorCorrectionLevel errorCorrectionLevel) {
            this.errorCorrectionLevel = errorCorrectionLevel;
            return this;
        }

        /**
         * @param format 输出格式；不能为 {@code null}
         * @return 当前 builder
         */
        public Builder format(QrCodeImageFormat format) {
            this.format = format;
            return this;
        }

        /**
         * @param style 颜色样式；不能为 {@code null}
         * @return 当前 builder
         */
        public Builder style(QrCodeStyle style) {
            this.style = style;
            return this;
        }

        /**
         * 设置 Logo。传入非 {@code null} 时会自动把纠错升级到 {@link ErrorCorrectionLevel#H}，
         * 以保留足够的容错空间。
         *
         * @param logo Logo；可为 {@code null}
         * @return 当前 builder
         */
        public Builder logo(QrCodeLogo logo) {
            this.logo = logo;
            if (Objects.nonNull(logo)) {
                this.errorCorrectionLevel = ErrorCorrectionLevel.H;
            }
            return this;
        }

        /**
         * @param frame 可选外套壳
         * @return 当前 builder
         */
        public Builder frame(QrCodeFrame frame) {
            this.frame = frame;
            return this;
        }

        /**
         * @param selfCheck 是否在编码后立即反向解码校验内容
         * @return 当前 builder
         */
        public Builder selfCheck(boolean selfCheck) {
            this.selfCheck = selfCheck;
            return this;
        }

        /**
         * @return 不可变请求
         */
        public QrCodeRequest build() {
            if (Objects.isNull(charset) || Objects.isNull(errorCorrectionLevel)
                    || Objects.isNull(format) || Objects.isNull(style)) {
                throw new IllegalArgumentException("charset, errorCorrectionLevel, format and style must not be null");
            }
            return new QrCodeRequest(this);
        }
    }
}
