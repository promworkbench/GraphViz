package org.processmining.plugins.graphviz.colourMaps;

public class ColourMapBlue implements ColourMap {

	public String colour(long weight, long maxWeight) {
		return ColourMaps.colourMapBlue(weight, maxWeight);
	}

}
