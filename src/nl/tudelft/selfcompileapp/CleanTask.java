package nl.tudelft.selfcompileapp;

import java.io.File;
import java.io.InputStream;

import android.content.Context;
import android.os.Handler;

public class CleanTask extends ProgressTask {

	CleanTask(UserInputFragment userInput, Context appContext, Handler listener) {
		super(userInput, appContext, listener);
	}

	public void run() {

		if (setProgress(1, R.string.stsClean)) {
			return;
		}
		Util.deleteRecursive(S.dirProj);

		if (setProgress(10, R.string.stsUnpackSrc)) {
			return;
		}
		S.mkDirs();

		if (setProgress(15)) {
			return;
		}
		unzipAsset(S.zipSrc, S.dirSrc);

		if (setProgress(25)) {
			return;
		}
		copyAsset(S.xmlMan, S.xmlMan);

		if (setProgress(30)) {
			return;
		}
		copyAsset(S.jksEmbedded, S.jksEmbedded);

		if (setProgress(35)) {
			return;
		}
		unzipAsset(S.zipRes, S.dirRes);

		if (setProgress(40)) {
			return;
		}
		copyAsset(S.pngAppIcon, S.pngAppIcon);

		if (setProgress(45, R.string.stsUnpackDep)) {
			return;
		}
		unzipAsset(S.zipLibs, S.dirLibs);

		if (setProgress(60)) {
			return;
		}
		unzipAsset(S.zipDexedLibs, S.dirDexedLibs);

		if (setProgress(75)) {
			return;
		}
		copyAsset(S.jarAndroid, S.jarAndroid);

		if (setProgress(100)) {
			return;
		}
	}

	void unzipAsset(File file, File dest) {
		try {
			InputStream is = appContext.getAssets().open(file.getName());
			Util.unzip(is, dest);
			is.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	void copyAsset(File file, File dest) {
		try {
			InputStream is = appContext.getAssets().open(file.getName());
			Util.copy(is, dest);
			is.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
