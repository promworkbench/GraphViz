package org.processmining.plugins.graphviz.dot;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Dot extends DotCluster {

	public enum GraphDirection {
		topDown("TD"), leftRight("LR"), bottomTop("BT"), rightLeft("RL");

		private final String name;

		private GraphDirection(String s) {
			name = s;
		}

		public String getName() {
			return name;
		}
	}

	private String stringValue = null;

	private boolean keepOrderingOfChildren = true;

	public Dot() {
		setOption("rankdir", "TD");
		
	}

	public String toString() {
		if (stringValue != null) {
			return stringValue;
		}

		StringBuilder result = new StringBuilder();
		result.append("digraph G {\n");

		if (keepOrderingOfChildren) {
			result.append("graph [ordering=\"out\"];\n");
		}

		for (String key : getOptionKeySet()) {
			result.append(key + "=\"" + getOption(key) + "\";\n");
		}

		contentToString(result);

		result.append("}");

		return result.toString();
	}

	public Dot(InputStream input) throws IOException {
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
		String value = getOption("rankdir");
		for (GraphDirection dir : GraphDirection.values()) {
			if (dir.getName().equals(value)) {
				return dir;
			}
		}
		return GraphDirection.topDown;
	}

	public void setDirection(GraphDirection direction) {
		setOption("rankdir", direction.getName());
	}

	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}

	public boolean isKeepOrderingOfChildren() {
		return keepOrderingOfChildren;
	}

	public void setKeepOrderingOfChildren(boolean keepOrderingOfChildren) {
		this.keepOrderingOfChildren = keepOrderingOfChildren;
	}
}