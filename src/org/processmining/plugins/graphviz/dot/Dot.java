package org.processmining.plugins.graphviz.dot;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.processmining.framework.util.HTMLToString;

public class Dot implements HTMLToString {

	private StringBuilder result;

	public Dot() {
		result = new StringBuilder();
	}
	
	public Dot(String s) {
		result = new StringBuilder();
		result.append(s);
	}

	public void append(String s) {
		result.append(s);
	}

	public String toHTMLString(boolean includeHTMLTags) {
		if (includeHTMLTags) {
			return "<html>" + result.toString() + "</html>";
		}
		return result.toString();
	}
	
	public String toString() {
		return result.toString();
	}

	public Dot(InputStream input) throws IOException {
		BufferedReader br = null;
		result = new StringBuilder();
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
	}

	public void exportToFile(File file) throws IOException {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(file));
			writer.write(result.toString());
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
}