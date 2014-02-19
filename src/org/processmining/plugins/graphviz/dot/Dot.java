package org.processmining.plugins.graphviz.dot;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Dot {
	
	public enum GraphDirection {
		topDown, leftRight
	}

	private final Set<DotNode> nodes;
	private final List<DotEdge> edges;
	private String options = "";
	
	private String stringValue = null;

	private GraphDirection direction = GraphDirection.topDown;

	public Dot() {
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
	
	@Deprecated
	public void append(String s) {
		if (stringValue == null) {
			stringValue = "";
		}
		stringValue += s;
	}

	public String toString() {
		if (stringValue != null) {
			return stringValue;
		}
		
		StringBuilder result = new StringBuilder();
		result.append("digraph G {\n");
		if (direction == GraphDirection.leftRight) {
			result.append("rankdir=LR;\n");
		} else {
			result.append("rankdir=TD;\n");
		}
		
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

	public Dot(InputStream input) throws IOException {
		nodes = new HashSet<DotNode>();
		edges = new LinkedList<DotEdge>();
		BufferedReader br = null;
		StringBuilder result = new StringBuilder();
		try {
			String line;
			br = new BufferedReader(new InputStreamReader(input));
			while ((line = br.readLine()) != null) {
				result.append(line);
			}
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		this.stringValue = result.toString();
	}

	public void exportToFile(File file) throws IOException {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(file));
			writer.write(toString());
		} finally {
			try {
				if (writer != null) {
					writer.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public GraphDirection getDirection() {
		return direction;
	}

	public void setDirection(GraphDirection direction) {
		this.direction = direction;
	}

	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}

	public String getOptions() {
		return options;
	}

	public void setOptions(String options) {
		this.options = options;
	}
}