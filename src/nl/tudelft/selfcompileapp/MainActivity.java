package nl.tudelft.selfcompileapp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

import android.app.Activity;
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
		new DexClasses().execute();
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

			} catch (IOException e) {
				// TODO Auto-generated catch block
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

	private class DexClasses extends AsyncTask<Object, Object, Object> {

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

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

	}
}
