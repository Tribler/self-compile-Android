package nl.tudelft.selfcompileapp;

import java.io.File;
import java.io.FileOutputStream;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import com.android.dex.Dex;
import com.android.dx.merge.CollisionPolicy;
import com.android.dx.merge.DexMerger;
import com.android.sdklib.build.ApkBuilder;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;

public class BuildTask extends ProgressTask {

	BuildTask(Context appContext, Handler listener) {
		super(appContext, listener);
	}

	private void runAidl() throws Exception {
		// TODO make aidl.so
	}

	private void runAapt() throws Exception {
		// TODO update aapt.so
		// DELETE UNSUPPORTED RESOURCES
		Util.deleteRecursive(new File(S.dirRes, "drawable-xxhdpi"));

		Aapt aapt = new Aapt();
		int exitCode = aapt.fnExecute("aapt p -f -v -M " + S.xmlMan.getPath() + " -F " + S.ap_Resources.getPath()
				+ " -I " + S.jarAndroid.getPath() + " -A " + S.dirAssets.getPath() + " -S " + S.dirRes.getPath()
				+ " -J " + S.dirGen.getPath());

		if (exitCode != 0) {
			throw new Exception("AAPT exit(" + exitCode + ")");
		}

		/*
		 * strStatus = "INDEXING RESOURCES"; exitCode = aapt.fnExecute(
		 * "aapt p -m -v -J " + dirGen.getPath() + " -M " + xmlMan.getPath() +
		 * " -S " + dirRes.getPath() + " -I " + jarAndroid.getPath());
		 * 
		 * strStatus = "CRUNCH RESOURCES"; exitCode = aapt.fnExecute(
		 * "aapt c -v -S " + dirRes.getPath() + " -C " + dirCrunch.getPath());
		 * 
		 * strStatus = "PACKAGE RESOURCES"; exitCode = aapt .fnExecute(
		 * "aapt p -v -S " + dirCrunch.getPath() + " -S " + dirRes.getPath() +
		 * " -f --no-crunch --auto-add-overlay --debug-mode -0 apk -M " +
		 * xmlBinMan.getPath() + " -A " + dirAssets.getPath() + " -I " +
		 * jarAndroid.getPath() + " -F " + ap_Resources.getPath());
		 */
	}

	private void compileJava() throws Exception {
		org.eclipse.jdt.core.compiler.batch.BatchCompiler.compile(
				new String[] { "-1.5", "-showversion", "-verbose", "-deprecation", "-bootclasspath",
						S.getJavaBootClassPath(), "-cp", S.getJavaClassPath(), "-d", S.dirClasses.getPath(),
						S.dirGen.getPath(), S.dirSrc.getPath() },
				new java.io.PrintWriter(System.out), new java.io.PrintWriter(System.err), new CompileProgress());
	}

	private void dexLibs() throws Exception {
		int percent = 20;

		for (File jarLib : S.dirLibs.listFiles()) {

			// skip native libs in sub directories
			if (!jarLib.isFile() || !jarLib.getName().endsWith(".jar")) {
				continue;
			}

			// compare hash of jar contents to name of dexed version
			String md5 = Util.getMD5Checksum(jarLib);

			// check if jar is pre-dexed
			File dexLib = new File(S.dirDexedLibs, jarLib.getName().replace(".jar", "-" + md5 + ".jar"));
			System.out.println(dexLib.getName());
			if (!dexLib.exists()) {
				com.android.dx.command.dexer.Main
						.main(new String[] { "--verbose", "--output=" + dexLib.getPath(), jarLib.getPath() });
			}

			percent += 1;
			if (setProgress(percent)) {
				return;
			}
		}
	}

	private void dexClasses() throws Exception {
		com.android.dx.command.dexer.Main
				.main(new String[] { "--verbose", "--output=" + S.dexClasses.getPath(), S.dirClasses.getPath() });
	}

	private void dexMerge() throws Exception {
		int percent = 40;

		for (File dexLib : S.dirDexedLibs.listFiles()) {
			Dex merged = new DexMerger(new Dex(S.dexClasses), new Dex(dexLib), CollisionPolicy.FAIL).merge();
			merged.writeTo(S.dexClasses);

			if (setProgress(++percent)) {
				return;
			}
		}
	}

