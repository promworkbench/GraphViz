package org.processmining.plugins.graphviz.dot;

import javax.swing.JComponent;

public class DotEdge extends JComponent {
	
	private static final long serialVersionUID = 8744582777462666393L;

	private DotNode source;
	private DotNode target;
	private String label;
	private String options;

	public DotEdge(DotNode source, DotNode target) {
		this.setSource(source);
		this.setTarget(target);
		this.setLabel("");
		this.setOptions("");
	}

	public DotEdge(DotNode source, DotNode target, String label, String options) {
		this.setSource(source);
		this.setTarget(target);
		this.setLabel(label);
		this.setOptions(options);
	}
	
	public void appendOption(String option) {
		if (options.equals("")) {
			options += option;
		} else {
			options += ", " + option;
		}
	}

	public String getOptions() {
		return options;
	}

	public void setOptions(String options) {
		this.options = options;
	}

	public DotNode getTarget() {
		return target;
	}

	public void setTarget(DotNode target) {
		this.target = target;
	}

	public DotNode getSource() {
		return source;
	}

	public void setSource(DotNode source) {
		this.source = source;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String toString() {
		String result = "\"" + source.getId() + "\" -> \"" + target.getId() + "\" [label=\"" + label + "\"";
		if (!options.equals("")) {
			result += ", " + options;
		}
		return result + "];";
	}

}
