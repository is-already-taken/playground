package net.acme.opencv.setgame.utils;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

/**
 * Line finding
 */
public class Lines {

	/**
	 * Get lines by probabilistic Hough transform.
	 * 
	 * @param edges input image (pre-processed to contain edges)
	 * @param rho rho (1 is a good value)
	 * @param theta theta (Constants.PI_OVER_180) is a good value
	 * @param votes how many votes a line shall get
	 * @param minLen line min length
	 * @param maxGap max gap lenght in a line
	 * @return array of lines
	 * @see https://docs.opencv.org/master/d6/d10/tutorial_py_houghlines.html
	 */
	public static List<Line> byHoughP(Mat edges, double rho, double theta, int votes, int minLen, int maxGap) {
		List<Line> lines = new ArrayList<>();
		Mat lineMat = new Mat();
		Imgproc.HoughLinesP(edges, lineMat, rho, theta, votes, minLen, maxGap);
		for (int lineIdx = 0; lineIdx < lineMat.rows(); lineIdx++) {
			lines.add(new Line(
				new Point((int) lineMat.get(lineIdx, 0)[0], (int) lineMat.get(lineIdx, 0)[1]),
				new Point((int) lineMat.get(lineIdx, 0)[2], (int) lineMat.get(lineIdx, 0)[3])));
		}
		return lines;
	}

	/**
	 * 
	 * Get lines by probabilistic Hough transform with fixed rho (1) and theta
	 * (PI/180).
	 * 
	 * @param edges input image (pre-processed to contain edges)
	 * @param rho rho (1 is a good value)
	 * @param theta theta (Constants.PI_OVER_180) is a good value
	 * @param votes how many votes a line shall get
	 * @param minLen line min length
	 * @param maxGap max gap lenght in a line
	 * @return array of lines
	 * @see https://docs.opencv.org/master/d6/d10/tutorial_py_houghlines.html
	 */
	public static List<Line> byHoughP(Mat edges, int votes, int minLen, int maxGap) {
		return byHoughP(edges, 1, Constants.PI_OVER_180, votes, minLen, maxGap);
	}

	/**
	 * Calculate angle of a line.
	 * 
	 * @param line
	 * @return angle in degrees
	 */
	public static double angle(Line line) {
		double x = line.p2.x - line.p1.x;
		double y = line.p2.y - line.p1.y;
		return Math.atan(y / x) / Constants.PI_OVER_180;
	}

	public static class Line {
		public Point p1;
		public Point p2;

		public Line(Point p1, Point p2) {
			this.p1 = p1;
			this.p2 = p2;
		}

		@Override
		public String toString() {
			return String.format("(%1.0f;%1.0f)-(%1.0f;%1.0f)", p1.x, p1.y, p2.x, p2.y);
		}
	}
}
