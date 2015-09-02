package nl.tudelft.selfcompileapp;

import java.io.File;
import java.io.FileOutputStream;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import com.android.dex.Dex;
import com.android.dx.merge.CollisionPolicy;
import com.android.dx.merge.DexMerger;
import com.android.sdklib.build.ApkBuilder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.AsyncTask;
import kellinwood.security.zipsigner.ProgressEvent;
import kellinwood.security.zipsigner.ZipSigner;
import nl.tudelft.selfcompileapp.SelfCompileActivity.TaskManagerFragment;

public class BuildTask extends AsyncTask<Object, Object, Object> {

	TaskManagerFragment mgr;
	Context context;
	AssetManager asset;

	public BuildTask(TaskManagerFragment mgr) {
		this.mgr = mgr;
		this.context = mgr.getActivity();
		this.asset = context.getAssets();
	}

	@Override
	protected void onProgressUpdate(Object... values) {
		if (values.length > 0) {
			mgr.intProgress = (Integer) values[0];
		}
		if (values.length > 1) {
			mgr.strStatus = (String) values[1];
		}
		mgr.handler.sendEmptyMessage(Activity.RESULT_FIRST_USER);
	}

	@Override
	protected void onPreExecute() {
		publishProgress(0, "");
	}

	@Override
	protected void onPostExecute(Object result) {
		mgr.handler.sendEmptyMessage(Activity.RESULT_OK);
	}

	@Override
	protected void onCancelled() {
		mgr.handler.sendEmptyMessage(Activity.RESULT_CANCELED);
	}

	boolean setProgress(Object... values) {
		publishProgress(values);
		return isCancelled();
	}

	@Override
	protected Object doInBackground(Object... params) {

		try {
			if (setProgress(1, "PROCESS INTERFACES")) {
				return null;
			}
			// TODO make aidl.so

			// DELETE UNSUPPORTED RESOURCES // TODO update aapt.so
			Util.deleteRecursive(new File(S.dirRes, "drawable-xxhdpi"));

			if (setProgress(5, "PROCESS RESOURCES")) {
				return null;
			}
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

			if (setProgress(15, "COMPILE SOURCE")) {
				return null;
			}
			org.eclipse.jdt.core.compiler.batch.BatchCompiler.compile(
					new String[] { "-1.5", "-showversion", "-verbose", "-deprecation", "-bootclasspath",
							S.getJavaBootClassPath(), "-cp", S.getJavaClassPath(), "-d", S.dirClasses.getPath(),
							S.dirGen.getPath(), S.dirSrc.getPath() },
					new java.io.PrintWriter(System.out), new java.io.PrintWriter(System.err), new CompileProgress());

			if (setProgress(25, "PROCESS DEPENDENCIES")) {
				return null;
			}
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
			}

			if (setProgress(40, "INTEGRATE DEPENDENCIES")) {
				return null;
			}
			// dex project classes
			com.android.dx.command.dexer.Main
					.main(new String[] { "--verbose", "--output=" + S.dexClasses.getPath(), S.dirClasses.getPath() });

			// merge pre-dexed libs
			for (File dexLib : S.dirDexedLibs.listFiles()) {
				Dex merged = new DexMerger(new Dex(S.dexClasses), new Dex(dexLib), CollisionPolicy.FAIL).merge();
				merged.writeTo(S.dexClasses);
			}

			if (setProgress(55, "PACKAGE APP")) {
				return null;
			}
			// Do NOT use embedded JarSigner
			PrivateKey privateKey = null;
			X509Certificate x509Cert = null;

			ApkBuilder apkbuilder = new ApkBuilder(S.apkUnsigned, S.ap_Resources, S.dexClasses, privateKey, x509Cert,
					System.out);

			if (setProgress(60, "PACKAGE DEPENDENCIES")) {
				return null;
			}
			apkbuilder.addNativeLibraries(S.dirLibs);

			if (setProgress(65, "PACKAGE RESOURCES")) {
				return null;
			}
			for (File jarLib : S.dirLibs.listFiles()) {

				// skip native libs in sub directories
				if (!jarLib.isFile() || !jarLib.getName().endsWith(".jar")) {
					continue;
				}
				apkbuilder.addResourcesFromJar(jarLib);
			}

			if (setProgress(70, "COMPRESSING RESOURCES")) {
				return null;
			}
			Util.zip(S.dirSrc, S.zipSrc);
			Util.zip(S.dirRes, S.zipRes);
			Util.zip(S.dirLibs, S.zipLibs);
			Util.zip(S.dirDexedLibs, S.zipDexedLibs);

			if (setProgress(75, "PACKAGE RESOURCES")) {
				return null;
			}
			String strAssets = S.dirAssets.getName() + File.separator;
			apkbuilder.addFile(S.xmlMan, strAssets + S.xmlMan.getName());
			apkbuilder.addFile(S.zipSrc, strAssets + S.zipSrc.getName());
			apkbuilder.addFile(S.zipRes, strAssets + S.zipRes.getName());
			apkbuilder.addFile(S.zipLibs, strAssets + S.zipLibs.getName());
			apkbuilder.addFile(S.zipDexedLibs, strAssets + S.zipDexedLibs.getName());

			apkbuilder.setDebugMode(true);
			apkbuilder.sealApk();

			if (setProgress(80, "PLACE SIGNATURE")) {
				return null;
			}
			if (!context.getString(R.string.keystore).contentEquals(S.jksEmbedded.getName())) {
				// TODO use user defined certificate
			}
			// use embedded private key
			String keystorePath = S.jksEmbedded.getPath();
			char[] keystorePw = context.getString(R.string.keystorePw).toCharArray();
			String certAlias = context.getString(R.string.certAlias);
			char[] certPw = context.getString(R.string.certPw).toCharArray();
			String signatureAlgorithm = context.getString(R.string.signatureAlgorithm);

			ZipSigner zipsigner = new ZipSigner();
			zipsigner.addProgressListener(new SignProgress());
			kellinwood.security.zipsigner.optional.CustomKeySigner.signZip(zipsigner, keystorePath, keystorePw,
					certAlias, certPw, signatureAlgorithm, S.apkUnsigned.getPath(), S.apkUnaligned.getPath());

			if (setProgress(85, "OPTIMIZE APP")) {
				return null;
			}
			// TODO make zipalign.so

			if (setProgress(90, "PREPARE INSTALLATION")) {
				return null;
			}
			String strAppName = context.getString(R.string.appName);
			File apkCopy = new File(S.dirRoot, strAppName + ".apk");
			if (apkCopy.exists()) {
				apkCopy.delete();
			}
			Util.copy(S.apkUnaligned, new FileOutputStream(apkCopy));
			Uri uriApk = Uri.fromFile(apkCopy);

			if (setProgress(99, "LAUNCH INSTALLATION")) {
				return null;
			}
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setDataAndType(uriApk, "application/vnd.android.package-archive");
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(i);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
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

		public void onProgress(ProgressEvent arg0) {
			// TODO Auto-generated method stub
		}

	}
}