package org.processmining.plugins.graphviz.dot;

import java.util.Map;
import java.util.Map.Entry;

public class DotEdge extends AbstractDotElement {

	private DotNode source;
	private DotNode target;

	public DotEdge(DotNode source, DotNode target) {
		this.setSource(source);
		this.setTarget(target);
	}

	public DotEdge(DotNode source, DotNode target, String label, Map<String, String> optionsMap) {
		this.setSource(source);
		this.setTarget(target);
		this.setLabel(label);
		if (optionsMap != null) {
			for (Entry<String, String> e : optionsMap.entrySet()) {
				setOption(e.getKey(), e.getValue());
			}
		}
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
		String result = "\"" + source.getId() + "\" -> \"" + target.getId() + "\" [label=" + labelToString() + " id=\""
				+ getId() + "\"";

		for (String key : getOptionKeySet()) {
			result += "," + key + "=\"" + getOption(key) + "\"";
		}

		return result + "];";
	}
}
