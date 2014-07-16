package org.processmining.plugins.graphviz.colourMaps;

import java.awt.Color;

public class ColourMapRed extends ColourMap {
	
	public Color colour2(long weight, long maxWeight) {
		return ColourMaps.colourMapRed(weight, maxWeight);
	}

}
