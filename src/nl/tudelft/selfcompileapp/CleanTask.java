package nl.tudelft.selfcompileapp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import android.app.ProgressDialog;
import nl.tudelft.selfcompileapp.SelfCompileActivity.TaskManagerFragment;

public class CleanTask extends ProgressTask {

	public CleanTask(TaskManagerFragment mgr, Runnable done) {
		super(mgr, done);
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		dialog.setMax(100);
		dialog.setTitle("Resetting...");
		dialog.setMessage("");
	}

	@Override
	protected Object doInBackground(Object... params) {

		if (isCancelled())
			return null;

		publishProgress(0, "Remove old files");

		Util.deleteRecursive(S.dirProj);

		publishProgress(10, "Unpack source code");

		S.mkDirs();

		publishProgress(15);

		unzipAsset(S.zipSrc, S.dirSrc);

		publishProgress(25);

		copyAsset(S.xmlMan, S.xmlMan);

		publishProgress(30);

		copyAsset(S.jksEmbedded, S.jksEmbedded);

		publishProgress(35);

		unzipAsset(S.zipRes, S.dirRes);

		publishProgress(40);

		copyAsset(S.pngAppIcon, S.pngAppIcon);

		publishProgress(45, "Unpack dependencies");

		unzipAsset(S.zipLibs, S.dirLibs);

		publishProgress(60);

		unzipAsset(S.zipDexedLibs, S.dirDexedLibs);

		publishProgress(75);

		copyAsset(S.jarAndroid, S.jarAndroid);

		publishProgress(100);

		return null;
	}

	void unzipAsset(File file, File dest) {
		try {
			InputStream is = asset.open(file.getName());
			Util.unzip(is, dest);
			is.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	void copyAsset(File file, File dest) {
		try {
			InputStream is = asset.open(file.getName());
			Util.copy(is, dest);
			is.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

}
