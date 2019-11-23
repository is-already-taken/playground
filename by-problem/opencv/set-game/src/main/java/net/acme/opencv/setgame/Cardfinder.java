package net.acme.opencv.setgame;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import net.acme.opencv.setgame.utils.Histogram;

/**
 * Find cards in an image. Make basic plausibility checks on the boxes.
 */
public class Cardfinder {
	// Threshold to separate cards from background  
	private static final int THRESHOLD = 200;

	// Aspect ratio of a card
	private static final double ASPECT_RATIO = 90 / 60;

	// Max deviation from the ideal aspect ratio
	private static final double MAX_RATIO_DEV = 0.1;

	// Ratio of one card area to the grid 4x3 area of such a card
	// Assumes the image contains mostly cards (ideal = 0.085)
	// Probing history: 0.03 too large for slightly skewed image
	private static final double MAX_CARD_GRID_RATIO = 0.025;

	/**
	 * Process image to find cards.
	 * 
	 * @param image the input image in grayscale format
	 * @return list of Rects of the card outlines
	 */
	public static List<Rect> process(Mat image) {
		int imageSize = image.rows() * image.cols();
		List<Rect> cards = new ArrayList<>();
		Histogram hist = Histogram.generate(image);
		int averageValue = hist.average();
		Mat threshed = new Mat();
		Mat edges = new Mat();
		List<MatOfPoint> contours = new ArrayList<>();
		Mat hierarchy = new Mat();
		Rect rect;

		Imgproc.threshold(image, threshed, THRESHOLD, 255, Imgproc.THRESH_BINARY);

		Imgproc.Canny(threshed, edges, averageValue * 0.66, averageValue * 1.66, 3);

		// Using RETR_EXTERNAL to limit to enclosing contours/boxes
		Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

		for (MatOfPoint contour : contours) {
			// https://docs.opencv.org/master/dd/d49/tutorial_py_contour_features.html
			rect = Imgproc.boundingRect(contour);

			// boundingRects() may have returned duplicate Rects for one cards
			if (isLikelyCard(rect, imageSize)) {
				cards.add(rect);
			}
		}

		return cards;
	}

	private static boolean isLikelyCard(Rect rect, int imageSize) {
		double aspectRatio;
		double cardSize = rect.width * rect.height;

		if (rect.width > rect.height) {
			aspectRatio = rect.width / rect.height;
		} else {
			aspectRatio = rect.height / rect.width;
		}

		boolean aspectRatioMatch = Math.abs(aspectRatio - ASPECT_RATIO) <= MAX_RATIO_DEV;
		boolean cardToGridRatioMatch = cardSize / imageSize >= MAX_CARD_GRID_RATIO;

		return aspectRatioMatch && cardToGridRatioMatch;
	}
}
