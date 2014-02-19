package org.processmining.plugins.graphviz.dot;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class DotCluster extends DotNode {

	private final Set<DotNode> nodes;
	private final List<DotEdge> edges;
	private String options;
	
	public DotCluster() {
		super("");
		nodes = new HashSet<DotNode>();
		edges = new LinkedList<DotEdge>();
	}
	
	public DotNode addNode(String label) {
		return addNode(label, "");
	}

	public DotNode addNode(String label, String options) {
		DotNode result = new DotNode(label, options);
		addNode(result);
		return result;
	}

	public void addNode(DotNode node) {
		nodes.add(node);
	}
	
	public void removeNode(DotNode node) {
		Iterator<DotNode> it = nodes.iterator();
		while (it.hasNext()) {
			if (node.equals(it.next())) {
				it.remove();
			}
		}
	}

	public DotEdge addEdge(DotNode source, DotNode target) {
		return addEdge(source, target, "", "");
	}

	public DotEdge addEdge(DotNode source, DotNode target, String label, String options) {
		DotEdge result = new DotEdge(source, target, label, options);
		addEdge(result);
		return result;
	}

	public void addEdge(DotEdge edge) {
		edges.add(edge);
	}

	public void removeEdge(DotEdge edge) {
		Iterator<DotEdge> it = edges.iterator();
		while (it.hasNext()) {
			if (edge == it.next()) {
				it.remove();
			}
		}
	}

	public DotEdge getFirstEdge(DotNode source, DotNode target) {
		for (DotEdge edge : edges) {
			if (edge.getSource() == source && edge.getTarget() == target) {
				return edge;
			}
		}
		return null;
	}
	
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("subgraph \"cluster_" + getId() + "\"{\n");
		
		result.append(options + "\n");
		
		for (DotNode node: nodes) {
			result.append(node);
		}
		
		for (DotEdge edge : edges) {
			result.append(edge);
		}
		
		result.append("}");
		
		return result.toString();
	}
	
	public String getOptions() {
		return options;
	}

	public void setOptions(String options) {
		this.options = options;
	}
	
}
