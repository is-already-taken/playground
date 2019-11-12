package net.acme.opencv.setgame;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import net.acme.opencv.setgame.ColorDetection.Color;
import net.acme.opencv.setgame.FillDetection.Fill;
import net.acme.opencv.setgame.Shapeextraction.Shape;

public class App {
	private static final String opencv = Paths.get("target", "classes", "libopencv_java412.so").toString();

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

		List<Rect> cards = Cardfinder.process(imageGray);
		System.out.println(String.format("Found %d possible cards", cards.size()));

		for (Rect card : cards) {
			Mat cardImage = image.submat(card);
			Mat cardImageGray = new Mat();
			Imgproc.cvtColor(cardImage, cardImageGray, Imgproc.COLOR_BGR2GRAY);

			boolean written = Imgcodecs.imwrite(pathOut.getAbsolutePath(), cardImage);

			Shape shape = Shapeextraction.process(cardImageGray);
			if (shape == null) {
				System.out.println("No shape detected");
				return;
			}
			System.out.println(String.format("Shape count=%d", shape.count));

			Fill fill = FillDetection.process(cardImageGray, shape.location);
			if (fill == null) {
				System.out.println("No fill dectected");
				return;
			}
			System.out.println("Fill: " + fill.name());

			Color color = ColorDetection.process(cardImage, shape.location);
			if (color == null) {
				System.out.println("No color detected");
				return;
			}
			System.out.println("Color: " + color.name());

			ShapeDetection.Shape form = ShapeDetection.process(cardImageGray, shape.location);
			if (form == null) {
				System.out.println("No shape detected");
				return;
			}
			System.out.println("Shape: " + form.name());
		}

		// boolean written = Imgcodecs.imwrite(pathOut.getAbsolutePath(), imageOut);
		// System.out.println(String.format("Contours written to %s (%s)", pathOut, Boolean.toString(written)));
	}
}
