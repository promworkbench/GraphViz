package org.processmining.plugins.graphviz.visualisation.listeners;


@Deprecated
public interface ZoomPanChangedListener {
	/**
	 * Called when the zoom or pan changes.
	 * @param zoomPanState 
	 */
	public void zoomPanChanged(Object zoomPanState);
}
