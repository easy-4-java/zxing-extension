package com.google.zxing.utils;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Objects;

import javax.imageio.ImageIO;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.source.BufferedImageLuminanceSource;
import com.google.zxing.source.BufferedImageWithLogoLuminanceSource;
import com.google.zxing.source.MatrixToImageWriter;

public class BitMatrixUtils {

	public static final String CHARSET = "utf-8";
	private static final MultiFormatWriter MULTI_WRITER = new MultiFormatWriter();

	public static void drawLogo(BufferedImage source, Image logo) throws IOException {
		drawLogo(source, logo, logo.getWidth(null), logo.getHeight(null));
	}

	public static void drawLogo(BufferedImage source, Image logo, int logoWidth, int logoHeight) throws IOException {
		BufferedImage scaleImage = ImageUtils.scale(logo, logoWidth, logoHeight);
		Graphics2D graph = source.createGraphics();
		int x = (source.getWidth() - logoWidth) / 2;
		int y = (source.getHeight() - logoHeight) / 2;
		graph.drawImage(scaleImage, x, y, logoWidth, logoHeight, null);
		Shape shape = new RoundRectangle2D.Float(x, y, logoWidth, logoHeight, 6, 6);
		graph.setStroke(new BasicStroke(3f));
		graph.draw(shape);
		graph.dispose();
	}

	public static BitMatrix bitMatrix(String content, int width, int height, ErrorCorrectionLevel level)
			throws WriterException {
		Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
		hints.put(EncodeHintType.ERROR_CORRECTION, level);
		hints.put(EncodeHintType.CHARACTER_SET, CHARSET);
		hints.put(EncodeHintType.MARGIN, 0);
		return MULTI_WRITER.encode(content, BarcodeFormat.QR_CODE, width, height, hints);
	}

	public static BitMatrix bitMatrixWithMargin(String content, int width, int height, ErrorCorrectionLevel level,
			int margin) throws WriterException {
		Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
		hints.put(EncodeHintType.ERROR_CORRECTION, level);
		hints.put(EncodeHintType.CHARACTER_SET, CHARSET);
		hints.put(EncodeHintType.MARGIN, 0);
		BitMatrix byteMatrix = MULTI_WRITER.encode(content, BarcodeFormat.QR_CODE, width, height, hints);
		return MatrixToImageWriter.updateBit(byteMatrix, margin);
	}

	public static byte[] bitMatrix(String content, int width, int height, ErrorCorrectionLevel level, String formatName)
			throws WriterException, IOException {
		try (ByteArrayOutputStream byteArray = new ByteArrayOutputStream()) {
			BitMatrix byteMatrix = bitMatrix(content, width, height, level);
			MatrixToImageWriter.writeToStream(byteMatrix, formatName, byteArray);
			return byteArray.toByteArray();
		}
	}

	public static Result parse(byte[] bytes) throws ReaderException, IOException {
		return parse(new ByteArrayInputStream(bytes));
	}

	public static Result parse(File file) throws FileNotFoundException, ReaderException, IOException {
		return parse(new FileInputStream(file));
	}

	public static Result parse(InputStream in) throws ReaderException, IOException {
		BufferedImage image = ImageIO.read(in);
		return parse(image);
	}

	public static Result parse(BufferedImage image) throws ReaderException, IOException {
		if (Objects.isNull(image)) {
			return null;
		}
		BufferedImageLuminanceSource source = new BufferedImageLuminanceSource(image);
		BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
		Hashtable<DecodeHintType, String> hints = new Hashtable<DecodeHintType, String>();
		hints.put(DecodeHintType.CHARACTER_SET, CHARSET);
		return new MultiFormatReader().decode(bitmap, hints);
	}

	public static Result parseWithLogo(byte[] bytes) throws ReaderException, IOException {
		return parseWithLogo(new ByteArrayInputStream(bytes));
	}

	public static Result parseWithLogo(InputStream in) throws ReaderException, IOException {
		BufferedImage image = ImageIO.read(in);
		return parse(image);
	}

	public static Result parseWithLogo(BufferedImage image) throws ReaderException, IOException {
		if (Objects.isNull(image)) {
			return null;
		}
		BufferedImageLuminanceSource source = new BufferedImageWithLogoLuminanceSource(image);
		BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
		Hashtable<DecodeHintType, String> hints = new Hashtable<DecodeHintType, String>();
		hints.put(DecodeHintType.CHARACTER_SET, CHARSET);
		return new MultiFormatReader().decode(bitmap, hints);
	}

}
