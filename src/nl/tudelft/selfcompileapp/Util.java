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
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Deque;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Util {

	/**
	 * @source http://stackoverflow.com/a/304350
	 */
	private static byte[] hash(String alg, InputStream in) throws NoSuchAlgorithmException, IOException {
		MessageDigest md = MessageDigest.getInstance(alg);
		DigestInputStream dis = new DigestInputStream(new BufferedInputStream(in), md);
		try {
			byte[] buffer = new byte[1024];
			while (true) {
				int readCount = dis.read(buffer);
				if (readCount < 0) {
					break;
				}
			}
			return md.digest();
		} finally {
			in.close();
		}
	}

	/**
	 * @source http://stackoverflow.com/a/304275
	 */
	public static String getMD5Checksum(File file) throws Exception {
		byte[] b = hash("MD5", new FileInputStream(file));
		String result = "";
		for (int i = 0; i < b.length; i++) {
			result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
		}
		return result;
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
		ZipInputStream in = new ZipInputStream(new BufferedInputStream(zipFile));
		try {
			ZipEntry ze;
			int count;
			byte[] buffer = new byte[8192];
			while ((ze = in.getNextEntry()) != null) {
				File file = new File(targetDirectory, ze.getName());
				File dir = ze.isDirectory() ? file : file.getParentFile();
				if (!dir.isDirectory() && !dir.mkdirs()) {
					throw new FileNotFoundException("Failed to ensure directory: " + dir.getAbsolutePath());
				}
				if (ze.isDirectory()) {
					continue;
				}
				BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
				try {
					while ((count = in.read(buffer)) != -1)
						out.write(buffer, 0, count);
				} finally {
					out.close();
				}
				long time = ze.getTime();
				if (time > 0) {
					file.setLastModified(time);
				}
			}
		} finally {
			in.close();
		}
	}

	/**
	 * @source http://stackoverflow.com/a/1399432
	 */
	public static void zip(File directory, File zipfile) throws IOException {
		URI base = directory.toURI();
		Deque<File> queue = new LinkedList<File>();
		queue.push(directory);
		ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipfile)));
		try {
			while (!queue.isEmpty()) {
				directory = queue.pop();
				for (File path : directory.listFiles()) {
					String name = base.relativize(path.toURI()).getPath();
					if (path.isDirectory()) {
						queue.push(path);
						name = name.endsWith("/") ? name : name + "/";
						out.putNextEntry(new ZipEntry(name));
					} else {
						out.putNextEntry(new ZipEntry(name));
						copy(path, out);
						out.closeEntry();
					}
				}
			}
		} finally {
			out.close();
		}
	}

	private static void copy(InputStream in, OutputStream out) throws IOException {
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
		InputStream in = new BufferedInputStream(new FileInputStream(file));
		try {
			copy(in, out);
		} finally {
			in.close();
		}
	}

	public static void copy(InputStream in, File file) throws IOException {
		OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
		try {
			copy(in, out);
		} finally {
			out.close();
		}
	}

}
