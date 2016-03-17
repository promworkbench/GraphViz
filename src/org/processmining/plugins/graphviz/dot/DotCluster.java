package org.processmining.plugins.graphviz.dot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DotCluster extends DotNode {

	private final List<DotNode> nodes;
	private final List<DotEdge> edges;
	private final List<DotCluster> clusters;

	private final Map<String, String> graphOptionMap;

	protected DotCluster() {
		super("", null);
		nodes = new ArrayList<DotNode>();
		edges = new ArrayList<DotEdge>();
		clusters = new ArrayList<DotCluster>();
		graphOptionMap = new HashMap<>();
	}

	public DotNode addNode(String label) {
		return addNode(label, null);
	}
	
	public DotNode insertNode(int index, String label) {
		return insertNode(index, label, null);
	}

	public DotNode addNode(String label, Map<String, String> options) {
		DotNode result = new DotNode(label, options);
		addNode(result);
		return result;
	}
	
	public DotNode insertNode(int index, String label, Map<String, String> options) {
		DotNode result = new DotNode(label, options);
		insertNode(index, result);
		return result;
	}

	public void addNode(DotNode node) {
		nodes.add(node);
	}
	
	public void insertNode(int index, DotNode node) {
		nodes.add(index, node);
	}

	public void removeNode(DotNode node) {
		Iterator<DotNode> it = nodes.iterator();
		while (it.hasNext()) {
			if (node.equals(it.next())) {
				it.remove();
			}
		}
	}

	public void addEdge(DotEdge edge) {
		edges.add(edge);
	}

	public DotEdge addEdge(DotNode source, DotNode target) {
		return addEdge(source, target, "");
	}

	public DotEdge addEdge(DotNode source, DotNode target, String label) {
		return addEdge(source, target, label, null);
	}

	public DotEdge addEdge(DotNode source, DotNode target, String label, Map<String, String> optionsMap) {
		DotEdge result = new DotEdge(source, target, label, optionsMap);
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
	
	public List<DotNode> getNodes() {
		return Collections.unmodifiableList(nodes);
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
	
	public List<DotEdge> getEdges() {
		return Collections.unmodifiableList(edges);
	}

	public List<DotEdge> getEdgesRecursive() {
		List<DotEdge> result = new LinkedList<DotEdge>();
		result.addAll(edges);

		for (DotCluster cluster : clusters) {
			result.addAll(cluster.getEdgesRecursive());
		}

		return Collections.unmodifiableList(result);
	}
	
		public void setGraphOption(String key, String value) {
		graphOptionMap.put(key, value);
	}

	public String getGraphOption(String key) {
		if (graphOptionMap.containsKey(key)) {
			return graphOptionMap.get(key);
		}
		return null;
	}

	public Set<String> getGraphOptionKeySet() {
		return Collections.unmodifiableSet(graphOptionMap.keySet());
	}
	

	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("subgraph \"" + getId() + "\"{\n");

		for (String key : getOptionKeySet()) {
			result.append(key + "=\"" + getOption(key) + "\";\n");
		}

		if (!getGraphOptionKeySet().isEmpty()) {
			result.append("graph[");
			for (String key : getGraphOptionKeySet()) {
				result.append(key + "=\"" + getGraphOption(key) + "\";\n");
			}
			result.append("];");
		}

		contentToString(result);

		result.append("}");

		return result.toString();
	}

	protected void contentToString(StringBuilder result) {
		for (DotNode node : nodes) {
			result.append(node);
			result.append("\n");
		}

		for (DotEdge edge : edges) {
			result.append(edge);
			result.append("\n");
		}

		for (DotCluster cluster : clusters) {
			result.append(cluster);
			result.append("\n");
		}
	}
	
	@Override
	public String getId() {
		return "cluster_" + super.getId();
	}

}
