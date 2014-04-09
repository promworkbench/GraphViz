package org.processmining.plugins.graphviz.colourMaps;

public class ColourMapRed implements ColourMap {

	public String colour(long weight, long maxWeight) {
		return ColourMaps.colourMapRed(weight, maxWeight);
	}

}
