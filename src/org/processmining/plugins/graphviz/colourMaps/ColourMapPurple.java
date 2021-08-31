package org.processmining.plugins.graphviz.colourMaps;

public class ColourMapPurple extends ColourMapInterpolate {

	protected int[][] data2 = new int[][] { { 252, 251, 253 }, { 239, 237, 245 }, { 218, 218, 235 }, { 188, 189, 220 },
			{ 158, 154, 200 }, { 128, 125, 186 }, { 106, 81, 163 }, { 84, 39, 143 }, { 63, 0, 125 } };

	protected float[][] data = new float[data2.length][3];

	public ColourMapPurple() {
		for (int i = 0; i < data2.length; i++) {
			for (int j = 0; j < 3; j++) {
				data[i][j] = (float) (data2[i][j] / 256.0);
			}
		}
	}

	protected float[][] getData() {
		return data;
	}

}
