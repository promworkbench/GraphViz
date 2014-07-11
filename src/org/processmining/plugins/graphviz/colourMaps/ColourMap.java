package org.processmining.plugins.graphviz.colourMaps;

import java.awt.Color;

public abstract class ColourMap {
	public abstract Color colour(long weight, long maxWeight);

	public Color colour(long weight, long min, long max) {
		if (max == min) {
			return colour(weight, weight);
		}
		return colour(weight - min, max - min);
	}
	
	public String colourString(long weight, long min, long max) {
		return toHexString(colour(weight, min, max));
	}

	public static String toHexString(Color colour) {
		String hexColour = Integer.toHexString(colour.getRGB());
		return "#" + hexColour.substring(2, hexColour.length());
	}
}
