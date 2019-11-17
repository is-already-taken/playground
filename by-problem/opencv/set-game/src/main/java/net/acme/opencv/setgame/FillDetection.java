package net.acme.opencv.setgame;

import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import net.acme.opencv.setgame.utils.Histogram;

/**
 * Detect fill of a shape
 */
public class FillDetection {
	// Pick tiny probe of the shape center to limit 
	// artifacts/disturbances caused by the border.
	private static final int PROBE_SIZE = 4;

	// Blur image to get average value of the shape content
	private static final Size BLUR_KERNEL = new Size(new double[] { 4, 4 });

	// Value ranges from high-, mid-, to low-key
	private static final int[][] VALUE_RANGES = new int[][] { { 230, 255 }, { 160, 229 }, { 0, 159 } };

	/**
	 * Process card image to get shape.
	 * 
	 * @param image of a shape
	 * @param location location of a shape on the image
	 * @return fill
	 */
	public static Fill process(Mat image, Rect location) {
		Rect probe = new Rect(
			// Center
			location.x + Math.round(location.width / 2) - (PROBE_SIZE / 2),
			location.y + Math.round(location.height / 2) - (PROBE_SIZE / 2),
			PROBE_SIZE,
			PROBE_SIZE);
		Mat shapeImage = image.submat(probe);
		Mat blurred = new Mat();
		int[] range;
		int average;
		int rangeIndex;

		// Determine fill by average value. An empty shape must be high-key,
		// because the content is white. A filled shape must be low-key
		// because the content is not white. A pattern, after blurring 
		// produces a mid-key image because white pixels and pattern
		// pixels are mixed together.

		Imgproc.blur(shapeImage, blurred, BLUR_KERNEL);

		Histogram probeHist = Histogram.generate(blurred);
		average = probeHist.average();

		for (rangeIndex = 0; rangeIndex < VALUE_RANGES.length; rangeIndex++) {
			range = VALUE_RANGES[rangeIndex];

			if (average >= range[0] && average <= range[1]) {
				return Fill.values()[rangeIndex];
			}
		}

		return null;
	}

	// Has to be in order of the average luminance value from 
	// lowest (high-key) to highest (low key).
	/**
	 * How the shape is filled
	 */
	public static enum Fill {
		empty, pattern, solid
	}
}
