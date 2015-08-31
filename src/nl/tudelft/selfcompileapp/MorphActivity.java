package nl.tudelft.selfcompileapp;

import java.io.IOException;
import java.io.InputStream;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
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

public class MorphActivity extends android.app.Activity {

	private UserInputFragment userInput;
	private TaskResultReceiver receiver;
	private ProgressStatusTask runningTask;

	public static TextView lblStatus;
	public static ProgressBar pbBgProgress;
	public static Button btnReset;
	public static Button btnCancel;
	public static Button btnInstall;

	private static final int SELECT_PHOTO = 1;
	private static final int ICON_WIDTH = 150;
	private static final int ICON_HEIGHT = 150;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_morph);

		// TODO use service messages
		lblStatus = (TextView) findViewById(R.id.lblStatus);
		pbBgProgress = (ProgressBar) findViewById(R.id.pbBgProgress);
		btnReset = (Button) findViewById(R.id.btnReset);
		btnCancel = (Button) findViewById(R.id.btnCancel);
		btnInstall = (Button) findViewById(R.id.btnInstall);

		// Start single task service
		// setupServiceReceiver();

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

	public void doSomething() {
		Intent i = new Intent(this, SingleTaskService.class);
		i.putExtra("foo", "bar");
		i.putExtra("receiver", receiver);
		startService(i);
	}

	/**
	 * Setup the callback for when data is received from the service
	 */
	public void setupServiceReceiver() {

		receiver = new TaskResultReceiver(new Handler());

		receiver.setReceiver(new TaskResultReceiver.Receiver() {

			@Override
			public void onReceiveResult(int resultCode, Bundle resultData) {

				if (resultCode == RESULT_OK) {

					String resultValue = resultData.getString("resultValue");
					Toast.makeText(MorphActivity.this, resultValue, Toast.LENGTH_SHORT).show();

				} else if (resultCode == RESULT_CANCELED) {

				}
			}
		});
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

class UserInputFragment extends Fragment {

	Bitmap appIcon;
	String appName;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}
}
