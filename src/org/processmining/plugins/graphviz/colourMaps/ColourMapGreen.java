package org.processmining.plugins.graphviz.colourMaps;

public class ColourMapGreen implements ColourMap {

	public String colour(long weight, long maxWeight) {
		return ColourMaps.colourMapGreen(weight, maxWeight);
	}

}
