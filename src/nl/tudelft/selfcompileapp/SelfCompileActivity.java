package nl.tudelft.selfcompileapp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class SelfCompileActivity extends android.app.Activity implements TaskResultReceiver.Receiver {

	private UserInputFragment userInput;
	private TaskResultReceiver receiver;

	private static final int SELECT_PHOTO = 1;
	private static final int ICON_WIDTH = 150;
	private static final int ICON_HEIGHT = 150;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_morph);

		receiver = new TaskResultReceiver(new Handler());
		receiver.setReceiver(this);

		initUserInput();

		boolean working = false;

		Button btnReset = (Button) findViewById(R.id.btnReset);
		btnReset.setEnabled(!working);

		Button btnCancel = (Button) findViewById(R.id.btnCancel);
		btnCancel.setEnabled(working);

		Button btnInstall = (Button) findViewById(R.id.btnInstall);
		btnInstall.setEnabled(!working);

		if (!S.dirProj.exists()) {
			btnReset(btnReset);
		}

	}

	public void btnBrowseIcon(View btnBrowseIcon) {
		Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
		photoPickerIntent.setType("image/*");
		startActivityForResult(photoPickerIntent, SELECT_PHOTO);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent returnedIntent) {
		super.onActivityResult(requestCode, resultCode, returnedIntent);

		switch (requestCode) {

		case SELECT_PHOTO:
			if (resultCode == RESULT_OK) {
				try {
					Uri uriImg = returnedIntent.getData();
					InputStream is = getContentResolver().openInputStream(uriImg);

					userInput.appIcon = Bitmap.createScaledBitmap(BitmapFactory.decodeStream(is), ICON_WIDTH,
							ICON_HEIGHT, false);
					is.close();

					ImageButton btnAppIcon = (ImageButton) findViewById(R.id.btnAppIcon);
					btnAppIcon.setImageBitmap(userInput.appIcon);

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			break;
		}
	}

	private void initUserInput() {
		// Fetch previous user input
		FragmentManager fm = getFragmentManager();
		userInput = (UserInputFragment) fm.findFragmentByTag("userInput");

		if (userInput == null) {
			userInput = new UserInputFragment();

			fm.beginTransaction().add(userInput, "userInput").commit();

			// Default app icon
			try {
				InputStream is = getAssets().open(S.pngAppIcon.getName());

				userInput.appIcon = Bitmap.createScaledBitmap(BitmapFactory.decodeStream(is), ICON_WIDTH, ICON_HEIGHT,
						false);
				is.close();

			} catch (IOException e) {
				e.printStackTrace();
			}

			// Default app name
			userInput.appName = getString(R.string.appName);
		}

		// Restore previous user input
		ImageButton btnAppIcon = (ImageButton) findViewById(R.id.btnAppIcon);
		btnAppIcon.setImageBitmap(userInput.appIcon);

		EditText txtAppName = (EditText) findViewById(R.id.txtAppName);
		txtAppName.setText(userInput.appName);

		// Handle user input
		/** handle user icon pick @see btnBrowseIcon & onActivityResult **/

		txtAppName.addTextChangedListener(new TextWatcher() {

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				userInput.appName = s.toString();
			}

		});
	}

	@Override
	public void onReceiveResult(int resultCode, Bundle resultData) {

		if (resultCode == RESULT_OK) {

			Notification.Builder mBuilder = new Notification.Builder(this).setSmallIcon(R.drawable.ic_launcher)
					.setContentTitle("Ready to install!")
					.setContentText("The modified app is ready for installation. Tap this to start.");

			Intent resultIntent = new Intent(this, SelfCompileActivity.class);

			TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
			stackBuilder.addParentStack(SelfCompileActivity.class);
			stackBuilder.addNextIntent(resultIntent);
			PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
			mBuilder.setContentIntent(resultPendingIntent);
			NotificationManager mNotificationManager = (NotificationManager) getSystemService(
					Context.NOTIFICATION_SERVICE);
			// mId allows you to update the notification later on.
			mNotificationManager.notify(1, mBuilder.build());

			Toast.makeText(SelfCompileActivity.this, resultData.getString("statusMsg"), Toast.LENGTH_SHORT).show();

		} else if (resultCode == RESULT_CANCELED) {

		} else {

			ProgressBar pbBgProgress = (ProgressBar) findViewById(R.id.pbBgProgress);
			pbBgProgress.incrementProgressBy(1);

			TextView lblStatus = (TextView) findViewById(R.id.lblStatus);
			lblStatus.setText((String) resultData.getString("statusMsg"));
		}
	}

	public void btnReset(View btnReset) {
		new CleanProject(this).execute();
	}

	public void btnCancel(View btnCancel) {
		Intent i = new Intent(this, SelfCompileService.class);
		stopService(i);
	}

	public void btnInstall(View btnInstall) {
		Intent i = new Intent(this, SelfCompileService.class);
		i.putExtra("receiver", receiver);
		startService(i);
	}

}

class UserInputFragment extends Fragment {

	protected Bitmap appIcon;
	protected String appName;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}
}

class CleanProject extends AsyncTask<Object, Object, Object> {

	public static Context context;
	private AssetManager asset;
	private ProgressDialog progress;

	protected CleanProject(SelfCompileActivity activity) {
		asset = activity.getAssets();
		progress = new ProgressDialog(activity);
		progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progress.setMax(100);
		progress.setTitle("Resetting...");
		progress.setMessage("Remove old files");
	}

	@Override
	protected void onPreExecute() {
		if (progress != null) {
			progress.show();
		}
	}

	@Override
	protected void onPostExecute(Object result) {
		if (progress != null) {
			progress.dismiss();
		}
	}

	@Override
	protected void onProgressUpdate(Object... values) {
		if (progress != null) {
			if (values.length > 0) {
				progress.setProgress((Integer) values[0]);
			}
			if (values.length > 1) {
				progress.setMessage((String) values[1]);
			}
		} else {
			System.err.println("progress is null");
		}
	}

	@Override
	protected Object doInBackground(Object... params) {

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

	private void unzipAsset(File file, File dest) {
		try {
			InputStream is = asset.open(file.getName());
			Util.unzip(is, dest);
			is.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private void copyAsset(File file, File dest) {
		try {
			InputStream is = asset.open(file.getName());
			Util.copy(is, dest);
			is.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

}
