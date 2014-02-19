package org.processmining.plugins.graphviz.visualisation;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.Dot2Image;
import org.processmining.plugins.graphviz.dot.Dot2Image.Type;

import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGUniverse;

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

	private Dot dot;
	
	public DotPanel(Dot dot) throws IOException {
		
		this.dot = dot;

		//create svg file
		SVGUniverse universe = new SVGUniverse();

		InputStream stream = Dot2Image.dot2imageInputStream(dot, Type.svg);
		URI uri = universe.loadSVG(stream, "hoi");

		SVGDiagram diagram = universe.getDiagram(uri);

		setImage(diagram);

		//set up save as
		fc.setAcceptAllFileFilterUsed(false);
		fc.addChoosableFileFilter(new PNGFilter());
		fc.addChoosableFileFilter(new PDFFilter());

		//listen to ctrl+s to save a file
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK), "saveAs");
		getActionMap().put("saveAs", saveAs);
	}

	public void saveViewAs() {
		int returnVal = fc.showSaveDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			
			//get type and add file extension
			Type type;
			FileFilter fileFilter = fc.getFileFilter();
			if (fileFilter instanceof PNGFilter) {
				file = new File(file + ".png");
				type = Type.png;
			} else {
				file = new File(file + ".pdf");
				type = Type.pdf;
			}

			//save the file
			Dot2Image.dot2image(dot, file, type);
		}
	}

	public class PNGFilter extends FileFilter {
		public boolean accept(File file) {
			String extension = "";
			int i = file.getName().lastIndexOf('.');
			if (i >= 0) {
				extension = file.getName().substring(i + 1);
			}
			return file.isFile() && extension.toLowerCase().equals("png");
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
			return file.isFile() && extension.toLowerCase().equals("pdf");
		}

		public String getDescription() {
			return "pdf";
		}
	}
}
