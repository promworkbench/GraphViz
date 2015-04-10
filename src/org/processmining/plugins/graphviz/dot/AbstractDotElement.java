package org.processmining.plugins.graphviz.dot;

import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import org.processmining.plugins.graphviz.visualisation.listeners.ElementSelectionListener;

public abstract class AbstractDotElement implements DotElement {
	private final String id;
	private Map<String, String> optionsMap;
	private String label;

	private boolean selectable = false;
	private List<ActionListener> selectionListeners2 = new LinkedList<ActionListener>();
	private List<ActionListener> deselectionListeners2 = new LinkedList<ActionListener>();
	private CopyOnWriteArrayList<ElementSelectionListener<DotElement>> selectionListeners = new CopyOnWriteArrayList<>();

	private final List<MouseListener> mouseListeners;

	public AbstractDotElement() {
		id = "e" + UUID.randomUUID().toString();
		mouseListeners = new LinkedList<MouseListener>();
		label = "";
		optionsMap = new HashMap<>();
	}

	public String getId() {
		return id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String labelToString() {
		if (label.length() > 2 && label.substring(0, 1).equals("<")
				&& label.substring(label.length() - 1, label.length()).equals(">")) {
			return label;
		} else {
			String label2 = label.replace("\"", "\\\"");
			return "\"" + label2 + "\"";
		}
	}

	public Map<String, String> getOptionsMap() {
		return optionsMap;
	}

	public void setOption(String key, String value) {
		optionsMap.put(key, value);
	}

	public void addMouseListener(MouseListener l) {
		mouseListeners.add(l);
	}

	public void mouseClicked(MouseEvent e) {
		for (MouseListener l : mouseListeners) {
			l.mouseClicked(e);
		}
	}

	public void mouseEntered(MouseEvent e) {
		for (MouseListener l : mouseListeners) {
			l.mouseEntered(e);
		}
	}

	public void mouseExited(MouseEvent e) {
		for (MouseListener l : mouseListeners) {
			l.mouseExited(e);
		}
	}

	public void mousePressed(MouseEvent e) {
		for (MouseListener l : mouseListeners) {
			l.mousePressed(e);
		}
	}

	public void mouseReleased(MouseEvent e) {
		for (MouseListener l : mouseListeners) {
			l.mouseReleased(e);
		}
	}

	//selection stuff
	public void setSelectable(boolean selectable) {
		this.selectable = selectable;
	}

	public boolean isSelectable() {
		return selectable;
	}

	public Collection<ActionListener> getSelectionListeners() {
		return Collections.unmodifiableCollection(selectionListeners2);
	}

	public void addSelectionListener(ActionListener listener) {
		selectable = true;
		selectionListeners2.add(listener);
	}

	public Collection<ActionListener> getDeselectionListeners() {
		return Collections.unmodifiableCollection(deselectionListeners2);
	}

	public void addDeselectionListener(ActionListener listener) {
		selectable = true;
		deselectionListeners2.add(listener);
	}

	public void addSelectionListener(ElementSelectionListener<DotElement> listener) {
		selectable = true;
		selectionListeners.add(listener);
	}
	
	public List<ElementSelectionListener<DotElement>> getSelectionListeners2() {
		return Collections.unmodifiableList(selectionListeners);	
	}
}
