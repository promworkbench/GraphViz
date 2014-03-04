package org.processmining.plugins.graphviz.visualisation;

import java.awt.Point;
import java.awt.event.ActionEvent;
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
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

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
import com.kitfox.svg.SVGException;
import com.kitfox.svg.SVGUniverse;
import com.kitfox.svg.xml.StyleAttribute;

public class DotPanel extends NavigableSVGPanel {

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

	public DotPanel(Dot dot) throws IOException {
		this();
		changeDot(dot, true);
	}

	public DotPanel() {
		//set up save as
		fc.setAcceptAllFileFilterUsed(false);
		fc.addChoosableFileFilter(new PNGFilter());
		fc.addChoosableFileFilter(new PDFFilter());
		fc.addChoosableFileFilter(new SVGFilter());

		//listen to ctrl+s to save a file
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK), "saveAs");
		getActionMap().put("saveAs", saveAs);

		//add mouse listeners
		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				for (DotElement element : getClicked(e)) {
					element.mousePressed(e);
				}
			}

			public void mouseClicked(MouseEvent e) {
				for (DotElement element : getClicked(e)) {
					element.mouseClicked(e);
				}
			}

			public void mouseReleased(MouseEvent e) {
				for (DotElement element : getClicked(e)) {
					element.mouseReleased(e);
				}
			}
		});
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

	public void changeDot(Dot dot, boolean resetView) throws IOException {
		this.dot = dot;

		id2element = new HashMap<String, DotElement>();
		for (DotNode dotNode : dot.getNodesRecursive()) {
			id2element.put(dotNode.getId(), dotNode);
		}
		for (DotEdge dotEdge : dot.getEdgesRecursive()) {
			id2element.put(dotEdge.getId(), dotEdge);
		}

		//create svg file
		SVGUniverse universe = new SVGUniverse();

		InputStream stream = Dot2Image.dot2imageInputStream(dot, Type.svg);
		URI uri = universe.loadSVG(stream, "hoi");

		SVGDiagram diagram = universe.getDiagram(uri);

		setImage(diagram, true);
	}

	/*
	 * Get the svg element of a node
	 */
	public Group getSVGElementOf(DotElement element) {
		SVGElement svgElement = image.getElement(element.getId());
		if (svgElement instanceof Group) {
			return (Group) svgElement;
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

			//save the file
			Dot2Image.dot2image(dot, file, type);
		}
	}

}
