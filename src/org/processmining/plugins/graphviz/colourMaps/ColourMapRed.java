package org.processmining.plugins.graphviz.colourMaps;

public class ColourMapRed extends ColourMap {

	public String colour(long weight, long maxWeight) {
		return ColourMaps.colourMapRed(weight, maxWeight);
	}

}
