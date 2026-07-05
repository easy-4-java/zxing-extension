/*
 * Copyright (c) 2018, hiwepy (https://github.com/hiwepy).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.zxing;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Base64;

import javax.imageio.ImageIO;

import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.utils.BitMatrixUtils;
import com.google.zxing.utils.ImageUtils;

/**
 * 二维码工具类:生成和解析二维码；可处理有logo二维码
 *
 * @author ： <a href="https://github.com/hiwepy">wandl</a>
 */
public class ZxingQrCodeColorfullTemplate {

	// 1290*1290 860*860 430*430 344*344 258*258
	public static final int QRCODE_258 = 258; // 258*258
	public static final int QRCODE_344 = 344; // 344*344
	public static final int QRCODE_430 = 430; // 430*430
	public static final int QRCODE_860 = 860; // 860*860
	public static final int QRCODE_1290 = 1290; // 1290*1290
	private static final int LOGO_WIDTH = 48; // LOGO宽度
	private static final int LOGO_HEIGHT = 48; // LOGO高度
	private static final int LOGO_MARGIN = 5; // LOGO边距
	private static final String FORMAT_NAME = "png";
	private static final String BASE64_PREFIX = "data:image/png;base64,";

	/*
	 * 生成二维码(内嵌LOGO)
	 */
	public void qrcode(String content, Image logo, OutputStream output) throws WriterException, IOException {
		qrcode(content, QRCODE_258, QRCODE_258, ErrorCorrectionLevel.M, logo, LOGO_WIDTH, LOGO_HEIGHT, LOGO_MARGIN,
				output);
	}

	/*
	 * 生成二维码(内嵌LOGO)
	 */
	public void qrcode(String content, int width, int height, Image logo, OutputStream output)
			throws WriterException, IOException {
		qrcode(content, width, height, ErrorCorrectionLevel.M, logo, LOGO_WIDTH, LOGO_HEIGHT, LOGO_MARGIN, output);
	}

	/*
	 * 生成二维码(内嵌LOGO)
	 */
	public void qrcode(String content, int width, int height, ErrorCorrectionLevel level, Image logo, int logoWidth,
			int logoHeight, int logoMargin, OutputStream output) throws WriterException, IOException {
		BufferedImage image = qrcode(content, width, height, level, logo, logoWidth, logoHeight, logoMargin);
		ImageIO.write(image, FORMAT_NAME, output);
	}

	/*
	 * 生成二维码(内嵌LOGO)
	 */
	public BufferedImage qrcode(String content, Image logo) throws WriterException, IOException {
		return qrcode(content, QRCODE_258, QRCODE_258, ErrorCorrectionLevel.M, logo);
	}

	/*
	 * 生成二维码(内嵌LOGO)
	 */
	public BufferedImage qrcode(String content, int width, int height, Image logo) throws WriterException, IOException {
		return qrcode(content, width, height, ErrorCorrectionLevel.M, logo);
	}

	public BufferedImage qrcode(String content, int width, int height, ErrorCorrectionLevel level, Image logo)
			throws WriterException, IOException {
		return qrcode(content, width, height, level, logo, LOGO_WIDTH, LOGO_HEIGHT, LOGO_MARGIN);
	}

	/*
	 * 生成二维码(内嵌LOGO)
	 */
	public BufferedImage qrcode(String content, int width, int height, ErrorCorrectionLevel level, Image logo,
			int logoWidth, int logoHeight, int logoMargin) throws WriterException, IOException {

		BitMatrix matrix = BitMatrixUtils.bitMatrix(content, width, height, level);

		BufferedImage logoImage = ImageUtils.scale(logo, logoWidth, logoHeight);
		int[][] srcPixels = new int[logoWidth][logoHeight];
		for (int i = 0; i < logoImage.getWidth(); i++) {
			for (int j = 0; j < logoImage.getHeight(); j++) {
				srcPixels[i][j] = logoImage.getRGB(i, j);
			}
		}

		int halfW = matrix.getWidth() / 2;
		int halfH = matrix.getHeight() / 2;
		int logoHalfWidth = logoWidth / 2;
		int[] pixels = new int[width * height];
		for (int y = 0; y < matrix.getHeight(); y++) {
			for (int x = 0; x < matrix.getWidth(); x++) {
				int left = 90;
				if (width >= 1290) {
					left = 450;
				} else if (860 <= width && width < 1290) {
					left = 300;
				} else if (430 <= width && width < 860) {
					left = 150;
				} else if (344 <= width && width < 430) {
					left = 120;
				}
				if (x > 0 && x < left && y > 0 && y < left) {
					Color color = new Color(231, 144, 56);
					int colorInt = color.getRGB();
					pixels[y * width + x] = matrix.get(x, y) ? colorInt : 16777215;
				} else if (x > halfW - logoHalfWidth && x < halfW + logoHalfWidth && y > halfH - logoHalfWidth
						&& y < halfH + logoHalfWidth) {
					pixels[width * y + x] = srcPixels[x - halfW + logoHalfWidth][y - halfH + logoHalfWidth];
				} else if ((x > halfW - logoHalfWidth - logoMargin && x < halfW - logoHalfWidth + logoMargin
						&& y > halfH - logoHalfWidth - logoMargin && y < halfH + logoHalfWidth + logoMargin)
						|| (x > halfW + logoHalfWidth - logoMargin && x < halfW + logoHalfWidth + logoMargin
								&& y > halfW - logoHalfWidth - logoMargin && y < halfH + logoHalfWidth + logoMargin)
						|| (x > halfW - logoHalfWidth - logoMargin && x < halfW + logoHalfWidth + logoMargin
								&& y > halfH - logoHalfWidth - logoMargin && y < halfH - logoHalfWidth + logoMargin)
						|| (x > halfW - logoHalfWidth - logoMargin && x < halfW + logoHalfWidth + logoMargin
								&& y > halfH + logoHalfWidth - logoMargin
								&& y < halfH + logoHalfWidth + logoMargin)) {
					pixels[width * y + x] = 0xfffffff;
				} else {
					int num1 = (int) (50 - (50.0 - 13.0) / matrix.getHeight() * (y + 1));
					int num2 = (int) (165 - (165.0 - 72.0) / matrix.getHeight() * (y + 1));
					int num3 = (int) (162 - (162.0 - 107.0) / matrix.getHeight() * (y + 1));
					Color color = new Color(num1, num2, num3);
					int colorInt = color.getRGB();
					pixels[y * width + x] = matrix.get(x, y) ? colorInt : 16777215;
				}
			}
		}
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		image.getRaster().setDataElements(0, 0, width, height, pixels);
		return image;
	}

