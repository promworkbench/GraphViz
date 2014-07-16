package org.processmining.plugins.graphviz.colourMaps;

import java.awt.Color;

public class ColourMapOpacity extends ColourMap {
	private final ColourMap base;
	public ColourMapOpacity(ColourMap base) {
		this.base = base;
	}
	public Color colour2(long weight, long maxWeight) {
		float opacity = weight / (float) maxWeight;
		Color colour = base.colour2(weight, maxWeight);
		return new Color(colour.getRed(), colour.getGreen(), colour.getBlue(), opacity);
	}

}
