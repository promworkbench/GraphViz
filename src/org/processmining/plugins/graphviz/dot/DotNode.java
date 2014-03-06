package org.processmining.plugins.graphviz.dot;



public class DotNode extends AbstractDotElement {

	protected DotNode(String label, String options) {
		this.setLabel(label);
		this.setOptions(options);
	}

	public boolean equals(Object object) {
		if (!(object instanceof DotNode)) {
			return false;
		}
		return ((DotNode) object).getId().equals(getId());
	}

	public String toString() {
		String result = "\"" + getId() + "\" [label=" + labelToString() + ", id=\"" + getId() + "\"";
		if (!getOptions().equals("")) {
			result += ", " + getOptions();
		}
		return result + "];";
	}
}
