package org.processmining.plugins.graphviz.dot;

import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.util.Collection;
import java.util.Map;


public interface DotElement extends MouseListener {
	
	public String getLabel();
	public void setLabel(String label);
	
	public Map<String, String> getOptionsMap();
	public void setOption(String key, String value);
	
	public String getId();
	
	//mouse listeners, gui stuff
	public void addMouseListener(MouseListener l);
	public void setSelectable(boolean selectable);
	public boolean isSelectable();
	public Collection<ActionListener> getSelectionListeners();
	public void addSelectionListener(ActionListener listener);
	public Collection<ActionListener> getDeselectionListeners();
	public void addDeselectionListener(ActionListener listener);
}
