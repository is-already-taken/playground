package net.acme.opencv.setgame;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * Detect shape
 */
public class ShapeDetection {
	// Blur shape to smooth edges
	private static final Size BLUR_KERNEL = new Size(new double[] { 3, 3 });

	// The threshold strongly determines how well the 
	// shape is "carved" out
	private static final int THRESHOLD = 240;

	/**
	 * Process card image to get shape.
	 * 
	 * @param image of a shape
	 * @param location location of a shape on the image
	 * @return shape
	 */
	public static Shape process(Mat image, Rect location) {
		Mat shapeImage = image.submat(location);
		Mat edges = new Mat();
		Mat blurred = new Mat();
		Mat threshed = new Mat();
		Mat hierarchy = new Mat();
		List<MatOfPoint> contours = new ArrayList<>();
		int numberOfPoints;

		Imgproc.blur(shapeImage, blurred, BLUR_KERNEL);
		Imgproc.threshold(blurred, threshed, THRESHOLD, 255, Imgproc.THRESH_BINARY);
		Imgproc.Canny(threshed, edges, 0, 255, 3);
		Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

		if (contours.size() == 0) {
			return null;
		}

		numberOfPoints = contours.get(0).rows();

		if (numberOfPoints < 20) {
			return Shape.diamond;
		} else if (numberOfPoints < 40) {
			return Shape.oval;
		} else if (numberOfPoints < 60) {
			return Shape.sigmoid;
		}

		return null;
	}

	/**
	 * The shape
	 */
	public static enum Shape {
		diamond, oval, sigmoid
	}
}
