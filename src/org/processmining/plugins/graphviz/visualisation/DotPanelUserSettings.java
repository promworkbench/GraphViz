package org.processmining.plugins.graphviz.visualisation;

import java.util.Locale;

import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.Dot.GraphDirection;

/**
 * Keeps track of the user state, i.e. key presses.
 * 
 * @author sleemans
 *
 */

public class DotPanelUserSettings {
	public GraphDirection direction;
	public double nodeSeparation;
	
	public DotPanelUserSettings(Dot dot) {
		reset();
	}

	/**
	 * Applies the dot panel user settings to a Dot instance.
	 * 
	 * @param dot
	 */
	public void applyToDot(Dot dot) {
		dot.setDirection(direction);
		dot.setOption("nodesep", "" + String.format(Locale.ENGLISH, "%.2f", nodeSeparation));
		dot.setOption("ranksep", "" + String.format(Locale.ENGLISH, "%.2f", nodeSeparation * 1.2));
	}
	
	public void reset() {
		nodeSeparation = 0.4;
	}

	public void setDirection(GraphDirection direction) {
		this.direction = direction;
	}
}
