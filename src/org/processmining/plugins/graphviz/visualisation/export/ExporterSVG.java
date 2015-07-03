package org.processmining.plugins.graphviz.visualisation.export;

import java.io.File;
import java.io.FileOutputStream;

import com.kitfox.svg.SVGDiagram;

import de.erichseifert.vectorgraphics2d.SVGGraphics2D;

public class ExporterSVG extends Exporter {

	protected String getExtension() {
		return "svg";
	}

	public void export(SVGDiagram image, File file) throws Exception {
		SVGGraphics2D g = new SVGGraphics2D(0.0, 0.0, image.getWidth(), image.getHeight());
		image.render(g);
		FileOutputStream s = new FileOutputStream(file);
		try {
			s.write(g.getBytes());
		} finally {
			s.close();
		}
	}

}
