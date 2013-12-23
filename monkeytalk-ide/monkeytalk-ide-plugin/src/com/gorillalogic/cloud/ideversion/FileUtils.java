package com.gorillalogic.cloud.ideversion;

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
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


public class FileUtils {
	private static final String TEMP_DIR_PREFIX = "cloud";
	private static final int BUFFER_SIZE = 4096;

	private FileUtils() {
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
		return readStream(new FileInputStream(f));
	}

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
	 * Write the binary input stream to the given file.
	 * 
	 * @param f
	 *            the file to be written
	 * @param in
	 *            the contents
	 * @throws IOException
	 */
	public static void writeFile(File f, InputStream in) throws IOException {
		byte[] buf = new byte[BUFFER_SIZE];

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
	 * Validate the given file (is not null, exists, and is actually a file), throws an
	 * {@link IOException} with the error message on failure.
	 * 
	 * @param f
	 *            the file
	 * @param msg
	 *            the error message
	 * @throws IOException
	 */
	public static void checkFile(File f, String msg) throws IOException {
		if (f == null) {
			throw new IOException((msg != null ? msg : "file") + " is null");
		} else if (!f.exists()) {
			throw new IOException((msg != null ? msg : "file") + " not found: "
					+ f.getAbsolutePath());
		} else if (!f.isFile()) {
			throw new IOException((msg != null ? msg : "file") + " not a file: "
					+ f.getAbsolutePath());
		}
	}

	/**
	 * Cleanup all child folders in the main temp dir.
	 * 
	 * @throws IOException
	 */
	public static void cleanup() throws IOException {
		File dummy = File.createTempFile("dummy", null);
		dummy.deleteOnExit();

		for (File f : dummy.getParentFile().listFiles()) {
			if (f.isDirectory() && f.getName().startsWith(TEMP_DIR_PREFIX)) {
				FileUtils.deleteDir(f);
			}
		}
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
	 * Create a temp file with the given contents in the given folder.
	 * 
	 * @param filename
	 *            the temp file to be created
	 * @param contents
	 *            the contents
	 * @param dir
	 *            the folder
	 * @return the temp file
	 * @throws IOException
	 */
	public static File tempFile(String filename, String contents, File dir) throws IOException {
		final File tmp = new File(dir, filename);
		FileUtils.writeFile(tmp, contents);
		return tmp;
	}



	/**
	 * Print all the files in the given folder and recursively print all its children too.
	 * 
	 * @param dir
	 *            the folder
	 * @throws IOException
	 */
	public static String printDir(File dir) throws IOException {
		StringBuilder sb = new StringBuilder();
		sb.append(dir.getAbsolutePath());
		if (dir.isDirectory()) {
			for (File f : dir.listFiles()) {
				sb.append('\n').append(printDir(f));
			}
		}
		return sb.toString();
	}
	
	/**
	 * create a zip file of the contents of the folder, optionally including the folder itself. 
	 * 
	 * if "includeDirInZip" is true, the specified directory - one level only - will be included 
	 * in the zip file, i.e. all entry paths will begin with the directory name. For example, suppose 
	 * the "dir" is /home/user/banana and contains two files, "apple.txt" and "orange.txt". 
	 * If "includeDirInZip" is false, the zip file will contain two entries, "apple.txt" and "orange.txt".
	 * If "includeDirInZip" is true, the zip file will contain three entries, "banana" (a directory), and 
	 * "banana/apple.txt" and "banana/orange.txt".
	 * 
	 * @param dir
	 *            the folder
	 * @param includeDirInZip
	 *            if true, include the target directory in the zip file
	 * @return the zip file 
	 * @throws IOException
	 */
	public static File zipDirectory(File dir, boolean includeDirInZip, boolean includeHidden) throws IOException {
		return zipDirectory(dir, includeDirInZip, includeHidden, null);
	}
	
	/**
	 * create a zip file of the contents of the folder, optionally including the folder itself, with optional filter by Extensions to include 
	 * 
	 * if "includeDirInZip" is true, the specified directory - one level only - will be included 
	 * in the zip file, i.e. all entry paths will begin with the directory name. For example, suppose 
	 * the "dir" is /home/user/banana and contains two files, "apple.txt" and "orange.txt". 
	 * If "includeDirInZip" is false, the zip file will contain two entries, "apple.txt" and "orange.txt".
	 * If "includeDirInZip" is true, the zip file will contain three entries, "banana" (a directory), and 
	 * "banana/apple.txt" and "banana/orange.txt".
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
	public static File zipDirectory(File dir, boolean includeDirInZip, boolean includeHidden, List<String> extFilter) throws IOException {
		if (dir==null) {
			throw new IOException("zipDirectory was passed a null directory to zip"); 
		}
		File outputDir=tempDir();
		String dirName=dir.getName();
        File zipFile = new File(outputDir, dirName + ".zip");
        FileOutputStream dest = new FileOutputStream(zipFile);
        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
        BufferedInputStream origin = null;
 
        try {
        	//out.setMethod(ZipOutputStream.DEFLATED);
        	byte data[] = new byte[BUFFER_SIZE];
        	// get a list of files from current directory
        	List<String> files = getAllFilesInDir(true, dir, includeHidden, null);

        	for (String file : files) {
        		if(extFilter != null && !extFilter.contains(getExtFromFileName(file))) {
        	 		System.out.println("zipDirectory: " + file + " excluded by extension");
        			continue;
        		}
        		
        		File ff=new File(dir, file);
        		if(!ff.exists()) {
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
        				while((count = origin.read(data, 0, BUFFER_SIZE)) != -1) {
        					out.write(data, 0, count);
        				}
        			} finally {
                		origin.close();
        			}
        		}
        	}
        } finally {
        	if (out!=null) {
            	out.close();
        	}
        }
		return zipFile;
	}
        
	// relative filenames
    private static List<String> getAllFilesInDir(boolean traverseSubDirs, File dir, boolean includeHidden, String prefix){
    	List<String> files = new ArrayList<String>();
    	String fileNames[] = dir.list();

    	for (String fileName : fileNames) {
    		File currentFile = new File(dir, fileName);
    		if (currentFile.isHidden() && !includeHidden) {
    			continue;
    		}
    		if(currentFile.isDirectory()) {
    			if (traverseSubDirs){
    				String pfx;
    				if (prefix==null || prefix.length()==0) {
    					pfx=fileName;
    				} else {
    					pfx = prefix + "/" + fileName;
    				}
    				files.addAll(getAllFilesInDir(true, currentFile, includeHidden, pfx));					
    			}
    		} else {
    			String fullname=fileName;
    			if (prefix!=null && prefix.length()>0) {
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
       
}