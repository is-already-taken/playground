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

	private static final int EMPTY_MAX_DIFFERENCE = 10;
	private static final int PATTERN_MAX_DIFFERENCE = 75;

	/**
	 * Process card image to get shape.
	 * 
	 * @param image of a shape
	 * @param location location of a shape on the image
	 * @return fill
	 */
	public static Fill process(Mat image, Rect location) {
		Rect insideProbe = new Rect(
			// Center
			location.x + Math.round(location.width / 2) - (PROBE_SIZE / 2),
			location.y + Math.round(location.height / 2) - (PROBE_SIZE / 2),
			PROBE_SIZE,
			PROBE_SIZE);
		Rect outsideProbe = new Rect(
			// Center
			location.x + location.width - PROBE_SIZE,
			location.y + 0,
			PROBE_SIZE,
			PROBE_SIZE);
		Mat insideImage = image.submat(insideProbe);
		Mat outsideImage = image.submat(outsideProbe);
		Mat blurred = new Mat();
		int insideAverage;
		int outsideAverage;
		int difference;

		// Determine fill by comparing the average values of areas
		// outside the shape and inside the shape. The shape inside
		// is blurred to limit modes. The inside and outside average
		// values are then compared.

		Imgproc.blur(insideImage, blurred, BLUR_KERNEL);
		insideImage = blurred;

		blurred = new Mat();
		Imgproc.blur(outsideImage, blurred, BLUR_KERNEL);
		outsideImage = blurred;

		insideAverage = Histogram.generate(insideImage).average();
		outsideAverage = Histogram.generate(outsideImage).average();

		difference = Math.abs(insideAverage - outsideAverage);

		if (difference >= PATTERN_MAX_DIFFERENCE) {
			return Fill.solid;
		} else if (difference <= PATTERN_MAX_DIFFERENCE && difference > EMPTY_MAX_DIFFERENCE) {
			return Fill.pattern;
		} else if (difference < EMPTY_MAX_DIFFERENCE) {
			return Fill.empty;
		} else {
			return null;
		}
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
