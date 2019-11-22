package net.acme.opencv.setgame;

import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import net.acme.opencv.setgame.utils.Histogram;
import net.acme.opencv.setgame.utils.Similarity;

/**
 * Detect shape
 */
public class ShapeDetection {
	// Shape similarity parameters
	static List<ShapeParam> PARAMETERS = new ArrayList<>();

	static {
		PARAMETERS.add(new ShapeParam(Shape.oval, 0.962, 0.792));
		PARAMETERS.add(new ShapeParam(Shape.sigmoid, 0.886, 0.742));
		PARAMETERS.add(new ShapeParam(Shape.diamond, 0.962, 0.555));
	}

	// Max accepted deviation from the specified parameters.
	static double EPSILON = 0.07;

	/**
	 * Process card image to get shape.
	 * 
	 * @param image of a shape
	 * @param location location of a shape on the image
	 * @return shape
	 */
	public static Shape process(Mat image, Rect location) {
		Mat shapeImage = image.submat(location);
		Histogram histogram;
		List<Integer> modes;
		int threshold;
		Mat edges = new Mat();
		Mat threshed = new Mat();
		Mat hierarchy = new Mat();
		List<MatOfPoint> contours = new ArrayList<>();
		double extent;
		double solidity;

		histogram = Histogram.generate(shapeImage, 0);
		modes = histogram.modes(20, 2);

		// Calculate the valley between both modes - this sets out threshold
		// since lower values contribute to the shape, higher values
		// contribute to the card.
		threshold = (modes.get(1) + (modes.get(0) - modes.get(1)) / 2);

		Imgproc.threshold(shapeImage, threshed, threshold, 255, Imgproc.THRESH_BINARY);
		Imgproc.floodFill(threshed, new Mat(), new Point(location.width / 2, location.height / 2), new Scalar(0, 0, 0));
		Imgproc.Canny(threshed, edges, 0, 255, 3);
		Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

		if (contours.size() == 0) {
			return null;
		}

		extent = Similarity.extent(contours.get(0));
		solidity = Similarity.solidity(contours.get(0));

		return findBestMatch(extent, solidity);
	}

	/**
	 * Match passed extent and solidity against calibrated parameters.
	 */
	private static Shape findBestMatch(double extent, double solidity) {
		List<AbstractMap.SimpleEntry<Double, Shape>> matchedShapes = new ArrayList<>();

		for (ShapeParam param : PARAMETERS) {
			double extentDeviation = param.extent - extent;
			double solidityDeviation = param.solidity - solidity;

			boolean extentWithinEpsilon = extentDeviation < EPSILON;
			boolean solidityWithinEpsilon = solidityDeviation < EPSILON;

			if (extentWithinEpsilon && solidityWithinEpsilon) {
				matchedShapes.add(new AbstractMap.SimpleEntry<Double, Shape>(
					extentDeviation * solidityDeviation,
					param.shape));
			}
		}

		if (matchedShapes.size() == 0) {
			return null;
		}

		// Multiple shapes might be within EPSILON, pick the best match
		matchedShapes.sort(new Comparator<AbstractMap.SimpleEntry<Double, Shape>>() {
			@Override
			public int compare(SimpleEntry<Double, Shape> o1, SimpleEntry<Double, Shape> o2) {
				return (o1.getKey() < o2.getKey()) ? -1 : (o1.getKey() > o2.getKey() ? 1 : 0);
			}
		});

		return matchedShapes.get(0).getValue();
	}

	/**
	 * The shape
	 */
	public static enum Shape {
		diamond, oval, sigmoid
	}

	/**
	 * Similarity parameters.
	 * See
	 * https://docs.opencv.org/master/d1/d32/tutorial_py_contour_properties.html
	 */
	static class ShapeParam {
		double solidity;
		double extent;
		Shape shape;

		public ShapeParam(Shape shape, double solidity, double extent) {
			this.solidity = solidity;
			this.extent = extent;
			this.shape = shape;
		}
	}
}
