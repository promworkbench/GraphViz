package org.processmining.plugins.graphviz.colourMaps;

import java.awt.Color;


public class ColourMapBlue extends ColourMap {

	public Color colour2(long weight, long maxWeight) {
		return ColourMaps.colourMapBlue(weight, maxWeight);
	}

	public String colour(long weight, long maxWeight) {
		// TODO Auto-generated method stub
		return null;
	}

}
