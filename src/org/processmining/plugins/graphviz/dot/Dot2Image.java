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
		//File dotPath = new File("C:\\Program Files (x86)\\Graphviz2.31\\bin");
		String args[] = new String[2];
		//args[0] = "C:\\Program Files (x86)\\Graphviz2.31\\bin\\dot.exe";
		
		URL dotUrl = Dot2Image.class.getResource("dot.exe");
		if (dotUrl == null) {
			System.out.println("not found");
		} else {
			System.out.println("found");
		}
		
		args[0] = "d:\\dot\\dot.exe";
		args[1] = "-T" + type;

		final ProcessBuilder pb = new ProcessBuilder(args);
		pb.directory(new File("d:\\output"));
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
	
	public static boolean dot2image(Dot dot, File pngFile, File pdfFile) {
		return dot2image(dot.toString(), pngFile, pdfFile);
	}

	public static boolean dot2image(String dot, File pngFile, File pdfFile) {
		//convert to graph and write to file

		if (pngFile != null) {
			try {
				InputStream inputStream = dot2imageInputStream(dot, Type.png);
				FileOutputStream outputStream = new FileOutputStream(pngFile);
				IOUtils.copy(inputStream, outputStream);
				outputStream.flush();
				outputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}

		if (pdfFile != null) {
			try {
				InputStream inputStream = dot2imageInputStream(dot, Type.pdf);
				FileOutputStream outputStream = new FileOutputStream(pdfFile);
				IOUtils.copy(inputStream, outputStream);
				outputStream.flush();
				outputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}

		return true;
	}
}
