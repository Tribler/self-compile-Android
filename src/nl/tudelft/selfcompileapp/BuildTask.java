package nl.tudelft.selfcompileapp;

import java.io.File;
import java.io.FileOutputStream;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import com.android.dex.Dex;
import com.android.dx.merge.CollisionPolicy;
import com.android.dx.merge.DexMerger;
import com.android.sdklib.build.ApkBuilder;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import kellinwood.security.zipsigner.ProgressEvent;
import kellinwood.security.zipsigner.ZipSigner;
import nl.tudelft.selfcompileapp.SelfCompileActivity.TaskManagerFragment;

public class BuildTask extends ProgressTask {

	public BuildTask(TaskManagerFragment mgr, Runnable done) {
		super(mgr, done);
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		dialog.setMax(100);
		dialog.setTitle("Working...");
		dialog.setMessage("");
	}

	@Override
	protected Object doInBackground(Object... params) {

		try {
			publishProgress(0, "PROCESS INTERFACES"); // TODO make aidl.so

			// DELETE UNSUPPORTED RESOURCES // TODO update aapt.so
			Util.deleteRecursive(new File(S.dirRes, "drawable-xxhdpi"));

			publishProgress(5, "PROCESS RESOURCES");

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

			publishProgress(15, "COMPILE SOURCE");

			org.eclipse.jdt.core.compiler.batch.BatchCompiler.compile(
					new String[] { "-1.5", "-showversion", "-verbose", "-deprecation", "-bootclasspath",
							S.getJavaBootClassPath(), "-cp", S.getJavaClassPath(), "-d", S.dirClasses.getPath(),
							S.dirGen.getPath(), S.dirSrc.getPath() },
					new java.io.PrintWriter(System.out), new java.io.PrintWriter(System.err), new CompileProgress());

			publishProgress(25, "PROCESS DEPENDENCIES");

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

			publishProgress(40, "INTEGRATE DEPENDENCIES");

			// dex project classes
			com.android.dx.command.dexer.Main
					.main(new String[] { "--verbose", "--output=" + S.dexClasses.getPath(), S.dirClasses.getPath() });

			// merge pre-dexed libs
			for (File dexLib : S.dirDexedLibs.listFiles()) {
				Dex merged = new DexMerger(new Dex(S.dexClasses), new Dex(dexLib), CollisionPolicy.FAIL).merge();
				merged.writeTo(S.dexClasses);
			}

			publishProgress(55, "PACKAGE APP");

			// Do NOT use embedded JarSigner
			PrivateKey privateKey = null;
			X509Certificate x509Cert = null;

			ApkBuilder apkbuilder = new ApkBuilder(S.apkUnsigned, S.ap_Resources, S.dexClasses, privateKey, x509Cert,
					System.out);

			publishProgress(60, "PACKAGE DEPENDENCIES");

			apkbuilder.addNativeLibraries(S.dirLibs);

			publishProgress(65, "PACKAGE RESOURCES");

			for (File jarLib : S.dirLibs.listFiles()) {

				// skip native libs in sub directories
				if (!jarLib.isFile() || !jarLib.getName().endsWith(".jar")) {
					continue;
				}
				apkbuilder.addResourcesFromJar(jarLib);
			}

			publishProgress(70, "COMPRESSING RESOURCES");

			Util.zip(S.dirSrc, S.zipSrc);
			Util.zip(S.dirRes, S.zipRes);
			Util.zip(S.dirLibs, S.zipLibs);
			Util.zip(S.dirDexedLibs, S.zipDexedLibs);

			publishProgress(75, "PACKAGE RESOURCES");

			String strAssets = S.dirAssets.getName() + File.separator;
			apkbuilder.addFile(S.xmlMan, strAssets + S.xmlMan.getName());
			apkbuilder.addFile(S.zipSrc, strAssets + S.zipSrc.getName());
			apkbuilder.addFile(S.zipRes, strAssets + S.zipRes.getName());
			apkbuilder.addFile(S.zipLibs, strAssets + S.zipLibs.getName());
			apkbuilder.addFile(S.zipDexedLibs, strAssets + S.zipDexedLibs.getName());

			apkbuilder.setDebugMode(true);
			apkbuilder.sealApk();

			publishProgress(80, "PLACE SIGNATURE");

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

			publishProgress(85, "OPTIMIZE APP"); // TODO make zipalign.so

			publishProgress(90, "PREPARE INSTALLATION");

			String strAppName = context.getString(R.string.appName);
			File apkCopy = new File(S.dirRoot, strAppName + ".apk");
			if (apkCopy.exists()) {
				apkCopy.delete();
			}
			Util.copy(S.apkUnaligned, new FileOutputStream(apkCopy));
			Uri uriApk = Uri.fromFile(apkCopy);

			publishProgress(99, "LAUNCH INSTALLATION");

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

	class SignProgress implements kellinwood.security.zipsigner.ProgressListener {

		public void onProgress(ProgressEvent arg0) {
			// TODO Auto-generated method stub
		}

	}
}