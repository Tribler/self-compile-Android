package nl.tudelft.selfcompileapp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Deque;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class Util {

	public static final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	public static final TransformerFactory tf = TransformerFactory.newInstance();

	public static Document readXml(File xmlFile) throws Exception {
		DocumentBuilder db = dbf.newDocumentBuilder();
		return db.parse(xmlFile);
	}

	/**
	 * @source http://stackoverflow.com/a/7373596
	 */
	public static void writeXml(Document dom, File xmlFile) throws Exception {
		Transformer t = tf.newTransformer();
		t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
		t.setOutputProperty(OutputKeys.INDENT, "yes");
		t.setOutputProperty(OutputKeys.METHOD, "xml");
		t.setOutputProperty(OutputKeys.VERSION, "1.0");
		t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		t.transform(new DOMSource(dom), new StreamResult(new FileOutputStream(xmlFile)));
	}

	/**
	 * @source http://stackoverflow.com/a/8563446
	 */
	public static void replaceFirstLine(String newLine, File file) {
		File tmpFile = new File(file.getPath(), file.getName() + ".tmp");
		BufferedReader br = null;
		BufferedWriter bw = null;
		try {
			br = new BufferedReader(new FileReader(file));
			bw = new BufferedWriter(new FileWriter(tmpFile));
			String line;
			boolean first = true;
			while ((line = br.readLine()) != null) {
				if (first) {
					first = false;
					line = newLine;
				}
				bw.write(line + "\n");
			}
		} catch (Exception e) {
			return;
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				if (bw != null)
					bw.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		file.delete();
		tmpFile.renameTo(file);

	}

	/**
	 * @source http://stackoverflow.com/a/304350
	 */
	private static byte[] hash(String alg, InputStream in) throws Exception {
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
	public static void unzip(InputStream zipFile, File targetDirectory) throws Exception {
		ZipInputStream in = new ZipInputStream(new BufferedInputStream(zipFile));
		try {
			ZipEntry ze;
			int count;
			byte[] buffer = new byte[8192];
			while ((ze = in.getNextEntry()) != null) {
				File file = new File(targetDirectory, ze.getName());
				File dir = ze.isDirectory() ? file : file.getParentFile();
				if (!dir.isDirectory() && !dir.mkdirs()) {
					throw new Exception("Failed to ensure directory: " + dir.getAbsolutePath());
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
	public static void zip(File directory, File zipfile) throws Exception {
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

	private static void copy(InputStream in, OutputStream out) throws Exception {
		byte[] buffer = new byte[1024];
		while (true) {
			int readCount = in.read(buffer);
			if (readCount < 0) {
				break;
			}
			out.write(buffer, 0, readCount);
		}
	}

	public static void copy(File file, OutputStream out) throws Exception {
		InputStream in = new BufferedInputStream(new FileInputStream(file));
		try {
			copy(in, out);
		} finally {
			in.close();
		}
	}

	public static void copy(InputStream in, File file) throws Exception {
		OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
		try {
			copy(in, out);
		} finally {
			out.close();
		}
	}

	/**
	 * @source http://stackoverflow.com/a/10600736
	 */
	public static Bitmap drawableToBitmap(Drawable drawable) {
		Bitmap bitmap = null;
		if (drawable instanceof BitmapDrawable) {
			BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
			if (bitmapDrawable.getBitmap() != null) {
				return bitmapDrawable.getBitmap();
			}
		}
		if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
			// Single color bitmap will be created of 1x1 pixel
			bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);

		} else {
			bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
					Bitmap.Config.ARGB_8888);
		}
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);
		return bitmap;
	}

}
