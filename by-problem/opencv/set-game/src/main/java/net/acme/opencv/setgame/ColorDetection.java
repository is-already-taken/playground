package net.acme.opencv.setgame;

import java.util.Arrays;
import java.util.Comparator;

import org.opencv.core.Mat;
import org.opencv.core.Rect;

import net.acme.opencv.setgame.utils.Histogram;

/**
 * Detect fill of a shape
 */
public class ColorDetection {

	/**
	 * Process card image to get color.
	 * 
	 * @param image color image of a shape
	 * @param location location of a shape on the image
	 * @return color
	 */
	public static Color process(Mat image, Rect location) {
		Mat shapeImage = image.submat(location);

		Histogram blueHistogram = Histogram.generate(shapeImage, 0);
		Histogram greenHistogram = Histogram.generate(shapeImage, 1);
		Histogram redHistogram = Histogram.generate(shapeImage, 2);

		// Pair color-index with the corresponding average value
		int[][] rgb = new int[][] {
				{ 0, redHistogram.average() },
				{ 1, greenHistogram.average() },
				{ 2, blueHistogram.average() }
		};

		int colorIndex = Arrays.asList(rgb)
			.stream()
			.max(new RgbValuePeakComparator())
			.get()[0];

		return Color.values()[colorIndex];
	}

	/**
	 * The color
	 */
	public static enum Color {
		red, green, blue
	}

	/**
	 * Compares two color-index/value tuples for their value.
	 */
	private static class RgbValuePeakComparator implements Comparator<int[]> {
		@Override
		public int compare(int[] o1, int[] o2) {
			return (o1[1] < o2[1]) ? -1 : (o1[1] > o2[1] ? 1 : 0);
		}
	}
}
