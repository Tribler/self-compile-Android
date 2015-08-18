package nl.tudelft.selfcompileapp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Deque;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Util {

	public static final long MAX_JAR_SIZE = 10000000; // 10 MB

	/**
	 * Split jar file if larger than max size
	 * 
	 * @return amount of files created
	 */
	public static int setMaxJarSize(File jar) throws FileNotFoundException, IOException {
		if (jar.length() > MAX_JAR_SIZE) {
			File dirTemp = new File(jar.getParent(), "temp-" + jar.getName());
			dirTemp.mkdirs();
			unzip(new FileInputStream(jar), dirTemp);
			int i = zip(dirTemp, jar, MAX_JAR_SIZE);
			deleteRecursive(dirTemp);
			return i;
		}
		return 1;
	}

	public static void zip(File directory, File zipFile) throws IOException {
		if (zipFile.getName().endsWith(".jar")) {
			zip(directory, zipFile, MAX_JAR_SIZE);
		} else {
			zip(directory, zipFile, -1);
		}
	}

	private static ZipOutputStream newZipOutputStream(File path, int i) throws IOException {
		File partial = new File(path.getParent(), i + "-" + path.getName());
		FileOutputStream dest = new FileOutputStream(partial);
		return new ZipOutputStream(new BufferedOutputStream(dest));
	}

	/**
	 * Modified to support max file size.
	 * 
	 * @source http://stackoverflow.com/a/1399432
	 * 
	 * @return amount of files created
	 */
	private static int zip(File directory, File zipFile, long maxFileSize) throws IOException {
		URI base = directory.toURI();
		Deque<File> queue = new LinkedList<File>();
		queue.push(directory);
		int i = 1;
		ZipOutputStream zout = newZipOutputStream(zipFile, i);
		long sizeCounter = 0;
		// recursive iteration
		while (!queue.isEmpty()) {
			directory = queue.pop();
			for (File dirEntry : directory.listFiles()) {
				// split if max size reached
				if (sizeCounter > maxFileSize) {
					zout.close();
					i++;
					zout = newZipOutputStream(zipFile, i);
					sizeCounter = 0;
				}
				String name = base.relativize(dirEntry.toURI()).getPath();
				// keep directory structure
				if (dirEntry.isDirectory()) {
					queue.push(dirEntry);
					name = name.endsWith("/") ? name : name + "/";
					zout.putNextEntry(new ZipEntry(name));
				} else {
					ZipEntry ze = new ZipEntry(name);
					zout.putNextEntry(ze);
					copy(dirEntry, zout);
					zout.closeEntry();
					// only count if max size is set
					if (maxFileSize > 0) {
						sizeCounter += ze.getCompressedSize();
					}
				}
			}
		}
		zout.close();
		return i;
	}

	public static void copy(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		while (true) {
			int readCount = in.read(buffer);
			if (readCount < 0) {
				break;
			}
			out.write(buffer, 0, readCount);
		}
	}

	public static void copy(File file, OutputStream out) throws IOException {
		InputStream in = new FileInputStream(file);
		try {
			copy(in, out);
		} finally {
			in.close();
		}
	}

	public static void copy(InputStream in, File file) throws IOException {
		OutputStream out = new FileOutputStream(file);
		try {
			copy(in, out);
		} finally {
			out.close();
		}
	}

	/**
	 * @source http://stackoverflow.com/a/6425744
	 */
	public static void deleteRecursive(File path) {
		if (path.isDirectory()) {
			for (File child : path.listFiles()) {
				deleteRecursive(child);
			}
		}
		path.delete();
	}

	/**
	 * @source http://stackoverflow.com/a/27050680
	 */
	public static void unzip(InputStream zipFile, File targetDirectory) throws IOException {
		ZipInputStream zin = new ZipInputStream(new BufferedInputStream(zipFile));
		try {
			ZipEntry ze;
			int count;
			byte[] buffer = new byte[8192];
			while ((ze = zin.getNextEntry()) != null) {
				File file = new File(targetDirectory, ze.getName());
				File dir = ze.isDirectory() ? file : file.getParentFile();
				if (!dir.isDirectory() && !dir.mkdirs()) {
					throw new FileNotFoundException("Failed to ensure directory: " + dir.getAbsolutePath());
				}
				if (ze.isDirectory()) {
					continue;
				}
				FileOutputStream fout = new FileOutputStream(file);
				try {
					while ((count = zin.read(buffer)) != -1) {
						fout.write(buffer, 0, count);
					}
				} finally {
					fout.close();
				}
				/*
				 * if time should be restored as well long time = ze.getTime();
				 * if (time > 0) file.setLastModified(time);
				 */
			}
		} finally {
			zin.close();
		}
	}
}
