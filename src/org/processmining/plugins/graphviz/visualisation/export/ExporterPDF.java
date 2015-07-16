package org.processmining.plugins.graphviz.visualisation.export;

import java.io.File;
import java.io.FileOutputStream;

import org.processmining.plugins.graphviz.visualisation.NavigableSVGPanel;

import de.erichseifert.vectorgraphics2d.PDFGraphics2D;

public class ExporterPDF extends Exporter {

	protected String getExtension() {
		return "pdf";
	}

	public void export(NavigableSVGPanel panel, File file) throws Exception {
		PDFGraphics2D g = new PDFGraphics2D(0.0, 0.0, panel.getImage().getWidth(), panel.getImage().getHeight());
		panel.print(g);
		FileOutputStream s = new FileOutputStream(file);
		try {
			s.write(g.getBytes());
		} finally {
			s.close();
		}
	}

}
