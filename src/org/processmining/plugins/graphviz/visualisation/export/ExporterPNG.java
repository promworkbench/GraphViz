package org.processmining.plugins.graphviz.visualisation.export;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.w3c.dom.svg.SVGDocument;

public class ExporterPNG extends Exporter {

	protected String getExtension() {
		return "png";
	}

	public void export(SVGDocument svgDocument, File file) throws Exception {
		TranscoderInput input = new TranscoderInput(svgDocument);
		input.setURI("svg");
		
		OutputStream out = new FileOutputStream(file);
		TranscoderOutput output = new TranscoderOutput(out);

		PNGTranscoder t = new PNGTranscoder();
		t.transcode(input, output);

        // Flush and close the stream.
        out.flush();
        out.close();
	}
	

}
