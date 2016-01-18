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
		PDFGraphics2D g = new PDFGraphics2D(panel.getImage().getViewRect().getX(), panel.getImage().getViewRect().getY(), panel.getImage().getViewRect().getWidth(), panel.getImage().getViewRect().getHeight());
		panel.print(g);
		FileOutputStream s = new FileOutputStream(file);
		try {
			s.write(g.getBytes());
		} finally {
			s.close();
		}
		
//		double width = panel.getImage().getViewRect().getWidth();
//		double height = panel.getImage().getViewRect().getHeight();
//		
//		Dimension dimension = new Dimension((int) Math.ceil(width), (int) Math.ceil(height));
//		VectorGraphics g = new PDFGraphics2D(file, dimension);
//		Properties p = new Properties(PDFGraphics2D.getDefaultProperties());
//		p.setProperty(PDFGraphics2D.PAGE_SIZE, PDFGraphics2D.PAGE_SIZE);
//		p.setProperty(PDFGraphics2D.PAGE_MARGINS, "0, 0, 0, 0");
//		p.put(PDFGraphics2D.PAGE_SIZE, dimension.width + ", " + dimension.height);
//		g.setProperties(p);
//		g.startExport();
//		panel.print(g);
//		g.endExport();
	}

}
