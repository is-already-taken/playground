package net.acme.opencv.setgame.utils;

import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

/**
 * Utils for similarity checks.
 */
public class Similarity {

	/**
	 * Calculate the contour to bounding box area ratio.
	 * 
	 * @param contour the contour to calculate with
	 * @return contour-area over bounding box area ratio
	 * @see https://docs.opencv.org/master/d1/d32/tutorial_py_contour_properties.html
	 */
	public static double extent(MatOfPoint contour) {
		Rect boundingBox = Imgproc.boundingRect(contour);
		double boxArea = boundingBox.width * boundingBox.height;

		double patternArea = Imgproc.contourArea(new MatOfPoint2f(contour.toArray()));

		return patternArea / boxArea;
	}

	/**
	 * Calculate the contour-area to hull area ratio.
	 * 
	 * @param contour the contour to calculate with
	 * @return contour area over hull area ratio
	 * @see https://docs.opencv.org/master/d1/d32/tutorial_py_contour_properties.html
	 */
	public static double solidity(MatOfPoint contour) {
		double patternArea = Imgproc.contourArea(new MatOfPoint2f(contour.toArray()));
		MatOfInt patternHull = new MatOfInt();

		Imgproc.convexHull(contour, patternHull);
		double patternHullArea = Imgproc.contourArea(pickPoints(contour, patternHull));

		return patternArea / patternHullArea;
	}

	public static MatOfPoint2f pickPoints(MatOfPoint points, MatOfInt indices) {
		Point[] pickedPoints = new Point[indices.rows()];
		int newRow = 0;
		for (int index : indices.toArray()) {
			pickedPoints[newRow++] = new Point(points.get(index, 0));
		}
		return new MatOfPoint2f(pickedPoints);
	}
}
