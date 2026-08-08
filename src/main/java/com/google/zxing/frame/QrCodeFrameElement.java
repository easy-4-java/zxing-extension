package com.google.zxing.frame;

/**
 * Position and zIndex contract for elements placed on a {@link QrCodeFrame} canvas.
 *
 * <p>All {@link QrCodeFrameElement} implementations must report their rectangular
 * bounds and z-order. {@link QrCodeFrame} uses these values to enforce full
 * containment within the canvas and to sort elements by z-order.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see QrCodeFrame
 * @see QrCodeBlockElement
 * @see QrCodeTextElement
 * @see QrCodeImageElement
 */
public interface QrCodeFrameElement {

    /**
     * Returns the left X coordinate of this element in pixels (non-negative).
     *
     * @return the left X coordinate
     */
    int getX();

    /**
     * Returns the top Y coordinate of this element in pixels (non-negative).
     *
     * @return the top Y coordinate
     */
    int getY();

    /**
     * Returns the width of this element in pixels (strictly positive).
     *
     * @return the width in pixels
     */
    int getWidth();

    /**
     * Returns the height of this element in pixels (strictly positive).
     *
     * @return the height in pixels
     */
    int getHeight();

    /**
     * Returns the z-order index; elements with higher values render on top of
     * elements with lower values. Elements sharing the same z-order preserve
     * their insertion order.
     *
     * @return the z-order index
     */
    int getZIndex();
}
