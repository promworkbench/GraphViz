package org.processmining.plugins.graphviz.colourMaps;

import java.awt.Color;

public class ColourMapLightBlue implements ColourMap {

	public String colour(long weight, long maxWeight) {
		float x = weight / (float) maxWeight;

		x = (x * 0.25f) + 0.1f;
		Color colour = new Color(1 - x, 1 - x, 1);

		String hexColour = Integer.toHexString(colour.getRGB());
		return "#" + hexColour.substring(2, hexColour.length());
	}

}
