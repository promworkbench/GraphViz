package org.processmining.plugins.graphviz.dot;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class DotCluster extends DotNode {

	private static final long serialVersionUID = -365012750106649848L;

	private final List<DotNode> nodes;
	private final List<DotEdge> edges;
	private final List<DotCluster> clusters;

	protected DotCluster() {
		super("", "");
		nodes = new LinkedList<DotNode>();
		edges = new LinkedList<DotEdge>();
		clusters = new LinkedList<DotCluster>();
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
		return addEdge(source, target, "");
	}
	
	public DotEdge addEdge(DotNode source, DotNode target, String label) {
		return addEdge(source, target, label, "");
	}

	public DotEdge addEdge(DotNode source, DotNode target, String label, String options) {
		DotEdge result = new DotEdge(source, target, label, options);
		edges.add(result);
		return result;
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

	public DotCluster addCluster() {
		DotCluster cluster = new DotCluster();
		clusters.add(cluster);
		return cluster;
	}

	public void removeCluster(DotCluster cluster) {
		clusters.remove(cluster);
		for (DotCluster c : clusters) {
			c.removeCluster(cluster);
		}
	}

	public List<DotCluster> getClusters() {
		return Collections.unmodifiableList(clusters);
	}

	public List<DotNode> getNodesRecursive() {
		List<DotNode> result = new LinkedList<DotNode>();
		result.addAll(nodes);
		result.addAll(clusters);

		for (DotCluster cluster : clusters) {
			result.addAll(cluster.getNodesRecursive());
		}

		return Collections.unmodifiableList(result);
	}
	
	public List<DotEdge> getEdgesRecursive() {
		List<DotEdge> result = new LinkedList<DotEdge>();
		result.addAll(edges);

		for (DotCluster cluster : clusters) {
			result.addAll(cluster.getEdgesRecursive());
		}

		return Collections.unmodifiableList(result);
	}

	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("subgraph \"cluster_" + getId() + "\"{\n");

		result.append(getOptions() + "\n");

		result.append(result);

		result.append("}");

		return result.toString();
	}

	protected void contentToString(StringBuilder result) {
		for (DotNode node : nodes) {
			result.append(node);
		}

		for (DotEdge edge : edges) {
			result.append(edge);
		}

		for (DotCluster cluster : clusters) {
			result.append(cluster);
		}
	}
}
