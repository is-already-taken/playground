package net.acme.opencv.setgame;

import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import net.acme.opencv.setgame.debug.Image;
import net.acme.opencv.setgame.utils.Contour;
import net.acme.opencv.setgame.utils.Similarity;

/**
 * Detect shape
 */
public class ShapeDetection {
	// Threshold for the threshold function to separate the share
	// from the card. Inspecting average value or modes was not
	// reliable. The value was determined by looking at histograms
	// of cards with low detection scores.
	static int THRESHOLD = 180;

	// Shape similarity parameters
	static List<ShapeParam> PARAMETERS = new ArrayList<>();

	static {
		PARAMETERS.add(new ShapeParam(Shape.oval, 0.962, 0.792));
		// solidity=0.9601 extent=0.7703
		PARAMETERS.add(new ShapeParam(Shape.sigmoid, 0.886, 0.742));
		PARAMETERS.add(new ShapeParam(Shape.diamond, 0.962, 0.555));
	}

	// Max accepted deviation from the specified parameters.
	static double EPSILON = 0.1;

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
		Mat threshed = new Mat();
		Mat hierarchy = new Mat();
		List<MatOfPoint> contours = new ArrayList<>();
		double extent;
		double solidity;

		Imgproc.threshold(shapeImage, threshed, THRESHOLD, 255, Imgproc.THRESH_BINARY);
		Imgproc.floodFill(threshed, new Mat(), new Point(location.width / 2, location.height / 2), new Scalar(0, 0, 0));
		Imgproc.Canny(threshed, edges, 0, 255, 7);
		Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

		if (contours.size() == 0) {
			return null;
		}

		Mat debugImage = image.clone();
		Image.writeDebugImage(debugImage, contours, false, "shape_det_contours_" + App.id);

		List<Map.Entry<MatOfPoint, Rect>> contoursByArea = contours.stream()
			.collect(Collectors.toMap(Function.identity(), (c) -> Imgproc.boundingRect(c)))
			.entrySet()
			.stream()
			.collect(Collectors.toList());

		contoursByArea.sort(new Comparator<Map.Entry<MatOfPoint, Rect>>() {
			@Override
			public int compare(Map.Entry<MatOfPoint, Rect> o1, Map.Entry<MatOfPoint, Rect> o2) {
				double area1 = o1.getValue().area();
				double area2 = o2.getValue().area();

				return (area1 < area2) ? -1 : (area1 > area2 ? 1 : 0);
			}
		});

		Collections.reverse(contoursByArea);
		System.out.println(Contour.toList(contoursByArea.get(0).getKey()).stream()
			.map((p) -> new Point(p).toString())
			.collect(Collectors.toList()));

		extent = Similarity.extent(contoursByArea.get(0).getKey());
		solidity = Similarity.solidity(contoursByArea.get(0).getKey());

		System.out.println(String.format("solidity=%2.4f extent=%2.4f", solidity, extent));
		Shape s = findBestMatch(extent, solidity);
		if (s == null) {
			MatOfInt patternHull = new MatOfInt();
			MatOfPoint contour = contoursByArea.get(0).getKey();
			Imgproc.convexHull(contour, patternHull);
			double patternHullArea = Imgproc.contourArea(Similarity.pickPoints(contour, patternHull));
			System.out.println(String.format("patternHullArea=%2.5f area=%2.5f area=%2.5f", patternHullArea, Imgproc
				.contourArea(new MatOfPoint2f(contour.toArray())), Imgproc.contourArea(contour)));

			Image.writeDebugImage(shapeImage, (Rect) null, "no_shape_det_raw_" + App.id);
			Image.writeDebugImage(threshed, (Rect) null, "no_shape_det_threshed_" + App.id);
			Image.writeDebugImage(edges, (Rect) null, "no_shape_det_edges_" + App.id);
			Mat debugImageRect = shapeImage.clone();
			Image.writeDebugImage(debugImageRect, contoursByArea.get(0).getValue(), "no_shape_det_raw_box_" + App.id);
			System.out.println("N O   S H A P E   D E T E C T E D");
		} else if (App.id.equals("xcard_8")) {
			Image.writeDebugImage(threshed, (Rect) null, "wrong_shape_det_threshed_" + App.id);
			Image.writeDebugImage(edges, (Rect) null, "wrong_shape_det_edges_" + App.id);
		} else if (App.id.equals("card_15")) {
			Mat debugImageRect = shapeImage.clone();

			List<MatOfPoint> foo = new ArrayList<MatOfPoint>();
			foo.add(contoursByArea.get(0).getKey());

			Image.writeDebugImage(debugImageRect, foo, false, "good_shape_" + App.id);
		}
		return s;
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
				if (App.id.equals("cardx_8")) {
					System.out.println(String
						.format("prod=%2.5f vs pex=%2.3f psol=%2.3f ex=%2.3f sol=%2.3f shape=%s", (extentDeviation
							* solidityDeviation), param.extent, param.solidity, extent, solidity, param.shape
								.toString()));
				}
				matchedShapes.add(new AbstractMap.SimpleEntry<Double, Shape>(
					// Use absolute value, we don't card in which direction we've deviated.
					Math.abs(extentDeviation * solidityDeviation),
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
