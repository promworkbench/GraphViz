package org.processmining.plugins.graphviz.visualisation.export;

import java.io.File;
import java.io.FileOutputStream;

import com.kitfox.svg.SVGDiagram;

import de.erichseifert.vectorgraphics2d.PDFGraphics2D;

public class ExporterPDF extends Exporter {

	protected String getExtension() {
		return "pdf";
	}

	public void export(SVGDiagram image, File file) throws Exception {
		PDFGraphics2D g = new PDFGraphics2D(0.0, 0.0, image.getWidth(), image.getHeight());
		image.render(g);
		FileOutputStream s = new FileOutputStream(file);
		try {
			s.write(g.getBytes());
		} finally {
			s.close();
		}
	}

}
