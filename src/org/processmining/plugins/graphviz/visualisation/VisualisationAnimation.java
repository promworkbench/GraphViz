package org.processmining.plugins.graphviz.visualisation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import javax.swing.JComponent;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGUniverse;

@Plugin(name = "Animation test", returnLabels = { "Dot visualization" }, returnTypes = { JComponent.class }, parameterLabels = { "Log" }, userAccessible = false)
@Visualizer
public class VisualisationAnimation {

	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent visualize(PluginContext context, XLog log) throws IOException {

		NavigableSVGPanel panel = new NavigableSVGPanel();
		
		//create svg file
		SVGUniverse universe = new SVGUniverse();

		InputStream stream = new FileInputStream(new File("d://animation.svg"));
		URI uri = universe.loadSVG(stream, "hoi");

		SVGDiagram diagram = universe.getDiagram(uri);

		panel.setImage(diagram, true);
		panel.setEnableAnimation(true, universe);
		
		return panel;
	}
}
