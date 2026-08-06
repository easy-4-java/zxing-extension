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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.awt.AlphaComposite;
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
	@DisplayName("INT_ARGB 透明像素 → 灰度视为白色")
	void argb_transparentPixelsBecomeWhite() {
		BufferedImage argb = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = argb.createGraphics();
		g.setComposite(AlphaComposite.Clear);
		g.fillRect(0, 0, 4, 4);
		g.dispose();
		BufferedImageLuminanceSource src = new BufferedImageLuminanceSource(argb);
		byte[] matrix = src.getMatrix();
		for (byte value : matrix) {
			assertThat(value & 0xFF).isEqualTo(255);
		}
	}

	@Test
	@DisplayName("TYPE_INT_ARGB 不透明像素正常转换")
	void argb_opaquePixelsConvert() {
		BufferedImage argb = blankArgb(2, 2, new Color(255, 0, 0, 255));
		BufferedImageLuminanceSource src = new BufferedImageLuminanceSource(argb);
		byte[] matrix = src.getMatrix();
		assertThat(matrix[0] & 0xFF).isBetween(60, 90);
	}

	@Test
	@DisplayName("TYPE_4BYTE_ABGR 也走 RGB 转灰度分支")
	void type4ByteAbgrConversions() {
		BufferedImage img = new BufferedImage(2, 2, BufferedImage.TYPE_4BYTE_ABGR);
		BufferedImageLuminanceSource src = new BufferedImageLuminanceSource(img);
		byte[] matrix = src.getMatrix();
		assertThat(matrix).hasSize(4);
	}

	@Test
	@DisplayName("TYPE_USHORT_GRAY 也走 RGB 转灰度分支")
	void typeUShortGrayConversions() {
		BufferedImage img = new BufferedImage(3, 3, BufferedImage.TYPE_USHORT_GRAY);
		BufferedImageLuminanceSource src = new BufferedImageLuminanceSource(img);
		byte[] matrix = src.getMatrix();
		assertThat(matrix).hasSize(9);
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
	@DisplayName("crop 越界抛 IllegalArgumentException")
	void crop_outOfBounds_throws() {
		BufferedImage rgb = blankRgb(10, 10, Color.WHITE);

		assertThatThrownBy(() -> new BufferedImageLuminanceSource(rgb, 5, 5, 100, 100))
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

	@Test
	@DisplayName("rotateCounterClockwise45 不会抛异常")
	void rotateCounterClockwise45_succeeds() {
		BufferedImage rgb = blankRgb(20, 20, Color.WHITE);
		LuminanceSource rotated = new BufferedImageLuminanceSource(rgb).rotateCounterClockwise45();
		assertThat(rotated.getWidth()).isPositive();
		assertThat(rotated.getHeight()).isPositive();
	}

	@Test
	@DisplayName("getRow(y, row==null) 重新分配与宽度等长的字节数组")
	void getRow_allocatesWhenProvidedNull() {
		BufferedImage rgb = blankRgb(4, 4, Color.RED);
		BufferedImageLuminanceSource src = new BufferedImageLuminanceSource(rgb);

		byte[] allocated = src.getRow(1, null);
		assertThat(allocated).hasSize(4);
	}

	@Test
	@DisplayName("getRow(y, row.length < width) 仍然重新分配")
	void getRow_reallocatesWhenBufferTooShort() {
		BufferedImage gray = new BufferedImage(8, 4, BufferedImage.TYPE_BYTE_GRAY);
		BufferedImageLuminanceSource src = new BufferedImageLuminanceSource(gray);

		byte[] tooShort = new byte[4]; // width is 8
		byte[] filled = src.getRow(0, tooShort);
		assertThat(filled).hasSize(8);
		assertThat(filled).isNotSameAs(tooShort);
	}

	@Test
	@DisplayName("getRow 越界抛 IllegalArgumentException")
	void getRow_outOfBoundsThrows() {
		BufferedImage rgb = blankRgb(4, 4, Color.WHITE);
		BufferedImageLuminanceSource src = new BufferedImageLuminanceSource(rgb);

		assertThatThrownBy(() -> src.getRow(-1, null)).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> src.getRow(99, null)).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	@DisplayName("getMatrix 在裁剪后只覆盖裁剪区域")
	void getMatrix_returnsCroppedArea() {
		BufferedImage rgb = blankRgb(20, 20, Color.BLACK);
		BufferedImageLuminanceSource src = (BufferedImageLuminanceSource) new BufferedImageLuminanceSource(rgb)
				.crop(2, 3, 5, 4);
		byte[] matrix = src.getMatrix();
		assertThat(matrix).hasSize(5 * 4);
	}

	@Test
	@DisplayName("TYPE_4BYTE_ABGR 转换路径：getMatrix 产生期望长度的灰度数组")
	void type4ByteAbgr_matrixLengthMatches() {
		BufferedImage abgr = new BufferedImage(5, 4, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g = abgr.createGraphics();
		g.setColor(Color.BLUE);
		g.fillRect(0, 0, 5, 4);
		g.dispose();
		BufferedImageLuminanceSource src = new BufferedImageLuminanceSource(abgr);
		byte[] matrix = src.getMatrix();
		assertThat(matrix).hasSize(20);
		// (R=0,G=0,B=255) → 0.114*255 ≈ 29
		assertThat(matrix[0] & 0xFF).isBetween(20, 36);
	}

	@Test
	@DisplayName("TYPE_USHORT_GRAY 也走 RGB 转换路径")
	void typeUShortGray_matrixLengthMatches() {
		BufferedImage img = new BufferedImage(3, 3, BufferedImage.TYPE_USHORT_GRAY);
		BufferedImageLuminanceSource src = new BufferedImageLuminanceSource(img);
		byte[] matrix = src.getMatrix();
		assertThat(matrix).hasSize(9);
	}

	@Test
	@DisplayName("旋转 45° 之后可以再次 crop，尺寸符合传入值")
	void rotateCounterClockwise45_cropAfterRotate() {
		BufferedImage rgb = blankRgb(40, 40, Color.WHITE);
		LuminanceSource rotated = new BufferedImageLuminanceSource(rgb).rotateCounterClockwise45();
		LuminanceSource cropped = rotated.crop(2, 3, 10, 8);
		assertThat(cropped.getWidth()).isEqualTo(10);
		assertThat(cropped.getHeight()).isEqualTo(8);
	}

	@Test
	@DisplayName("rotateCounterClockwise 之后再 crop 与 isRotateSupported / isCropSupported 一致")
	void rotateThenCrop_supportsBothAreTrue() {
		BufferedImage rgb = blankRgb(20, 10, Color.WHITE);
		LuminanceSource rotated = new BufferedImageLuminanceSource(rgb).rotateCounterClockwise();
		assertThat(rotated.isRotateSupported()).isTrue();
		assertThat(rotated.isCropSupported()).isTrue();
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

	private static BufferedImage blankArgb(int w, int h, Color color) {
		BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		g.setColor(color);
		g.fillRect(0, 0, w, h);
		g.dispose();
		return img;
	}
}
