package org.processmining.plugins.graphviz.colourMaps;

public class ColourMapFixedOpacity extends ColourMap {
	private final ColourMap base;
	private final String opacity;
	public ColourMapFixedOpacity(ColourMap base, float opacity) {
		this.base = base;
		this.opacity = Integer.toHexString((int) (opacity * 255));
	}
	public String colour(long weight, long maxWeight) {
		return base.colour(weight, maxWeight) + opacity;
	}
}
