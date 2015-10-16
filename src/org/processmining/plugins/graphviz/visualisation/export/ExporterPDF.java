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
//		Document document = new Document(new com.itextpdf.text.Rectangle(150, 150));
//		PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
//		document.open();
//
//		PdfContentByte canvas = writer.getDirectContent();
//		Graphics2D g2 = canvas.createGraphics(150, 150);
//		panel.print(g2);
//		g2.dispose();
//		document.close();

		PDFGraphics2D g = new PDFGraphics2D(panel.getImage().getViewRect().getX(), panel.getImage().getViewRect().getY(), panel.getImage().getViewRect().getWidth(), panel.getImage().getViewRect().getHeight());
		panel.print(g);
		FileOutputStream s = new FileOutputStream(file);
		try {
			s.write(g.getBytes());
		} finally {
			s.close();
		}
	}

}
