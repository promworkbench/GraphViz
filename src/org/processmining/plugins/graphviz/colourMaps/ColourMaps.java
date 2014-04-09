package org.processmining.plugins.graphviz.colourMaps;

import java.awt.Color;

public class ColourMaps {
	public static String colourMapBlackBody(long weight, long maxWeight) {
		float x = weight / (float) maxWeight;

		/*
		 * //blue-yellow x = (x * (float) 0.5) + (float) 0.5; return new
		 * Color(x, x, 1-x);
		 */
		x = (x * (float) 0.75) + (float) 0.25;

		//black-body
		Color colour = new Color(Math.min(Math.max((1 - x) * 3, 0), 1), Math.min(
				Math.max((((1 - x) - 1 / (float) 3) * 3), 0), 1), Math.min(
				Math.max((((1 - x) - 2 / (float) 3) * 3), 0), 1));

		String hexColour = Integer.toHexString(colour.getRGB());
		return "#" + hexColour.substring(2, hexColour.length());
	}

	public static String colourMapRed(long weight, long maxWeight) {
		float x = weight / (float) maxWeight;

		x = (x * (float) 0.5) + (float) 0.5;
		Color colour = new Color(1, 1 - x, 1 - x);

		String hexColour = Integer.toHexString(colour.getRGB());
		return "#" + hexColour.substring(2, hexColour.length());
	}

	public static String colourMapGreen(long weight, long maxWeight) {
		float x = weight / (float) maxWeight;

		x = (x * (float) 0.75) + (float) 0.25;
		Color colour = new Color(1 - x, 1, 1 - x);

		String hexColour = Integer.toHexString(colour.getRGB());
		return "#" + hexColour.substring(2, hexColour.length());
	}

	public static String colourMapBlue(long weight, long maxWeight) {
		float x = weight / (float) maxWeight;

		x = (x * (float) 0.75) + (float) 0.25;
		Color colour = new Color(1 - x, 1 - x, 1);

		String hexColour = Integer.toHexString(colour.getRGB());
		return "#" + hexColour.substring(2, hexColour.length());
	}

	public static String colourMapGreyBlack(long weight, long maxWeight) {
		float x = weight / (float) maxWeight;

		x = (x * (float) 0.6) + (float) 0.4;
		Color colour = new Color(1 - x, 1 - x, 1 - x);

		String hexColour = Integer.toHexString(colour.getRGB());
		return "#" + hexColour.substring(2, hexColour.length());
	}

	public static double getLuma(String hex) {
		int R = Integer.valueOf(hex.substring(1, 3), 16);
		int G = Integer.valueOf(hex.substring(3, 5), 16);
		int B = Integer.valueOf(hex.substring(5, 7), 16);

		return 0.299 * R + 0.587 * G + 0.114 * B;
	}
}
