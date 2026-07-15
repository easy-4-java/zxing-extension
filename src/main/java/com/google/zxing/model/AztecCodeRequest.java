package com.google.zxing.model;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.google.zxing.aztec.encoder.Encoder;

import lombok.Getter;

/**
 * 不可变的 Aztec 编码请求。
 *
 * <p>默认值：
 * <ul>
 *     <li>宽高：{@link #DEFAULT_SIZE}（258 像素）；</li>
 *     <li>纠错百分比：{@link #DEFAULT_ERROR_CORRECTION_PERCENT}（来自 ZXing
 *         {@code Encoder.DEFAULT_EC_PERCENT}）；</li>
 *     <li>margin：{@link #DEFAULT_MARGIN}（2 像素）；</li>
 *     <li>charset：UTF-8。</li>
 * </ul>
 *
 * <p>校验：content 非空白；width/height 大于 0；errorCorrectionPercent 在 [1,100]；margin 非负。
 */
@Getter
public final class AztecCodeRequest {

    /** 默认输出宽高（像素）。 */
    public static final int DEFAULT_SIZE = 258;
    /** 默认纠错百分比；通常为 ZXing 上游默认。 */
    public static final int DEFAULT_ERROR_CORRECTION_PERCENT = Encoder.DEFAULT_EC_PERCENT;
    /** 默认留白宽度（像素）。 */
    public static final int DEFAULT_MARGIN = 2;

    private final String content;
    private final int width;
    private final int height;
    private final int errorCorrectionPercent;
    private final int margin;
    private final Charset charset;

    private AztecCodeRequest(Builder builder) {
        if (StringUtils.isBlank(builder.content)) {
            throw new IllegalArgumentException("content must not be blank");
        }
        if (builder.width <= 0 || builder.height <= 0) {
            throw new IllegalArgumentException("width and height must be positive");
        }
        if (builder.errorCorrectionPercent <= 0 || builder.errorCorrectionPercent > 100) {
            throw new IllegalArgumentException("errorCorrectionPercent must be between 1 and 100");
        }
        if (builder.margin < 0) {
            throw new IllegalArgumentException("margin must not be negative");
        }
        this.content = builder.content;
        this.width = builder.width;
        this.height = builder.height;
        this.errorCorrectionPercent = builder.errorCorrectionPercent;
        this.margin = builder.margin;
        this.charset = Objects.requireNonNull(builder.charset, "charset must not be null");
    }

    /**
     * @param content 编码内容；不能为 {@code null} 或空白
     * @return 新 Builder
     */
    public static Builder builder(String content) {
        return new Builder(content);
    }

    /**
     * Aztec 请求链式构造器。
     */
    public static final class Builder {

        private final String content;
        private int width = DEFAULT_SIZE;
        private int height = DEFAULT_SIZE;
        private int errorCorrectionPercent = DEFAULT_ERROR_CORRECTION_PERCENT;
        private int margin = DEFAULT_MARGIN;
        private Charset charset = StandardCharsets.UTF_8;

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
         * @param errorCorrectionPercent 纠错百分比；取值 1-100
         * @return 当前 builder
         */
        public Builder errorCorrectionPercent(int errorCorrectionPercent) {
            this.errorCorrectionPercent = errorCorrectionPercent;
            return this;
        }

        /**
         * @param margin 留白（像素），非负
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
         * @return 不可变请求
         */
        public AztecCodeRequest build() {
            return new AztecCodeRequest(this);
        }
    }
}
