package org.processmining.plugins.graphviz.dot;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public abstract class AbstractDotElement implements DotElement {
	private final String id;
	private String options;
	private String label;

	private final List<MouseListener> mouseListeners;

	public AbstractDotElement() {
		id = UUID.randomUUID().toString();
		mouseListeners = new LinkedList<MouseListener>();
		label = "";
		options = "";
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

	public String getOptions() {
		return options;
	}

	public void setOptions(String options) {
		this.options = options;
	}

	public void appendOption(String option) {
		if (options.equals("")) {
			options += option;
		} else {
			options += ", " + option;
		}
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

}
