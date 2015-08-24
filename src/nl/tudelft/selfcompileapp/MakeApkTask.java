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
import android.content.Intent;
import android.net.Uri;
import kellinwood.security.zipsigner.ProgressEvent;
import kellinwood.security.zipsigner.ZipSigner;

public class MakeApkTask extends ProgressStatusTask {

	public MakeApkTask(Context app) {
		super(app, 14);
	}

	@Override
	protected Intent doInBackground(Object... params) {
		try {
			if (setMsg("PROCESS INTERFACES")) // TODO make aidl.so
				return null;

			// DELETE UNSUPPORTED RESOURCES // TODO update aapt.so
			Util.deleteRecursive(new File(S.dirRes, "drawable-xxhdpi"));

			if (setMsg("PROCESS RESOURCES"))
				return null;
			Aapt aapt = new Aapt();
			int exitCode = aapt.fnExecute("aapt p -f -v -M " + S.xmlMan.getPath() + " -F " + S.ap_Resources.getPath()
					+ " -I " + S.jarAndroid.getPath() + " -A " + S.dirAssets.getPath() + " -S " + S.dirRes.getPath()
					+ " -J " + S.dirGen.getPath());

			if (exitCode != 0) {
				throw new Exception("AAPT exit(" + exitCode + ")");
			}
			/*
			 * strStatus = "INDEXING RESOURCES"; exitCode = aapt.fnExecute(
			 * "aapt p -m -v -J " + dirGen.getPath() + " -M " + xmlMan.getPath()
			 * + " -S " + dirRes.getPath() + " -I " + jarAndroid.getPath());
			 * 
			 * strStatus = "CRUNCH RESOURCES"; exitCode = aapt.fnExecute(
			 * "aapt c -v -S " + dirRes.getPath() + " -C " +
			 * dirCrunch.getPath());
			 * 
			 * strStatus = "PACKAGE RESOURCES"; exitCode = aapt .fnExecute(
			 * "aapt p -v -S " + dirCrunch.getPath() + " -S " + dirRes.getPath()
			 * + " -f --no-crunch --auto-add-overlay --debug-mode -0 apk -M " +
			 * xmlBinMan.getPath() + " -A " + dirAssets.getPath() + " -I " +
			 * jarAndroid.getPath() + " -F " + ap_Resources.getPath());
			 */

			if (setMsg("COMPILE SOURCE"))
				return null;
			org.eclipse.jdt.core.compiler.batch.BatchCompiler.compile(
					new String[] { "-1.5", "-showversion", "-verbose", "-deprecation", "-bootclasspath",
							S.getJavaBootClassPath(), "-cp", S.getJavaClassPath(), "-d", S.dirClasses.getPath(),
							S.dirGen.getPath(), S.dirSrc.getPath() },
					new java.io.PrintWriter(System.out), new java.io.PrintWriter(System.err), new CompileProgress());

			if (setMsg("PROCESS DEPENDENCIES"))
				return null;
			boolean changedLibs = false;
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
					changedLibs = true;
				}
			}

			// dex project classes
			com.android.dx.command.dexer.Main
					.main(new String[] { "--verbose", "--output=" + S.dexClasses.getPath(), S.dirClasses.getPath() });

			if (setMsg("INTEGRATE DEPENDENCIES"))
				return null;
			if (changedLibs || !S.dexLibs.exists()) {
				// re-merge dexed libs
				if (S.dexLibs.exists()) {
					S.dexLibs.delete();
				}
				for (File dexLib : S.dirDexedLibs.listFiles()) {
					// pre-merge dexed lib into libs.dex
					Dex merged = new DexMerger(new Dex(S.dexLibs), new Dex(dexLib), CollisionPolicy.FAIL).merge();
					merged.writeTo(S.dexLibs);
				}
			}
			// use pre-merged libs
			Dex merged = new DexMerger(new Dex(S.dexClasses), new Dex(S.dexLibs), CollisionPolicy.FAIL).merge();
			merged.writeTo(S.dexClasses);

