package org.processmining.plugins.graphviz.dot;

import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.processmining.plugins.graphviz.visualisation.listeners.ElementSelectionListener;


public interface DotElement extends MouseListener {
	
	public String getLabel();
	public void setLabel(String label);
	
	public Map<String, String> getOptionsMap();
	public void setOption(String key, String value);
	
	public String getId();
	
	//mouse listeners, gui stuff
	public void addMouseListener(MouseListener l);
	
	//selection listeners
	
	/**
	 * Sets whether this node can be selected.
	 * @param selectable
	 */
	public void setSelectable(boolean selectable);
	
	/**
	 * 
	 * @return whether the element is selectable.
	 */
	public boolean isSelectable();
	
	@Deprecated
	public Collection<ActionListener> getSelectionListeners();
	@Deprecated
	public void addSelectionListener(ActionListener listener);
	@Deprecated
	public Collection<ActionListener> getDeselectionListeners();
	@Deprecated
	public void addDeselectionListener(ActionListener listener);
	
	/**
	 * Add a selection/deselection listener. Side-effect: enables selection of the element.
	 * Thread-safe.
	 * @param listener
	 */
	public void addSelectionListener(ElementSelectionListener<DotElement> listener);
	
	public List<ElementSelectionListener<DotElement>> getSelectionListeners2();
}
