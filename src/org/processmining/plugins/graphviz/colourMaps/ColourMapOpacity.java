package org.processmining.plugins.graphviz.colourMaps;

import java.awt.Color;

public class ColourMapOpacity extends ColourMap {
	private final ColourMap base;

	public ColourMapOpacity(ColourMap base) {
		this.base = base;
	}

	public Color colour(long weight, long maxWeight) {
		double opacity = weight / (double) maxWeight;
		Color colour = base.colour(weight, maxWeight);
		return new Color(colour.getRed(), colour.getGreen(), colour.getBlue(), Math.round(opacity * 255));
	}

	public Color colour(double opacity) {
		Color colour = base.colour(opacity);
		return new Color(colour.getRed(), colour.getGreen(), colour.getBlue(), Math.round(opacity * 255));
	}

}
