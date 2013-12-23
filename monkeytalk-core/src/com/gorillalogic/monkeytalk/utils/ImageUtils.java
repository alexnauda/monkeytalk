package com.gorillalogic.monkeytalk.utils;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * Helper class for comparing images.
 */
public class ImageUtils {
	public static int DEFAULT_TOLERANCE = 5;
	public static int MIN_TOLERANCE = 0;
	public static int MAX_TOLERANCE = 255;

	/**
	 * Load the image from the given path.
	 * 
	 * @param path
	 *            path to the image file
	 * @return the image as a BufferedImage
	 */
	public static BufferedImage getImageFromFile(String path) throws IOException {
		return getImageFromFile(new File(path));
	}

	/**
	 * Load the image from the given file.
	 * 
	 * @param file
	 *            the file containing the image
	 * @return the image as a BufferedImage
	 */
	public static BufferedImage getImageFromFile(File file) throws IOException {
		BufferedImage image = ImageIO.read(file);
		return image;
	}

	/**
	 * Save the given image as a PNG file.
	 * 
	 * @param filename
	 *            the filename of the new image
	 * @param image
	 *            the image to save
	 * @return true if save was successful, otherwise false
	 */
	public static boolean savePNG(String filename, BufferedImage image) {
		boolean success = true;
		try {
			File outputfile = new File(filename);
			ImageIO.write(image, "png", outputfile);
		} catch (IOException ioe) {
			ioe.printStackTrace();
			success = false;
		}
		return success;
	}

	/**
	 * Get the dimensions of the given image.
	 * 
	 * @param image
	 *            the image
	 * @return the image dimensions
	 */
	public static Dimension getDimensions(BufferedImage image) {
		Dimension dimension = null;
		if (image != null) {
			dimension = new Dimension(image.getWidth(null), image.getHeight(null));
		}
		return dimension;
	}

	/**
	 * Crop the given rectangle out of the original image and return it.
	 * 
	 * @param uncroppedImage
	 *            the original (uncropped) image
	 * @param x
	 *            crop rectangle top left x
	 * @param y
	 *            crop rectangle top left y
	 * @param w
	 *            crop rectangle width
	 * @param h
	 *            crop rectangle height
	 * @return the cropped image
	 */
	public static BufferedImage cropImage(BufferedImage uncroppedImage, int x, int y, int w, int h) {
		return uncroppedImage.getSubimage(x, y, w, h);
	}

	/**
	 * Compare two BufferedImage objects pixel-by-pixel for each byte of color (R,G,B,A). Returns
	 * true if and only if the images are an exact match (aka tolerance=0). Immediately returns
	 * false if images are different dimensions.
	 * 
	 * @param img1
	 *            first image
	 * @param img2
	 *            second image
	 * @return true if they are perfectly equal, otherwise false
	 */
	public static boolean compare(BufferedImage img1, BufferedImage img2) {
		return compare(img1, img2, DEFAULT_TOLERANCE);
	}

	/**
	 * Compare two BufferedImage objects pixel-by-pixel for each byte of color (R,G,B,A) using the
	 * given tolerance. Immediately returns false if images are different dimensions.
	 * 
	 * @param img1
	 *            first image
	 * @param img2
	 *            second image
	 * @param tolerance
	 *            the amount each byte of color can vary
	 * @return true if they are equal within the tolerance, otherwise false
	 */
	public static boolean compare(BufferedImage img1, BufferedImage img2, int tolerance) {
		int width = img1.getWidth();
		int height = img1.getHeight();

		if (width != img2.getWidth() || height != img2.getHeight()) {
			return false;
		}

		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				if (!equalsWithTolerance(img1.getRGB(j, i), img2.getRGB(j, i), tolerance)) {
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * Helper to compare two colors (including alpha) with the given tolerance.
	 * 
	 * @param pixel1
	 *            color one
	 * @param pixel2
	 *            color two
	 * @param tolerance
	 *            the amount each color can vary
	 * @return true if they are equal within the tolerance, otherwise false
	 */
	private static boolean equalsWithTolerance(int pixel1, int pixel2, int tolerance) {
		Color color1 = new Color(pixel1, true);
		Color color2 = new Color(pixel2, true);

		if (color1.getRed() > (color2.getRed() + tolerance)) {
			return false;
		}
		if (color1.getRed() < (color2.getRed() - tolerance)) {
			return false;
		}
		if (color1.getGreen() > (color2.getGreen() + tolerance)) {
			return false;
		}
		if (color1.getGreen() < (color2.getGreen() - tolerance)) {
			return false;
		}
		if (color1.getBlue() > (color2.getBlue() + tolerance)) {
			return false;
		}
		if (color1.getBlue() < (color2.getBlue() - tolerance)) {
			return false;
		}
		if (color1.getAlpha() > (color2.getAlpha() + tolerance)) {
			return false;
		}
		if (color1.getAlpha() < (color2.getAlpha() - tolerance)) {
			return false;
		}
		return true;
	}
}
