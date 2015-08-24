package nl.tudelft.selfcompileapp;

import java.io.*;

/**
 * @author Tom Arn (www.t-arn.com)
 */
class G {

	static {
		stWorkDir = android.os.Environment.getExternalStorageDirectory().getPath() + "/.JNImain/";
	}

	public static String stWorkDir;

	// ===================================================================
	public static boolean fnCheckWorkDir()
	// ===================================================================
	{
		boolean ok = true;
		if (!new File(stWorkDir).exists()) {
			ok = false;
			System.err.println("Workdir missing");
		}
		return ok;
	}// fnCheckWorkDir

	// ===================================================================
	public static boolean fnMakeLogDir()
	// ===================================================================
	{
		File dir;
		boolean ok = false;
		try {
			dir = new File(stWorkDir);
			if (!dir.exists())
				dir.mkdir();
			if (dir.isDirectory())
				ok = true;
		} catch (Exception e) {
			ok = false;
		}
		fnCheckWorkDir();
		return ok;
	} // fnMakeLogDir
}

// ##################################################################
public class Aapt
// ##################################################################
{
	private static boolean bInitialized = false;

	private native int JNImain(String args);

	// ===================================================================
	public Aapt()
	// ===================================================================
	{
		if (!bInitialized)
			fnInit();
	} // constructor
		// ===================================================================

	private static boolean fnInit()
	// ===================================================================
	{
		boolean ok = G.fnMakeLogDir();
		if (!ok) {
			System.err.println("Error mkdir: " + G.stWorkDir);
			return false;
		}
		try {
			System.out.println("Loading native library aaptcomplete...");
			System.loadLibrary("aaptcomplete");
			bInitialized = true;
			ok = true;
		} catch (Exception e) {
			System.err.println(e.getMessage());
			ok = false;
		}
		return ok;
	} // fnInit
		// ===================================================================

	public synchronized int fnExecute(String args)
	// ===================================================================
	{
		int rc = 99;

		System.out.println("Calling JNImain...");
		rc = JNImain(args.replace(' ', '\t'));
		System.out.println("Result from native lib=" + rc);
		fnGetNativeOutput();
		return rc;
	} // fnExecute
		// ===================================================================

	private void fnGetNativeOutput()
	// ===================================================================
	{
		LineNumberReader lnr;
		String st = "";

		try {
			lnr = new LineNumberReader(new FileReader(G.stWorkDir + "native_stdout.txt"));
			st = "";
			while (st != null) {
				st = lnr.readLine();
				if (st != null)
					System.out.println(st);
			} // while
			lnr.close();

			lnr = new LineNumberReader(new FileReader(G.stWorkDir + "native_stderr.txt"));
			st = "";
			while (st != null) {
				st = lnr.readLine();
				if (st != null)
					System.err.println(st);
			} // while
			lnr.close();
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	} //
		// ===================================================================

	public boolean isInitialized()
	// ===================================================================
	{
		return bInitialized;
	} //
		// ===================================================================
}
// ##################################################################
