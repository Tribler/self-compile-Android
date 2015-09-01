package nl.tudelft.selfcompileapp;

import java.io.File;
import java.io.FileOutputStream;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import com.android.dex.Dex;
import com.android.dx.merge.CollisionPolicy;
import com.android.dx.merge.DexMerger;
import com.android.sdklib.build.ApkBuilder;

import android.app.Activity;
import android.app.Fragment;
import android.app.IntentService;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import kellinwood.security.zipsigner.ProgressEvent;
import kellinwood.security.zipsigner.ZipSigner;

public class ProgressFragment extends Fragment {

	private ProgressDialog progress;

	public ProgressFragment() {
	}

	public boolean inProgess() {
		return progress != null && progress.isShowing();
	}

	public void onStart() {
		progress = new ProgressDialog(getActivity());
		progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progress.setMax(100);
		progress.setTitle("Resetting...");
		progress.setMessage("Remove old files");
		
		new MorphTask(null, null).execute();
	}
	
	public void onProgress(final int step) {
		new Handler(Looper.getMainLooper()).post(new Runnable() {
			@Override
			public void run() {
				if (progress != null) {
					progress.setMessage(Integer.toString(step));
				}
			}
		});
	}

	public void onFail(final Exception failure) {
		progress.dismiss();
		progress = null;
		failure.printStackTrace();
	}

	public void onSuccess(final File newApk) {

		new Handler(Looper.getMainLooper()).post(new Runnable() {
			@Override
			public void run() {
				if (progress != null) {
					progress.dismiss();
					progress = null;
				}
				System.out.println("DONE!");
			}
		});
	}

}

class MorphTask extends AsyncTask<Void, Void, Void> {

	private String mLabel;
	private Uri mIcon;

	public MorphTask(String label, Uri icon) {
		mLabel = label;
		mIcon = icon;
	}

	@Override
	protected Void doInBackground(Void... voids) {

		System.out.println("MORPHING...");

		return null;
	}
}

class CompileProgress extends org.eclipse.jdt.core.compiler.CompilationProgress {

	@Override
	public void begin(int arg0) {

	}

	@Override
	public void done() {

	}

	@Override
	public boolean isCanceled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setTaskName(String arg0) {

	}

	@Override
	public void worked(int arg0, int arg1) {

	}

}

class SignProgress implements kellinwood.security.zipsigner.ProgressListener {

	public void onProgress(ProgressEvent arg0) {
		// TODO Auto-generated method stub
	}

}