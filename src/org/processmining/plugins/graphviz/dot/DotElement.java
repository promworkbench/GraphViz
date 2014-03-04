package org.processmining.plugins.graphviz.dot;

import java.awt.event.MouseListener;


public interface DotElement extends MouseListener {
	
	public String getLabel();
	public void setLabel(String label);
	
	public String getOptions();
	public void setOptions(String options);
	public void appendOption(String option);
	
	public String getId();
	
	//mouse listeners
	public void addMouseListener(MouseListener l);
}
