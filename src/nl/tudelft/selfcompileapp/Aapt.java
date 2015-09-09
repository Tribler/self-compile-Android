package nl.tudelft.selfcompileapp;

import java.io.*;

/**
 * @author Tom Arn (www.t-arn.com)
 */
public class Aapt {
	private static boolean bInitialized = false;

	private native int JNImain(String args);

	public Aapt() {
		if (!bInitialized) {
			fnInit();
		}
	}

	public boolean isInitialized() {
		return bInitialized;
	}

	private static boolean fnInit() {
		try {
			System.out.println("Loading native library aaptcomplete...");
			System.loadLibrary("aaptcomplete");
			bInitialized = true;
			return true;
		} catch (Exception e) {
			System.err.println(e.getMessage());
			return false;
		}
	}

	public synchronized int fnExecute(String args) {
		int rc = 99;
		System.out.println("Calling JNImain...");
		rc = JNImain(args.replace(' ', '\t'));
		System.out.println("Result from native lib=" + rc);
		fnGetNativeOutput();
		return rc;
	}

	private void fnGetNativeOutput() {
		LineNumberReader lnr;
		String st = "";
		try {
			lnr = new LineNumberReader(new FileReader(S.txtOut));
			st = "";
			while (st != null) {
				st = lnr.readLine();
				if (st != null)
					System.out.println(st);
			}
			lnr.close();
			lnr = new LineNumberReader(new FileReader(S.txtErr));
			st = "";
			while (st != null) {
				st = lnr.readLine();
				if (st != null)
					System.err.println(st);
			}
			lnr.close();
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

}