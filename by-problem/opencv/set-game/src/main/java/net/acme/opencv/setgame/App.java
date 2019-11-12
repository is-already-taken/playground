package net.acme.opencv.setgame;

import java.io.File;
import java.nio.file.Paths;

public class App {
	private static final String opencv = Paths.get(
		"target",
		"classes",
		"libopencv_java412.so"
	).toString();

	public static void main(String[] args) throws Exception {
		System.load(new File(opencv).getAbsolutePath());
	}
}
