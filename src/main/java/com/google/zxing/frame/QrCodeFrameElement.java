package com.google.zxing.frame;

/**
 * 外套壳上可定位元素的位置 + zIndex 契约。
 *
 * <p>所有 {@link QrCodeFrameElement} 都必须报告其矩形位置和层级，{@link QrCodeFrame}
 * 据此校验完全包含关系和 zIndex 排序。
 */
public interface QrCodeFrameElement {

    /**
     * @return 左上角 X（像素），非负
     */
    int getX();

    /**
     * @return 左上角 Y（像素），非负
     */
    int getY();

    /**
     * @return 宽度（像素），严格大于 0
     */
    int getWidth();

    /**
     * @return 高度（像素），严格大于 0
     */
    int getHeight();

    /**
     * @return zIndex，相同值保持添加顺序
     */
    int getZIndex();
}
