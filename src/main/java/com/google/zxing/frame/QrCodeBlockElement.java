package com.google.zxing.frame;

import lombok.Builder;
import lombok.Getter;

/**
 * 外套壳上 QR Code 的摆放位置。
 *
 * <p>由 Lombok {@code @Builder} 生成，字段含义见 {@link QrCodeFrameElement}。
 */
@Getter
@Builder
public final class QrCodeBlockElement implements QrCodeFrameElement {

    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final int zIndex;
}
