package org.processmining.plugins.graphviz.visualisation.export;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import org.w3c.dom.svg.SVGDocument;


public abstract class Exporter extends FileFilter {
	protected abstract String getExtension();

	public abstract void export(SVGDocument svgDocument, File file) throws Exception;

	public String getDescription() {
		return getExtension();
	}
	
	public File addExtension(File file) {
		if (!file.getName().endsWith("." + getExtension())) {
			return new File(file + "." + getExtension());
		}
		return file;
	}

	@Override
	public boolean accept(final File file) {
		String extension = "";
		int i = file.getName().lastIndexOf('.');
		if (i >= 0) {
			extension = file.getName().substring(i + 1);
		}
		return file.isDirectory() || extension.toLowerCase().equals(getExtension());
	}
}
