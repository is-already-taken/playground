package net.acme.opencv.setgame;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import net.acme.opencv.setgame.Cardfinder.Card;
import net.acme.opencv.setgame.ColorDetection.Color;
import net.acme.opencv.setgame.FillDetection.Fill;
import net.acme.opencv.setgame.Shapeextraction.Shape;
import net.acme.opencv.setgame.utils.Transformation;

public class App {
	private static final String opencv = Paths.get("target", "classes", "libopencv_java412.so").toString();

	public static String id = "";

	public static void main(String[] args) throws Exception {
		System.load(new File(opencv).getAbsolutePath());

		File path = new File(args[0]);
		File pathOut = new File(args[1]);

		if (!path.exists()) {
			throw new Exception("File not found: " + path);
		}

		System.out.println("Loading " + path);

		Mat image = Imgcodecs.imread(path.getAbsolutePath(), Imgcodecs.IMREAD_COLOR);
		Mat imageGray = new Mat();
		Imgproc.cvtColor(image, imageGray, Imgproc.COLOR_BGR2GRAY);

		List<Card> cards = Cardfinder.process(imageGray);
		System.out.println(String.format("Found %d possible cards", cards.size()));
		int failCount = 0;
		Mat imageMarks = image.clone();
		Mat imageMarksCardsOnly = image.clone();

		int cardNo = 0;
		for (Card card : cards) {
			Rect cardRect = card.box;

			cardNo++;

			id = "card_" + cardNo;

			System.out.println("vvv=============== " + App.id + " ===============vvv");

			String attributes = "";

			Mat cardImage = image.submat(cardRect);
			String rot = "not_";

			if (card.rotation != 0) {
				cardImage = Transformation.rotate(cardImage, card.rotation, new Point(cardImage.cols() / 2, cardImage
					.rows() / 2));
				cardImage = cardImage.submat(new Rect(5, 5, cardRect.width - 10, cardRect.height - 10));
				rot = "";
			}

			Mat cardImageGray = new Mat();
			Imgproc.cvtColor(cardImage, cardImageGray, Imgproc.COLOR_BGR2GRAY);

			// writeDebugImage(image, cardRect, "card_" + cardNo);
			// writeDebugImage(cardImage, "card_rotated_" + cardNo);

			Imgproc.rectangle(imageMarksCardsOnly, cardRect, new Scalar(255, 0, 255));
			Imgproc.putText(imageMarksCardsOnly, "" + cardNo, new Point(cardRect.x + 10, cardRect.y
				+ 20), Imgproc.FONT_HERSHEY_PLAIN, 0.8, new Scalar(255, 0, 255));

			Shape shape = null;
			try {
				shape = Shapeextraction.process(cardImageGray);
				if (shape == null) {
					failCount++;
					writeDebugImage(cardImage, rot + "rotated_" + App.id);

					System.out.println("No shape detected (extraction)");
					writeDebugImage(image, cardRect, "no_shape_det");
					continue;
				}
				System.out.println(String.format("Shape count=%d", shape.count));
				attributes += shape.count;
			} catch (Exception e) {
				failCount++;
				System.err.println(e);
				e.printStackTrace(System.err);
				writeDebugImage(image, cardRect, "no_shape_det_ex");
				continue;
			}

			if (2 == 1) {
				continue;
			}

			try {
				ShapeDetection.Shape form = ShapeDetection.process(cardImageGray, shape.location);
				if (form == null) {
					failCount++;
					System.out.println("No shape detected");
					writeDebugImage(cardImageGray, shape.location, "no_shape_det_in_" + App.id);
					// writeDebugImage(image, cardRect, "no_shape_det");
					// writeDebugImage(cardImage, "no_shape_det_" + cardNo);
					continue;
				}
				System.out.println("Shape: " + form.name());
				attributes += " " + form.name();
			} catch (Exception e) {
				failCount++;
				System.err.println(e);
				e.printStackTrace(System.err);
				System.err.println(cardImageGray + " " + shape.location);
				writeDebugImage(image, cardRect, "no_shape_det_ex");
				continue;
			}

			// shape
			writeImage(image, cardRect, shape.location, "" + cardNo);

			try {
				Fill fill = FillDetection.process(cardImageGray, shape.location);
				writeDebugImage(cardImageGray, shape.location, "fill_raw_" + cardRect.x + "x" + cardRect.y);

				if (fill == null) {
					failCount++;
					System.out.println("No fill dectected");
					writeDebugImage(image, cardRect, "no_fill_det");
					continue;
				}
				System.out.println("Fill: " + fill.name());
				attributes += " " + fill.name().substring(0, 3);
			} catch (Exception e) {
				failCount++;
				System.err.println(e);
				e.printStackTrace(System.err);
				writeDebugImage(image, cardRect, "no_fill_det_ex");
				continue;
			}

			try {
				Color color = ColorDetection.process(cardImage, shape.location, "" + cardNo);
				if (color == null) {
					failCount++;
					System.out.println("No color detected");
					writeDebugImage(image, cardRect, "no_color_det");
					continue;
				}
				System.out.println("Color: " + color.name());
				attributes += " " + color.name().substring(0, 1).toUpperCase();
			} catch (Exception e) {
				failCount++;
				System.err.println(e);
				e.printStackTrace(System.err);
				writeDebugImage(image, cardRect, "no_color_det_ex");
				continue;
			}

			Imgproc.rectangle(imageMarks, cardRect, new Scalar(0, 0, 255));
			Imgproc.putText(imageMarks, attributes, new Point(cardRect.x, cardRect.y
				+ 10), Imgproc.FONT_HERSHEY_PLAIN, 0.6, new Scalar(0, 0, 255));
		}

		System.out.println("Fails: " + failCount);

		pathOut = getOutputFile(path, "out");
		boolean written = Imgcodecs.imwrite(pathOut.getAbsolutePath(), imageMarks);
		System.out.println(String.format("Marked image written to %s (%s)", pathOut, Boolean.toString(written)));

		Imgcodecs.imwrite(getOutputFile(path, "out.cardsonly").getAbsolutePath(), imageMarksCardsOnly);
	}

