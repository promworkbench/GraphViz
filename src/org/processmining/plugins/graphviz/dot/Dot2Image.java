package org.processmining.plugins.graphviz.dot;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

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
		//URL dotUrl = Dot2Image.class.getResource("/org/processmining/plugins/graphviz/dot/binaries/dot.exe");
		//if (dotUrl == null) {
		//	throw new RuntimeException("Graphviz-dot binary not found.");
		//}

		File dotFile = null;
		try {
			dotFile = new File(getDotDirectory(), "dot.exe");
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		
		if (dotFile == null) {
			throw new RuntimeException("Graphviz-dot binary not found.");
		}

		//System.out.println(dotUrl.getPath());

		String args[] = new String[2];
		//args[0] = dotUrl.getPath();
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

	private static List<String> dotFiles = new LinkedList<String>(Arrays.asList(
			"dot.exe",
			"cdt.dll",
			"cgraph.dll",
			"config6",
			"dot.exe",
			"fontconfig_fix.dll",
			"fontconfig.dll",
			"freetype6.dll",
			"glut32.dll",
			"gvc.dll",
			"gvplugin_core.dll",
			"gvplugin_dot_layout.dll",
			"gvplugin_gd.dll",
			"gvplugin_gdiplus.dll",
			"gvplugin_neato_layout.dll",
			"gvplugin_pango.dll",
			"iconv.dll",
			"intl.dll",
			"jpeg62.dll",
			"libatk-1.0-0.dll",
			"libcairo-2.dll",
			"libexpat-1.dll",
			"libexpat.dll",
			"libfontconfig-1.dll",
			"libfreetype-6.dll",
			"libgdk_pixbuf-2.0-0.dll",
			"libgdk-win32-2.0-0.dll",
			"libgdkglext-win32-1.0-0.dll",
			"libgio-2.0-0.dll",
			"libglib-2.0-0.dll",
			"libgmodule-2.0-0.dll",
			"libgobject-2.0-0.dll",
			"libgthread-2.0-0.dll",
			"libgtk-win32-2.0-0.dll",
			"libgtkglext-win32-1.0-0.dll",
			"libltdl-3.dll",
			"libpango-1.0-0.dll",
			"libpangocairo-1.0-0.dll",
			"libpangoft2-1.0-0.dll",
			"libpangowin32-1.0-0.dll",
			"cdt.dll",
			"cgraph.dll",
			"config6",
			"dot.exe",
			"fontconfig_fix.dll",
			"fontconfig.dll",
			"freetype6.dll",
			"glut32.dll",
			"gvc.dll",
			"gvplugin_core.dll",
			"gvplugin_dot_layout.dll",
			"gvplugin_gd.dll",
			"gvplugin_gdiplus.dll",
			"gvplugin_neato_layout.dll",
			"gvplugin_pango.dll",
			"iconv.dll",
			"intl.dll",
			"jpeg62.dll",
			"libatk-1.0-0.dll",
			"libcairo-2.dll",
			"libexpat-1.dll",
			"libexpat.dll",
			"libfontconfig-1.dll",
			"libfreetype-6.dll",
			"libgdk_pixbuf-2.0-0.dll",
			"libgdk-win32-2.0-0.dll",
			"libgdkglext-win32-1.0-0.dll",
			"libgio-2.0-0.dll",
			"libglib-2.0-0.dll",
			"libgmodule-2.0-0.dll",
			"libgobject-2.0-0.dll",
			"libgthread-2.0-0.dll",
			"libgtk-win32-2.0-0.dll",
			"libgtkglext-win32-1.0-0.dll",
			"libltdl-3.dll",
			"libpango-1.0-0.dll",
			"libpangocairo-1.0-0.dll",
			"libpangoft2-1.0-0.dll",
			"libpangowin32-1.0-0.dll",
			"libpng12.dll",
			"libpng14-14.dll",
			"libxml2.dll",
			"ltdl.dll",
			"Pathplan.dll",
			"zlib1.dll",
			"libpng12.dll",
			"libpng14-14.dll",
			"libxml2.dll",
			"ltdl.dll",
			"Pathplan.dll",
			"zlib1.dll"));

	private static File getDotDirectory() throws IOException, IOException, URISyntaxException {
		File jarDirectory = getJarDirectory();
		System.out.println("jar directory " + jarDirectory);

		File dotDirectory = new File(jarDirectory, "dotBinaries");
		System.out.println("dot directory " + dotDirectory);

		//if the binaries do not exist yet, copy them from the jar file
		if (!dotDirectory.exists()) {
			System.out.println("dot directory does not exist; create it");
			dotDirectory.mkdir();

			//copy files to dot directory
			for (String fileName : dotFiles) {
				System.out.println("copy " + fileName);
				InputStream inputStream = Dot2Image.class
						.getResourceAsStream("/org/processmining/plugins/graphviz/dot/binaries/" + fileName);
				FileOutputStream outputStream = new FileOutputStream(new File(dotDirectory, fileName));
				IOUtils.copy(inputStream, outputStream); 
			}
		}

		return dotDirectory;
	}

	private static File getJarDirectory() throws URISyntaxException {
		ProtectionDomain domain = Dot2Image.class.getProtectionDomain();
		CodeSource source = domain.getCodeSource();
		URL url = source.getLocation();
		return new File(url.getPath());
	}

	private static URI getJarURI() throws URISyntaxException {
		final ProtectionDomain domain;
		final CodeSource source;
		final URL url;
		final URI uri;

		domain = Dot2Image.class.getProtectionDomain();
		source = domain.getCodeSource();
		url = source.getLocation();
		uri = url.toURI();

		return (uri);
	}

	private static URI getFile(final URI where, final String fileName) throws ZipException, IOException {
		final File location;
		final URI fileURI;

		location = new File(where);

		// not in a JAR, just return the path on disk
		if (location.isDirectory()) {
			fileURI = URI.create(where.toString() + fileName);
		} else {
			final ZipFile zipFile;

			zipFile = new ZipFile(location);

			try {
				fileURI = extract(zipFile, fileName);
			} finally {
				zipFile.close();
			}
		}

		return (fileURI);
	}

	private static URI extract(final ZipFile zipFile, final String fileName) throws IOException {
		final File tempFile;
		final ZipEntry entry;
		final InputStream zipStream;
		OutputStream fileStream;

		tempFile = File.createTempFile(fileName, Long.toString(System.currentTimeMillis()));
		tempFile.deleteOnExit();
		entry = zipFile.getEntry(fileName);

		if (entry == null) {
			throw new FileNotFoundException("cannot find file: " + fileName + " in archive: " + zipFile.getName());
		}

		zipStream = zipFile.getInputStream(entry);
		fileStream = null;

		try {
			final byte[] buf;
			int i;

			fileStream = new FileOutputStream(tempFile);
			buf = new byte[1024];
			i = 0;

			while ((i = zipStream.read(buf)) != -1) {
				fileStream.write(buf, 0, i);
			}
		} finally {
			close(zipStream);
			close(fileStream);
		}

		return (tempFile.toURI());
	}

	private static void close(final Closeable stream) {
		if (stream != null) {
			try {
				stream.close();
			} catch (final IOException ex) {
				ex.printStackTrace();
			}
		}
	}
}
