package nl.tudelft.selfcompileapp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import org.eclipse.jdt.core.compiler.batch.BatchCompiler;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

	private final String proj_name = "demo_android";
	private final String target_platform = "android-22";

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
			File dirProject = new File(dirDownloads, proj_name);

			System.out.println("// REMOVE R.JAVA");
			File javaR = new File(dirProject, "/src/org/me/androiddemo/R.java");
			javaR.delete();

			System.out.println("// RUN AAPT & CREATE R.JAVA"); // TODO

			// aapt p -f -v -M AndroidManifest.xml -F ./build/resources.res -I
			// ~/system/classes/android.jar -S res/ -J src/org/me/androiddemo

			// DEBUG
			Util.listRecursive(dirProject);

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

			String strSrc = strProj + "src" + File.separator;
			String strGen = strProj + "gen" + File.separator;
			String strBoot = strRoot + target_platform + ".jar";
			String strClassPath = strSrc;
			strClassPath += File.pathSeparator + strGen;
			strClassPath += File.pathSeparator + strLibs + "demolib.jar";
			String strMain = strSrc + "org/me/androiddemo/MainActivity.java";

			System.out.println("// COMPILE SOURCE RECURSIVE");
			BatchCompiler.compile(
					"-1.5 -showversion -verbose -deprecation -bootclasspath "
							+ strBoot + " -cp " + strClassPath + " -d "
							+ strClass + " " + strMain, new PrintWriter(
							System.out), new PrintWriter(System.err), null);

			// DEBUG
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
				String strClassDex = strBuild + proj_name + ".dex";

				System.out.println("// PRE DEX LIBS"); // TODO
				// dx --dex --output=dexed.jar hello.jar

				System.out.println("// DEX CLASSES");
				com.android.dx.command.dexer.Main.main(new String[] {
						"--verbose", "--no-strict", "--output=" + strClassDex,
						strClass, strLibs + "demolib.jar" });

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
