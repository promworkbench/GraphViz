package org.processmining.plugins.graphviz.visualisation;

import javax.swing.JComponent;

import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.graphviz.dot.Dot;

@Plugin(name = "Dot visualization", returnLabels = { "Dot visualization" }, returnTypes = { JComponent.class }, parameterLabels = { "Dot" }, userAccessible = false)
@Visualizer
public class DotVisualisation {

	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent visualize(PluginContext context, Dot dot) {

		return new DotPanel2(dot);
	}
}
