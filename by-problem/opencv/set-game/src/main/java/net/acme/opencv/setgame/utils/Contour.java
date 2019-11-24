package net.acme.opencv.setgame.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;

/**
 * Contour utils.
 */
public class Contour {
	/**
	 * Close the given contour if there are any gaps
	 */
	public static MatOfPoint close(MatOfPoint contour, int maxGap) {
		List<double[]> points = toList(contour);
		List<double[]> closed = new ArrayList<>();
		int pointCount = points.size();
		int idx;
		List<double[]> win;
		List<double[]> filled;

		closed.add(points.get(0));

		for (idx = 1; idx < pointCount; idx++) {
			win = points.subList(idx - 1, idx + 1);
			filled = _fillPairRecursively(win, maxGap);

			closed.addAll(filled.subList(1, filled.size()));
		}

		return fromList(closed);
	}

	private static List<double[]> midPoint(double[] point1, double[] point2) {
		double x1 = point1[0];
		double y1 = point1[1];
		double dist[] = distance(point1, point2);
		double dxHalve = Math.floor(dist[0] / 2);
		double dyHalve = Math.floor(dist[1] / 2);
		List<double[]> point = new ArrayList<>();

		point.add(new double[] { x1 + dxHalve, y1 + dyHalve });

		return Collections.unmodifiableList(point);
	}

	private static double[] distance(double[] point1, double[] point2) {
		double x1 = point1[0];
		double y1 = point1[1];
		double x2 = point2[0];
		double y2 = point2[1];

		return new double[] { x2 - x1, y2 - y1 };
	}

	private static List<double[]> _fillPairRecursively(List<double[]> twoPoints, int maxGap) {
		List<double[]> filled = new CopyOnWriteArrayList<>(twoPoints);
		double[] left = twoPoints.get(0);
		double[] right = twoPoints.get(1);
		List<double[]> filledLeft = new CopyOnWriteArrayList<>();
		List<double[]> filledRight = new CopyOnWriteArrayList<>();
		double[] dist = distance(left, right);
		double dx = dist[0];
		double dy = dist[1];

		if (maxGap <= 1) {
			throw new IllegalArgumentException("_fillPairRecursively() may not be called with maxGap <= 1");
		}

		if (Math.abs(dx) < maxGap && Math.abs(dy) < maxGap) {
			return twoPoints;
		}

		filled.addAll(1, midPoint(left, right));

		filledLeft.addAll(_fillPairRecursively(filled, maxGap).subList(0, 2));
		filledRight.addAll(_fillPairRecursively(filled, maxGap).subList(1, 3));

		filled = new ArrayList<>();

		filled.addAll(filledLeft);
		filled.addAll(filledRight.subList(1, filledRight.size()));

		return Collections.unmodifiableList(filled);
	}

	/**
	 * Convert contour as matrix to list of points
	 * 
	 * @param contour as matrix
	 * @return contour as list of Points
	 */
	public static List<double[]> toList(MatOfPoint contour) {
		List<double[]> points = new CopyOnWriteArrayList<>();
		int pointsCount = contour.rows();
		int pointIdx;

		for (pointIdx = 0; pointIdx < pointsCount; pointIdx++) {
			points.add(contour.get(pointIdx, 0));
		}

		return Collections.unmodifiableList(points);
	}

	/**
	 * Convert contour as matrix to list of points
	 * 
	 * @param contour as matrix
	 * @return contour as list of Points
	 */
	public static MatOfPoint fromList(List<double[]> contour) {
		Point[] points = contour.stream()
			.map(point -> new Point(point))
			.collect(Collectors.toList())
			.toArray(new Point[contour.size()]);

		return new MatOfPoint(points);
	}
}
