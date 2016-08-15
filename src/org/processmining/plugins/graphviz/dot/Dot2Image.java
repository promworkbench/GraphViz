package org.processmining.plugins.graphviz.dot;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.processmining.framework.util.OsUtil;

public class Dot2Image {

	public static final int dotVersion = 6;

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
			throw new RuntimeException("Graphviz-dot binary not found. \n" + ExceptionUtils.getStackTrace(e1));
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
		} else if (System.getProperty("os.arch").contains("64")) {
			//assume linux 32 bit
			dotFile = new File(new File(dotDirectory, "linux64"), "dot");
			dotFile.setExecutable(true);
		} else {
			//assume linux 64 bit
			dotFile = new File(new File(dotDirectory, "linux32"), "dot");
			dotFile.setExecutable(true);
		}

		//		System.out.println(dotFile);

		if (!dotFile.exists() || !dotFile.canExecute()) {
			throw new RuntimeException("Graphviz-dot binary not found. " + dotFile.toString());
		}

		String args[] = new String[3];
		args[0] = dotFile.getAbsolutePath();
		args[1] = "-T" + type;
		args[2] = "-q";

		final ProcessBuilder pb = new ProcessBuilder(args);
		pb.redirectErrorStream(true);

		Process dotProcess = null;
		try {
			dotProcess = pb.start();
			BufferedWriter out2 = new BufferedWriter(new OutputStreamWriter(dotProcess.getOutputStream(), "UTF-8"));
			out2.write(dot.toString());
			out2.flush();
			out2.close();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		InputStream outputOfDot = new BufferedInputStream(dotProcess.getInputStream());

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
			"libpangowin32-1.0-0.dll", "libpng12.dll", "libpng14-14.dll", "libxml2.dll", "ltdl.dll", "Pathplan.dll",
			"zlib1.dll", "libpng12.dll", "libpng14-14.dll", "libxml2.dll", "ltdl.dll", "Pathplan.dll", "zlib1.dll",
			//linux
			"linux32/dot", "linux64/dot",
			//mac
			"mac/dot"));

	private static File getDotDirectory() throws IOException {

		File packageDirectory;
		{
			try {
				packageDirectory = OsUtil.getProMPackageDirectory();
			} catch (Exception | ExceptionInInitializerError | NoClassDefFoundError e) {
				//file not found --> this point is reached in RapidProM.
				packageDirectory = new File(System.getProperty("user.home"), ".prom-graphviz");
				if (!packageDirectory.exists()) {
					if (!packageDirectory.mkdirs()) {
						//in RapidProM, we are not allowed to write to the user folder, so try a temp folder
						packageDirectory = new File(System.getProperty("java.io.tmpdir"), ".prom-graphviz");
						if (!packageDirectory.exists()) {
							if (!packageDirectory.mkdirs()) {
								throw new RuntimeException("Could not create a folder to put GraphViz binaries.");
							}
						}
					}
				}
			}
		}

		File graphvizFolder = null;
		{
			File[] listOfFiles = packageDirectory.listFiles();
			for (File file : listOfFiles) {
				if (file.getName().startsWith("graphviz-")) {
					// do something with the filename
					graphvizFolder = file;
				}
			}
		}

		File dotDirectory = new File(new File(graphvizFolder, "lib"), "dotBinaries" + dotVersion);
		if (!dotDirectory.exists()) {
			createDotDirectoryByCopying(dotDirectory);
		}

		return dotDirectory;
	}

	private static void createDotDirectoryByCopying(File targetDirectory) throws IOException {
		System.out.println("dot directory " + targetDirectory + " does not exist; create it and copy binaries to it");
		targetDirectory.mkdir();
		new File(targetDirectory, "mac").mkdir();
		new File(targetDirectory, "linux32").mkdir();
		new File(targetDirectory, "linux64").mkdir();

		//copy files to dot directory
		for (String fileName : dotFiles) {
			File outputFile = new File(targetDirectory, fileName);
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

	/**
	 * 
	 * @return the directory in which the Dot2Image's .jar file is located that
	 *         is currently executing.
	 */
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
