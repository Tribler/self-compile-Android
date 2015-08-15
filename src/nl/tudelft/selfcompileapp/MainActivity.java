package nl.tudelft.selfcompileapp;

import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

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
	private final String proj_name = "demo_android";
	private final String[] proj_libs = { "demolib.jar" };

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
				File dirDownloads = Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
				File dirProject = new File(dirDownloads, proj_name);

				System.out.println("// COPY ANDROID PLATFORM");
				File jarAndroid = new File(dirDownloads, target_platform
						+ ".jar");
				if (!jarAndroid.exists()) {

					InputStream zipAndroidPlatform = getAssets().open(
							target_platform + ".zip");
					Util.unZip(zipAndroidPlatform, dirDownloads);
				}

				System.out.println("// DELETE PROJECT FOLDER");
				Util.deleteRecursive(dirProject);

				// DEBUG
				Util.listRecursive(dirDownloads);

				System.out.println("// EXTRACT PROJECT");
				InputStream zipProjSrc = getAssets().open(proj_name + ".zip");
				Util.unZip(zipProjSrc, dirDownloads);

				// DEBUG
				Util.listRecursive(dirProject);

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
			// TODO

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
			File dirDownloads = Environment
					.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

			String strRoot = dirDownloads.getAbsolutePath() + File.separator;
			String strProj = strRoot + proj_name + File.separator;
			String strBuild = strProj + "build" + File.separator;
			String strRes = strProj + "res" + File.separator;
			String strGen = strProj + "gen" + File.separator;

			String strBoot = strRoot + target_platform + ".jar";
			String strMan = strProj + "AndroidManifest.xml";

			System.out.println("// RUN AAPT & CREATE R.JAVA");
			Aapt aapt = new Aapt();
			int exitCode = aapt.fnExecute("aapt p -f -v -M " + strMan + " -F "
					+ strBuild + "resources.res -I " + strBoot + " -S "
					+ strRes + " -J " + strGen);

			System.out.println(exitCode);

			// DEBUG
			Util.listRecursive(new File(strProj));

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
			File dirDownloads = Environment
					.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

			String strRoot = dirDownloads.getAbsolutePath() + File.separator;
			String strProj = strRoot + proj_name + File.separator;
			String strLibs = strProj + "libs" + File.separator;
			String strBuild = strProj + "build" + File.separator;
			String strClass = strBuild + "classes" + File.separator;
			String strGen = strProj + "gen" + File.separator;
			String strSrc = strProj + "src" + File.separator;

			String strBoot = strRoot + target_platform + ".jar";
			String strClassPath = strSrc + File.pathSeparator + strGen;
			for (String lib : proj_libs) {
				strClassPath += File.pathSeparator + strLibs + lib;
			}

			System.out.println("// COMPILE SOURCE RECURSIVE");
			org.eclipse.jdt.core.compiler.batch.BatchCompiler.compile(
					"-1.5 -showversion -verbose -deprecation -bootclasspath "
							+ strBoot + " -cp " + strClassPath + " -d "
							+ strClass + " " + strGen + " " + strSrc,
					new PrintWriter(System.out), new PrintWriter(System.err),
					null);

			// DEBUG
			Util.listRecursive(new File(strGen));
			Util.listRecursive(new File(strBuild));

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
				File dirDownloads = Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

				String strRoot = dirDownloads.getAbsolutePath()
						+ File.separator;
				String strProj = strRoot + proj_name + File.separator;
				String strLibs = strProj + "libs" + File.separator;
				String strBuild = strProj + "build" + File.separator;
				String strClass = strBuild + "classes" + File.separator;
				String strLibsDex = strBuild + "dexedLibs" + File.separator;

				System.out.println("// PRE DEX LIBS");
				for (String lib : proj_libs) {
					com.android.dx.command.dexer.Main.main(new String[] {
							"--verbose",
							"--output=" + strLibsDex + lib + ".dex",
							strLibs + lib });
				}

				System.out.println("// DEX CLASSES");
				ArrayList<String> lstDexArgs = new ArrayList<String>();
				lstDexArgs.add("--verbose");
				lstDexArgs.add("--output=" + strBuild + "classes.dex");
				lstDexArgs.add(strClass);
				for (String lib : proj_libs) {
					lstDexArgs.add(strLibs + lib);
				}
				String[] arrDexArgs = new String[lstDexArgs.size()];
				lstDexArgs.toArray(arrDexArgs);
				com.android.dx.command.dexer.Main.main(arrDexArgs);

				System.out.println("// MERGE DEX"); // TODO

				// DEBUG
				Util.listRecursive(new File(strBuild));

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
				File dirDownloads = Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

				String strRoot = dirDownloads.getAbsolutePath()
						+ File.separator;
				String strProj = strRoot + proj_name + File.separator;
				String strBuild = strProj + "build" + File.separator;
				String strDist = strProj + "dist" + File.separator;

				File apkFile = new File(strDist + proj_name + ".unsigned.apk");
				File resFile = new File(strBuild + "resources.res");
				File dexFile = new File(strBuild + "classes.dex");

				// Do NOT use embedded JarSigner
				PrivateKey privateKey = null;
				X509Certificate x509Cert = null;

				System.out.println("// RUN APK BUILDER");
				com.android.sdklib.build.ApkBuilder apkbuilder = new com.android.sdklib.build.ApkBuilder(
						apkFile, resFile, dexFile, privateKey, x509Cert,
						System.out);

				System.out.println("// ADD NATIVE LIBS"); // TODO

				apkbuilder.setDebugMode(true);
				apkbuilder.sealApk();

				// DEBUG
				Util.listRecursive(new File(strDist));

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
				File dirDownloads = Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

				String strRoot = dirDownloads.getAbsolutePath()
						+ File.separator;
				String strProj = strRoot + proj_name + File.separator;
				String strDist = strProj + "dist" + File.separator;

				System.out.println("// RUN ZIP SIGNER");
				kellinwood.security.zipsigner.ZipSigner zipsigner = new kellinwood.security.zipsigner.ZipSigner();

				zipsigner.setKeymode("testkey"); // TODO

				zipsigner.signZip(strDist + proj_name + ".unsigned.apk",
						strDist + proj_name + ".unaligned.apk");

				// DEBUG
				Util.listRecursive(new File(strDist));

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
				File dirDownloads = Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

				String strRoot = dirDownloads.getAbsolutePath()
						+ File.separator;
				String strProj = strRoot + proj_name + File.separator;
				String strDist = strProj + "dist" + File.separator;

				System.out.println("// RUN ZIP ALIGN"); // TODO

				// DEBUG
				Util.listRecursive(new File(strDist));

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
				File dirDownloads = Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

				String strRoot = dirDownloads.getAbsolutePath()
						+ File.separator;
				String strProj = strRoot + proj_name + File.separator;
				String strDist = strProj + "dist" + File.separator;

				File apkFile = new File(strDist + proj_name + ".unaligned.apk");

				System.out.println("// INSTALL APK");
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.fromFile(apkFile),
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
