package nl.tudelft.selfcompileapp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import kellinwood.security.zipsigner.ProgressEvent;
import kellinwood.security.zipsigner.ProgressListener;

import com.android.dex.Dex;
import com.android.dx.merge.CollisionPolicy;
import com.android.dx.merge.DexMerger;
import com.android.sdklib.build.ApkBuilder;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	public void clean(View btnClean) {
		btnClean.setEnabled(false);
		new CleanBuild().execute();
	}

	public void aidl(View btnAidl) {
		btnAidl.setEnabled(false);
		new ConvertAidl().execute();
	}

	public void aapt(View btnAapt) {
		btnAapt.setEnabled(false);
		new PackAssets().execute();
	}

	public void compile(View btnCompile) {
		btnCompile.setEnabled(false);
		new CompileJava().execute();
	}

	public void dex(View btnDex) {
		btnDex.setEnabled(false);
		new DexMerge().execute();
	}

	public void apk(View btnPack) {
		btnPack.setEnabled(false);
		new BuildApk().execute();
	}

	public void sign(View btnSign) {
		btnSign.setEnabled(false);
		new SignApk().execute();
	}

	public void align(View btnAlign) {
		btnAlign.setEnabled(false);
		new AlignApk().execute();
	}

	public void install(View btnInstall) {
		btnInstall.setEnabled(false);
		new InstallApk().execute();
	}

	private class CleanBuild extends AsyncTask<Object, Object, Object> {

		@Override
		protected void onPostExecute(Object result) {
			Button btnClean = (Button) findViewById(R.id.btnClean);
			btnClean.setEnabled(true);
		}

		@Override
		protected Object doInBackground(Object... params) {
			try {
				File dirRoot = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
				File dirProj = new File(dirRoot, getString(R.string.app_name));
				File dirSrc = new File(dirProj, "src");
				File dirRes = new File(dirProj, "res");
				File dirLibs = new File(dirProj, "libs");
				File dirAssets = new File(dirProj, "assets");
				File dirBin = new File(dirProj, "bin");
				File dirDexedLibs = new File(dirBin, "dexedLibs");

				File xmlMan = new File(dirProj, "AndroidManifest.xml");
				File jarAndroid = new File(dirAssets, getString(R.string.platform) + ".jar");

				System.out.println("// DELETE PROJECT FOLDER");
				Util.deleteRecursive(dirProj);

				System.out.println("// EXTRACT PROJECT");
				dirSrc.mkdirs();
				dirRes.mkdirs();
				dirLibs.mkdirs();
				dirAssets.mkdirs();
				dirBin.mkdirs();
				dirDexedLibs.mkdirs();

				Util.copy(getAssets().open(xmlMan.getName()), xmlMan);

				Util.copy(getAssets().open(jarAndroid.getName()), jarAndroid);

				InputStream zipSrc = getAssets().open("src.zip");
				Util.unzip(zipSrc, dirSrc);

				InputStream zipRes = getAssets().open("res.zip");
				Util.unzip(zipRes, dirRes);

				InputStream zipLibs = getAssets().open("libs.zip");
				Util.unzip(zipLibs, dirLibs);

				InputStream zipDexedLibs = getAssets().open("dexedLibs.zip");
				Util.unzip(zipDexedLibs, dirDexedLibs);

			} catch (Exception e) {

				e.printStackTrace();
			}
			return null;
		}

	}

	private class ConvertAidl extends AsyncTask<Object, Object, Object> {

		@Override
		protected void onPostExecute(Object result) {
			Button btnAidl = (Button) findViewById(R.id.btnAidl);
			btnAidl.setEnabled(true);
		}

		@Override
		protected Object doInBackground(Object... params) {
			try {
				System.out.println("// RUN AIDL"); // TODO

			} catch (Exception e) {

				e.printStackTrace();
			}
			return null;
		}
	}

	private class PackAssets extends AsyncTask<Object, Object, Object> {

		@Override
		protected void onPostExecute(Object result) {
			Button btnAapt = (Button) findViewById(R.id.btnAapt);
			btnAapt.setEnabled(true);
		}

		@Override
		protected Object doInBackground(Object... params) {
			try {
				File dirRoot = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
				File dirProj = new File(dirRoot, getString(R.string.app_name));
				File dirGen = new File(dirProj, "gen");
				File dirRes = new File(dirProj, "res");
				File dirAssets = new File(dirProj, "assets");
				File dirBin = new File(dirProj, "bin");

				// File dirBinRes = new File(dirBin, "res");
				// File dirCrunch = new File(dirBinRes, "crunch");

				// File xmlBinMan = new File(dirBin, "AndroidManifest.xml");
				File xmlMan = new File(dirProj, "AndroidManifest.xml");
				File jarAndroid = new File(dirAssets, getString(R.string.platform) + ".jar");
				File ap_Resources = new File(dirBin, "resources.ap_");

				dirGen.mkdirs();

				System.out.println("// DELETE xxhdpi"); // TODO update aapt
				Util.deleteRecursive(new File(dirRes, "drawable-xxhdpi"));

				Aapt aapt = new Aapt();
				int exitCode;

				System.out.println("// RUN AAPT & CREATE R.JAVA");
				exitCode = aapt.fnExecute("aapt p -f -v -M " + xmlMan.getPath() + " -F " + ap_Resources.getPath()
						+ " -I " + jarAndroid.getPath() + " -A " + dirAssets.getPath() + " -S " + dirRes.getPath()
						+ " -J " + dirGen.getPath());
				System.out.println(exitCode);

				// System.out.println("// CREATE R.JAVA");

				// C:\android-sdk\build-tools\23.0.0\aapt.exe package -m -v -J
				// E:\AndroidEclipseWorkspace\SelfCompileApp\gen -M
				// E:\AndroidEclipseWorkspace\SelfCompileApp\AndroidManifest.xml
				// -S E:\AndroidEclipseWorkspace\SelfCompileApp\res -I
				// C:\android-sdk\platforms\android-18\android.jar

				// exitCode = aapt.fnExecute("aapt p -m -v -J " +
				// dirGen.getPath()
				// + " -M " + xmlMan.getPath() + " -S " + dirRes.getPath()
				// + " -I ");
				// System.out.println(exitCode);
				//
				// System.out.println("// CRUNCH PNG");

				// C:\android-sdk\build-tools\23.0.0\aapt.exe crunch -v -S
				// E:\AndroidEclipseWorkspace\SelfCompileApp\res -C
				// E:\AndroidEclipseWorkspace\SelfCompileApp\bin\res\crunch

				// exitCode = aapt.fnExecute("aapt c -v -S " + dirRes.getPath()
				// + " -C " + dirCrunch.getPath());
				// System.out.println(exitCode);
				//

				// System.out.println("// RUN AAPT");

				// C:\android-sdk\build-tools\23.0.0\aapt.exe package -v -S
				// E:\AndroidEclipseWorkspace\SelfCompileApp\bin\res\crunch -S
				// E:\AndroidEclipseWorkspace\SelfCompileApp\res -f --no-crunch
				// --auto-add-overlay --debug-mode -0 apk -M
				// E:\AndroidEclipseWorkspace\SelfCompileApp\bin\AndroidManifest.xml
				// -A E:\AndroidEclipseWorkspace\SelfCompileApp\assets -I
				// C:\android-sdk\platforms\android-18\android.jar -F
				// E:\AndroidEclipseWorkspace\SelfCompileApp\bin\resources.ap_

				// exitCode = aapt
				// .fnExecute("aapt p -v -S "
				// + dirCrunch.getPath()
				// + " -S "
				// + dirRes.getPath()
				// +
				// " -f --no-crunch --auto-add-overlay --debug-mode -0 apk -M "
				// + xmlBinMan.getPath() + " -A "
				// + dirAssets.getPath() + " -I "
				// + jarAndroid.getPath() + " -F "
				// + ap_Resources.getPath());
				// System.out.println(exitCode);

			} catch (Exception e) {

				e.printStackTrace();
			}
			return null;
		}
	}

	private class CompileJava extends AsyncTask<Object, Object, Object> {

		@Override
		protected void onPostExecute(Object result) {
			Button btnCompile = (Button) findViewById(R.id.btnCompile);
			btnCompile.setEnabled(true);
		}

		@Override
		protected Object doInBackground(Object... params) {
			try {
				File dirRoot = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
				File dirProj = new File(dirRoot, getString(R.string.app_name));
				File dirSrc = new File(dirProj, "src");
				File dirGen = new File(dirProj, "gen");
				File dirLibs = new File(dirProj, "libs");
				File dirAssets = new File(dirProj, "assets");
				File dirBin = new File(dirProj, "bin");
				File dirClasses = new File(dirBin, "classes");

				File jarAndroid = new File(dirAssets, getString(R.string.platform) + ".jar");

				dirClasses.mkdirs();

				String strBootCP = jarAndroid.getPath();
				String strClassPath = "";

				for (File jarLib : dirLibs.listFiles()) {
					if (jarLib.isDirectory()) {
						continue;
					}
					strClassPath += File.pathSeparator + jarLib.getPath();
				}

				Locale.setDefault(Locale.ROOT);

				System.out.println("// COMPILE SOURCE RECURSIVE");
				org.eclipse.jdt.core.compiler.batch.BatchCompiler.compile(
						new String[] { "-1.5", "-showversion", "-verbose", "-deprecation", "-bootclasspath", strBootCP,
								"-cp", strClassPath, "-d", dirClasses.getPath(), dirGen.getPath(), dirSrc.getPath() },
						new PrintWriter(System.out), new PrintWriter(System.err), new CompileProgress());

			} catch (Exception e) {

				e.printStackTrace();
			}
			return null;
		}

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

	private class DexMerge extends AsyncTask<Object, Object, Object> {

		@Override
		protected void onPostExecute(Object result) {
			Button btnDex = (Button) findViewById(R.id.btnDex);
			btnDex.setEnabled(true);
		}

		@Override
		protected Object doInBackground(Object... params) {
			try {
				File dirRoot = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
				File dirProj = new File(dirRoot, getString(R.string.app_name));
				File dirLibs = new File(dirProj, "libs");
				File dirBin = new File(dirProj, "bin");
				File dirClasses = new File(dirBin, "classes");
				File dirDexedLibs = new File(dirBin, "dexedLibs");

				File dexClasses = new File(dirBin, "classes.dex");

				dirDexedLibs.mkdirs();

				System.out.println("// PRE-DEX LIBS");
				for (File jarLib : dirLibs.listFiles()) {

					// skip native libs
					if (jarLib.isDirectory()) {
						continue;
					}

					// check if jar is pre-dexed
					File dexLib = new File(dirDexedLibs, jarLib.getName());
					if (!dexLib.exists()) {

						// limit jar size to prevent dx memory overflow
						int split = Util.setMaxJarSize(jarLib);
						if (split > 1) {

							// dex all jar parts
							for (int i = 1; i <= split; i++) {

								// sequence name
								File jarLibPart = new File(jarLib.getParent(), i + "-" + jarLib.getName());
								File dexLibPart = new File(dirDexedLibs, jarLibPart.getName());

								com.android.dx.command.dexer.Main.main(new String[] { "--verbose",
										"--output=" + dexLibPart.getPath(), jarLibPart.getPath() });

								jarLibPart.delete();

								// merge
								if (i > 1) {
									Dex merged = new DexMerger(new Dex(dexLib), new Dex(dexLibPart),
											CollisionPolicy.FAIL).merge();
									merged.writeTo(dexLib);

									dexLibPart.delete();

								} else {
									// rename first dex part to original
									dexLibPart.renameTo(dexLib);
								}
							}
						} else {
							// dex single jar
							com.android.dx.command.dexer.Main.main(
									new String[] { "--verbose", "--output=" + dexLib.getPath(), jarLib.getPath() });
						}
					}
				}

				System.out.println("// DEX CLASSES");
				com.android.dx.command.dexer.Main
						.main(new String[] { "--verbose", "--output=" + dexClasses.getPath(), dirClasses.getPath() });

				System.out.println("// MERGE PRE-DEXED LIBS");
				for (File dexLib : dirDexedLibs.listFiles()) {
					Dex merged = new DexMerger(new Dex(dexClasses), new Dex(dexLib), CollisionPolicy.FAIL).merge();
					merged.writeTo(dexClasses);
				}

			} catch (Exception e) {

				e.printStackTrace();
			}
			return null;
		}

	}

	private class BuildApk extends AsyncTask<Object, Object, Object> {

		@Override
		protected void onPostExecute(Object result) {
			Button btnApk = (Button) findViewById(R.id.btnApk);
			btnApk.setEnabled(true);
		}

		@Override
		protected Object doInBackground(Object... params) {
			try {
				File dirRoot = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
				File dirProj = new File(dirRoot, getString(R.string.app_name));
				File dirSrc = new File(dirProj, "src");
				File dirRes = new File(dirProj, "res");
				File dirLibs = new File(dirProj, "libs");
				File dirAssets = new File(dirProj, "assets");
				File dirBin = new File(dirProj, "bin");
				File dirDexedLibs = new File(dirBin, "dexedLibs");
				File dirDist = new File(dirProj, "dist");

				File apkUnsigned = new File(dirDist, getString(R.string.app_name) + ".unsigned.apk");
				File ap_Resources = new File(dirBin, "resources.ap_");
				File dexClasses = new File(dirBin, "classes.dex");
				File xmlMan = new File(dirProj, "AndroidManifest.xml");
				File zipSrc = new File(dirAssets, "src.zip");
				File zipRes = new File(dirAssets, "res.zip");
				File zipLibs = new File(dirAssets, "libs.zip");
				File zipDexedLibs = new File(dirAssets, "dexedLibs.zip");

				dirDist.mkdirs();

				// Do NOT use embedded JarSigner
				PrivateKey privateKey = null;
				X509Certificate x509Cert = null;

				System.out.println("// RUN APK BUILDER");
				ApkBuilder apkbuilder = new ApkBuilder(apkUnsigned, ap_Resources, dexClasses, privateKey, x509Cert,
						System.out);

				System.out.println("// ADD NATIVE LIBS");
				apkbuilder.addNativeLibraries(dirLibs);

				System.out.println("// ADD LIB RESOURCES");
				for (File jarLib : dirLibs.listFiles()) {
					if (jarLib.isDirectory()) {
						continue;
					}
					apkbuilder.addResourcesFromJar(jarLib);
				}

				System.out.println("// ZIP & ADD ASSETS");
				// android.jar already packed by aapt

				String strAssets = "assets" + File.separator;
				apkbuilder.addFile(xmlMan, strAssets + xmlMan.getName());

				Util.zip(dirSrc, zipSrc);
				apkbuilder.addFile(zipSrc, strAssets + zipSrc.getName());

				Util.zip(dirRes, zipRes);
				apkbuilder.addFile(zipRes, strAssets + zipRes.getName());

				Util.zip(dirLibs, zipLibs);
				apkbuilder.addFile(zipLibs, strAssets + zipLibs.getName());

				Util.zip(dirDexedLibs, zipDexedLibs);
				apkbuilder.addFile(zipDexedLibs, strAssets + zipDexedLibs.getName());

				apkbuilder.setDebugMode(true);
				apkbuilder.sealApk();

			} catch (Exception e) {

				e.printStackTrace();
			}
			return null;
		}
	}

	private class SignApk extends AsyncTask<Object, Object, Object> {

		@Override
		protected void onPostExecute(Object result) {
			Button btnSign = (Button) findViewById(R.id.btnSign);
			btnSign.setEnabled(true);
		}

		@Override
		protected Object doInBackground(Object... params) {
			try {
				File dirRoot = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
				File dirProj = new File(dirRoot, getString(R.string.app_name));
				File dirAssets = new File(dirProj, "assets");
				File dirDist = new File(dirProj, "dist");

				File apkUnsigned = new File(dirDist, getString(R.string.app_name) + ".unsigned.apk");
				File apkSigned = new File(dirDist, getString(R.string.app_name) + ".unaligned.apk");
				File jksEmbedded = new File(dirAssets, getString(R.string.keystore));

				String keystorePath = jksEmbedded.getPath();
				char[] keystorePw = "android".toCharArray();
				String certAlias = "androiddebugkey";
				char[] certPw = "android".toCharArray();
				String signatureAlgorithm = "SHA1withRSA";

				System.out.println("// RUN ZIP SIGNER");
				kellinwood.security.zipsigner.ZipSigner zipsigner = new kellinwood.security.zipsigner.ZipSigner();

				zipsigner.addProgressListener(new ProgressListener() {
					public void onProgress(ProgressEvent event) {
						// TODO event.getPercentDone();
					}
				});
				kellinwood.security.zipsigner.optional.CustomKeySigner.signZip(zipsigner, keystorePath, keystorePw,
						certAlias, certPw, signatureAlgorithm, apkUnsigned.getPath(), apkSigned.getPath());

			} catch (Exception e) {

				e.printStackTrace();
			}
			return null;
		}

	}

	private class AlignApk extends AsyncTask<Object, Object, Object> {

		@Override
		protected void onPostExecute(Object result) {
			Button btnAlign = (Button) findViewById(R.id.btnAlign);
			btnAlign.setEnabled(true);
		}

		@Override
		protected Object doInBackground(Object... params) {
			try {
				System.out.println("// RUN ZIP ALIGN"); // TODO

			} catch (Exception e) {

				e.printStackTrace();
			}
			return null;
		}

	}

	private class InstallApk extends AsyncTask<Object, Object, Object> {
		Uri url;

		@Override
		protected void onPostExecute(Object result) {
			Button btnInstall = (Button) findViewById(R.id.btnInstall);
			btnInstall.setEnabled(true);

			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(url, "application/vnd.android.package-archive");
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
		}

		@Override
		protected Object doInBackground(Object... params) {
			try {
				File dirRoot = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
				File dirProj = new File(dirRoot, getString(R.string.app_name));
				File dirDist = new File(dirProj, "dist");

				File apk = new File(dirDist, getString(R.string.app_name) + ".unaligned.apk");
				File apkCopy = new File(dirRoot, getString(R.string.app_name) + ".unaligned.apk");
				if (apkCopy.exists()) {
					apkCopy.delete();
				}

				System.out.println("// INSTALL APK");
				Util.copy(apk, new FileOutputStream(apkCopy));
				url = Uri.fromFile(apkCopy);

			} catch (Exception e) {

				e.printStackTrace();
			}
			return null;
		}

	}
}
