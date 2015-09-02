package nl.tudelft.selfcompileapp;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SelfCompileActivity extends Activity {

	UserInputFragment userInput;
	TaskManagerFragment taskManager;

	ImageButton btnAppIcon;
	EditText txtAppName;
	TextView lblStatus;
	ProgressBar prbProgress;
	Button btnReset;
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
		lblStatus = (TextView) findViewById(R.id.lblStatus);
		prbProgress = (ProgressBar) findViewById(R.id.prbProgress);
		btnReset = (Button) findViewById(R.id.btnReset);
		btnInstall = (Button) findViewById(R.id.btnInstall);

		initUserInput();
		initProgressListener();

		if (!S.dirProj.exists()) {
			taskManager.startClean();
		}
	}

	public void btnReset(View btnReset) {
		taskManager.startClean();
	}

	public void btnInstall(View btnInstall) {
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
		blockUserInput(taskManager.isRunning());

	}

	protected void blockUserInput(boolean working) {
		btnReset.setEnabled(!working);
		btnInstall.setEnabled(!working);
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

	class TaskManagerFragment extends DialogFragment {

		protected String strStatus;
		protected int intProgress;

		ProgressDialog dialog;

		private ProgressTask runningTask;
		private boolean activityReady = true;
		private List<Runnable> lstPendingCallbacks = new LinkedList<Runnable>();

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setRetainInstance(true);
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			dialog = new ProgressDialog(getActivity());
			return dialog;
		}

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			activityReady = true;
			int pendingCallbacks = lstPendingCallbacks.size();
			while (pendingCallbacks-- > 0) {
				getActivity().runOnUiThread(lstPendingCallbacks.remove(0));
			}
		}

		@Override
		public void onDetach() {
			super.onDetach();
			activityReady = false;
		}

		public void runWhenReady(Runnable runnable) {
			if (activityReady) {
				getActivity().runOnUiThread(runnable);
			} else {
				lstPendingCallbacks.add(runnable);
			}
		}

		boolean isRunning() {
			return runningTask != null;
		}

		void startClean(Object... params) {
			if (runningTask == null) {
				blockUserInput(true);
				runningTask = new CleanTask(this, new Runnable() {
					@Override
					public void run() {
						runningTask = null;
						blockUserInput(false);
					}
				});
				runningTask.execute(params);
			}
		}

		void startBuild(Object... params) {
			if (runningTask == null) {
				blockUserInput(true);
				runningTask = new BuildTask(this, new Runnable() {
					@Override
					public void run() {
						runningTask = null;
						blockUserInput(false);
					}
				});
				runningTask.execute(params);
			}
		}

	}

}
