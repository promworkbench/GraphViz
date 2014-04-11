package org.processmining.plugins.graphviz.colourMaps;

public class ColourMapFixed extends ColourMap {
	private String colour = "#FFFFFF";
	public ColourMapFixed(String colour) {
		this.colour = colour;
	}
	public String colour(long weight, long maxWeight) {
		return colour;
	}
	
}
