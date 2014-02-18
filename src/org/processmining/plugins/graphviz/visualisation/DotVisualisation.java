package org.processmining.plugins.graphviz.visualisation;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import javax.swing.JComponent;

import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.Dot2Image;
import org.processmining.plugins.graphviz.dot.Dot2Image.Type;

import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGUniverse;

@Plugin(name = "Dot visualization", returnLabels = { "Dot visualization" }, returnTypes = { JComponent.class }, parameterLabels = { "Dot" }, userAccessible = false)
@Visualizer
public class DotVisualisation {

	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent visualize(PluginContext context, Dot dot) throws IOException {

		//create graph visualisation panel
		SVGUniverse universe = new SVGUniverse();
		
		InputStream stream = Dot2Image.dot2imageInputStream(dot, Type.svg);
		URI uri = universe.loadSVG(stream, "hoi");
		
		SVGDiagram diagram = universe.getDiagram(uri);
		
		NavigableSVGPanel scroller = new NavigableSVGPanel(diagram);
		
		scroller.setDoubleBuffered(true);
		scroller.setNavigationImageEnabled(true);
		scroller.setNavigationImageBorderColor(Color.black);
		
		return scroller;
	}
}
