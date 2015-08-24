package nl.tudelft.selfcompileapp;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;

public abstract class ProgressStatusTask extends AsyncTask<Object, Object, Object> {

	protected Context app;
	protected int total_steps;

	public ProgressStatusTask(Context app, int total_steps) {
		this.app = app;
		this.total_steps = total_steps;
	}

	@Override
	protected void onPreExecute() {
		toggleButtons(true);
		MorphActivity.pbBgProgress.setProgress(0);
		MorphActivity.pbBgProgress.setMax(total_steps);
	}

	@Override
	protected void onProgressUpdate(Object... values) {
		MorphActivity.pbBgProgress.incrementProgressBy(1);
		MorphActivity.lblStatus.setText((String) values[0]);
	}

	@Override
	protected void onPostExecute(Object result) {
		MorphActivity.pbBgProgress.setProgress(total_steps);
		toggleButtons(false);
	}

	@Override
	protected void onCancelled(Object result) {
		toggleButtons(false);
	}

	protected boolean setMsg(String msg) {
		publishProgress(msg);
		return isCancelled();
	}

	private void toggleButtons(boolean working) {
		MorphActivity.btnReset.setEnabled(!working);
		MorphActivity.btnCancel.setEnabled(working);
		MorphActivity.btnInstall.setEnabled(!working);
		MorphActivity.pbBgProgress.setVisibility(working ? View.VISIBLE : View.INVISIBLE);
		MorphActivity.lblStatus.setVisibility(working ? View.VISIBLE : View.INVISIBLE);
		MorphActivity.lblStatus.setText("");
	}

}
