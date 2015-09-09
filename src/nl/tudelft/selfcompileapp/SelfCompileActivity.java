package nl.tudelft.selfcompileapp;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Arrays;

import org.w3c.dom.Document;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Offers various changes to be made to the app by the user. Also the user can
 * install the (changed) app as an update or share it via nfc.
 * 
 * @author Paul Brussee
 *
 */
public class SelfCompileActivity extends Activity {

	static final int REQ_APP_ICON = 1;
	static final int REQ_APP_INFO = 2;
	static final int ICON_WIDTH = 150;
	static final int ICON_HEIGHT = 150;

	UserInputFragment userInput;
	TaskManagerFragment taskManager;

	View frmChange;
	Button btnMimicApp;
	ImageView btnAppIcon;
	EditText txtAppName;
	Spinner spnAppTheme;
	EditText txtAppPackage;
	TextView lblStatus;
	ProgressBar prbProgress;
	ProgressBar prbSpinner;
	Button btnReset;
	Button btnCancel;
	Button btnInstall;

	protected void updateGui(boolean enabled) {
		frmChange.setVisibility(!enabled ? View.GONE : View.VISIBLE);
		btnMimicApp.setEnabled(enabled);
		btnAppIcon.setEnabled(enabled);
		txtAppName.setEnabled(enabled);
		lblStatus.setVisibility(enabled ? View.GONE : View.VISIBLE);
		lblStatus.setText(taskManager.getStatus());
		prbProgress.setVisibility(enabled ? View.GONE : View.VISIBLE);
		prbProgress.setProgress(taskManager.getProgress());
		prbSpinner.setVisibility(enabled ? View.GONE : View.VISIBLE);
		btnReset.setVisibility(!enabled ? View.GONE : View.VISIBLE);
		btnReset.setEnabled(enabled);
		btnCancel.setVisibility(enabled ? View.GONE : View.VISIBLE);
		btnCancel.setEnabled(!enabled);
		btnInstall.setVisibility(!enabled ? View.GONE : View.VISIBLE);
		btnInstall.setEnabled(enabled);
	}

