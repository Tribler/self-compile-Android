package nl.tudelft.selfcompileapp;

import java.io.File;

/**
 * Structure of app files & directories
 * 
 * @author Paul Brussee
 */
public class S {

	public static final File dirRoot = android.os.Environment
			.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS);
	public static final File dirProj = new File(dirRoot, "TEMP-app");

	/* PROJECT */
	public static final File dirSrc = new File(dirProj, "src");
	public static final File dirGen = new File(dirProj, "gen");
	public static final File dirRes = new File(dirProj, "res");
	public static final File dirLibs = new File(dirProj, "libs");
	public static final File dirAssets = new File(dirProj, "assets");
	public static final File dirBin = new File(dirProj, "bin");
	public static final File dirDist = new File(dirProj, "dist");

	public static final File xmlMan = new File(dirProj, "AndroidManifest.xml");

	/* RES */
	public static final File dirDrawLdpi = new File(dirRes, "drawable-ldpi");
	public static final File dirDrawMdpi = new File(dirRes, "drawable-mdpi");
	public static final File dirDrawHdpi = new File(dirRes, "drawable-hdpi");
	public static final File dirDrawXhdpi = new File(dirRes, "drawable-xhdpi");
	public static final File dirDrawXxhdpi = new File(dirRes, "drawable-xxhdpi");
	public static final File dirLayout = new File(dirRes, "layout");
	public static final File dirValues = new File(dirRes, "values");
	public static final File dirValues11 = new File(dirRes, "values-v11");
	public static final File dirValues14 = new File(dirRes, "values-v14");

	// File pngAppIconLdpi = new File(dirDrawLdpi, "app_icon.png");
	public static final File pngAppIconMdpi = new File(dirDrawMdpi, "app_icon.png");
	public static final File pngAppIconHdpi = new File(dirDrawHdpi, "app_icon.png");
	public static final File pngAppIconXhdpi = new File(dirDrawXhdpi, "app_icon.png");
	public static final File pngAppIconXxhdpi = new File(dirDrawXxhdpi, "app_icon.png");

	public static final File xmlActivitySelfCompile = new File(dirLayout, "activity_self_compile.xml");
	public static final File xmlActivityPickApp = new File(dirLayout, "activity_pick_app.xml");
	public static final File xmlItemAppInfo = new File(dirLayout, "item_app_info.xml");

	public static final File xmlStrings = new File(dirValues, "strings.xml");
	public static final File xmlStyles = new File(dirValues, "styles.xml");
	public static final File xmlStyles11 = new File(dirValues11, "styles.xml");
	public static final File xmlStyles14 = new File(dirValues14, "styles.xml");

	/* BIN */
	public static final File dirClasses = new File(dirBin, "classes");
	public static final File dirDexedLibs = new File(dirBin, "dexedLibs");
	// File dirBinRes = new File(dirBin, "res");
	// File dirCrunch = new File(dirBinRes, "crunch");

	public static final File ap_Resources = new File(dirBin, "resources.ap_");
	public static final File dexClasses = new File(dirBin, "classes.dex");
	public static final File dexLibs = new File(dirBin, "libs.dex");
	// File xmlBinMan = new File(dirBin, "AndroidManifest.xml");

	/* ASSETS */
	public static final File pngAppIcon = new File(dirAssets, "app_icon.png");
	public static final File jarAndroid = new File(dirAssets, "android-18.jar");
	public static final File jksEmbedded = new File(dirAssets, "Embedded.jks");

	public static final File zipSrc = new File(dirAssets, "src.zip");
	public static final File zipRes = new File(dirAssets, "res.zip");
	public static final File zipLibs = new File(dirAssets, "libs.zip");
	public static final File zipDexedLibs = new File(dirAssets, "dexedLibs.zip");

	/* DIST */
	public static final File apkUnsigned = new File(S.dirDist, "app.unsigned.apk");
	public static final File apkUnaligned = new File(S.dirDist, "app.unaligned.apk");

	public static File apkRedistributable = null;

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
