package org.processmining.plugins.graphviz.dot;

import java.awt.event.MouseListener;
import java.util.List;
import java.util.Set;

import org.processmining.plugins.graphviz.visualisation.listeners.DotElementSelectionListener;

public interface DotElement extends MouseListener {

	public String getLabel();

	public void setLabel(String label);

	public void setOption(String key, String value);

	/**
	 * 
	 * @param key
	 * @return the value of the option if it was set, otherwise null.
	 */
	public String getOption(String key);
	
	/**
	 * 
	 * @return the set of options that is set (keys)
	 */
	public Set<String> getOptionKeySet();

	public String getId();

	//mouse listeners, gui stuff
	public void addMouseListener(MouseListener l);

	//selection listeners

	/**
	 * Sets whether this node can be selected.
	 * 
	 * @param selectable
	 */
	public void setSelectable(boolean selectable);

	/**
	 * 
	 * @return whether the element is selectable.
	 */
	public boolean isSelectable();

	/**
	 * Add a selection/deselection listener. Side-effect: enables selection of
	 * the element. Thread-safe.
	 * 
	 * @param listener
	 */
	public void addSelectionListener(DotElementSelectionListener listener);

	public List<DotElementSelectionListener> getSelectionListeners();
}
