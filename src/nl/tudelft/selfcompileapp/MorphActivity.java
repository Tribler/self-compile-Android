package nl.tudelft.selfcompileapp;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

public class MorphActivity extends android.app.Activity {

	private static final int SELECT_PHOTO = 1;
	private static final int ICON_WIDTH = 150;
	private static final int ICON_HEIGHT = 150;

	/*
	 * Accessed from AsyncTasks
	 */
	public static ImageButton btnAppIcon;
	public static EditText txtAppName;
	public static Button btnCloneApp;
	public static Spinner spnAppTheme;
	public static TextView lblStatus;
	public static ProgressBar pbBgProgress;
	public static Button btnReset;
	public static Button btnCancel;
	public static Button btnInstall;

	private static Bitmap resizedAppIcon;
	private static ProgressStatusTask runningTask;

	@Override
	protected void onCreate(android.os.Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_morph);

		// TODO read settings from disk

		if (savedInstanceState == null) {
			savedInstanceState = new Bundle();
		}
		onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		btnAppIcon = (ImageButton) findViewById(R.id.btnAppIcon);
		txtAppName = (EditText) findViewById(R.id.txtAppName);
		btnCloneApp = (Button) findViewById(R.id.btnCloneApp);
		spnAppTheme = (Spinner) findViewById(R.id.spnAppTheme);
		lblStatus = (TextView) findViewById(R.id.lblStatus);
		pbBgProgress = (ProgressBar) findViewById(R.id.pbBgProgress);
		btnReset = (Button) findViewById(R.id.btnReset);
		btnCancel = (Button) findViewById(R.id.btnCancel);
		btnInstall = (Button) findViewById(R.id.btnInstall);

		if (!S.dirProj.exists()) {
			btnReset(null);
		}
		if (savedInstanceState.containsKey("resizedAppIcon")) {
			resizedAppIcon = savedInstanceState.getParcelable("resizedAppIcon");
			btnAppIcon.setImageBitmap(resizedAppIcon);
		} else {
			try {
				InputStream is = getAssets().open(S.pngAppIcon.getName());
				resizedAppIcon = Bitmap.createScaledBitmap(BitmapFactory.decodeStream(is), ICON_WIDTH, ICON_HEIGHT,
						false);
				btnAppIcon.setImageBitmap(resizedAppIcon);
				is.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (savedInstanceState.containsKey("txtAppName")) {
			txtAppName.setText(savedInstanceState.getString("txtAppName"));
		} else {
			txtAppName.setText(getString(R.string.appName));
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putParcelable("resizedAppIcon", resizedAppIcon);
		outState.putString("txtAppName", txtAppName.getText().toString());
	}

	public void btnBrowseIcon(View btnBrowseIcon) {
		Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
		photoPickerIntent.setType("image/*");
		startActivityForResult(photoPickerIntent, SELECT_PHOTO);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
		super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

		switch (requestCode) {

		case SELECT_PHOTO:
			if (resultCode == RESULT_OK) {
				try {
					Uri uriImg = imageReturnedIntent.getData();
					InputStream is = getContentResolver().openInputStream(uriImg);
					resizedAppIcon = Bitmap.createScaledBitmap(BitmapFactory.decodeStream(is), 250, 250, false);
					btnAppIcon.setImageBitmap(resizedAppIcon);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	public void btnReset(View btnReset) {
		try {
			runningTask = (ProgressStatusTask) new ExtractProjectTask(getApplicationContext()).execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void btnCancel(View btnCancel) {
		runningTask.cancel(true);
	}

	public void btnInstall(View btnInstall) {
		try {
			runningTask = (ProgressStatusTask) new MakeApkTask(getApplicationContext()).execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
