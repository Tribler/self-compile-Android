package nl.tudelft.selfcompileapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import nl.tudelft.selfcompileapp.SelfCompileActivity.TaskManagerFragment;

abstract class ProgressTask extends AsyncTask<Object, Object, Object> {

	TaskManagerFragment mgr;
	Runnable done;
	Context context;
	AssetManager asset;
	ProgressDialog dialog;

	public ProgressTask(TaskManagerFragment mgr, Runnable done) {
		this.mgr = mgr;
		this.done = done;
		this.context = mgr.getActivity();
		this.asset = context.getAssets();
		this.dialog = mgr.dialog;
	}

	@Override
	protected void onCancelled(Object result) {
		dialog.dismiss();
		mgr.runWhenReady(done);
	}

	@Override
	protected void onPreExecute() {
		dialog.show();
	}

	@Override
	protected void onProgressUpdate(Object... values) {
		if (values.length > 0) {
			// dialog.setProgress((Integer) values[0]);
		}
		if (values.length > 1) {
			dialog.setMessage((String) values[1]);
		}
	}

	@Override
	protected void onPostExecute(Object result) {
		dialog.dismiss();
		mgr.runWhenReady(done);
	}
}
