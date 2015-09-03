package nl.tudelft.selfcompileapp;

import java.io.File;

import android.net.Uri;

/**
 * Structure of app files & directories
 * 
 * @author Paul Brussee
 */
public class S {

	public static final File dirRoot = android.os.Environment
			.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS);
	public static final File dirProj = new File(dirRoot, "TEMP-app");
	public static final File dirSrc = new File(dirProj, "src");
	public static final File dirGen = new File(dirProj, "gen");
	public static final File dirRes = new File(dirProj, "res");
	public static final File dirLibs = new File(dirProj, "libs");
	public static final File dirAssets = new File(dirProj, "assets");
	public static final File dirBin = new File(dirProj, "bin");
	public static final File dirDist = new File(dirProj, "dist");

	public static final File xmlMan = new File(dirProj, "AndroidManifest.xml");

	public static final File dirClasses = new File(dirBin, "classes");
	public static final File dirDexedLibs = new File(dirBin, "dexedLibs");
	// File dirBinRes = new File(dirBin, "res");
	// File dirCrunch = new File(dirBinRes, "crunch");

	public static final File ap_Resources = new File(dirBin, "resources.ap_");
	public static final File dexClasses = new File(dirBin, "classes.dex");
	public static final File dexLibs = new File(dirBin, "libs.dex");
	// File xmlBinMan = new File(dirBin, "AndroidManifest.xml");

	public static final File pngAppIcon = new File(dirAssets, "ic_launcher.png");
	public static final File jarAndroid = new File(dirAssets, "android-18.jar");
	public static final File jksEmbedded = new File(dirAssets, "Embedded.jks");

	public static final File zipSrc = new File(dirAssets, "src.zip");
	public static final File zipRes = new File(dirAssets, "res.zip");
	public static final File zipLibs = new File(dirAssets, "libs.zip");
	public static final File zipDexedLibs = new File(dirAssets, "dexedLibs.zip");

	public static final File apkUnsigned = new File(S.dirDist, "app.unsigned.apk");
	public static final File apkUnaligned = new File(S.dirDist, "app.unaligned.apk");

	public static Uri apkRedistributable = null;

	public static void mkDirs() {
		dirRoot.mkdirs();
		dirProj.mkdirs();
		dirSrc.mkdirs();
		dirGen.mkdirs();
		dirRes.mkdirs();
		dirLibs.mkdirs();
		dirAssets.mkdirs();
		dirBin.mkdirs();
		dirDexedLibs.mkdirs();
		dirDist.mkdirs();
	}

	public static String getJavaBootClassPath() {
		return jarAndroid.getPath();
	}

	public static String getJavaClassPath() {
		String strClassPath = "";
		for (File jarLib : dirLibs.listFiles()) {
			if (jarLib.isFile() && jarLib.getName().endsWith(".jar")) {
				strClassPath += File.pathSeparator + jarLib.getPath();
			}
		}
		return strClassPath;
	}

}
