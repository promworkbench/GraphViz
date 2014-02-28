package org.processmining.plugins.graphviz.dot;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Dot extends DotCluster {
	
	private static final long serialVersionUID = 4124770739980939267L;

	public enum GraphDirection {
		topDown, leftRight
	}
	
	private String stringValue = null;

	private GraphDirection direction = GraphDirection.topDown;
	private boolean keepOrderingOfChildren = true;

	public Dot() {
		
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
		
		if (keepOrderingOfChildren) {
			result.append("graph [ordering=\"out\"];\n");
		}
		
		result.append(getOptions() + "\n");
		
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
		return direction;
	}

	public void setDirection(GraphDirection direction) {
		this.direction = direction;
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