/*
 * Copyright (c) 2018, Loong Wan (https://github.com/loong10k).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.google.zxing.source;

import com.google.zxing.LuminanceSource;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

/**
 * A {@link LuminanceSource} backed by a {@link BufferedImage} for J2SE clients.
 *
 * <p>Non-{@code TYPE_BYTE_GRAY} images are converted to grayscale on
 * construction. Fully transparent pixels are treated as white. The source
 * supports both crop and 90/45-degree rotation.</p>
 *
 * @author dswitkin@google.com (Daniel Switkin)
 * @author Sean Owen
 * @author code@elektrowolle.de (Wolfgang Jung)
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see LuminanceSource
 * @see MatrixToImageWriter
 */
public class BufferedImageLuminanceSource extends LuminanceSource {

	private static final double MINUS_45_IN_RADIANS = -0.7853981633974483; // Math.toRadians(-45.0)

	private final BufferedImage image;
	private final int left;
	private final int top;

	/**
	 * Creates a luminance source covering the entire image.
	 *
	 * @param image the source image; must not be {@code null}
	 */
	public BufferedImageLuminanceSource(BufferedImage image) {
		this(image, 0, 0, image.getWidth(), image.getHeight());
	}

	/**
	 * Creates a luminance source covering a sub-region of the image.
	 *
	 * @param image  the source image; must not be {@code null}
	 * @param left   the left X offset
	 * @param top    the top Y offset
	 * @param width  the region width
	 * @param height the region height
	 * @throws IllegalArgumentException if the crop rectangle does not fit
	 *         within the image
	 */
	public BufferedImageLuminanceSource(BufferedImage image, int left, int top, int width, int height) {
		super(width, height);

		if (image.getType() == BufferedImage.TYPE_BYTE_GRAY) {
			this.image = image;
		} else {
			int sourceWidth = image.getWidth();
			int sourceHeight = image.getHeight();
			if (left + width > sourceWidth || top + height > sourceHeight) {
				throw new IllegalArgumentException("Crop rectangle does not fit within image data.");
			}

			this.image = new BufferedImage(sourceWidth, sourceHeight, BufferedImage.TYPE_BYTE_GRAY);

			WritableRaster raster = this.image.getRaster();
			int[] buffer = new int[width];
			for (int y = top; y < top + height; y++) {
				image.getRGB(left, y, width, 1, buffer, 0, sourceWidth);
				for (int x = 0; x < width; x++) {
					int pixel = buffer[x];

					// Fully-transparent pixels are forced to white, as they are commonly
					// used as the "white" area in barcode images.
					if ((pixel & 0xFF000000) == 0) {
						pixel = 0xFFFFFFFF; // = white
					}

					// YUV/YIQ luminance: 0.299R + 0.587G + 0.114B
					buffer[x] = (306 * ((pixel >> 16) & 0xFF) + 601 * ((pixel >> 8) & 0xFF) + 117 * (pixel & 0xFF)
							+ 0x200) >> 10;
				}
				raster.setPixels(left, y, width, 1, buffer);
			}

		}
		this.left = left;
		this.top = top;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public byte[] getRow(int y, byte[] row) {
		if (y < 0 || y >= getHeight()) {
			throw new IllegalArgumentException("Requested row is outside the image: " + y);
		}
		int width = getWidth();
		if (row == null || row.length < width) {
			row = new byte[width];
		}
		image.getRaster().getDataElements(left, top + y, width, 1, row);
		return row;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public byte[] getMatrix() {
		int width = getWidth();
		int height = getHeight();
		int area = width * height;
		byte[] matrix = new byte[area];
		image.getRaster().getDataElements(left, top, width, height, matrix);
		return matrix;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isCropSupported() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LuminanceSource crop(int left, int top, int width, int height) {
		return new BufferedImageLuminanceSource(image, this.left + left, this.top + top, width, height);
	}

	/**
	 * Returns {@code true}; rotation is always supported since the underlying
	 * image is grayscale.
	 *
	 * @return {@code true}
	 */
	@Override
	public boolean isRotateSupported() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LuminanceSource rotateCounterClockwise() {
		int sourceWidth = image.getWidth();
		int sourceHeight = image.getHeight();

		AffineTransform transform = new AffineTransform(0.0, -1.0, 1.0, 0.0, 0.0, sourceWidth);

		BufferedImage rotatedImage = new BufferedImage(sourceHeight, sourceWidth, BufferedImage.TYPE_BYTE_GRAY);

		Graphics2D g = rotatedImage.createGraphics();
		g.drawImage(image, transform, null);
		g.dispose();

		int width = getWidth();
		return new BufferedImageLuminanceSource(rotatedImage, top, sourceWidth - (left + width), getHeight(), width);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LuminanceSource rotateCounterClockwise45() {
		int width = getWidth();
		int height = getHeight();

		int oldCenterX = left + width / 2;
		int oldCenterY = top + height / 2;

		AffineTransform transform = AffineTransform.getRotateInstance(MINUS_45_IN_RADIANS, oldCenterX, oldCenterY);

		int sourceDimension = Math.max(image.getWidth(), image.getHeight());
		BufferedImage rotatedImage = new BufferedImage(sourceDimension, sourceDimension, BufferedImage.TYPE_BYTE_GRAY);

		Graphics2D g = rotatedImage.createGraphics();
		g.drawImage(image, transform, null);
		g.dispose();

		int halfDimension = Math.max(width, height) / 2;
		int newLeft = Math.max(0, oldCenterX - halfDimension);
		int newTop = Math.max(0, oldCenterY - halfDimension);
		int newRight = Math.min(sourceDimension - 1, oldCenterX + halfDimension);
		int newBottom = Math.min(sourceDimension - 1, oldCenterY + halfDimension);

		return new BufferedImageLuminanceSource(rotatedImage, newLeft, newTop, newRight - newLeft, newBottom - newTop);
	}

}
