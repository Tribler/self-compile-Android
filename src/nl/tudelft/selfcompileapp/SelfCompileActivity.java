package nl.tudelft.selfcompileapp;

import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask.Status;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SelfCompileActivity extends Activity implements Handler.Callback {

	UserInputFragment userInput;
	TaskManagerFragment taskManager;

	ImageButton btnAppIcon;
	EditText txtAppName;
	Button btnCloneApp;
	TextView lblStatus;
	ProgressBar prbProgress;
	Button btnReset;
	Button btnCancel;
	Button btnInstall;

	static final int SELECT_PHOTO = 1;
	static final int ICON_WIDTH = 150;
	static final int ICON_HEIGHT = 150;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_morph);

		btnAppIcon = (ImageButton) findViewById(R.id.btnAppIcon);
		txtAppName = (EditText) findViewById(R.id.txtAppName);
		btnCloneApp = (Button) findViewById(R.id.btnCloneApp);
		lblStatus = (TextView) findViewById(R.id.lblStatus);
		prbProgress = (ProgressBar) findViewById(R.id.prbProgress);
		btnReset = (Button) findViewById(R.id.btnReset);
		btnCancel = (Button) findViewById(R.id.btnCancel);
		btnInstall = (Button) findViewById(R.id.btnInstall);

		initUserInput();
		initProgressListener();

		if (!S.dirProj.exists()) {
			taskManager.startClean();
		}
	}

	public void btnReset(View btnReset) {
		btnReset.setEnabled(false);
		taskManager.startClean();
	}

	public void btnCancel(View btnCancel) {
		btnCancel.setEnabled(false);
		taskManager.cancelTask();
	}

	public void btnInstall(View btnInstall) {
		btnInstall.setEnabled(false);
		taskManager.startBuild();
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

					btnAppIcon.setImageBitmap(userInput.appIcon);

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			break;
		}
	}

	/**
	 * Fetch previous user input
	 */
	private void initUserInput() {
		FragmentManager fm = getFragmentManager();
		userInput = (UserInputFragment) fm.findFragmentByTag(UserInputFragment.class.getSimpleName());

		if (userInput == null) {
			userInput = new UserInputFragment();

			fm.beginTransaction().add(userInput, UserInputFragment.class.getSimpleName()).commit();

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
		btnAppIcon.setImageBitmap(userInput.appIcon);
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

	/**
	 * Fetch current progress status
	 */
	private void initProgressListener() {
		FragmentManager fm = getFragmentManager();
		taskManager = (TaskManagerFragment) fm.findFragmentByTag(TaskManagerFragment.class.getSimpleName());

		if (taskManager == null) {
			taskManager = new TaskManagerFragment();

			fm.beginTransaction().add(taskManager, TaskManagerFragment.class.getSimpleName()).commit();
		}

		// Set current working state
		toggleGui(taskManager.isIdle());
		lblStatus.setText(taskManager.strStatus);
		prbProgress.setProgress(taskManager.intProgress);

		// Handle state changes
		taskManager.handler = new Handler(this);
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {

		case RESULT_OK:
		case RESULT_CANCELED:
			toggleGui(true);
			return true;

		case RESULT_FIRST_USER:
		default:
			lblStatus.setText(taskManager.strStatus);
			prbProgress.setProgress(taskManager.intProgress);
			return true;
		}
	}

	protected void toggleGui(boolean enabled) {
		btnAppIcon.setEnabled(enabled);
		txtAppName.setEnabled(enabled);
		btnCloneApp.setEnabled(enabled);
		lblStatus.setVisibility(enabled ? View.INVISIBLE : View.VISIBLE);
		prbProgress.setVisibility(enabled ? View.INVISIBLE : View.VISIBLE);
		btnReset.setEnabled(enabled);
		btnCancel.setEnabled(!enabled);
		btnInstall.setEnabled(enabled);
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

	class TaskManagerFragment extends Fragment {

		protected String strStatus;
		protected int intProgress;

		protected Handler handler;

		private AsyncTask<Object, Object, Object> runningTask;

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setRetainInstance(true);
		}

		boolean isIdle() {
			return runningTask == null || runningTask.getStatus() == Status.FINISHED;
		}

		void startClean(Object... params) {
			if (isIdle()) {
				toggleGui(false);
				runningTask = new CleanTask(this).execute(params);
			}
		}

		void cancelTask() {
			if (!isIdle()) {
				runningTask.cancel(true);
			}
		}

		void startBuild(Object... params) {
			if (isIdle()) {
				toggleGui(false);
				runningTask = new BuildTask(this).execute(params);
			}
		}

	}

}
