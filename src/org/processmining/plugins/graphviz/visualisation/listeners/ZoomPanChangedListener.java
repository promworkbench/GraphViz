package org.processmining.plugins.graphviz.visualisation.listeners;

import org.processmining.plugins.graphviz.visualisation.ZoomPanState;

public interface ZoomPanChangedListener {
	/**
	 * Called when the zoom or pan changes.
	 * @param zoomPanState 
	 */
	public void zoomPanChanged(ZoomPanState zoomPanState);
}