	private static File getOutputFile(File file, String suffix) {
		String name = file.getName();
		// String parent = new File("/tmp");
		int lastDot = name.lastIndexOf('.');

		String basename = name.substring(0, lastDot);
		String ext = name.substring(lastDot + 1);

		name = String.format("%s.%s.%s", basename, suffix, ext);

		return new File(new File("/tmp"), name);
	}

	private static void writeDebugImage(Mat gridImage, String id) {
		String location = Paths.get("/tmp", "cards_debug_card_" + id + ".png").toString();

		boolean written = Imgcodecs.imwrite(location, gridImage);
		if (written) {
			System.out.println(String.format("Debug image written to %s", location));
		} else {
			System.out.println("Error writing debug image.");
		}
	}

	private static void writeDebugImage(Mat gridImage, Rect cardRect, String id) {
		String point = cardRect.x + "x" + cardRect.y;
		String location = Paths.get("/tmp", "cards_debug_card_" + point + "_" + id + ".png").toString();
		Mat debugImage = gridImage.clone();
		Imgproc.rectangle(debugImage, cardRect, new Scalar(0, 0, 255), 2);
		boolean written = Imgcodecs.imwrite(location, debugImage);
		if (written) {
			System.out.println(String.format("Debug image written to %s", location));
		} else {
			System.out.println("Error writing debug image.");
		}
	}

	private static void writeImage(Mat gridImage, Rect cardRect, Rect shapeRect, String id) {
		int x = cardRect.x + shapeRect.x;
		int y = cardRect.y + shapeRect.y;
		Rect r = new Rect(x, y, shapeRect.width, shapeRect.height);
		String location = Paths.get("/tmp", "card_" + id + "_" + cardRect.x + "x" + cardRect.y + ".png").toString();
		Mat debugImage = gridImage.clone();
		Imgproc.rectangle(debugImage, cardRect, new Scalar(0, 0, 255), 2);
		boolean written = Imgcodecs.imwrite(location, debugImage);
		if (written) {
			System.out.println(String.format("Image written to %s", location));
		} else {
			System.out.println("Error writing image.");
		}
	}
}
