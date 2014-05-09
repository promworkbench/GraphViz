package org.processmining.plugins.graphviz.colourMaps;

public abstract class ColourMap {
	public abstract String colour(long weight, long maxWeight);
	
	public String colour(long weight, long min, long max) {
		if (max == min) {
			return colour(weight, weight);
		}
		return colour(weight - min, max - min);
	}
}
