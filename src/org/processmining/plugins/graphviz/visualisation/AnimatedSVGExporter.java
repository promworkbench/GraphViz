package org.processmining.plugins.graphviz.visualisation;

import java.awt.Graphics2D;
import java.awt.RenderingHints;

import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGException;
import com.kitfox.svg.SVGUniverse;

public class AnimatedSVGExporter {

	private final SVGUniverse universe;
	private final SVGDiagram image;
	private int width = 200;
	private int height = 200;

	public AnimatedSVGExporter(SVGUniverse universe, SVGDiagram image, int width, int height) {
		this.universe = universe;
		this.image = image;
		this.width = width;
		this.height = height;
	}

	public void paintFrame(Graphics2D g, double time) {
		//update universe time
		universe.setCurTime(time);
		try {
			universe.updateTime();
		} catch (SVGException e) {
			e.printStackTrace();
		}

		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g.setClip(0, 0, width, height);

		AnimatableSVGPanel.drawSVG(g, image, 0, 0, width, height);
	}

}
