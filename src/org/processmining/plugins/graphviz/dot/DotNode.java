package org.processmining.plugins.graphviz.dot;

import java.util.Map;
import java.util.Map.Entry;

public class DotNode extends AbstractDotElement {

	protected DotNode(String label, Map<String, String> optionsMap) {
		this.setLabel(label);
		if (optionsMap != null) {
			for (Entry<String, String> e : optionsMap.entrySet()) {
				setOption(e.getKey(), e.getValue());
			}
		}
	}

	public boolean equals(Object object) {
		if (!(object instanceof DotNode)) {
			return false;
		}
		return ((DotNode) object).getId().equals(getId());
	}

	public String toString() {
		String result = "\"" + getId() + "\" [label=" + labelToString() + ", id=\"" + getId() + "\"";
		for (Entry<String, String> p : getOptionsMap().entrySet()) {
			result += "," + p.getKey() + "=\"" + p.getValue() + "\"";
		}
		return result + "];";
	}
}
