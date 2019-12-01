package net.acme.opencv.setgame.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collector;
import java.util.stream.Collectors;

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
	 * Get the modes of the histogram.
	 * 
	 * Its not guaranteed that the resulting list contains up to the number of
	 * modes specified.
	 * 
	 * @param minValueDistance accept only values with a minimum distance to the
	 *        last mode
	 * @param grades number of modes
	 * @return list of modes with most frequent value first.
	 */
	public List<Integer> modes(int minValueDistance, int grades) {
		List<Integer> normalizedValues = normalized();
		List<Integer> modes = new ArrayList<>();
		final AtomicInteger index = new AtomicInteger(0);
		int grade = 0;
		int lastValue;

		if (grades <= 0 || grades >= BIN_COUNT) {
			throw new IllegalArgumentException("Number of grades must be between 1 - " + BIN_COUNT);
		}

		// Pair values with their frequency (count) and sort by the frequency.
		// Map to value, most frequent value last.
		normalizedValues = normalizedValues.stream()
			.map((value) -> new int[] { index.getAndIncrement(), value })
			.sorted(new CompareByFrequency())
			.map((valueFrequencyPair) -> valueFrequencyPair[0])
			.collect(Collectors.toList());

		// Most frequent value first
		Collections.reverse(normalizedValues);

		// First mode
		modes.add(normalizedValues.get(0));
		lastValue = normalizedValues.get(0);

		while (modes.size() < grades && grade < MAX_VALUE) {
			int value = normalizedValues.get(grade);

			if (lastValue - value > minValueDistance) {
				modes.add(value);
				lastValue = value;
			}

			grade++;
		}

		return modes;
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

	/**
	 * Compare a pair of value-frequency by the frequency.
	 */
	private static class CompareByFrequency implements Comparator<int[]> {

		@Override
		public int compare(int[] o1, int[] o2) {
			int freq1 = o1[1];
			int freq2 = o2[1];

			return (freq1 < freq2) ? -1 : (freq1 > freq2 ? 1 : 0);
		}

	}
}
