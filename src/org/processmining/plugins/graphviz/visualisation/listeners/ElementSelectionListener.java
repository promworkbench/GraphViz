package org.processmining.plugins.graphviz.visualisation.listeners;

public interface ElementSelectionListener<E> {
	public void selected(E element);
	public void deselected(E element);
}