	//////////////////// ACTIVITY LIFECYCLE ////////////////////

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_self_compile);

		frmChange = findViewById(R.id.frmChange);
		btnMimicApp = (Button) findViewById(R.id.btnMimicApp);
		btnAppIcon = (ImageView) findViewById(R.id.btnAppIcon);
		txtAppName = (EditText) findViewById(R.id.txtAppName);
		spnAppTheme = (Spinner) findViewById(R.id.spnAppTheme);
		txtAppPackage = (EditText) findViewById(R.id.txtAppPackage);
		lblStatus = (TextView) findViewById(R.id.lblStatus);
		prbProgress = (ProgressBar) findViewById(R.id.prbProgress);
		prbSpinner = (ProgressBar) findViewById(R.id.prbSpinner);
		btnReset = (Button) findViewById(R.id.btnReset);
		btnCancel = (Button) findViewById(R.id.btnCancel);
		btnInstall = (Button) findViewById(R.id.btnInstall);

		S.mkDirs();

		initTaskManager();
		initUserInput();

		if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC)) {
			initNfc();
		}

		// Set current working state
		updateGui(taskManager.isIdle());

		if (!S.dirProj.exists()) {
			btnReset(btnReset);
		}
	}

	private void initNfc() {
		NfcAdapter.getDefaultAdapter(this).setBeamPushUrisCallback(new NfcAdapter.CreateBeamUrisCallback() {

			public Uri[] createBeamUris(android.nfc.NfcEvent event) {

				return new Uri[] { Uri.fromFile(S.apkRedistributable) };
			}
		}, this);
	}

	//////////////////// RETAINED FRAGMENTS ////////////////////

	private void initTaskManager() {
		// Fetch saved progress status
		FragmentManager fm = getFragmentManager();
		taskManager = (TaskManagerFragment) fm.findFragmentByTag(TaskManagerFragment.class.getSimpleName());

		if (taskManager == null) {
			taskManager = new TaskManagerFragment();

			fm.beginTransaction().add(taskManager, TaskManagerFragment.class.getSimpleName()).commit();
		}
	}

	private void initUserInput() {
		// Fetch saved user input
		FragmentManager fm = getFragmentManager();
		userInput = (UserInputFragment) fm.findFragmentByTag(UserInputFragment.class.getSimpleName());

		if (userInput == null) {
			userInput = new UserInputFragment();

			fm.beginTransaction().add(userInput, UserInputFragment.class.getSimpleName()).commit();

			// Default app name
			userInput.setAppName(getString(R.string.appName));

			// Default app icon
			userInput.setAppIcon(BitmapFactory.decodeResource(getResources(), R.drawable.app_icon));

			// Default app package
			userInput.setAppPackage(getPackageName());

			// Default app theme
			userInput.setAppTheme(getTheme().toString());
		}

		// Restore previous user input
		txtAppName.setText(userInput.getAppName());
		btnAppIcon.setImageBitmap(userInput.getAppIcon());

		spnAppTheme.setSelection(
				Arrays.asList(getResources().getStringArray(R.array.appThemes)).indexOf(userInput.getAppTheme()));

		setTheme(android.R.style.Theme_Holo_Light); // DEBUG

		txtAppPackage.setText(userInput.getAppPackage());

		// Handle app icon change
		/**
		 * @see onActivityResult
		 **/

		// Handle app name change
		txtAppName.addTextChangedListener(new TextWatcher() {

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			public void afterTextChanged(Editable s) {
				userInput.setAppName(s.toString());
			}

		});

		// Handle app theme change
		spnAppTheme.setOnItemSelectedListener(new OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				userInput.setAppTheme(getResources().getStringArray(R.array.appThemes)[position]);
			}

			public void onNothingSelected(AdapterView<?> parent) {
			}

		});

		// Handle app package change
		txtAppPackage.addTextChangedListener(new TextWatcher() {

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			public void afterTextChanged(Editable s) {
				userInput.setAppPackage(s.toString());
			}

		});
	}

	//////////////////// ON CLICK BUTTONS ////////////////////

	public void btnMimicApp(View btnMimicApp) {
		btnMimicApp.setEnabled(false);
		Intent pickApp = new Intent(this, PickAppActivity.class);
		startActivityForResult(pickApp, REQ_APP_INFO);
	}

	public void btnAppIcon(View btnAppIcon) {
		btnAppIcon.setEnabled(false);
		Intent pickIcon = new Intent(Intent.ACTION_PICK);
		pickIcon.setType("image/*");
		startActivityForResult(pickIcon, REQ_APP_ICON);
	}

	public void btnReset(View btnReset) {
		btnReset.setEnabled(false);
		taskManager.startClean(this, null);
	}

	public void btnCancel(View btnCancel) {
		btnCancel.setEnabled(false);
		taskManager.cancelTask(this, null);
	}

	public void btnInstall(View btnInstall) {
		btnInstall.setEnabled(false);
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setDataAndType(Uri.fromFile(S.apkRedistributable), "application/vnd.android.package-archive");
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		if (S.apkRedistributable.exists()) {
			startActivity(i);
		} else {
			taskManager.startBuild(this, i);
		}
	}

	//////////////////// INTENT CALLBACKS ////////////////////

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent returnedIntent) {
		super.onActivityResult(requestCode, resultCode, returnedIntent);
		switch (requestCode) {

		case REQ_APP_ICON:
			if (resultCode == RESULT_OK) {
				try {
					Uri uriImg = returnedIntent.getData();
					InputStream is = getContentResolver().openInputStream(uriImg);

					Bitmap icon = Bitmap.createScaledBitmap(BitmapFactory.decodeStream(is), ICON_WIDTH, ICON_HEIGHT,
							false);
					is.close();

					userInput.setAppIcon(icon);

					btnAppIcon.setImageBitmap(icon);

					btnAppIcon.setEnabled(true);

				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			break;

		case REQ_APP_INFO:
			if (resultCode == RESULT_OK) {
				try {
					String packageName = returnedIntent.getStringExtra("app");

					PackageManager packMgr = getPackageManager();
					ApplicationInfo info = packMgr.getApplicationInfo(packageName, 0);

					String name = packMgr.getApplicationLabel(info).toString();
					Bitmap icon = Util.drawableToBitmap(packMgr.getApplicationIcon(info));

					userInput.setAppName(name);
					userInput.setAppIcon(icon);

					txtAppName.setText(name);
					btnAppIcon.setImageBitmap(icon);

					btnMimicApp.setEnabled(true);

				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			break;
		}
	}
}

/**
 * Keeps track of the changes made by the user.
 * 
 * @author Paul Brussee
 *
 */
class UserInputFragment extends Fragment {

	private String appName;
	private Bitmap appIcon;
	private String appPackage;
	private String appTheme;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	String getAppName() {
		if (S.xmlStrings.exists()) {
			try {
				Document strings = Util.readXml(S.xmlStrings);
				System.out.println(strings.getDocumentElement().getFirstChild().getTextContent());

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return appName;
	}

	Bitmap getAppIcon() {
		// TODO: Read from res/drawable-...

		return appIcon;
	}

	String getAppPackage() {
		// TODO: Read from manifest.xml

		return appPackage;
	}

	String getAppTheme() {
		// TODO: Read from values-.../styles.xml

		return appTheme;
	}

	void setAppName(String name) {
		appName = name;

		// Set name of the final output file
		S.apkRedistributable = new File(S.dirRoot, appName + ".apk");

		// TODO: Write to strings.xml
	}

	void setAppIcon(Bitmap icon) {
		appIcon = icon;

		// Write to assets dir
		try {
			FileOutputStream pngIcon = new FileOutputStream(S.pngAppIcon);
			appIcon.compress(Bitmap.CompressFormat.PNG, 100, new BufferedOutputStream(pngIcon));
			pngIcon.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		// TODO: Write to all res/drawable-...
	}

	void setAppPackage(String packagePath) {
		// TODO: Write to manifest.xml
	}

	void setAppTheme(String theme) {
		appTheme = theme;

		// TODO: Write to all values-.../styles.xml
	}

}

/**
 * Keeps track of running tasks and progress. Updates activity gui if attached.
 * Starts optional intent after task is done.
 * 
 * @author Paul Brussee
 *
 */
class TaskManagerFragment extends Fragment implements Handler.Callback {

	static final int TASK_CANCELED = 0;
	static final int TASK_FINISHED = -1;
	static final int TASK_PROGRESS = 1;

	private int intProgress;
	private String strStatus;

	protected Handler listener = new Handler(this);
	private Thread runningTask;
	private Intent done;

	int getProgress() {
		return intProgress;
	}

	String getStatus() {
		return strStatus;
	}

	boolean isIdle() {
		return runningTask == null;
	}

	//////////////////// FRAGMENT LIFECYCLE ////////////////////

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	//////////////////// HANDLER CALLBACKS ////////////////////

	public boolean handleMessage(Message msg) {
		// update state
		switch (msg.what) {

		case TASK_FINISHED:
		case TASK_CANCELED:
			runningTask = null;
			intProgress = 0;
			strStatus = "";
			if (done != null) {
				if (isAdded()) {
					((SelfCompileActivity) getActivity()).startActivity(done);
				}
				done = null;
			}
			break;

		case TASK_PROGRESS:
			intProgress = msg.arg1;
			if (msg.arg2 != 0) {
				strStatus = getString(msg.arg2);
			}
			break;
		}

		// update gui
		if (isAdded()) {
			((SelfCompileActivity) getActivity()).updateGui(isIdle());
		}
		return true;
	}

	//////////////////// TASKS ////////////////////

	void startClean(SelfCompileActivity activity, Intent done) {
		if (isIdle()) {
			activity.updateGui(false);
			this.done = done;
			runningTask = new Thread(new CleanTask(activity.getApplicationContext(), listener));
			runningTask.start();
		}
	}

	void cancelTask(SelfCompileActivity activity, Intent done) {
		if (!isIdle()) {
			strStatus = activity.getString(R.string.stsCancel);
			activity.updateGui(false);
			this.done = done;
			runningTask.interrupt();
		}
	}

	void startBuild(SelfCompileActivity activity, Intent done) {
		if (isIdle()) {
			activity.updateGui(false);
			this.done = done;
			runningTask = new Thread(new BuildTask(activity.getApplicationContext(), listener));
			runningTask.start();
		}
	}

}
