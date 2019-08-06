package org.processmining.plugins.graphviz.colourMaps;

public class ColourMapLightGreen extends ColourMapInterpolate {

	protected float[][] data = new float[][] { { 0.807843137f, 1f, 0.968627451f },
			{ 0.450980392f, 0.937254902f, 0.870588235f }, { 0.129411765f, 0.858823529f, 0.776470588f },
			{ 0.094117647f, 0.780392157f, 0.709803922f }, { 0.094117647f, 0.698039216f, 0.647058824f },
			{ 0.094117647f, 0.635294118f, 0.611764706f }, { 0.062745098f, 0.588235294f, 0.580392157f },
			{ 0.031372549f, 0.525490196f, 0.517647059f }, { 0.031372549f, 0.443137255f, 0.450980392f },
			{ 0, 0.333333333f, 0.321568627f }, };

	protected float[][] getData() {
		return data;
	}
}