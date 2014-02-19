package org.processmining.plugins.graphviz.dot;

import java.util.UUID;

public class DotNode {
	private final String id;
	private String label;
	private String options;
	
	public DotNode(String label) {
		this.setLabel(label);
		this.setOptions("");
		id = UUID.randomUUID().toString();
	}
	
	public DotNode(String label, String options) {
		id = UUID.randomUUID().toString();
		this.setLabel(label);
		this.setOptions(options);
	}
	
	public String getId() {
		return id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getOptions() {
		return options;
	}

	public void setOptions(String options) {
		this.options = options;
	}
	
	public boolean equals(Object object){
		if (!(object instanceof DotNode)) {
			return false;
		}
		return ((DotNode) object).getId().equals(getId());
	}
	
	public String toString() {
		String result = "\"" + id + "\" [label=\"" + label + "\"";
		if (!options.equals("")) {
			result += ", " + options;
		}
		return result + "];\n"; 
	}
}
