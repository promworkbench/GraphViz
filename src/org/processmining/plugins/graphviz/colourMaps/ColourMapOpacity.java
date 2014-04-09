package org.processmining.plugins.graphviz.colourMaps;

public class ColourMapOpacity implements ColourMap {
	private final ColourMap base;
	public ColourMapOpacity(ColourMap base) {
		this.base = base;
	}
	public String colour(long weight, long maxWeight) {
		float x = weight / (float) maxWeight;
		return base.colour(weight, maxWeight) + Integer.toHexString((int) (x * 255));
	}
}
