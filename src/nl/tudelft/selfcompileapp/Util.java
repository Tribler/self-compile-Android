package nl.tudelft.selfcompileapp;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
					// TODO Auto-generated catch block
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			in.close();
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

}
