package nl.tudelft.selfcompileapp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import org.eclipse.jdt.core.compiler.batch.BatchCompiler;
import org.eclipse.jdt.internal.compiler.tool.EclipseCompiler;

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

	void toggleBtnCompile(boolean enabled) {

	}

	public void selfCompile(View btnCompile) {
		btnCompile.setEnabled(false);
		new CompilePrjSrc().execute();
	}

	private class CompilePrjSrc extends AsyncTask<Object, Object, Object> {

		@Override
		protected void onPostExecute(Object result) {
			Button btnCompile = (Button) findViewById(R.id.btnCompile);
			btnCompile.setEnabled(true);
		}

		@Override
		protected Object doInBackground(Object... params) {
			try {
				System.out.println("// COPY ANDROID PLATFORM");
				File dirDownloads = Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
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
				InputStream zipPrjSrc = getAssets().open("demo_android.zip");
				Util.unZip(zipPrjSrc, dirDownloads);

				// TODO REMOVE R.JAVA

				// TODO RUN AAPT & CREATE R.JAVA
				File dirGen = new File(dirProject, "gen");
				dirGen.mkdir();

				System.out.println("// COMPILE SOURCE RECURSIVE");

				String strRoot = dirDownloads.getAbsolutePath();
				String strProj = dirProject.getAbsolutePath();
				String strArgs = " -1.5 -showversion -verbose -deprecation";
				strArgs += " -bootclasspath \"" + strRoot + "/android-22.jar\"";
				strArgs += " -cp \"" + strProj + "/libs/demolib.jar"
						+ File.pathSeparator + strProj + "/src"
						+ File.pathSeparator + strProj + "/gen\"";
				strArgs += " -d \"" + strProj + "/build/classes\""; // output
				strArgs += " \"" + strProj
						+ "/src/org/me/androiddemo/MainActivity.java\"";

				BatchCompiler.compile(strArgs, new PrintWriter(System.out),
						new PrintWriter(System.err), null);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

	}
}
