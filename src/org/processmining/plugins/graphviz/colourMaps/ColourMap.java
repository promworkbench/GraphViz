package org.processmining.plugins.graphviz.colourMaps;

public abstract class ColourMap {
	public abstract String colour(long weight, long maxWeight);
	
	public String colour(long weight, long min, long max) {
		return colour(weight - min, max - min);
	}
}
