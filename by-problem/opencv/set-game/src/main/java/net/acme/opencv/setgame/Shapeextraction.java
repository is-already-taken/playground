package net.acme.opencv.setgame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import net.acme.opencv.setgame.utils.Correction;
import net.acme.opencv.setgame.utils.Histogram;

/**
 * Receives an image of a card and extracts the interesting area with the
 * shape/s.
 */
public class Shapeextraction {
	// Shrink Npx from each side to remove possible card edges
	private static final int SHRINK = 5;

	// Gamma correct input image to improve contrast
	// Higher is better - 1.2 is not enough, 1.5 is good
	private static final double GAMMA = 1.6;

	// Blur image to extend shape bounding boxes
	// 5x5 produces fairly good results for most cards
	private static final Size BLUR_KERNEL = new Size(new double[] { 3, 3 });

	// When determining the modes of the histogram require this distance.
	private static final int MODE_DISTANCE = 20;

	// Bounding box padding
	private static final int PADDING = 1;

	// Fraction of the card area that shall be considered as shape
	private static final double MIN_CARD_SCALE_FACTOR = 0.1;

	/**
	 * Process card image to get shape.
	 * 
	 * @param image of a card
	 * @return shape information {@link Shape}
	 */
	public static Shape process(Mat image) {
		Rect crop = new Rect(
			SHRINK,
			SHRINK,
			image.cols() - (2 * SHRINK),
			image.rows() - (2 * SHRINK));
		Mat subimg = image.submat(crop);
		Mat blurred = new Mat();
		Histogram hist;
		List<Integer> modes;
		int threshold;
		Mat threshed = new Mat();
		Mat edges = new Mat();
		Mat hierarchy = new Mat();
		List<MatOfPoint> contours = new ArrayList<>();

		subimg = Correction.gamma(subimg, GAMMA);

		Imgproc.blur(subimg, blurred, BLUR_KERNEL);

		hist = Histogram.generate(blurred);
		modes = hist.modes(MODE_DISTANCE, 2);

		if (modes.size() != 2) {
			throw new RuntimeException("Not enough modes detected in histogram");
		}

		// Calculate the valley between both modes - this sets out threshold
		// since lower values contribute to the shape, higher values
		// contribute to the card.
		threshold = (modes.get(1) + (modes.get(0) - modes.get(1)) / 2);

		Imgproc.threshold(blurred, threshed, threshold, 255, Imgproc.THRESH_BINARY);

		// A second pass blur + threshold with 3x3 and 220 was initially
		// implemented. It was removed though, since, during development,
		// it did not produce better results.

		Imgproc.Canny(threshed, edges, 0, 255, 3);
		Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

		int cardSize = subimg.rows() * subimg.cols();

		List<Rect> rects = contours
			.stream()
			.map((matOfPt) -> Imgproc.boundingRect(matOfPt))
			// Remove rects that are proportionally too small
			.filter((rect) -> (rect.width * rect.height) > (cardSize * MIN_CARD_SCALE_FACTOR))
			.collect(Collectors.toList());

		rects = normalizeRects(rects);

		int shapeCount = rects.size();

		// Reject bad shape count
		if (shapeCount < 1 || shapeCount > 3) {
			return null;
		}

		// Pick arbitrary shape bounding box
		Rect shapeRect = rects.get(0);

		Shape shape = new Shape(
			shapeCount,
			new Rect(
				// We've cropped a part out of the image, add the difference
				// since it shall refer to the input image.
				shapeRect.x + SHRINK - PADDING,
				shapeRect.y + SHRINK - PADDING,
				shapeRect.width + (2 * PADDING),
				shapeRect.height + (2 * PADDING)));

		return shape;
	}

	/**
	 * Remove rects contained in other rects (artifacts).
	 * 
	 * @param rects input list
	 * @return list of rects free from rects contained in other rects
	 */
	private static List<Rect> normalizeRects(List<Rect> rects) {
		List<Rect> rectsFiltered = new ArrayList<>();

		rects = rects
			.stream()
			.sorted(new RectSizeComparator())
			.collect(Collectors.toList());

		// Reverse, because we pick larger rect first, then check that the next
		// - smaller - one is not contained.
		Collections.reverse(rects);

		// Add largest rect, it's excluded by the loop
		rectsFiltered.add(rects.get(0));

		int outerIndex = -1;
		int innerIndex;

		for (Rect largerRect : rects) {
			outerIndex++;

			for (innerIndex = outerIndex + 1; innerIndex < rects.size(); innerIndex++) {
				Rect innerRect = rects.get(innerIndex);

				if (!contained(innerRect, largerRect) && !rectsFiltered.contains(innerRect)) {
					rectsFiltered.add(innerRect);
				}
			}
		}

		return rectsFiltered;
	}

	/**
	 * Test whether innerRect is contained in outerRect
	 */
	private static boolean contained(Rect outerRect, Rect innerRect) {
		int x1 = outerRect.x;
		int y1 = outerRect.y;
		int x2 = outerRect.x + outerRect.width;
		int y2 = outerRect.y + outerRect.height;

		return (innerRect.x >= x1 && (innerRect.x + innerRect.width) <= x2)
			&& (innerRect.y >= y1 && (innerRect.y + innerRect.height) <= y2);
	}

	/**
	 * Information on shape/s of a card.
	 */
	public static class Shape {
		/**
		 * Count of shapes on the card
		 */
		int count = 0;
		/**
		 * Arbitrary location of the shape on the input image of the card
		 */
		Rect location = null;

		public Shape(int count, Rect location) {
			this.count = count;
			this.location = location;
		}
	}

	/**
	 * Compares two cards by their area (width x height)
	 */
	static class RectSizeComparator implements Comparator<Rect> {
		@Override
		public int compare(Rect o1, Rect o2) {
			int size1 = o1.width * o1.height;
			int size2 = o2.width * o2.height;
			return (size1 < size2) ? -1 : (size1 > size2 ? 1 : 0);
		}
	}

}
