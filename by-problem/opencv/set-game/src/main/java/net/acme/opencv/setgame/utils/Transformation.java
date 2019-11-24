package net.acme.opencv.setgame.utils;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * Image transformations
 */
public class Transformation {

	/**
	 * Rotate image.
	 * 
	 * @param image input image
	 * @param angle angle
	 * @param origin origin of rotation
	 * @return rotated image
	 */
	public static Mat rotate(Mat image, double angle, Point origin) {
		Mat rotationMatrix = Imgproc.getRotationMatrix2D(origin, angle, 1);
		Size size = new Size(image.cols(), image.rows());
		Mat out = new Mat();
		Imgproc.warpAffine(image, out, rotationMatrix, size);
		return out;
	}
}
