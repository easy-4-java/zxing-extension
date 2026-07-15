package com.google.zxing.model;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.google.zxing.BarcodeFormat;

import lombok.Getter;

/**
 * 不可变的一维条形码编码请求。
 *
 * <p>默认值：
 * <ul>
 *     <li>{@link #DEFAULT_WIDTH} × {@link #DEFAULT_HEIGHT}（300 × 100）；</li>
 *     <li>margin = 10；</li>
 *     <li>charset = UTF-8。</li>
 * </ul>
 * {@link BarcodeFormat} 必须由调用方显式指定（{@link #builder(String, BarcodeFormat)}）。
 *
 * <p>校验：content 非空白；width/height 大于 0、margin 非负；format、charset 非空。
 */
@Getter
public final class BarCodeRequest {

    /** 默认输出宽度（像素）。 */
    public static final int DEFAULT_WIDTH = 300;
    /** 默认输出高度（像素）。 */
    public static final int DEFAULT_HEIGHT = 100;

    private final String content;
    private final BarcodeFormat format;
    private final int width;
    private final int height;
    private final int margin;
    private final Charset charset;

    private BarCodeRequest(Builder builder) {
        if (StringUtils.isBlank(builder.content)) {
            throw new IllegalArgumentException("content must not be blank");
        }
        if (builder.width <= 0 || builder.height <= 0 || builder.margin < 0) {
            throw new IllegalArgumentException("width and height must be positive and margin must not be negative");
        }
        this.content = builder.content;
        this.format = Objects.requireNonNull(builder.format, "format must not be null");
        this.width = builder.width;
        this.height = builder.height;
        this.margin = builder.margin;
        this.charset = Objects.requireNonNull(builder.charset, "charset must not be null");
    }

    /**
     * @param content 编码内容
     * @param format   条码格式
     * @return 新 Builder
     */
    public static Builder builder(String content, BarcodeFormat format) {
        return new Builder(content, format);
    }

    /**
     * 一维条形码请求链式构造器。
     */
    public static final class Builder {

        private final String content;
        private final BarcodeFormat format;
        private int width = DEFAULT_WIDTH;
        private int height = DEFAULT_HEIGHT;
        private int margin = 10;
        private Charset charset = StandardCharsets.UTF_8;

        private Builder(String content, BarcodeFormat format) {
            this.content = content;
            this.format = format;
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
        public BarCodeRequest build() {
            return new BarCodeRequest(this);
        }
    }
}
