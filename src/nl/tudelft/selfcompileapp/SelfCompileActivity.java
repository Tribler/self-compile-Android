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

public class SelfCompileActivity extends Activity {

	static final int ACTION_PICK_IMAGE = 1;
	static final int ICON_WIDTH = 150;
	static final int ICON_HEIGHT = 150;

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
			taskManager.startClean(this);
		}
	}

	public void btnReset(View btnReset) {
		btnReset.setEnabled(false);
		taskManager.startClean(this);
	}

	public void btnCancel(View btnCancel) {
		btnCancel.setEnabled(false);
		taskManager.cancelTask(this);
	}

	public void btnInstall(View btnInstall) {
		btnInstall.setEnabled(false);
		taskManager.startBuild(this);
	}

	public void btnBrowseIcon(View btnBrowseIcon) {
		Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
		photoPickerIntent.setType("image/*");
		startActivityForResult(photoPickerIntent, ACTION_PICK_IMAGE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent returnedIntent) {
		super.onActivityResult(requestCode, resultCode, returnedIntent);

		switch (requestCode) {

		case ACTION_PICK_IMAGE:
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

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

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
	}

	protected void toggleGui(boolean enabled) {
		btnAppIcon.setEnabled(enabled);
		txtAppName.setEnabled(enabled);
		btnCloneApp.setEnabled(enabled);
		lblStatus.setVisibility(enabled ? View.INVISIBLE : View.VISIBLE);
		lblStatus.setText(taskManager.strStatus);
		prbProgress.setVisibility(enabled ? View.INVISIBLE : View.VISIBLE);
		prbProgress.setProgress(taskManager.intProgress);
		btnReset.setEnabled(enabled);
		btnCancel.setEnabled(!enabled);
		btnInstall.setEnabled(enabled);
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

class TaskManagerFragment extends Fragment implements Handler.Callback {

	static final int TASK_CANCELED = 0;
	static final int TASK_FINISHED = -1;
	static final int TASK_PROGRESS = 1;

	protected int intProgress;
	protected String strStatus;

	protected Handler handler = new Handler(this);
	private Thread runningTask;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	boolean isIdle() {
		return runningTask == null || !runningTask.isAlive();
	}

	public boolean handleMessage(Message msg) {
		// update state
		switch (msg.what) {

		case TASK_FINISHED:
		case TASK_CANCELED:
			runningTask = null;
			intProgress = 0;
			strStatus = "";
			break;

		case TASK_PROGRESS:
		default:
			intProgress = msg.arg1;
			if (msg.arg2 != 0) {
				strStatus = getString(msg.arg2);
			}
			break;
		}

		// update gui
		if (isAdded()) {
			((SelfCompileActivity) getActivity()).toggleGui(isIdle());
		}
		return true;
	}

	void startClean(SelfCompileActivity activity) {
		if (isIdle()) {
			activity.toggleGui(false);
			runningTask = new Thread(new CleanTask(activity.getApplicationContext(), handler));
			runningTask.start();
		}
	}

	void cancelTask(SelfCompileActivity activity) {
		if (!isIdle()) {
			runningTask.interrupt();
		}
	}

	void startBuild(SelfCompileActivity activity) {
		if (isIdle()) {
			activity.toggleGui(false);
			runningTask = new Thread(new BuildTask(activity.getApplicationContext(), handler));
			runningTask.start();
		}
	}

}
