package org.processmining.plugins.graphviz.colourMaps;

public class ColourMapFixed implements ColourMap {
	private String colour = "#FFFFFF";
	public ColourMapFixed(String colour) {
		this.colour = colour;
	}
	public String colour(long weight, long maxWeight) {
		return colour;
	}
	
}
