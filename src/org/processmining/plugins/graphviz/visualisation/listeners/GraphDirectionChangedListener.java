package org.processmining.plugins.graphviz.visualisation.listeners;

import org.processmining.plugins.graphviz.dot.Dot.GraphDirection;

public interface GraphDirectionChangedListener {
	
	/**
	 * Called when the graph direction changes.
	 * @param direction 
	 * @return Whether the view should be updated by the DotPanel.
	 */
	public boolean graphDirectionChanged(GraphDirection direction);
}
