package org.processmining.plugins.graphviz.dot;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.compress.utils.IOUtils;
import org.processmining.framework.util.OsUtil;

public class Dot2Image {

	public enum Type {
		png, pdf, svg
	}

	public static InputStream dot2imageInputStream(Dot dot, Type type) {
		return dot2imageInputStream(dot.toString(), type);
	}

	public static InputStream dot2imageInputStream(String dot, Type type) {
		
		File dotDirectory;
		try {
			dotDirectory = getDotDirectory();
		} catch (IOException e1) {
			e1.printStackTrace();
			throw new RuntimeException("Graphviz-dot binary not found.");
		}

		//detect the operating system and locate dot binary
		String os = System.getProperty("os.name").toLowerCase();
		File dotFile;
		if (os.indexOf("win") >= 0) {
			//windows
			dotFile = new File(dotDirectory, "dot.exe");
		} else if (os.indexOf("mac") >= 0) {
			//assume mac
			dotFile = new File(new File(dotDirectory, "mac"), "dot");
			dotFile.setExecutable(true);
		} else {
			//assume linux
			dotFile = new File(new File(dotDirectory, "linux"), "dot");
			dotFile.setExecutable(true);
		}

//		System.out.println(dotFile);

		if (!dotFile.exists() || !dotFile.canExecute()) {
			throw new RuntimeException("Graphviz-dot binary not found.");
		}

		String args[] = new String[2];
		args[0] = dotFile.getAbsolutePath();
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

	private static List<String> dotFiles = new LinkedList<String>(Arrays.asList("dot.exe", "cdt.dll", "cgraph.dll",
			"config6", "dot.exe", "fontconfig_fix.dll", "fontconfig.dll", "freetype6.dll", "glut32.dll", "gvc.dll",
			"gvplugin_core.dll", "gvplugin_dot_layout.dll", "gvplugin_gd.dll", "gvplugin_gdiplus.dll",
			"gvplugin_neato_layout.dll", "gvplugin_pango.dll", "iconv.dll", "intl.dll", "jpeg62.dll",
			"libatk-1.0-0.dll", "libcairo-2.dll", "libexpat-1.dll", "libexpat.dll", "libfontconfig-1.dll",
			"libfreetype-6.dll", "libgdk_pixbuf-2.0-0.dll", "libgdk-win32-2.0-0.dll", "libgdkglext-win32-1.0-0.dll",
			"libgio-2.0-0.dll", "libglib-2.0-0.dll", "libgmodule-2.0-0.dll", "libgobject-2.0-0.dll",
			"libgthread-2.0-0.dll", "libgtk-win32-2.0-0.dll", "libgtkglext-win32-1.0-0.dll", "libltdl-3.dll",
			"libpango-1.0-0.dll", "libpangocairo-1.0-0.dll", "libpangoft2-1.0-0.dll", "libpangowin32-1.0-0.dll",
			"cdt.dll", "cgraph.dll", "config6", "dot.exe", "fontconfig_fix.dll", "fontconfig.dll", "freetype6.dll",
			"glut32.dll", "gvc.dll", "gvplugin_core.dll", "gvplugin_dot_layout.dll", "gvplugin_gd.dll",
			"gvplugin_gdiplus.dll", "gvplugin_neato_layout.dll", "gvplugin_pango.dll", "iconv.dll", "intl.dll",
			"jpeg62.dll", "libatk-1.0-0.dll", "libcairo-2.dll", "libexpat-1.dll", "libexpat.dll",
			"libfontconfig-1.dll", "libfreetype-6.dll", "libgdk_pixbuf-2.0-0.dll", "libgdk-win32-2.0-0.dll",
			"libgdkglext-win32-1.0-0.dll", "libgio-2.0-0.dll", "libglib-2.0-0.dll", "libgmodule-2.0-0.dll",
			"libgobject-2.0-0.dll", "libgthread-2.0-0.dll", "libgtk-win32-2.0-0.dll", "libgtkglext-win32-1.0-0.dll",
			"libltdl-3.dll", "libpango-1.0-0.dll", "libpangocairo-1.0-0.dll", "libpangoft2-1.0-0.dll",
			"libpangowin32-1.0-0.dll", "libpng12.dll", "libpng14-14.dll",
			"libxml2.dll",
			"ltdl.dll",
			"Pathplan.dll",
			"zlib1.dll",
			"libpng12.dll",
			"libpng14-14.dll",
			"libxml2.dll",
			"ltdl.dll",
			"Pathplan.dll",
			"zlib1.dll",
			//linux
			"linux/acyclic", "linux/bcomps", "linux/ccomps", "linux/circo", "linux/cluster", "linux/dijkstra",
			"linux/dot", "linux/dot_builtins", "linux/dot2gxl", "linux/dotty", "linux/fdp", "linux/gc", "linux/gml2gv",
			"linux/graphml2gv", "linux/gv2gml", "linux/gv2gxl", "linux/gvcolor", "linux/gvgen", "linux/gvmap",
			"linux/gvmap.sh", "linux/gvpack", "linux/gvpr", "linux/gxl2dot", "linux/gxl2gv", "linux/lneato",
			"linux/mm2gv", "linux/neato", "linux/nop", "linux/osage", "linux/patchwork", "linux/prune", "linux/sccmap",
			"linux/sfdp",
			"linux/tred",
			"linux/twopi",
			"linux/unflatten",
			"linux/vimdot",
			//mac
			"mac/acyclic", "mac/bcomps", "mac/ccomps", "mac/circo", "mac/cluster", "mac/dijkstra", "mac/dot",
			"mac/dot_builtins", "mac/dot2gxl", "mac/dotty", "mac/fdp", "mac/gc", "mac/gml2gv", "mac/graphml2gv",
			"mac/gv-fc-cache", "mac/gv2gml", "mac/gv2gxl", "mac/gvcolor", "mac/gvgen", "mac/gvmap", "mac/gvmap.sh",
			"mac/gvpack", "mac/gvpr", "mac/gxl2dot", "mac/gxl2gv", "mac/lefty", "mac/lneato", "mac/mm2gv", "mac/neato",
			"mac/nop", "mac/osage", "mac/patchwork", "mac/prune", "mac/sccmap", "mac/sfdp", "mac/tred", "mac/twopi",
			"mac/unflatten", "mac/vimdot"));

	private static File getDotDirectory() throws IOException {

		File packageDirectory = OsUtil.getProMPackageDirectory();

		File[] listOfFiles = packageDirectory.listFiles();
		File graphvizFolder = null;
		for (File file : listOfFiles) {
			if (file.getName().startsWith("graphviz-")) {
				// do something with the filename
				graphvizFolder = file;
			}
		}

		File dotDirectory = new File(new File(graphvizFolder, "lib"), "dotBinaries");
		if (!dotDirectory.exists()) {
			dotDirectory = getDotDirectoryByCopying();
		}

		return dotDirectory;

		//System.out.println(OsUtil.getProMPackageDirectory());
		//System.out.println(System.getProperty("user.dir"));
	}

	private static File getDotDirectoryByCopying() throws IOException {
		File jarDirectory = getJarDirectory();
//		System.out.println("jar directory " + jarDirectory);

		File libDirectory = new File(jarDirectory, "GraphViz-lib");
		if (!libDirectory.exists()) {
			libDirectory = new File(jarDirectory, "lib-GraphViz");
			new File(jarDirectory, "lib-GraphViz").mkdir();
		}

		File dotDirectory = new File(libDirectory, "dotBinaries");
//		System.out.println("dot directory " + dotDirectory);

		//if the binaries do not exist yet, copy them from the jar file
		if (!dotDirectory.exists()) {
			System.out.println("dot directory does not exist; create it and copy binaries to it");
			dotDirectory.mkdir();
			new File(dotDirectory, "linux").mkdir();
			new File(dotDirectory, "mac").mkdir();

			//copy files to dot directory
			for (String fileName : dotFiles) {
				File outputFile = new File(dotDirectory, fileName);
				System.out.println("copy " + fileName);
				InputStream inputStream = Dot2Image.class
						.getResourceAsStream("/org/processmining/plugins/graphviz/dot/binaries/" + fileName);
				FileOutputStream outputStream = new FileOutputStream(outputFile);
				IOUtils.copy(inputStream, outputStream);
				outputFile.setExecutable(true);

				outputStream.flush();
				outputStream.close();
			}
		}

		return dotDirectory;
	}

	private static File getJarDirectory() {
		ProtectionDomain domain = Dot2Image.class.getProtectionDomain();
		CodeSource source = domain.getCodeSource();
		URL url = source.getLocation();
		File file = new File(url.getPath());
		while (!file.isDirectory()) {
			file = file.getParentFile();
		}
		return file;
	}
}
