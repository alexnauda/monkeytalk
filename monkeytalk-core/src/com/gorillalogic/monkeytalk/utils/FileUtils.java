/*  MonkeyTalk - a cross-platform functional testing tool
    Copyright (C) 2012 Gorilla Logic, Inc.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>. */
package com.gorillalogic.monkeytalk.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class FileUtils {
	private static final String TEMP_DIR_PREFIX = "report";
	private static final int BUFFER_SIZE = 4096;

	/**
	 * Read the given input stream with UTF-8 encoding into a string and return it.
	 * 
	 * @param in
	 *            the input stream to be read
	 * @return the contents as text
	 * @throws IOException
	 */
	public static String readStream(InputStream in) throws IOException {
		StringBuilder sb = new StringBuilder();
		String line = null;
		BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
		while ((line = reader.readLine()) != null) {
			sb.append(line).append('\n');
		}

		return (sb.length() > 0 ? sb.substring(0, sb.length() - 1) : "");
	}

	/**
	 * Read the given file with UTF-8 encoding into a string and return it.
	 * 
	 * @param f
	 *            the file to be read
	 * @return the contents as text
	 * @throws IOException
	 */
	public static String readFile(File f) throws FileNotFoundException, IOException {
		StringBuilder sb = new StringBuilder();
		Scanner scanner = new Scanner(new FileInputStream(f), "UTF-8");
		try {
			while (scanner.hasNextLine()) {
				sb.append(scanner.nextLine()).append('\n');
			}
		} finally {
			scanner.close();
		}
		return (sb.length() > 0 ? sb.substring(0, sb.length() - 1) : "");
	}

	/**
	 * Write the given contents with UTF-8 encoding to the given file.
	 * 
	 * @param f
	 *            the file to be written
	 * @param contents
	 *            the contents to be written
	 * @throws IOException
	 */
	public static void writeFile(File f, String contents) throws IOException {
		Writer out = new OutputStreamWriter(new FileOutputStream(f), "UTF-8");
		try {
			out.write(contents);
		} finally {
			out.close();
		}
	}

	/**
	 * Write the given contents with UTF-8 encoding to the given filename.
	 * 
	 * @param filename
	 *            the filename
	 * @param contents
	 *            the contents to be written
	 * @throws IOException
	 */
	public static void writeFile(String filename, String contents) throws IOException {
		writeFile(new File(filename), contents);
	}

	/**
	 * Write the given raw bytes to the given file.
	 * 
	 * @param f
	 *            the file to be written
	 * @param bytes
	 *            the raw bytes to be written
	 * @throws IOException
	 */
	public static void writeFile(File f, byte[] bytes) throws IOException {
		OutputStream out = new FileOutputStream(f);
		try {
			out.write(bytes);
		} finally {
			out.close();
		}
	}

	/**
	 * Remote the extension (if it exists) from the given filename and return the trimmed filename.
	 * 
	 * @param filename
	 *            the filename
	 * @param ext
	 *            the extension to be removed
	 * @return the filename trimmed of the extension
	 */
	public static String removeExt(String filename, String ext) {
		if (filename != null && filename.toLowerCase().endsWith(ext)) {
			return filename.substring(0, filename.length() - ext.length());
		}
		return filename;
	}

	/**
	 * Delete the given dir and recursively delete all of its children.
	 * 
	 * @param dir
	 *            the folder to be deleted
	 * @throws IOException
	 */
	public static void deleteDir(File dir) throws IOException {
		for (File f : dir.listFiles()) {
			if (f.isDirectory()) {
				deleteDir(f);
			} else {
				f.delete();
			}
		}
		dir.delete();
	}

	/**
	 * Find a file by filename in the given directory (does not search recursively). NOTE: search is
	 * case-insensitive on filename.
	 * 
	 * @param filename
	 *            the filename to search for
	 * @param dir
	 *            the search directory
	 * @return the file if found, otherwise null
	 */
	public static File findFile(String filename, File dir) {
		if (dir != null && dir.isDirectory()) {
			for (File f : dir.listFiles()) {
				if (f.getName().equalsIgnoreCase(filename)) {
					return f;
				}
			}
		}
		return null;
	}

	/**
	 * Read the resource into a string.
	 * 
	 * @param resource
	 *            the resource name
	 * 
	 * @return contents of the file as a string
	 * @throws IOException
	 */
	public static String resourceToString(String resource) throws IOException {
		InputStream is = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(resource);
		if (is != null) {
			Writer writer = new StringWriter();

			char[] buf = new char[1024];
			try {
				Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				int n;
				while ((n = reader.read(buf)) != -1) {
					writer.write(buf, 0, n);
				}
			} finally {
				is.close();
			}
			return writer.toString();
		} else {
			return "";
		}
	}

	/**
	 * Make the given folder (and any parent folders), throws an {@link IOException} on failure.
	 * 
	 * @param dir
	 *            the folder
	 * @throws IOException
	 */
	public static void makeDir(File dir) throws IOException {
		makeDir(dir, null);
	}

	/**
	 * Make the given folder (and any parent folders), throws an {@link IOException} with the error
	 * message on failure.
	 * 
	 * @param dir
	 *            the folder
	 * @param msg
	 *            the error message
	 * @throws IOException
	 */
	public static void makeDir(File dir, String msg) throws IOException {
		if (dir == null) {
			throw new IOException((msg != null ? msg : "dir") + " is null");
		} else if (!dir.exists()) {
			boolean success = dir.mkdirs();
			if (!success) {
				throw new IOException("Failed to make " + (msg != null ? msg : "dir") + ": "
						+ dir.getAbsolutePath());
			}
		} else if (!dir.isDirectory()) {
			throw new IOException((msg != null ? msg : "dir") + " not a folder: "
					+ dir.getAbsolutePath());
		}
	}

	/**
	 * use the supplied object to find the resource, and copy its contents to the supplied file
	 * 
	 * @param o
	 *            an <code>Object</code> which can be used to locate the desired resource. If null,
	 *            <code>FileUtils.class</code>
	 * @param resource
	 *            a <code>String</code>, the path to the resource
	 * @param target
	 *            the <code>File</code> which will be written with the contents of the resource
	 * @throws IOException
	 */
	public static void copyResourceToFile(Object o, String resource, File target)
			throws IOException {
		if (target == null) {
			throw new IOException("could not copy resource \"" + resource
					+ "\" to file: null target file specified");
		}

		Class<?> klass = null;
		if (o == null) {
			klass = FileUtils.class;
		} else if (o instanceof Class) {
			klass = (Class<?>) o;
		} else {
			klass = o.getClass();
		}

		InputStream resourceStream = klass.getResourceAsStream(resource);
		if (resourceStream == null) {
			String errorHeader = "could not copy resource \"" + resource + "\" to file \""
					+ target.getPath() + "\": ";
			throw new IOException(errorHeader + "could not locate resource");
		}

		writeFile(target, resourceStream);
	}

	public static void copyFile(File srcFile, File desDir) throws IOException {
		InputStream is = null;
		OutputStream os = null;
		if (!desDir.exists()) {
			try {
				is = new FileInputStream(srcFile);
				os = new FileOutputStream(desDir);
				byte[] buffer = new byte[1024];
				int length;
				while ((length = is.read(buffer)) > 0) {
					os.write(buffer, 0, length);
				}
			} finally {
				is.close();
				os.close();
			}
		}
	}

	/**
	 * create a zip file of the contents of the folder, optionally including the folder itself.
	 * 
	 * if "includeDirInZip" is true, the specified directory - one level only - will be included in
	 * the zip file, i.e. all entry paths will begin with the directory name. For example, suppose
	 * the "dir" is /home/user/banana and contains two files, "apple.txt" and "orange.txt". If
	 * "includeDirInZip" is false, the zip file will contain two entries, "apple.txt" and
	 * "orange.txt". If "includeDirInZip" is true, the zip file will contain three entries, "banana"
	 * (a directory), and "banana/apple.txt" and "banana/orange.txt".
	 * 
	 * @param dir
	 *            the folder
	 * @param includeDirInZip
	 *            if true, include the target directory in the zip file
	 * @return the zip file
	 * @throws IOException
	 */
	public static File zipDirectory(File dir, boolean includeDirInZip, boolean includeHidden)
			throws IOException {
		return zipDirectory(dir, includeDirInZip, includeHidden, null);
	}

	/**
	 * Create a child temp folder inside the main temp dir.
	 * 
	 * @return the folder
	 * @throws IOException
	 */
	public static File tempDir() throws IOException {
		final File dir = File.createTempFile(TEMP_DIR_PREFIX, Long.toString(System.nanoTime()));

		if (!dir.delete()) {
			throw new IOException("failed to delete file: " + dir.getAbsolutePath());
		}

		if (!dir.mkdir()) {
			throw new IOException("failed to create dir: " + dir.getAbsolutePath());
		}

		return dir;
	}

	/**
	 * create a zip file of the contents of the folder, optionally including the folder itself, with
	 * optional filter by Extensions to include
	 * 
	 * if "includeDirInZip" is true, the specified directory - one level only - will be included in
	 * the zip file, i.e. all entry paths will begin with the directory name. For example, suppose
	 * the "dir" is /home/user/banana and contains two files, "apple.txt" and "orange.txt". If
	 * "includeDirInZip" is false, the zip file will contain two entries, "apple.txt" and
	 * "orange.txt". If "includeDirInZip" is true, the zip file will contain three entries, "banana"
	 * (a directory), and "banana/apple.txt" and "banana/orange.txt".
	 * 
	 * @param dir
	 *            the folder
	 * @param includeDirInZip
	 *            if true, include the target directory in the zip file
	 * @param excludeExtensions
	 *            a list of file extensions to include; if null, all files will be included
	 * @return the zip file
	 * @throws IOException
	 */
	public static File zipDirectory(File dir, boolean includeDirInZip, boolean includeHidden,
			List<String> extFilter) throws IOException {
		if (dir == null) {
			throw new IOException("zipDirectory was passed a null directory to zip");
		}
		File outputDir = tempDir();
		String dirName = dir.getName();
		File zipFile = new File(outputDir, dirName + ".zip");
		FileOutputStream dest = new FileOutputStream(zipFile);
		ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
		BufferedInputStream origin = null;

		try {
			// out.setMethod(ZipOutputStream.DEFLATED);
			byte data[] = new byte[BUFFER_SIZE];
			// get a list of files from current directory
			List<String> files = getAllFilesInDir(true, dir, includeHidden, null);

			for (String file : files) {
				if (extFilter != null && !extFilter.contains(getExtFromFileName(file))) {
					System.out.println("zipDirectory: " + file + " excluded by extension");
					continue;
				}

				File ff = new File(dir, file);
				if (!ff.exists()) {
					System.out.println("zipDirectory: " + file + " no longer exists....");
					continue;
				}

				if (includeDirInZip) {
					file = dir.getName() + "/" + file;
				}

				System.out.println("zipDirectory: adding " + file);

				FileInputStream fi = new FileInputStream(ff);
				ZipEntry entry = new ZipEntry(file);
				out.putNextEntry(entry);

				if (!ff.isDirectory()) {
					try {
						origin = new BufferedInputStream(fi, BUFFER_SIZE);
						int count;
						while ((count = origin.read(data, 0, BUFFER_SIZE)) != -1) {
							out.write(data, 0, count);
						}
					} finally {
						origin.close();
					}
				}
			}
		} finally {
			if (out != null) {
				out.close();
			}
		}
		return zipFile;
	}

	// relative filenames
	private static List<String> getAllFilesInDir(boolean traverseSubDirs, File dir,
			boolean includeHidden, String prefix) {
		List<String> files = new ArrayList<String>();
		String fileNames[] = dir.list();

		for (String fileName : fileNames) {
			File currentFile = new File(dir, fileName);
			if (currentFile.isHidden() && !includeHidden) {
				continue;
			}
			if (currentFile.isDirectory()) {
				if (traverseSubDirs) {
					String pfx;
					if (prefix == null || prefix.length() == 0) {
						pfx = fileName;
					} else {
						pfx = prefix + "/" + fileName;
					}
					files.addAll(getAllFilesInDir(true, currentFile, includeHidden, pfx));
				}
			} else {
				String fullname = fileName;
				if (prefix != null && prefix.length() > 0) {
					fullname = prefix + "/" + fileName;
				}
				files.add(fullname);
			}
		}
		return files;
	}

	public static String getExtFromFileName(String name) {
		name = new File(name).getName();
		return name.substring(name.lastIndexOf(".") + 1);
	}

	/**
	 * Write the binary input stream to the given file.
	 * 
	 * @param f
	 *            the file to be written
	 * @param in
	 *            the contents
	 * @throws IOException
	 */
	public static void writeFile(File f, InputStream in) throws IOException {
		byte[] buf = new byte[4096];

		try {
			OutputStream out = new FileOutputStream(f);
			try {
				int len;
				while ((len = in.read(buf)) != -1) {
					out.write(buf, 0, len);
				}
			} finally {
				out.close();
			}
		} finally {
			in.close();
		}
	}

	/**
	 * useful for Runtime.exec() calls
	 */
	public static class StreamEater {
		StringBuilder sb = new StringBuilder();
		final InputStream _in;
		boolean running = false;
		Thread eaterThread;

		@Override
		public String toString() {
			while (running) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// nothing
				}
			}
			return (sb.length() > 0 ? sb.substring(0, sb.length() - 1) : "");
		}

		public StreamEater(InputStream in) {
			this._in = in;
			this.sb = new StringBuilder();
			running = true;
			eaterThread = new Thread(new Runnable() {
				@Override
				public void run() {
					InputStreamReader rdr = new InputStreamReader(_in);
					try {
						int i;
						while ((i = rdr.read()) != -1) {
							sb.append((char) i);
						}
						rdr.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					running = false;
				}
			});
			eaterThread.start();
		}
		
		public Thread getEaterThread() {
			return eaterThread;
		}
	}

	/**
	 * Unzips a file in the supplied target directory, or in the current working director
	 * 
	 * @param zipfile
	 *            the file to unzip, cannot be null
	 * @param targetDir
	 *            the directory to unzip the file in. Will be created if it does not exist. if null
	 *            will use the current working directory
	 * @return
	 * @throws IOException
	 * @throws ZipException
	 */
	@SuppressWarnings("unchecked")
	public static void unzipFile(File zipfile, File targetDir) throws IOException, ZipException {
		if (targetDir == null) {
			targetDir = new File(System.getProperty("user.dir"));
		}

		if (!targetDir.exists()) {
			targetDir.mkdirs();
		}

		// unzip in the temp dir
		ZipFile zfile = new ZipFile(zipfile);
		for (Enumeration<ZipEntry> e = (Enumeration<ZipEntry>) zfile.entries(); e.hasMoreElements();) {
			ZipEntry entry = e.nextElement();
			File target = new File(targetDir.getAbsolutePath() + File.separator + entry.getName());
			if (entry.isDirectory()) {
				target.mkdirs();
			} else {
				FileUtils.writeFile(target, zfile.getInputStream(entry));
				target.setExecutable(true);
			}
		}
	}
}
