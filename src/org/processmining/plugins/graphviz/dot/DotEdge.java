package org.processmining.plugins.graphviz.dot;


public class DotEdge extends AbstractDotElement {
	
	private static final long serialVersionUID = 8744582777462666393L;

	private DotNode source;
	private DotNode target;
	
	public DotEdge(DotNode source, DotNode target) {
		this.setSource(source);
		this.setTarget(target);
	}

	public DotEdge(DotNode source, DotNode target, String label, String options) {
		this.setSource(source);
		this.setTarget(target);
		this.setLabel(label);
		this.setOptions(options);
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

	public String toString() {
		String result = "\"" + source.getId() + "\" -> \"" + target.getId() + "\" [label=\"" + getLabel() + "\" id=\"" + getId()
				+ "\"";
		if (!getOptions().equals("")) {
			result += ", " + getOptions();
		}
		return result + "];";
	}
}
