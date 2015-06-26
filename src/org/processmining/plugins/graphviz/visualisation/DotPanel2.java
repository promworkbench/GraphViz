package org.processmining.plugins.graphviz.visualisation;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.util.XMLResourceDescriptor;
import org.processmining.plugins.graphviz.colourMaps.ColourMap;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.Dot.GraphDirection;
import org.processmining.plugins.graphviz.dot.Dot2Image;
import org.processmining.plugins.graphviz.dot.Dot2Image.Type;
import org.processmining.plugins.graphviz.dot.DotEdge;
import org.processmining.plugins.graphviz.dot.DotElement;
import org.processmining.plugins.graphviz.dot.DotNode;
import org.processmining.plugins.graphviz.visualisation.listeners.ElementSelectionListener;
import org.processmining.plugins.graphviz.visualisation.listeners.GraphDirectionChangedListener;
import org.processmining.plugins.graphviz.visualisation.listeners.SelectionChangedListener;
import org.w3c.dom.svg.SVGDocument;

import com.kitfox.svg.Group;
import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGElement;
import com.kitfox.svg.SVGElementException;
import com.kitfox.svg.SVGException;
import com.kitfox.svg.animation.AnimationElement;
import com.kitfox.svg.xml.StyleAttribute;

public class DotPanel2 extends AnimatableSVGDocumentPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6201301504669783161L;

	private Action changeGraphDirection = new AbstractAction() {
		private static final long serialVersionUID = -38576326322179480L;

		public void actionPerformed(ActionEvent e) {
			GraphDirection newDirection;
			switch (dot.getDirection()) {
				case downTop :
					newDirection = GraphDirection.leftRight;
					break;
				case leftRight :
					newDirection = GraphDirection.rightLeft;
					break;
				case rightLeft :
					newDirection = GraphDirection.topDown;
					break;
				case topDown :
					newDirection = GraphDirection.downTop;
					break;
				default :
					newDirection = GraphDirection.downTop;
					break;
			}
			if (graphDirectionChanged(newDirection)) {
				dot.setDirection(newDirection);
				changeDot(dot, true);
			}
		}
	};

	private Dot dot;
	private HashMap<String, DotElement> id2element;
	private Set<DotElement> selectedElements;
	private CopyOnWriteArrayList<SelectionChangedListener<DotElement>> selectionChangedListeners = new CopyOnWriteArrayList<>();
	private CopyOnWriteArrayList<GraphDirectionChangedListener> graphDirectionChangedListeners = new CopyOnWriteArrayList<>();

	public DotPanel2(Dot dot) {
		super(dot2svgDocument(dot));
		this.dot = dot;
		prepareNodeSelection(dot);
		setEnableAnimation(false);

		//listen to ctrl+d for a change in graph layouting direction
		helperControlsShortcuts.add("ctrl d");
		helperControlsExplanations.add("change graph direction");
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_MASK),
				"changeGraphDirection");
		getActionMap().put("changeGraphDirection", changeGraphDirection);

		//add mouse listeners
		final DotPanel2 this2 = this;
		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				e.setSource(this2);
				for (DotElement element : getClicked(e)) {
					element.mousePressed(e);
				}
			}

			public void mouseClicked(MouseEvent e) {
				e.setSource(this2);

				boolean selectionChange = false;
				for (DotElement element : getClicked(e)) {
					element.mouseClicked(e);

					if (SwingUtilities.isLeftMouseButton(e)) {
						selectionChange = selectionChange || processSelection(element, e);
					}
				}

				if (SwingUtilities.isLeftMouseButton(e) && !selectionChange && !e.isControlDown()
						&& !isInNavigation(e.getPoint()) && (controls == null || !controls.contains(e.getPoint()))) {
					//the user did not click on anything clickable. Remove the selection.
					selectionChange = removeSelection();
				}

				if (selectionChange) {
					selectionChanged();
				}
			}

			public void mouseReleased(MouseEvent e) {
				e.setSource(this2);
				for (DotElement element : getClicked(e)) {
					element.mouseReleased(e);
				}
			}
		});
	}

	/**
	 * Deselect all nodes; return whether the selection changed
	 * 
	 * @return
	 */
	private boolean removeSelection() {
		for (DotElement element : selectedElements) {
			for (ElementSelectionListener<DotElement> listener : element.getSelectionListeners()) {
				listener.deselected(element);
			}
		}
		boolean result = !selectedElements.isEmpty();
		selectedElements.clear();
		return result;
	}

	private boolean processSelection(DotElement element, MouseEvent e) {
		if (element.isSelectable()) {
			if (e.isControlDown()) {
				//only change this element
				if (selectedElements.contains(element)) {
					//deselect element
					selectedElements.remove(element);
					for (ElementSelectionListener<DotElement> listener : element.getSelectionListeners()) {
						listener.deselected(element);
					}
				} else {
					//select element
					selectedElements.add(element);
					for (ElementSelectionListener<DotElement> listener : element.getSelectionListeners()) {
						listener.selected(element);
					}
				}
			} else {
				if (selectedElements.contains(element)) {
					//clicked on selected element without keypress
					if (selectedElements.size() > 1) {
						//deselect all other selected elements
						Iterator<DotElement> it = selectedElements.iterator();
						while (it.hasNext()) {
							DotElement selectedElement = it.next();
							if (selectedElement != element) {
								for (ElementSelectionListener<DotElement> listener : selectedElement
										.getSelectionListeners()) {
									listener.selected(selectedElement);
								}
								it.remove();
							}
						}
					} else {
						//only this element was selected, deselect it
						selectedElements.remove(element);
						for (ElementSelectionListener<DotElement> a : element.getSelectionListeners()) {
							a.deselected(element);
						}
					}
				} else {
					//clicked on not selected element without keypress
					//deselect all selected elements
					Iterator<DotElement> it = selectedElements.iterator();
					while (it.hasNext()) {
						DotElement selectedElement = it.next();
						if (selectedElement != element) {
							for (ElementSelectionListener<DotElement> listener : selectedElement
									.getSelectionListeners()) {
								listener.deselected(selectedElement);
							}
							it.remove();
						}
					}
					//select this element
					selectedElements.add(element);
					for (ElementSelectionListener<DotElement> listener : element.getSelectionListeners()) {
						listener.selected(element);
					}
				}
			}
			return true;
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	private Set<DotElement> getClicked(MouseEvent e) {
		HashSet<DotElement> result = new HashSet<DotElement>();
		Point pointPanelCoordinates = e.getPoint();
		if (isInImage(pointPanelCoordinates)) {
			Point2D pointImageCoordinates = ZoomPan.getImage2PanelTransformation(getSVGDocument(), panel).transformToImage(
					pointPanelCoordinates, state.getZoomPanState());
//			try {
//				//get the elements at the clicked position
//				List<List<RenderableElement>> elements = image.pick(pointImageCoordinates, false, null);
//
//				StyleAttribute classAttribute = new StyleAttribute("class");
//				StyleAttribute idAttribute = new StyleAttribute("id");
//				for (List<RenderableElement> path : elements) {
//					for (RenderableElement element : path) {
//						//RenderableElement element = path.iterator().next();
//						if (element instanceof Group) {
//							Group group = (Group) element;
//
//							//get the class
//							group.getPres(classAttribute);
//
//							//get the id
//							group.getPres(idAttribute);
//							String id = idAttribute.getStringValue();
//
//							if (classAttribute.getStringValue().equals("node")
//									|| classAttribute.getStringValue().equals("edge")) {
//								//we have found a node or edge
//								DotElement dotElement = id2element.get(id);
//								if (dotElement != null) {
//									result.add(dotElement);
//								}
//							}
//						}
//					}
//				}

//			} catch (SVGException e1) {
//				e1.printStackTrace();
//			}
		}
		return result;
	}

	/**
	 * Sets a new image
	 * 
	 * @param dot
	 *            ; set dot to this
	 * @param resetView
	 *            ; whether reset the view to centered+fitting
	 */
	public void changeDot(Dot dot, boolean resetView) {
		SVGDocument diagram = dot2svgDocument(dot);
		changeDot(dot, diagram, resetView);
	}

	/**
	 * Sets a new precomputed image. Assumptions are made about the dot & the
	 * diagram, so do not provide arbitrary ones.
	 * 
	 * @param dot
	 *            ; set dot to this
	 * @param diagram
	 *            ; use this SVG image
	 * @param resetView
	 *            ; whether reset the view to centered+fitting
	 */
	public void changeDot(Dot dot, SVGDocument diagram, boolean resetView) {
		prepareNodeSelection(dot);
		this.dot = dot;
		setImage(diagram, resetView);
	}

	private void prepareNodeSelection(Dot dot) {
		selectedElements = new HashSet<DotElement>();

		id2element = new HashMap<String, DotElement>();
		for (DotNode dotNode : dot.getNodesRecursive()) {
			id2element.put(dotNode.getId(), dotNode);
		}
		for (DotEdge dotEdge : dot.getEdgesRecursive()) {
			id2element.put(dotEdge.getId(), dotEdge);
		}
	}

	/*
	 * convert Dot into svg
	 */
	public static SVGDocument dot2svgDocument(Dot dot) {
		InputStream stream = Dot2Image.dot2imageInputStream(dot, Type.svg);

		String parser = XMLResourceDescriptor.getXMLParserClassName();
		SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);

		try {
			return f.createSVGDocument(null, stream);
		} catch (IOException e) {
			throw new RuntimeException("the dot-structure given is not valid\n" + dot.toString());
		}

	}

	/*
	 * select a dotElement
	 */
	public void select(DotElement element) {
		selectedElements.add(element);
		for (ElementSelectionListener<DotElement> listener : element.getSelectionListeners()) {
			listener.selected(element);
		}
		selectionChanged();
	}

	/**
	 * 
	 * @param image
	 * @param element
	 * @return the svg element of a DotElement
	 */
	public static Group getSVGElementOf(SVGDiagram image, DotElement element) {
		SVGElement svgElement = image.getElement(element.getId());
		if (svgElement instanceof Group) {
			return (Group) svgElement;
		}
		return null;
	}

	/**
	 * Set a css-property of a DotElement; returns the old value or null.
	 * 
	 * @param image
	 * @param element
	 * @param attribute
	 * @param value
	 * @return
	 */
	public static String setCSSAttributeOf(SVGDiagram image, DotElement element, String attribute, String value) {
		Group group = getSVGElementOf(image, element);
		return setCSSAttributeOf(group, attribute, value);
	}

	public static String getAttributeOf(SVGElement element, String attribute) {
		try {
			if (element.hasAttribute(attribute, AnimationElement.AT_CSS)) {
				StyleAttribute sty = new StyleAttribute(attribute);
				element.getStyle(sty);
				return sty.getStringValue();
			}
			if (element.hasAttribute(attribute, AnimationElement.AT_XML)) {
				StyleAttribute sty = new StyleAttribute(attribute);
				element.getPres(sty);
				return sty.getStringValue();
			}
		} catch (SVGElementException e) {
			e.printStackTrace();
		} catch (SVGException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String setCSSAttributeOf(SVGElement element, String attribute, Color colour) {
		return setCSSAttributeOf(element, attribute, ColourMap.toHexString(colour));
	}

	/**
	 * Set a css-property of an SVG element; returns the old value or null
	 * providing null as value removes the attribute
	 * 
	 * @param element
	 * @param attribute
	 * @param value
	 * @return
	 */
	public static String setCSSAttributeOf(SVGElement element, String attribute, String value) {
		try {
			if (element.hasAttribute(attribute, AnimationElement.AT_CSS)) {
				StyleAttribute sty = new StyleAttribute(attribute);
				element.getStyle(sty);
				String oldValue = sty.getStringValue();
				if (value != null) {
					element.setAttribute(attribute, AnimationElement.AT_CSS, value);
				} else {
					element.removeAttribute(attribute, AnimationElement.AT_CSS);
				}
				return oldValue;
			} else {
				if (value != null) {
					element.addAttribute(attribute, AnimationElement.AT_CSS, value);
				}
			}
		} catch (SVGElementException e) {
			e.printStackTrace();
		} catch (SVGException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Set<DotElement> getSelectedElements() {
		return Collections.unmodifiableSet(selectedElements);
	}

	public List<DotEdge> getEdges() {
		return dot.getEdgesRecursive();
	}

	public List<DotNode> getNodes() {
		return dot.getNodesRecursive();
	}

	public Dot getDot() {
		return dot;
	}

	//listeners

	private boolean graphDirectionChanged(GraphDirection direction) {
		boolean result = true;
		for (GraphDirectionChangedListener listener : graphDirectionChangedListeners) {
			result &= listener.graphDirectionChanged(direction);
		}
		return result;
	}

	private void selectionChanged() {
		for (SelectionChangedListener<DotElement> listener : selectionChangedListeners) {
			listener.selectionChanged(Collections.unmodifiableSet(selectedElements));
		}
	}

	public void addSelectionChangedListener(SelectionChangedListener<DotElement> listener) {
		selectionChangedListeners.add(listener);
	}

	public void addGraphDirectionChangedListener(GraphDirectionChangedListener listener) {
		graphDirectionChangedListeners.add(listener);
	}
}
