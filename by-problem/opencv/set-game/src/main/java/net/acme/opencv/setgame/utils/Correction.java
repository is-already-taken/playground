package net.acme.opencv.setgame.utils;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

/**
 * Correction utils
 */
public class Correction {

	/**
	 * Gamma correct image
	 * 
	 * @param image input image
	 * @param gamma gamma value
	 * @return gamma corrected image
	 */
	public static Mat gamma(Mat image, double gamma) {
		// Source:
		// https://docs.opencv.org/3.4/d3/dc1/tutorial_basic_linear_transform.html
		double invGamma = 1.0 / gamma;
		int values = 256;
		double maxValue = 255.0;
		Mat gammaMatrix = new Mat(values, 1, CvType.CV_8U);
		int i;
		double factor;
		Mat processed = new Mat();

		for (i = 0; i < values; i++) {
			factor = gammaMatrix.get(i, 0)[0];
			factor = Math.pow((double) i / maxValue, invGamma) * maxValue;
			gammaMatrix.put(i, 0, factor);
		}

		Core.LUT(image, gammaMatrix, processed);

		return processed;
	}

	public static Mat map(Mat image, int oldValue, int newValue) {
		int values = 256;
		Mat matrix = new Mat(values, 1, CvType.CV_8U);
		Mat processed = new Mat();
		int i;

		// init with 1:1 mapping of input => output values
		for (i = 0; i < values; i++) {
			matrix.put(i, 0, i);
		}

		matrix.put(oldValue, 0, newValue);

		Core.LUT(image, matrix, processed);

		return processed;
	}
}
