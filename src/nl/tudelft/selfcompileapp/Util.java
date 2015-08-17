package nl.tudelft.selfcompileapp;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Util {

	/**
	 * @source http://stackoverflow.com/a/27050680
	 */
	public static void unZip(InputStream zipFile, File targetDir)
			throws IOException {
		ZipInputStream in = new ZipInputStream(new BufferedInputStream(zipFile));
		try {
			ZipEntry entry;
			int count;
			byte[] buffer = new byte[8192];
			while ((entry = in.getNextEntry()) != null) {

				File file = new File(targetDir, entry.getName());
				File dir = entry.isDirectory() ? file : file.getParentFile();

				if (!dir.isDirectory() && !dir.mkdirs()) {
					throw new FileNotFoundException(
							"Failed to ensure directory: "
									+ dir.getAbsolutePath());
				}
				if (entry.isDirectory()) {
					continue;
				}
				FileOutputStream out = new FileOutputStream(file);
				try {
					while ((count = in.read(buffer)) != -1) {
						out.write(buffer, 0, count);
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					out.close();
				}
				/*
				 * if time should be restored as well long time = ze.getTime();
				 * if (time > 0) file.setLastModified(time);
				 */
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			in.close();
		}
	}

	/**
	 * @source http://stackoverflow.com/a/26676033
	 */
	public static void zipFolder(File srcFile, File outZip) {
		try {
			FileOutputStream fos = new FileOutputStream(outZip);
			ZipOutputStream zos = new ZipOutputStream(fos);

			File[] files = srcFile.listFiles();
			for (int i = 0; i < files.length; i++) {

				byte[] buffer = new byte[1024];
				FileInputStream fis = new FileInputStream(files[i]);
				zos.putNextEntry(new ZipEntry(files[i].getName()));
				int length;
				while ((length = fis.read(buffer)) > 0) {
					zos.write(buffer, 0, length);
				}
				zos.closeEntry();
				fis.close();
			}
			zos.close();
		} catch (IOException e) {
			e.printStackTrace();
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

	public static void listRecursive(File path) {
		if (path.isDirectory()) {
			for (File child : path.listFiles()) {
				listRecursive(child);
			}
		}
		System.out.println(path.getAbsolutePath());
	}

	/**
	 * @source http://www.journaldev.com/861/4-ways-to-copy-file-in-java
	 */
	public static void copyFile(InputStream is, OutputStream os)
			throws IOException {
		try {
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