	private void buildApk() throws Exception {
		// Do NOT use embedded JarSigner
		PrivateKey privateKey = null;
		X509Certificate x509Cert = null;

		ApkBuilder apkbuilder = new ApkBuilder(S.apkUnsigned, S.ap_Resources, S.dexClasses, privateKey, x509Cert,
				System.out);

		if (setProgress(65, R.string.addLibs)) {
			return;
		}
		apkbuilder.addNativeLibraries(S.dirLibs);

		int percent = 65;

		for (File jarLib : S.dirLibs.listFiles()) {

			// skip native libs in sub directories
			if (!jarLib.isFile() || !jarLib.getName().endsWith(".jar")) {
				continue;
			}
			apkbuilder.addResourcesFromJar(jarLib);

			if (setProgress(++percent)) {
				return;
			}
		}

		if (setProgress(75, R.string.zipAssets)) {
			return;
		}
		Util.zip(S.dirSrc, S.zipSrc);
		Util.zip(S.dirRes, S.zipRes);
		Util.zip(S.dirLibs, S.zipLibs);
		Util.zip(S.dirDexedLibs, S.zipDexedLibs);

		if (setProgress(80, R.string.addAssets)) {
			return;
		}
		String strAssets = S.dirAssets.getName() + File.separator;
		apkbuilder.addFile(S.xmlMan, strAssets + S.xmlMan.getName());
		apkbuilder.addFile(S.zipSrc, strAssets + S.zipSrc.getName());
		apkbuilder.addFile(S.zipRes, strAssets + S.zipRes.getName());
		apkbuilder.addFile(S.zipLibs, strAssets + S.zipLibs.getName());
		apkbuilder.addFile(S.zipDexedLibs, strAssets + S.zipDexedLibs.getName());

		apkbuilder.setDebugMode(true);
		apkbuilder.sealApk();
	}

	private void zipSign() throws Exception {
		if (!appContext.getString(R.string.keystore).contentEquals(S.jksEmbedded.getName())) {
			// TODO use user defined certificate
		}

		// use embedded private key
		String keystorePath = S.jksEmbedded.getPath();
		char[] keystorePw = appContext.getString(R.string.keystorePw).toCharArray();
		String certAlias = appContext.getString(R.string.certAlias);
		char[] certPw = appContext.getString(R.string.certPw).toCharArray();
		String signatureAlgorithm = appContext.getString(R.string.signatureAlgorithm);

		kellinwood.security.zipsigner.ZipSigner zipsigner = new kellinwood.security.zipsigner.ZipSigner();
		zipsigner.addProgressListener(new SignProgress());
		kellinwood.security.zipsigner.optional.CustomKeySigner.signZip(zipsigner, keystorePath, keystorePw, certAlias,
				certPw, signatureAlgorithm, S.apkUnsigned.getPath(), S.apkUnaligned.getPath());
	}

	private void zipAlign() throws Exception {
		// TODO make zipalign.so
	}

	private void publishApk() throws Exception {
		String strAppName = appContext.getString(R.string.appName);

		File apkCopy = new File(S.dirRoot, strAppName + ".apk");
		if (apkCopy.exists()) {
			apkCopy.delete();
		}
		Util.copy(S.apkUnaligned, new FileOutputStream(apkCopy));

		if (setProgress(100)) {
			return;
		}
		S.apkRedistributable = Uri.fromFile(apkCopy);
	}

	public void run() {

		try {
			if (setProgress(1, R.string.runAidl)) {
				return;
			}
			runAidl();

			if (setProgress(5, R.string.runAapt)) {
				return;
			}
			runAapt();

			if (setProgress(15, R.string.compileJava)) {
				return;
			}
			compileJava();

			if (setProgress(20, R.string.dexLibs)) {
				return;
			}
			dexLibs();

			if (setProgress(30, R.string.dexClasses)) {
				return;
			}
			dexClasses();

			if (setProgress(40)) {
				return;
			}
			dexMerge();

			if (setProgress(60, R.string.buildApk)) {
				return;
			}
			buildApk();

			if (setProgress(85, R.string.zipSign)) {
				return;
			}
			zipSign();

			if (setProgress(90, R.string.zipAlign)) {
				return;
			}
			zipAlign();

			if (setProgress(95, R.string.publishApk)) {
				return;
			}
			publishApk();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return;
	}

	class CompileProgress extends org.eclipse.jdt.core.compiler.CompilationProgress {

		@Override
		public void begin(int remainingWork) {
		}

		@Override
		public void done() {
		}

		@Override
		public boolean isCanceled() {
			return false;
		}

		@Override
		public void setTaskName(String name) {
		}

		@Override
		public void worked(int workIncrement, int remainingWork) {
		}

	}

	class SignProgress implements kellinwood.security.zipsigner.ProgressListener {

		public void onProgress(kellinwood.security.zipsigner.ProgressEvent event) {
		}

	}
}