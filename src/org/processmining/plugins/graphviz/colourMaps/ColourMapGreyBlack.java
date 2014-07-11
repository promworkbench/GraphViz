package org.processmining.plugins.graphviz.colourMaps;

import java.awt.Color;

public class ColourMapGreyBlack extends ColourMap {

	public Color colour2(long weight, long maxWeight) {
		return ColourMaps.colourMapGreyBlack(weight, maxWeight);
	}

	public String colour(long weight, long maxWeight) {
		// TODO Auto-generated method stub
		return null;
	}

}
