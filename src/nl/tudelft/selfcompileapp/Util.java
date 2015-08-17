package nl.tudelft.selfcompileapp;

import java.io.BufferedInputStream;
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

	public static void listRecursive(File path) {
		if (path.isDirectory()) {
			for (File child : path.listFiles()) {
				listRecursive(child);
			}
		}
		System.out.println(path.getAbsolutePath());
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
	public static void unzip(InputStream zipFile, File targetDirectory)
			throws IOException {
		ZipInputStream zis = new ZipInputStream(
				new BufferedInputStream(zipFile));
		try {
			ZipEntry ze;
			int count;
			byte[] buffer = new byte[8192];
			while ((ze = zis.getNextEntry()) != null) {
				File file = new File(targetDirectory, ze.getName());
				File dir = ze.isDirectory() ? file : file.getParentFile();
				if (!dir.isDirectory() && !dir.mkdirs())
					throw new FileNotFoundException(
							"Failed to ensure directory: "
									+ dir.getAbsolutePath());
				if (ze.isDirectory())
					continue;
				FileOutputStream fout = new FileOutputStream(file);
				try {
					while ((count = zis.read(buffer)) != -1)
						fout.write(buffer, 0, count);
				} finally {
					fout.close();
				}
				/*
				 * if time should be restored as well long time = ze.getTime();
				 * if (time > 0) file.setLastModified(time);
				 */
			}
		} finally {
			zis.close();
		}
	}

	/**
	 * @source http://stackoverflow.com/a/1399432
	 */
	public static void zip(File directory, File zipfile) throws IOException {
		URI base = directory.toURI();
		Deque<File> queue = new LinkedList<File>();
		queue.push(directory);
		OutputStream out = new FileOutputStream(zipfile);
		ZipOutputStream zout = new ZipOutputStream(out);
		try {
			while (!queue.isEmpty()) {
				directory = queue.pop();
				for (File kid : directory.listFiles()) {
					String name = base.relativize(kid.toURI()).getPath();
					if (kid.isDirectory()) {
						queue.push(kid);
						name = name.endsWith("/") ? name : name + "/";
						zout.putNextEntry(new ZipEntry(name));
					} else {
						zout.putNextEntry(new ZipEntry(name));
						copy(kid, zout);
						zout.closeEntry();
					}
				}
			}
		} finally {
			zout.close();
		}
	}

	public static void copy(InputStream in, OutputStream out)
			throws IOException {
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

}
