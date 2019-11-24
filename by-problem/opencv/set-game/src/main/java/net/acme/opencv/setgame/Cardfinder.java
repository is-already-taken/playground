package net.acme.opencv.setgame;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import net.acme.opencv.setgame.utils.Histogram;
import net.acme.opencv.setgame.utils.Lines;
import net.acme.opencv.setgame.utils.Lines.Line;

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

	// Parameters to find (horizontal) lines in a card to
	// detect rotation. Determined by testing.
	private static final int LINE_VOTES = 10;
	private static final int LINE_MIN_LENGTH = 30;
	private static final int LINE_MAX_GAP = 5;
	private static final double LINE_MAX_HOR_ANGLE = 30;

	/**
	 * Process image to find cards.
	 * 
	 * @param image the input image in grayscale format
	 * @return list of Rects of the card outlines
	 */
	public static List<Card> process(Mat image) {
		int imageSize = image.rows() * image.cols();
		List<Card> cards = new ArrayList<>();
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
				Mat cardImage = edges.submat(rect);
				List<Line> lines = Lines.byHoughP(cardImage, LINE_VOTES, LINE_MIN_LENGTH, LINE_MAX_GAP);
				List<Line> linesOut = horizontals(lines, LINE_MAX_HOR_ANGLE);

				if (!linesOut.isEmpty()) {
					cards.add(new Card(rect, Lines.angle(linesOut.get(0))));
				}
			}
		}

		return cards;
	}

	private static List<Line> horizontals(List<Line> lines, double maxAngle) {
		return lines.stream()
			.filter((line) -> Math.abs(Lines.angle(line)) <= maxAngle)
			.collect(Collectors.toList());
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

	public static class Card {
		public Rect box;
		public double rotation;

		public Card(Rect box, double rotation) {
			this.box = box;
			this.rotation = rotation;
		}
	}
}
