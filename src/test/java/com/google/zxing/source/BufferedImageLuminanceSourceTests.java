/*
 * Copyright (c) 2018, hiwepy (https://github.com/hiwepy).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.google.zxing.source;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.zxing.LuminanceSource;

@DisplayName("BufferedImageLuminanceSource")
class BufferedImageLuminanceSourceTests {

	@Test
	@DisplayName("TYPE_BYTE_GRAY 直通：getMatrix() 不重新转换")
	void typeByteGray_passThrough() {
		BufferedImage gray = new BufferedImage(10, 10, BufferedImage.TYPE_BYTE_GRAY);

		BufferedImageLuminanceSource src = new BufferedImageLuminanceSource(gray);

		assertThat(src.isCropSupported()).isTrue();
		assertThat(src.isRotateSupported()).isTrue();
		assertThat(src.getWidth()).isEqualTo(10);
		assertThat(src.getHeight()).isEqualTo(10);
		byte[] row0 = src.getRow(0, null);
		assertThat(row0).hasSize(10);
	}

	@Test
	@DisplayName("RGB 图像转灰度：getMatrix() 返回 byte[width*height]")
	void rgb_image_convertedToGray() {
		BufferedImage rgb = blankRgb(8, 6, Color.RED);

		BufferedImageLuminanceSource src = new BufferedImageLuminanceSource(rgb);

		byte[] matrix = src.getMatrix();
		assertThat(matrix).hasSize(8 * 6);
		// 红色 (255,0,0) → 0.299*255 ≈ 76
		assertThat(matrix[0] & 0xFF).isBetween(70, 82);
	}

	@Test
	@DisplayName("crop 返回新实例，且尺寸等于传入的 w/h")
	void crop_returnsNewInstanceWithCroppedSize() {
		BufferedImage rgb = blankRgb(20, 20, Color.WHITE);

		BufferedImageLuminanceSource cropped = (BufferedImageLuminanceSource) new BufferedImageLuminanceSource(rgb)
				.crop(2, 3, 10, 8);

		assertThat(cropped.getWidth()).isEqualTo(10);
		assertThat(cropped.getHeight()).isEqualTo(8);
	}

	@Test
	@DisplayName("构造器 crop 越界抛 IllegalArgumentException")
	void crop_outOfBounds_throws() {
		BufferedImage rgb = blankRgb(10, 10, Color.WHITE);

		org.assertj.core.api.Assertions.assertThatThrownBy(() ->
				new BufferedImageLuminanceSource(rgb, 5, 5, 100, 100))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	@DisplayName("rotateCounterClockwise 90° 后宽高互换")
	void rotateCounterClockwise_swapsWidthHeight() {
		BufferedImage rgb = blankRgb(20, 10, Color.WHITE);

		LuminanceSource rotated = new BufferedImageLuminanceSource(rgb).rotateCounterClockwise();

		assertThat(rotated.getWidth()).isEqualTo(10);
		assertThat(rotated.getHeight()).isEqualTo(20);
	}

	// --- helpers ---

	private static BufferedImage blankRgb(int w, int h, Color color) {
		BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = img.createGraphics();
		g.setColor(color);
		g.fillRect(0, 0, w, h);
		g.dispose();
		return img;
	}
}
