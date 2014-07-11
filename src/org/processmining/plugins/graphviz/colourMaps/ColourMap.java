package org.processmining.plugins.graphviz.colourMaps;

import java.awt.Color;

public abstract class ColourMap {
	public abstract String colour(long weight, long maxWeight);
	public abstract Color colour2(long weight, long maxWeight);
	
	public String colour(long weight, long min, long max) {
		if (max == min) {
			return colour(weight, weight);
		}
		return colour(weight - min, max - min);
	}
	
	public Color colour2(long weight, long min, long max) {
		if (max == min) {
			return colour2(weight, weight);
		}
		return colour2(weight - min, max - min);
	}
		
	public String colourString(long weight, long min, long max) {
		return toHexString(colour2(weight, min, max));
	}

	public static String toHexString(Color colour) {
		String hexColour = Integer.toHexString(colour.getRGB());
		return "#" + hexColour.substring(2, hexColour.length());
	}
}
