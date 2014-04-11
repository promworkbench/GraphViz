package org.processmining.plugins.graphviz.colourMaps;

public class ColourMapGreen extends ColourMap {

	public String colour(long weight, long maxWeight) {
		return ColourMaps.colourMapGreen(weight, maxWeight);
	}

}
