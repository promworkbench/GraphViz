package org.processmining.plugins.graphviz.visualisation.export;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.svg.SVGDocument;

public class ExporterSVG extends Exporter {

	protected String getExtension() {
		return "svg";
	}

	public void export(SVGDocument svgDocument, File file) throws IOException {
		Writer out = new PrintWriter(file, "UTF-8");
		SVGGraphics2D svgGenerator = new SVGGraphics2D(svgDocument);
		svgGenerator.stream(svgDocument.getRootElement(), out);
		svgGenerator.dispose();
		out.flush();
		out.close();
	}
	
}
