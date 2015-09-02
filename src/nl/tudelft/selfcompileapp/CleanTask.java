package nl.tudelft.selfcompileapp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import nl.tudelft.selfcompileapp.SelfCompileActivity.TaskManagerFragment;

public class CleanTask extends AsyncTask<Object, Object, Object> {

	TaskManagerFragment mgr;
	Context context;
	AssetManager asset;

	public CleanTask(TaskManagerFragment mgr) {
		this.mgr = mgr;
		this.context = mgr.getActivity();
		this.asset = context.getAssets();
	}

	@Override
	protected void onProgressUpdate(Object... values) {
		if (values.length > 0) {
			mgr.intProgress = (Integer) values[0];
		}
		if (values.length > 1) {
			mgr.strStatus = (String) values[1];
		}
		mgr.handler.sendEmptyMessage(Activity.RESULT_FIRST_USER);
	}

	@Override
	protected void onPreExecute() {
		publishProgress(0, "");
	}

	@Override
	protected void onPostExecute(Object result) {
		mgr.handler.sendEmptyMessage(Activity.RESULT_OK);
	}

	@Override
	protected void onCancelled() {
		mgr.handler.sendEmptyMessage(Activity.RESULT_CANCELED);
	}

	boolean setProgress(Object... values) {
		publishProgress(values);
		return isCancelled();
	}

	@Override
	protected Object doInBackground(Object... params) {

		if (setProgress(1, "REMOVE OLD FILES")) {
			return null;
		}
		Util.deleteRecursive(S.dirProj);

		if (setProgress(10, "UNPACK SOURCE CODE")) {
			return null;
		}
		S.mkDirs();

		if (setProgress(15)) {
			return null;
		}
		unzipAsset(S.zipSrc, S.dirSrc);

		if (setProgress(25)) {
			return null;
		}
		copyAsset(S.xmlMan, S.xmlMan);

		if (setProgress(30)) {
			return null;
		}
		copyAsset(S.jksEmbedded, S.jksEmbedded);

		if (setProgress(35)) {
			return null;
		}
		unzipAsset(S.zipRes, S.dirRes);

		if (setProgress(40)) {
			return null;
		}
		copyAsset(S.pngAppIcon, S.pngAppIcon);

		if (setProgress(45, "UNPACK DEPENDENCIES")) {
			return null;
		}
		unzipAsset(S.zipLibs, S.dirLibs);

		if (setProgress(60)) {
			return null;
		}
		unzipAsset(S.zipDexedLibs, S.dirDexedLibs);

		if (setProgress(75)) {
			return null;
		}
		copyAsset(S.jarAndroid, S.jarAndroid);

		if (setProgress(100)) {
			return null;
		}

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