			// Do NOT use embedded JarSigner
			PrivateKey privateKey = null;
			X509Certificate x509Cert = null;

			if (setMsg("PACKAGE APP"))
				return null;
			ApkBuilder apkbuilder = new ApkBuilder(S.apkUnsigned, S.ap_Resources, S.dexClasses, privateKey, x509Cert,
					System.out);

			if (setMsg("PACKAGE DEPENDENCIES"))
				return null;
			apkbuilder.addNativeLibraries(S.dirLibs);

			if (setMsg("PACKAGE RESOURCES"))
				return null;
			for (File jarLib : S.dirLibs.listFiles()) {

				// skip native libs in sub directories
				if (!jarLib.isFile() || !jarLib.getName().endsWith(".jar")) {
					continue;
				}
				apkbuilder.addResourcesFromJar(jarLib);
			}

			if (setMsg("COMPRESSING RESOURCES"))
				return null;
			Util.zip(S.dirSrc, S.zipSrc);
			Util.zip(S.dirRes, S.zipRes);
			Util.zip(S.dirLibs, S.zipLibs);
			Util.zip(S.dirDexedLibs, S.zipDexedLibs);

			if (setMsg("PACKAGE RESOURCES"))
				return null;
			String strAssets = S.dirAssets.getName() + File.separator;
			apkbuilder.addFile(S.xmlMan, strAssets + S.xmlMan.getName());
			apkbuilder.addFile(S.zipSrc, strAssets + S.zipSrc.getName());
			apkbuilder.addFile(S.zipRes, strAssets + S.zipRes.getName());
			apkbuilder.addFile(S.zipLibs, strAssets + S.zipLibs.getName());
			apkbuilder.addFile(S.zipDexedLibs, strAssets + S.zipDexedLibs.getName());

			apkbuilder.setDebugMode(true);
			apkbuilder.sealApk();

			if (setMsg("PLACE SIGNATURE"))
				return null;

			if (!app.getString(R.string.keystore).contentEquals(S.jksEmbedded.getName())) {
				// TODO use user defined certificate
			}
			// use embedded private key
			String keystorePath = S.jksEmbedded.getPath();
			char[] keystorePw = app.getString(R.string.keystorePw).toCharArray();
			String certAlias = app.getString(R.string.certAlias);
			char[] certPw = app.getString(R.string.certPw).toCharArray();
			String signatureAlgorithm = app.getString(R.string.signatureAlgorithm);

			ZipSigner zipsigner = new ZipSigner();
			zipsigner.addProgressListener(new SignProgress());
			kellinwood.security.zipsigner.optional.CustomKeySigner.signZip(zipsigner, keystorePath, keystorePw,
					certAlias, certPw, signatureAlgorithm, S.apkUnsigned.getPath(), S.apkUnaligned.getPath());

			if (setMsg("OPTIMIZE APP")) // TODO make zipalign.so
				return null;

			if (setMsg("PREPARE INSTALLATION"))
				return null;
			String strAppName = app.getString(R.string.appName);
			File apkCopy = new File(S.dirRoot, strAppName + ".apk");
			if (apkCopy.exists()) {
				apkCopy.delete();
			}
			Util.copy(S.apkUnaligned, new FileOutputStream(apkCopy));
			Uri uriApk = Uri.fromFile(apkCopy);

			if (setMsg("LAUNCH INSTALLATION"))
				return null;
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(uriApk, "application/vnd.android.package-archive");
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			app.startActivity(intent);

		} catch (Exception e) {

			e.printStackTrace();
		}
		return null;
	}

	private class CompileProgress extends org.eclipse.jdt.core.compiler.CompilationProgress {

		@Override
		public void begin(int arg0) {

		}

		@Override
		public void done() {

		}

		@Override
		public boolean isCanceled() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void setTaskName(String arg0) {

		}

		@Override
		public void worked(int arg0, int arg1) {

		}

	}

	private class SignProgress implements kellinwood.security.zipsigner.ProgressListener {

		public void onProgress(ProgressEvent arg0) {
			// TODO Auto-generated method stub
		}

	}

}