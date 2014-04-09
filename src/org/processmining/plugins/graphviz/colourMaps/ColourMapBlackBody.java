package org.processmining.plugins.graphviz.colourMaps;

public class ColourMapBlackBody implements ColourMap {

	public String colour(long weight, long maxWeight) {
		return ColourMaps.colourMapBlackBody(weight, maxWeight);
	}

}