	/*
	 * 生成二维码
	 */
	public BufferedImage qrcode(String content) throws WriterException, IOException {
		return qrcode(content, QRCODE_258, QRCODE_258, ErrorCorrectionLevel.M);
	}

	/*
	 * 生成二维码
	 */
	public BufferedImage qrcode(String content, int width, int height) throws WriterException, IOException {
		return qrcode(content, width, height, ErrorCorrectionLevel.M);
	}

	/*
	 * 生成二维码
	 */
	public BufferedImage qrcode(String content, int width, int height, ErrorCorrectionLevel level)
			throws WriterException {
		BitMatrix matrix = BitMatrixUtils.bitMatrix(content, width, height, level);
		int[] pixels = new int[width * height];
		for (int y = 0; y < matrix.getHeight(); y++) {
			for (int x = 0; x < matrix.getWidth(); x++) {
				int left = 90;
				if (width >= 1290) {
					left = 450;
				} else if (860 <= width && width < 1290) {
					left = 300;
				} else if (430 <= width && width < 860) {
					left = 150;
				} else if (344 <= width && width < 430) {
					left = 120;
				}
				if (x > 0 && x < left && y > 0 && y < left) {
					Color color = new Color(231, 144, 56);
					pixels[y * width + x] = matrix.get(x, y) ? color.getRGB() : 16777215;
				} else {
					int num1 = (int) (50 - (50.0 - 13.0) / matrix.getHeight() * (y + 1));
					int num2 = (int) (165 - (165.0 - 72.0) / matrix.getHeight() * (y + 1));
					int num3 = (int) (162 - (162.0 - 107.0) / matrix.getHeight() * (y + 1));
					Color color = new Color(num1, num2, num3);
					pixels[y * width + x] = matrix.get(x, y) ? color.getRGB() : 16777215;
				}
			}
		}
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		image.getRaster().setDataElements(0, 0, width, height, pixels);
		return image;
	}

	/*
	 * 生成二维码
	 */
	public void qrcode(String content, OutputStream output) throws WriterException, IOException {
		qrcode(content, QRCODE_258, QRCODE_258, ErrorCorrectionLevel.M, output);
	}

	/*
	 * 生成二维码
	 */
	public void qrcode(String content, int width, int height, OutputStream output) throws WriterException, IOException {
		qrcode(content, width, height, ErrorCorrectionLevel.M, output);
	}

	/*
	 * 生成二维码
	 */
	public void qrcode(String content, int width, int height, ErrorCorrectionLevel level, OutputStream output)
			throws WriterException, IOException {
		BufferedImage image = qrcode(content, width, height, level);
		ImageIO.write(image, FORMAT_NAME, output);
	}

	/*
	 * 生成二维码并编码为Base64
	 */
	public String qrcodeBase64(String content) throws WriterException, IOException {
		return qrcodeBase64(content, QRCODE_258, QRCODE_258, ErrorCorrectionLevel.M);
	}

	/*
	 * 生成二维码并编码为Base64
	 */
	public String qrcodeBase64(String content, int width, int height) throws WriterException, IOException {
		return qrcodeBase64(content, width, height, ErrorCorrectionLevel.M);
	}

	/*
	 * 生成二维码并编码为Base64
	 */
	public String qrcodeBase64(String content, int width, int height, ErrorCorrectionLevel level)
			throws WriterException, IOException {
		try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
			BufferedImage image = qrcode(content, width, height, level);
			ImageIO.write(image, FORMAT_NAME, output);
			return BASE64_PREFIX + Base64.getEncoder().encodeToString(output.toByteArray());
		}
	}

}
