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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	public void compile(View btnCompile) {
		btnCompile.setEnabled(false);
		new CompileSource().execute();
	}

	public void dex(View btnDex) {
		btnDex.setEnabled(false);
		new DexClasses().execute();
	}

	private class CompileSource extends AsyncTask<Object, Object, Object> {

		@Override
		protected void onPostExecute(Object result) {
			Button btnCompile = (Button) findViewById(R.id.btnCompile);
			btnCompile.setEnabled(true);
		}

		@Override
		protected Object doInBackground(Object... params) {
			try {
				File dirDownloads = Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

				System.out.println("// COPY ANDROID PLATFORM");

				File jarAndroid = new File(dirDownloads, "android-22.jar");
				if (!jarAndroid.exists()) {
					InputStream zipAndroidPlatform = getAssets().open(
							"android-22.zip");

					Util.unZip(zipAndroidPlatform, dirDownloads);
				}

				System.out.println("// CLEAR OUTPUT FOLDER");

				File dirProject = new File(dirDownloads, "demo_android");
				Util.deleteRecursive(dirProject);

				System.out.println("// COPY PROJECT SOURCE");

				InputStream zipProjSrc = getAssets().open("demo_android.zip");
				Util.unZip(zipProjSrc, dirDownloads);

				System.out.println("// REMOVE R.JAVA"); // TODO

				System.out.println("// RUN AAPT & CREATE R.JAVA"); // TODO

				File dirGen = new File(dirProject, "gen");
				dirGen.mkdir();

				System.out.println("// COMPILE SOURCE RECURSIVE");

				String strRoot = dirDownloads.getAbsolutePath();
				String strProj = dirProject.getAbsolutePath();

				String strArgs = " -1.5 -showversion -verbose -deprecation";
				strArgs += " -bootclasspath " + strRoot + "/android-22.jar";
				strArgs += " -cp " + strProj + "/libs/demolib.jar"
						+ File.pathSeparator + strProj + "/src"
						+ File.pathSeparator + strProj + "/gen";
				strArgs += " -d " + strProj + "/build/classes";
				strArgs += " " + strProj
						+ "/src/org/me/androiddemo/MainActivity.java";

				BatchCompiler.compile(strArgs, new PrintWriter(System.out),
						new PrintWriter(System.err), null);

				// DEBUG
				Util.listRecursive(new File(dirProject, "build"));

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
				String strProj = dirDownloads.getAbsolutePath()
						+ "/demo_android";

				System.out.println("// PRE DEX LIBS"); // TODO
				// dx --dex --output=dexed.jar hello.jar

				System.out.println("// DEX CLASSES");

				String[] aArgs = { "--verbose", "--no-strict",
						"--output=" + strProj + "/build/demo_android.dex",
						strProj + "/src/org", strProj + "/libs/demolib.jar" };
				com.android.dx.command.dexer.Main.main(aArgs);

				System.out.println("// MERGE DEX"); // TODO

				// DEBUG
				Util.listRecursive(new File(strProj, "build"));

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

	}
}
