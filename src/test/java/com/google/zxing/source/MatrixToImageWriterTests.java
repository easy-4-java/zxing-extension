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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

@DisplayName("MatrixToImageWriter")
class MatrixToImageWriterTests {

	private static final String CONTENT = "https://github.com/hiwepy/zxing-extension";

	@Test
	@DisplayName("toBufferedImage 输出 BufferedImage，黑点像素 0xFF000000，白点 0xFFFFFFFF")
	void toBufferedImage_pixelsCorrect() throws Exception {
		BitMatrix matrix = createMatrix();
		BufferedImage img = MatrixToImageWriter.toBufferedImage(matrix);

		assertThat(img).isNotNull();
		assertThat(img.getWidth()).isEqualTo(matrix.getWidth());
		assertThat(img.getHeight()).isEqualTo(matrix.getHeight());

		// 抽样 0,0 / center / W-1,H-1 三点
		int blackCount = 0;
		int whiteCount = 0;
		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				int rgb = img.getRGB(x, y) & 0x00FFFFFF;
				if (rgb == 0x000000) blackCount++;
				else if (rgb == 0xFFFFFF) whiteCount++;
			}
		}
		// QR 一定同时存在黑/白像素
		assertThat(blackCount).isPositive();
		assertThat(whiteCount).isPositive();
	}

	@Test
	@DisplayName("toBufferedImage(BitMatrix, int) 缩放至指定 size")
	void toBufferedImage_withSize() throws Exception {
		BitMatrix matrix = createMatrix();

		BufferedImage img = MatrixToImageWriter.toBufferedImage(matrix, 200);

		assertThat(img.getWidth()).isEqualTo(200);
		assertThat(img.getHeight()).isEqualTo(200);
	}

	@Test
	@DisplayName("updateBit 在有效区外扩 margin 后尺寸大于原有效区")
	void updateBit_expandsByMargin() throws Exception {
		BitMatrix matrix = createMatrix();
		int[] rect = matrix.getEnclosingRectangle();

		BitMatrix expanded = MatrixToImageWriter.updateBit(matrix, 5);

		assertThat(expanded.getWidth()).isGreaterThan(rect[2]);
		assertThat(expanded.getHeight()).isGreaterThan(rect[3]);
	}

	@Test
	@DisplayName("updateBit margin=0 仍按原有效矩形重建")
	void updateBit_zeroMarginRebuildsFromRectangle() throws Exception {
		BitMatrix matrix = createMatrix();
		int[] rect = matrix.getEnclosingRectangle();

		BitMatrix expanded = MatrixToImageWriter.updateBit(matrix, 0);

		assertThat(expanded.getWidth()).isEqualTo(rect[2]);
		assertThat(expanded.getHeight()).isEqualTo(rect[3]);
	}

	@Test
	@DisplayName("zoomInImage 把 BufferedImage 缩放到指定宽高")
	void zoomInImage_resizesCorrectly() {
		BufferedImage src = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);

		BufferedImage out = MatrixToImageWriter.zoomInImage(src, 200, 100);

		assertThat(out.getWidth()).isEqualTo(200);
		assertThat(out.getHeight()).isEqualTo(100);
	}

	@Test
	@DisplayName("zoomInImage 使用零类型 BufferedImage 也能生成")
	void zoomInImage_handlesZeroType() {
		BufferedImage src = new BufferedImage(20, 20, BufferedImage.TYPE_INT_RGB);
		BufferedImage out = MatrixToImageWriter.zoomInImage(src, 40, 40);
		assertThat(out.getWidth()).isEqualTo(40);
	}

	@Test
	@DisplayName("writeToStream 输出合法 PNG 字节（magic 89 50 4E 47）")
	void writeToStream_emitsPngBytes() throws Exception {
		BitMatrix matrix = createMatrix();
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		MatrixToImageWriter.writeToStream(matrix, "png", out);
		byte[] bytes = out.toByteArray();

		assertThat(bytes).isNotEmpty();
		assertThat(bytes[0]).isEqualTo((byte) 0x89);
		assertThat(bytes[1]).isEqualTo((byte) 0x50);
		assertThat(bytes[2]).isEqualTo((byte) 0x4E);
		assertThat(bytes[3]).isEqualTo((byte) 0x47);
	}

	@Test
	@DisplayName("writeToStream 写入已关闭流抛 IOException")
	void writeToStream_closedStreamThrows() throws Exception {
		BitMatrix matrix = createMatrix();
		OutputStream closed = new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				throw new IOException("closed");
			}

			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				throw new IOException("closed");
			}
		};

		assertThatThrownBy(() -> MatrixToImageWriter.writeToStream(matrix, "png", closed))
				.isInstanceOf(IOException.class);
	}

	@Test
	@DisplayName("writeToFile 写出文件且非空")
	void writeToFile_writesNonEmptyFile() throws Exception {
		BitMatrix matrix = createMatrix();
		File tmp = Files.createTempFile("matrix-", ".png").toFile();
		try {
			MatrixToImageWriter.writeToFile(matrix, "png", tmp);

			assertThat(tmp).exists();
			assertThat(tmp.length()).isPositive();
		} finally {
			tmp.delete();
		}
	}

	@Test
	@DisplayName("writeToFile 写入路径中含普通文件的父目录")
	void writeToFile_writesIntoExistingDirectory(@TempDir Path tempDir) throws Exception {
		BitMatrix matrix = createMatrix();
		File nested = tempDir.resolve("a/b.png").toFile();
		assertThat(nested.getParentFile().mkdirs()).isTrue();

		MatrixToImageWriter.writeToFile(matrix, "png", nested);

		assertThat(nested).exists();
		assertThat(nested.length()).isPositive();
	}

	private static BitMatrix createMatrix() throws Exception {
		Map<EncodeHintType, Object> hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
		hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
		hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
		hints.put(EncodeHintType.MARGIN, 0);
		return new MultiFormatWriter().encode(CONTENT, BarcodeFormat.QR_CODE, 100, 100, hints);
	}

}
