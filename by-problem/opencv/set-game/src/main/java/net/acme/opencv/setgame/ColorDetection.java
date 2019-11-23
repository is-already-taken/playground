package net.acme.opencv.setgame;

import java.util.Arrays;
import java.util.Comparator;

import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import net.acme.opencv.setgame.utils.Histogram;

/**
 * Detect fill of a shape
 */
public class ColorDetection {
	// Pick tiny probe of the shape center to limit 
	// artifacts/disturbances caused by the border.
	private static final int PROBE_SIZE = 4;

	// Blur image to get average value of the shape content
	private static final Size BLUR_KERNEL = new Size(new double[] { 4, 4 });

	/**
	 * Process card image to get color.
	 * 
	 * @param image color image of a shape
	 * @param location location of a shape on the image
	 * @param string
	 * @return color
	 */
	public static Color process(Mat image, Rect location, String string) {
		// Vertically centered slice to have as little white space as possible 
		Rect probe = new Rect(
			location.x,
			location.y + Math.round(location.height / 2) - (PROBE_SIZE / 2),
			Math.round(location.width / 2) + 5,
			PROBE_SIZE + 5);
		Mat probeImage = image.submat(probe);
		Mat blurred = new Mat();

		Imgproc.blur(probeImage, blurred, BLUR_KERNEL);

		Histogram blueHistogram = Histogram.generate(blurred, 0);
		Histogram greenHistogram = Histogram.generate(blurred, 1);
		Histogram redHistogram = Histogram.generate(blurred, 2);

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
