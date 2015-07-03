package org.processmining.plugins.graphviz.visualisation.export;

import java.io.File;

import org.w3c.dom.svg.SVGDocument;

public class ExporterPDF extends Exporter {

	protected String getExtension() {
		return "pdf";
	}

	public void export(SVGDocument svgDocument, File file) throws Exception {
//		TranscoderInput input = new TranscoderInput(svgDocument);
//		input.setURI("svg");
//		
//		OutputStream out = new FileOutputStream(file);
//		TranscoderOutput output = new TranscoderOutput(out);
//
//		PDFTranscoder t = new PDFTranscoder();
//		t.transcode(input, output);
//
//        // Flush and close the stream.
//        out.flush();
//        out.close();
	}
	

}
