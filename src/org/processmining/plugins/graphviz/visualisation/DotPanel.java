package org.processmining.plugins.graphviz.visualisation;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import org.processmining.plugins.graphviz.colourMaps.ColourMap;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.Dot2Image;
import org.processmining.plugins.graphviz.dot.Dot2Image.Type;
import org.processmining.plugins.graphviz.dot.DotEdge;
import org.processmining.plugins.graphviz.dot.DotElement;
import org.processmining.plugins.graphviz.dot.DotNode;

import com.kitfox.svg.Group;
import com.kitfox.svg.RenderableElement;
import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGElement;
import com.kitfox.svg.SVGElementException;
import com.kitfox.svg.SVGException;
import com.kitfox.svg.SVGUniverse;
import com.kitfox.svg.animation.AnimationElement;
import com.kitfox.svg.xml.StyleAttribute;

public class DotPanel extends AnimatableSVGPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6201301504669783161L;

	private Action saveAs = new AbstractAction() {
		private static final long serialVersionUID = 3863042569537144601L;

		public void actionPerformed(ActionEvent e) {
			saveViewAs();
		}
	};

	private JFileChooser fc = new JFileChooser() {
		private static final long serialVersionUID = 3208601153887279605L;

		@Override
		public void approveSelection() {
			File f = getSelectedFile();
			if (f.exists() && getDialogType() == SAVE_DIALOG) {
				int result = JOptionPane.showConfirmDialog(this,
						"The file already exists, do you want to overwrite it?", "Existing file",
						JOptionPane.YES_NO_CANCEL_OPTION);
				switch (result) {
					case JOptionPane.YES_OPTION :
						super.approveSelection();
						return;
					case JOptionPane.NO_OPTION :
						return;
					case JOptionPane.CLOSED_OPTION :
						return;
					case JOptionPane.CANCEL_OPTION :
						cancelSelection();
						return;
				}
			}
			super.approveSelection();
		}
	};

	public class PNGFilter extends FileFilter {
		public boolean accept(File file) {
			String extension = "";
			int i = file.getName().lastIndexOf('.');
			if (i >= 0) {
				extension = file.getName().substring(i + 1);
			}
			return file.isDirectory() || extension.toLowerCase().equals("png");
		}

		public String getDescription() {
			return "png";
		}
	}

	public class PDFFilter extends FileFilter {
		public boolean accept(File file) {
			String extension = "";
			int i = file.getName().lastIndexOf('.');
			if (i >= 0) {
				extension = file.getName().substring(i + 1);
			}
			return file.isDirectory() || extension.toLowerCase().equals("pdf");
		}

		public String getDescription() {
			return "pdf";
		}
	}

	public class SVGFilter extends FileFilter {
		public boolean accept(File file) {
			String extension = "";
			int i = file.getName().lastIndexOf('.');
			if (i >= 0) {
				extension = file.getName().substring(i + 1);
			}
			return file.isDirectory() || extension.toLowerCase().equals("svg");
		}

		public String getDescription() {
			return "svg";
		}
	}

	private Dot dot;
	private HashMap<String, DotElement> id2element;
	private Set<DotElement> selectedElements;

	public DotPanel(Dot dot) {
		super(dot2svg(dot));
		this.dot = dot;
		prepareNodeSelection(dot);
		
		//set up save as
		fc.setAcceptAllFileFilterUsed(false);
		fc.addChoosableFileFilter(new PNGFilter());
		fc.addChoosableFileFilter(new PDFFilter());
		fc.addChoosableFileFilter(new SVGFilter());

		//listen to ctrl+s to save a file
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK), "saveAs");
		getActionMap().put("saveAs", saveAs);

		//add mouse listeners
		final DotPanel this2 = this;
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

					selectionChange = selectionChange || processSelection(element, e);
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

	private boolean processSelection(DotElement element, MouseEvent e) {
		if (element.isSelectable()) {
			if (e.isControlDown()) {
				//only change this element
				if (selectedElements.contains(element)) {
					selectedElements.remove(element);
					for (ActionListener a : element.getDeselectionListeners()) {
						a.actionPerformed(new ActionEvent(this, 0, null));
					}
				} else {
					selectedElements.add(element);
					for (ActionListener a : element.getSelectionListeners()) {
						a.actionPerformed(new ActionEvent(this, 0, null));
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
								for (ActionListener a : selectedElement.getDeselectionListeners()) {
									a.actionPerformed(new ActionEvent(this, 0, null));
								}
								it.remove();
							}
						}
					} else {
						//only this element was selected, deselect it
						selectedElements.remove(element);
						for (ActionListener a : element.getDeselectionListeners()) {
							a.actionPerformed(new ActionEvent(this, 0, null));
						}
					}
				} else {
					//clicked on not selected element without keypress
					//deselect all selected elements
					Iterator<DotElement> it = selectedElements.iterator();
					while (it.hasNext()) {
						DotElement selectedElement = it.next();
						if (selectedElement != element) {
							for (ActionListener a : selectedElement.getDeselectionListeners()) {
								a.actionPerformed(new ActionEvent(this, 0, null));
							}
							it.remove();
						}
					}
					//select this element
					selectedElements.add(element);
					for (ActionListener a : element.getSelectionListeners()) {
						a.actionPerformed(new ActionEvent(this, 0, null));
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
		if (state.isInImage(pointPanelCoordinates)) {
			Point pointImageCoordinates = state.panelToImageCoords(pointPanelCoordinates).toPoint();
			try {
				//get the elements at the clicked position
				List<List<RenderableElement>> elements = image.pick(pointImageCoordinates, false, null);

				StyleAttribute classAttribute = new StyleAttribute("class");
				StyleAttribute idAttribute = new StyleAttribute("id");
				for (List<RenderableElement> path : elements) {
					for (RenderableElement element : path) {
						//RenderableElement element = path.iterator().next();
						if (element instanceof Group) {
							Group group = (Group) element;

							//get the class
							group.getPres(classAttribute);

							//get the id
							group.getPres(idAttribute);
							String id = idAttribute.getStringValue();

							if (classAttribute.getStringValue().equals("node")
									|| classAttribute.getStringValue().equals("edge")) {
								//we have found a node or edge
								result.add(id2element.get(id));
							}
						}
					}
				}

			} catch (SVGException e1) {
				e1.printStackTrace();
			}
		}
		return result;
	}

	/*
	 * set dot as the new graph
	 * resetView: revert view to begin position
	 */
	public void changeDot(Dot dot, boolean resetView) {
		
		prepareNodeSelection(dot);

		//create svg file
		SVGDiagram diagram = dot2svg(dot);
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
	public static SVGDiagram dot2svg(Dot dot) {
		SVGUniverse universe = new SVGUniverse();

		InputStream stream = Dot2Image.dot2imageInputStream(dot, Type.svg);
		URI uri;
		try {
			uri = universe.loadSVG(stream, "hoi");
		} catch (IOException e) {
			return null;
		}

		SVGDiagram diagram = universe.getDiagram(uri);
		
		if (diagram == null) {
			throw new RuntimeException("the dot-structure given is not valid\n" + dot.toString());
		}
		return diagram;
	}
	
	/*
	 * select a dotElement
	 */
	public void select(DotElement element) {
		selectedElements.add(element);
		for (ActionListener a : element.getSelectionListeners()) {
			a.actionPerformed(new ActionEvent(this, 0, null));
		}
		selectionChanged();
	}

	/*
	 * Get the svg element of a DotElement
	 */
	public static Group getSVGElementOf(SVGDiagram image, DotElement element) {
		SVGElement svgElement = image.getElement(element.getId());
		if (svgElement instanceof Group) {
			return (Group) svgElement;
		}
		return null;
	}

	/*
	 * Set a css-property of a DotElement; returns the old value or null
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

	public void saveViewAs() {
		int returnVal = fc.showSaveDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();

			//get type and add file extension
			Type type;
			FileFilter fileFilter = fc.getFileFilter();
			if (fileFilter instanceof PNGFilter) {
				if (!file.getName().endsWith(".png")) {
					file = new File(file + ".png");
				}
				type = Type.png;
			} else if (fileFilter instanceof PDFFilter) {
				if (!file.getName().endsWith(".pdf")) {
					file = new File(file + ".pdf");
				}
				type = Type.pdf;
			} else {
				if (!file.getName().endsWith(".svg")) {
					file = new File(file + ".svg");
				}
				type = Type.svg;
			}

			final File file2 = file;
			final Type type2 = type;

			//save the file asynchronously
			new Thread(new Runnable() {
				public void run() {
					Dot2Image.dot2image(dot, file2, type2);
				}
			}).start();

		}
	}

	public Set<DotElement> getSelectedElements() {
		return selectedElements;
	}
	
	public List<DotEdge> getEdges() {
		return dot.getEdgesRecursive();
	}
	
	public List<DotNode> getNodes() {
		return dot.getNodesRecursive();
	}
	
	public SVGDiagram getSVG() {
		return image;
	}
	
	public Dot getDot() {
		return dot;
	}

	public void selectionChanged() {

	}
}
