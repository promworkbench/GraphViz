package org.processmining.plugins.graphviz.visualisation.export;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import com.kitfox.svg.SVGDiagram;

public class ExporterPNG extends Exporter {

	protected String getExtension() {
		return "png";
	}

	public void export(SVGDiagram image, File file) throws Exception {
		BufferedImage bi = new BufferedImage(Math.round(image.getWidth()), Math.round(image.getHeight()),
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = bi.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		image.render(g);
		ImageIO.write(bi, "PNG", file);
	}

}
