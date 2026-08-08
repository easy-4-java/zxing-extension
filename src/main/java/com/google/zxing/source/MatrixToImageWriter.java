/*
 * Copyright 2009 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zxing.source;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import com.google.zxing.common.BitMatrix;

/**
 * Renders a {@link BitMatrix} to a {@link BufferedImage}, file or output stream.
 *
 * <p>Provided here instead of the core module since it depends on Java SE
 * libraries ({@code javax.imageio.ImageIO}).</p>
 *
 * @author Sean Owen
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see BufferedImageLuminanceSource
 * @see BitMatrix
 */
public final class MatrixToImageWriter {

	private static final int BLACK = 0xFF000000;
	private static final int WHITE = 0xFFFFFFFF;

	private MatrixToImageWriter() {
	}

	/**
	 * Renders a {@link BitMatrix} as a {@link BufferedImage} where {@code true}
	 * bits are rendered as black and {@code false} bits as white.
	 *
	 * @param matrix the bit matrix; must not be {@code null}
	 * @return the rendered image
	 */
	public static BufferedImage toBufferedImage(BitMatrix matrix) {
		int width = matrix.getWidth();
		int height = matrix.getHeight();
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				image.setRGB(x, y, matrix.get(x, y) ? BLACK : WHITE);
			}
		}
		return image;
	}

	/**
	 * Renders a {@link BitMatrix} as a {@link BufferedImage} scaled to the
	 * specified square size.
	 *
	 * @param matrix the bit matrix; must not be {@code null}
	 * @param size   the target width and height in pixels
	 * @return the scaled image
	 */
	public static BufferedImage toBufferedImage(BitMatrix matrix, int size) {
		BufferedImage image = toBufferedImage(matrix);
		return zoomInImage(image, size, size);
	}

	/**
	 * Creates a new {@link BitMatrix} with a custom quiet-zone margin around
	 * the encoded data. The original white border produced by ZXing is
	 * replaced by the specified margin.
	 *
	 * @param matrix the original bit matrix; must not be {@code null}
	 * @param margin the desired quiet-zone width in pixels
	 * @return a new {@link BitMatrix} with the custom margin applied
	 */
	public static BitMatrix updateBit(BitMatrix matrix, int margin) {
		int tempM = margin * 2;
		int[] rec = matrix.getEnclosingRectangle();
		int resWidth = rec[2] + tempM;
		int resHeight = rec[3] + tempM;
		BitMatrix resMatrix = new BitMatrix(resWidth, resHeight);
		resMatrix.clear();
		for (int i = margin; i < resWidth - margin; i++) {
			for (int j = margin; j < resHeight - margin; j++) {
				if (matrix.get(rec[0] + (i - margin), rec[1] + (j - margin))) {
					resMatrix.set(i, j);
				}
			}
		}
		return resMatrix;
	}

	/**
	 * Scales a {@link BufferedImage} to the specified dimensions.
	 *
	 * @param originalImage the source image; must not be {@code null}
	 * @param width         the target width in pixels
	 * @param height        the target height in pixels
	 * @return the scaled image
	 */
	public static BufferedImage zoomInImage(BufferedImage originalImage, int width, int height) {
		BufferedImage newImage = new BufferedImage(width, height, originalImage.getType());
		Graphics g = newImage.getGraphics();
		g.drawImage(originalImage, 0, 0, width, height, null);
		g.dispose();
		return newImage;
	}

	/**
	 * Writes a {@link BitMatrix} to a file in the specified image format.
	 *
	 * @param matrix the bit matrix; must not be {@code null}
	 * @param format the ImageIO format name (e.g. {@code "png"})
	 * @param file   the target file; must not be {@code null}
	 * @throws IOException if writing fails
	 * @see #toBufferedImage(BitMatrix)
	 */
	public static void writeToFile(BitMatrix matrix, String format, File file) throws IOException {
		BufferedImage image = toBufferedImage(matrix);
		ImageIO.write(image, format, file);
	}

	/**
	 * Writes a {@link BitMatrix} to an output stream in the specified image
	 * format. The stream is <strong>not</strong> closed by this method.
	 *
	 * @param matrix the bit matrix; must not be {@code null}
	 * @param format the ImageIO format name (e.g. {@code "png"})
	 * @param stream the target output stream; must not be {@code null}
	 * @throws IOException if writing fails
	 * @see #toBufferedImage(BitMatrix)
	 */
	public static void writeToStream(BitMatrix matrix, String format, OutputStream stream) throws IOException {
		BufferedImage image = toBufferedImage(matrix);
		ImageIO.write(image, format, stream);
	}

}
