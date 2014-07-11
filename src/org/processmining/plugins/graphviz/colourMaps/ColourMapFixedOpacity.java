package org.processmining.plugins.graphviz.colourMaps;

import java.awt.Color;

public class ColourMapFixedOpacity extends ColourMap {
	private final ColourMap base;
	private final int opacity;
	public ColourMapFixedOpacity(ColourMap base, float opacity) {
		this.base = base;
		this.opacity = (int) (opacity * 255);
	}
	public Color colour2(long weight, long maxWeight) {
		Color colour = base.colour2(weight, maxWeight);
		return new Color(colour.getRed(), colour.getGreen(), colour.getBlue(), opacity);
	}
	public String colour(long weight, long maxWeight) {
		// TODO Auto-generated method stub
		return null;
	}
}
