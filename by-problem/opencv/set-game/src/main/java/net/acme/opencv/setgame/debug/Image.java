package net.acme.opencv.setgame.debug;

import java.nio.file.Paths;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class Image {
	public static void writeDebugImage(Mat image, Rect shapeRect, String id) {
		String location = Paths.get("/tmp", "cards_" + id + ".png").toString();
		Mat debugImage = image.clone();
		if (shapeRect != null) {
			Imgproc.rectangle(debugImage, shapeRect, new Scalar(0, 0, 255), 2);
		}
		boolean written = Imgcodecs.imwrite(location, debugImage);
		if (written) {
			System.out.println(String.format("Debug image written to %s", location));
		} else {
			System.out.println("Error writing debug image.");
		}
	}

	public static void writeDebugImage(Mat image, List<Rect> shapeRects, String id) {
		String location = Paths.get("/tmp", "cards_" + id + ".png").toString();
		Mat debugImage = image.clone();
		if (shapeRects != null) {
			for (Rect shapeRect : shapeRects) {
				Imgproc.rectangle(debugImage, shapeRect, new Scalar(0, 0, 255), 1);
			}
		}
		boolean written = Imgcodecs.imwrite(location, debugImage);
		if (written) {
			System.out.println(String.format("Debug image written to %s", location));
		} else {
			System.out.println("Error writing debug image.");
		}
	}

	public static void writeDebugImage(Mat image, List<MatOfPoint> contours, boolean boundingBoxes, String id) {
		String location = Paths.get("/tmp", "cards_debug_shape_det_" + id + ".png").toString();
		Mat debugImage = image.clone();
		Scalar color = new Scalar(0, 0, 255);
		if (contours != null && !boundingBoxes) {
			Imgproc.drawContours(debugImage, contours, -1, color);
		} else if (contours != null) {
			for (MatOfPoint mop : contours) {
				Rect bb = Imgproc.boundingRect(mop);
				Imgproc.rectangle(debugImage, bb, color);
			}
		}
		boolean written = Imgcodecs.imwrite(location, debugImage);
		if (written) {
			System.out.println(String.format("Debug image written to %s", location));
		} else {
			System.out.println("Error writing debug image.");
		}
	}
}
