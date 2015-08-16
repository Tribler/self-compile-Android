package nl.tudelft.selfcompileapp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import com.android.dex.Dex;
import com.android.dx.merge.CollisionPolicy;
import com.android.dx.merge.DexMerger;
import com.android.sdklib.build.ApkBuilder;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

	private final String target_platform = "android-18";
	private final String proj_name = "SelfCompileApp";
	private final String[] proj_libs = { "kellinwood-logging-lib-1.1.jar",
			"zipio-lib-1.8.jar", "zipsigner-lib-1.17.jar", "sdklib-24.3.3.jar",
			"dx-22.0.1.jar", "ecj-4.3.2.jar" };

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

	public void dexlibs(View btnDexLibs) {
		btnDexLibs.setEnabled(false);
		new DexLibs().execute();
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
				File dirRoot = Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
				File dirProj = new File(dirRoot, proj_name);
				File dirAssets = new File(dirProj, "assets");
				File dirGen = new File(dirProj, "gen");
				File dirBin = new File(dirProj, "bin");
				File dirClasses = new File(dirBin, "classes");
				File dirDexedLibs = new File(dirBin, "dexedLibs");
				File dirDist = new File(dirProj, "dist");

				System.out.println("// EXTRACT ANDROID PLATFORM");
				File jarAndroid = new File(dirRoot, target_platform + ".jar");
				if (!jarAndroid.exists()) {

					InputStream zipAndroidPlatform = getAssets().open(
							target_platform + ".jar.zip");
					Util.unZip(zipAndroidPlatform, dirRoot);
				}

				System.out.println("// DELETE PROJECT FOLDER");
				Util.deleteRecursive(dirProj);

				// DEBUG
				Util.listRecursive(dirRoot);

				System.out.println("// EXTRACT PROJECT");
				InputStream zipProjSrc = getAssets().open(proj_name + ".zip");
				Util.unZip(zipProjSrc, dirProj);

				System.out.println("// CREATE BUILD FOLDERS");
				dirAssets.mkdirs();
				dirGen.mkdirs();
				dirClasses.mkdirs();
				dirDexedLibs.mkdirs();
				dirDist.mkdirs();

				System.out.println("// COPY ASSETS");
				zipProjSrc = getAssets().open(proj_name + ".zip");
				File zipCopy = new File(dirAssets, proj_name + ".zip");
				Util.copyFile(zipProjSrc, new FileOutputStream(zipCopy));

				InputStream zipAndroidPlatform = getAssets().open(
						target_platform + ".jar.zip");
				zipCopy = new File(dirAssets, target_platform + ".jar.zip");
				Util.copyFile(zipAndroidPlatform, new FileOutputStream(zipCopy));

				// DEBUG
				Util.listRecursive(dirProj);

			} catch (Exception e) {
				// TODO Auto-generated catch block
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
			System.out.println("// RUN AIDL"); // TODO

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
			File dirRoot = Environment
					.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
			File dirProj = new File(dirRoot, proj_name);
			File dirAssets = new File(dirProj, "assets");
			File dirGen = new File(dirProj, "gen");
			File dirRes = new File(dirProj, "res");
			File dirBin = new File(dirProj, "bin");
			// File dirBinRes = new File(dirBin, "res");
			// File dirCrunch = new File(dirBinRes, "crunch");
			// File xmlBinMan = new File(dirBin, "AndroidManifest.xml");
			File xmlMan = new File(dirProj, "AndroidManifest.xml");
			File jarAndroid = new File(dirRoot, target_platform + ".jar");
			File ap_Resources = new File(dirBin, "resources.ap_");

			System.out.println("// DELETE xxhdpi FOLDER"); // TODO update aapt
			Util.deleteRecursive(new File(dirRes, "drawable-xxhdpi"));

			Aapt aapt = new Aapt();
			int exitCode;

			System.out.println("// RUN AAPT & CREATE R.JAVA");
			exitCode = aapt.fnExecute("aapt p -f -v -M " + xmlMan.getPath()
					+ " -F " + ap_Resources.getPath() + " -I "
					+ jarAndroid.getPath() + " -A " + dirAssets.getPath()
					+ " -S " + dirRes.getPath() + " -J " + dirGen.getPath());
			System.out.println(exitCode);

			// System.out.println("// CREATE R.JAVA");
			// exitCode = aapt.fnExecute("aapt p -m -v -J " + dirGen.getPath()
			// + " -M " + xmlMan.getPath() + " -S " + dirRes.getPath()
			// + " -I ");
			// System.out.println(exitCode);
			//
			// System.out.println("// CRUNCH PNG");
			// exitCode = aapt.fnExecute("aapt c -v -S " + dirRes.getPath()
			// + " -C " + dirCrunch.getPath());
			// System.out.println(exitCode);
			//
			// System.out.println("// RUN AAPT");
			// exitCode = aapt
			// .fnExecute("aapt p -v -S "
			// + dirCrunch.getPath()
			// + " -S "
			// + dirRes.getPath()
			// + " -f --no-crunch --auto-add-overlay --debug-mode -0 apk -M "
			// + xmlBinMan.getPath() + " -A "
			// + dirAssets.getPath() + " -I "
			// + jarAndroid.getPath() + " -F "
			// + ap_Resources.getPath());
			// System.out.println(exitCode);

			// DEBUG
			Util.listRecursive(dirProj);

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
			File dirRoot = Environment
					.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
			File dirProj = new File(dirRoot, proj_name);
			File dirSrc = new File(dirProj, "src");
			File dirGen = new File(dirProj, "gen");
			File dirLibs = new File(dirProj, "libs");
			File dirBin = new File(dirProj, "bin");
			File dirClasses = new File(dirBin, "classes");
			File jarAndroid = new File(dirRoot, target_platform + ".jar");

			String strBootCP = jarAndroid.getPath();
			String strClassPath = dirSrc.getPath() + File.pathSeparator
					+ dirGen.getPath();
			for (String lib : proj_libs) {
				strClassPath += File.pathSeparator
						+ new File(dirLibs, lib).getPath();
			}

			System.out.println("// COMPILE SOURCE RECURSIVE");
			org.eclipse.jdt.core.compiler.batch.BatchCompiler.compile(
					"-1.5 -showversion -verbose -deprecation -bootclasspath "
							+ strBootCP + " -cp " + strClassPath + " -d "
							+ dirClasses.getPath() + " " + dirGen.getPath()
							+ " " + dirSrc.getPath(), new PrintWriter(
							System.out), new PrintWriter(System.err), null);

			// DEBUG
			Util.listRecursive(dirGen);
			Util.listRecursive(dirBin);

			return null;
		}

	}

	private class DexLibs extends AsyncTask<Object, Object, Object> {

		@Override
		protected void onPostExecute(Object result) {
			Button btnDexLibs = (Button) findViewById(R.id.btnDexLibs);
			btnDexLibs.setEnabled(true);
		}

		@Override
		protected Object doInBackground(Object... params) {
			try {
				File dirRoot = Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
				File dirProj = new File(dirRoot, proj_name);
				File dirLibs = new File(dirProj, "libs");
				File dirBin = new File(dirProj, "bin");
				File dirDexedLibs = new File(dirBin, "dexedLibs");

				System.out.println("// PRE-DEX LIBS");
				for (String lib : proj_libs) {
					File jarLib = new File(dirLibs, lib);
					File dexLib = new File(dirDexedLibs, lib + ".dex");

					if (!dexLib.exists()) {
						com.android.dx.command.dexer.Main.main(new String[] {
								"--verbose", "--output=" + dexLib.getPath(),
								jarLib.getPath() });
					}
				}

				// DEBUG
				Util.listRecursive(dirBin);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
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
				File dirRoot = Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
				File dirProj = new File(dirRoot, proj_name);
				File dirBin = new File(dirProj, "bin");
				File dirClasses = new File(dirBin, "classes");
				File dirDexedLibs = new File(dirBin, "dexedLibs");
				File dexClasses = new File(dirBin, "classes.dex");

				System.out.println("// DEX CLASSES");
				com.android.dx.command.dexer.Main.main(new String[] {
						"--verbose", "--output=" + dexClasses.getPath(),
						dirClasses.getPath() });

				System.out.println("// MERGE DEXED LIBS");
				for (String lib : proj_libs) {
					File dexLib = new File(dirDexedLibs, lib + ".dex");
					Dex merged = new DexMerger(new Dex(dexClasses), new Dex(
							dexLib), CollisionPolicy.FAIL).merge();
					merged.writeTo(dexClasses);
				}

				// DEBUG
				Util.listRecursive(dirBin);

			} catch (Exception e) {
				// TODO Auto-generated catch block
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
				File dirRoot = Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
				File dirProj = new File(dirRoot, proj_name);
				File dirSrc = new File(dirProj, "src");
				File dirLibs = new File(dirProj, "libs");
				File dirBin = new File(dirProj, "bin");
				File dirDist = new File(dirProj, "dist");
				File apkUnsigned = new File(dirDist, proj_name
						+ ".unsigned.apk");
				File ap_Resources = new File(dirBin, "resources.ap_");
				File dexClasses = new File(dirBin, "classes.dex");

				// Do NOT use embedded JarSigner
				PrivateKey privateKey = null;
				X509Certificate x509Cert = null;

				System.out.println("// RUN APK BUILDER");
				ApkBuilder apkbuilder = new ApkBuilder(apkUnsigned,
						ap_Resources, dexClasses, privateKey, x509Cert,
						System.out);

				System.out.println("// ADD SOURCE FOLDER");
				apkbuilder.addSourceFolder(dirSrc);

				System.out.println("// ADD NATIVE LIBS");
				apkbuilder.addNativeLibraries(dirLibs);

				apkbuilder.setDebugMode(true);
				apkbuilder.sealApk();

				// DEBUG
				Util.listRecursive(dirDist);

			} catch (Exception e) {
				// TODO Auto-generated catch block
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
				File dirRoot = Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
				File dirProj = new File(dirRoot, proj_name);
				File dirDist = new File(dirProj, "dist");
				File apkUnsigned = new File(dirDist, proj_name
						+ ".unsigned.apk");
				File apkSigned = new File(dirDist, proj_name + ".unaligned.apk");

				System.out.println("// RUN ZIP SIGNER");
				kellinwood.security.zipsigner.ZipSigner zipsigner = new kellinwood.security.zipsigner.ZipSigner();

				zipsigner.setKeymode("testkey"); // TODO

				zipsigner.signZip(apkUnsigned.getPath(), apkSigned.getPath());

				// DEBUG
				Util.listRecursive(dirDist);

			} catch (Exception e) {
				// TODO Auto-generated catch block
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
				File dirRoot = Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
				File dirProj = new File(dirRoot, proj_name);
				File dirDist = new File(dirProj, "dist");

				System.out.println("// RUN ZIP ALIGN"); // TODO

				// DEBUG
				Util.listRecursive(dirDist);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

	}

	private class InstallApk extends AsyncTask<Object, Object, Object> {

		@Override
		protected void onPostExecute(Object result) {
			Button btnInstall = (Button) findViewById(R.id.btnInstall);
			btnInstall.setEnabled(true);
		}

		@Override
		protected Object doInBackground(Object... params) {
			try {
				File dirRoot = Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
				File dirProj = new File(dirRoot, proj_name);
				File dirDist = new File(dirProj, "dist");
				File apkSigned = new File(dirDist, proj_name + ".unaligned.apk");

				System.out.println("// INSTALL APK");
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.fromFile(apkSigned),
						"application/vnd.android.package-archive");
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

	}
}
