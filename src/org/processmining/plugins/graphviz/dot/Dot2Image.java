package org.processmining.plugins.graphviz.dot;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;

import org.apache.commons.compress.utils.IOUtils;

public class Dot2Image {

	public enum Type {
		png, pdf, svg
	}

	public static InputStream dot2imageInputStream(Dot dot, Type type) {
		return dot2imageInputStream(dot.toString(), type);
	}

	public static InputStream dot2imageInputStream(String dot, Type type) {

		//find dot binaries
		URL dotUrl = Dot2Image.class.getResource("/org/processmining/plugins/graphviz/dot/binaries/dot.exe");
		if (dotUrl == null) {
			throw new RuntimeException("Graphviz-dot binary not found.");
		}
		
		System.out.println(dotUrl.getPath());

		String args[] = new String[2];
		args[0] = dotUrl.getPath();
		args[1] = "-T" + type;

		final ProcessBuilder pb = new ProcessBuilder(args);
		pb.redirectErrorStream(true);

		Process dotProcess = null;
		try {
			dotProcess = pb.start();
			BufferedWriter out2 = new BufferedWriter(new PrintWriter(dotProcess.getOutputStream()));
			out2.write(dot.toString());
			out2.flush();
			out2.close();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		InputStream outputOfDot = new BufferedInputStream(dotProcess.getInputStream());

		/*
		 * //Wait until Graphviz finishes try { dotProcess.waitFor(); } catch
		 * (InterruptedException e) { Thread.interrupted(); }
		 */

		return outputOfDot;
	}

	public static boolean dot2image(String dot, File file, Type type) {
		try {
			InputStream inputStream = dot2imageInputStream(dot, type);
			FileOutputStream outputStream = new FileOutputStream(file);
			IOUtils.copy(inputStream, outputStream);
			outputStream.flush();
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static boolean dot2image(Dot dot, File file, Type type) {
		return dot2image(dot.toString(), file, type);
	}
}
