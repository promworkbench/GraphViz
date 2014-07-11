package org.processmining.plugins.graphviz.colourMaps;

import java.awt.Color;


public class ColourMapFixed extends ColourMap {
	private Color colour;
	public ColourMapFixed(Color colour) {
		this.colour = colour;
	}
	public Color colour2(long weight, long maxWeight) {
		return colour;
	}
	public String colour(long weight, long maxWeight) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
