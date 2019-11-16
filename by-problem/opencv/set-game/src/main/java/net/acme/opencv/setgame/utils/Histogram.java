package net.acme.opencv.setgame.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.imgproc.Imgproc;

/**
 * Calculate histogram of an image.
 */
public class Histogram {
	public static final int BIN_COUNT = 256;
	public static final int MAX_VALUE = 256;

	Mat histogram;
	int pixelCount;

	private Histogram(Mat histogram, int pixelCount) {
		this.histogram = histogram;
		this.pixelCount = pixelCount;
	}

	/**
	 * Get the average value of the histogram
	 * 
	 * @return average value
	 */
	public int average() {
		double sum = 0;

		List<Integer> bins = asList(histogram);

		for (int i = 0; i < BIN_COUNT; i++) {
			double value = bins.get(i);
			sum += i * value;
		}

		return (int) (sum / pixelCount);
	}

	/**
	 * Get the histogram normalized
	 * 
	 * @return binned values normalized to the max values ({@link MAX_VALUE})
	 */
	public List<Integer> normalized() {
		Mat normalized = new Mat();

		Core.normalize(histogram, normalized, 0, MAX_VALUE, Core.NORM_MINMAX);

		return asList(normalized);
	}

	/**
	 * Get the histogram with raw counts.
	 * 
	 * @return binned counts per value
	 */
	public List<Integer> raw() {
		return asList(histogram);
	}

	private List<Integer> asList(Mat histogram) {
		List<Integer> bins = new ArrayList<>();

		for (int i = 0; i < BIN_COUNT; i++) {
			bins.add((int) histogram.get(i, 0)[0]);
		}

		return bins;
	}

	/**
	 * Calculate grayscale histogram.
	 */
	public static Histogram generate(Mat image) {
		int channel = 0;
		return generate(image, channel);
	}

	/**
	 * Calculate histogram for a channel.
	 */
	public static Histogram generate(Mat image, int channel) {
		Mat histogram = new Mat();

		Imgproc.calcHist(Arrays.asList(image), new MatOfInt(channel), new Mat(), histogram, new MatOfInt(
			MAX_VALUE), new MatOfFloat(0, BIN_COUNT), true);

		return new Histogram(histogram, image.rows() * image.cols());
	}
}
