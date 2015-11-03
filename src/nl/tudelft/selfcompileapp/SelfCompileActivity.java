package nl.tudelft.selfcompileapp;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Arrays;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
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

		initTaskManager();
		initUserInput();

		if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC)) {
			initNfc();
		}

		// Reset on first run
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

		S.mkDirs();
	}

	private void initUserInput() {
		// Fetch saved user input
		FragmentManager fm = getFragmentManager();
		userInput = (UserInputFragment) fm.findFragmentByTag(UserInputFragment.class.getSimpleName());

		if (userInput == null) {
			userInput = new UserInputFragment();

			fm.beginTransaction().add(userInput, UserInputFragment.class.getSimpleName()).commit();

			// Default name
			userInput.appName = getString(R.string.appName);

			// Default icon
			userInput.appIcon = BitmapFactory.decodeResource(getResources(), R.drawable.app_icon);

			// Default theme
			userInput.appTheme = "Theme." + getResources().getStringArray(R.array.appThemes)[0];

			// Default package
			userInput.appPackage = getApplicationContext().getPackageName();
		}

		// Restore previous user input
		try {
			setTheme(userInput.getAppThemeId());

		} catch (Exception e) {
			e.printStackTrace();
		}

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

		// Set current working state
		updateGui(taskManager.isIdle());

		// Restore previous user input
		txtAppName.setText(userInput.getAppName());
		btnAppIcon.setImageBitmap(userInput.getAppIcon(getApplicationContext()));
		spnAppTheme.setSelection(
				Arrays.asList(getResources().getStringArray(R.array.appThemes)).indexOf(userInput.getAppTheme()));
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
		/**
		 * @source http://stackoverflow.com/a/9375624
		 */
		spnAppTheme.post(new Runnable() {
			public void run() {
				spnAppTheme.setOnItemSelectedListener(new OnItemSelectedListener() {

					public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
						userInput.setAppTheme("Theme." + getResources().getStringArray(R.array.appThemes)[position]);
						recreate();
					}

					public void onNothingSelected(AdapterView<?> parent) {
					}

				});
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
					Bitmap b = BitmapFactory.decodeStream(is);
					Bitmap icon = Bitmap.createScaledBitmap(b, ModifyDrawables.XXHDPI_ICON_PIXELS,
							ModifyDrawables.XXHDPI_ICON_PIXELS, false);
					is.close();
					b.recycle();

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

	String appName;
	Bitmap appIcon;
	String appPackage;
	String appTheme;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	////////////////// GETTERS ////////////////////

	Bitmap getAppIcon(Context appContext) {
		try {
			InputStream is = appContext.getAssets().open(S.pngAppIcon.getName());
			Bitmap b = BitmapFactory.decodeStream(is);
			appIcon = Bitmap.createScaledBitmap(b, ModifyDrawables.XXHDPI_ICON_PIXELS,
					ModifyDrawables.XXHDPI_ICON_PIXELS, false);
			is.close();
			b.recycle();

			System.out.println("Icon: assets/" + S.pngAppIcon.getName());

		} catch (Exception e) {
			e.printStackTrace();
		}
		return appIcon;
	}

	String getAppName() {
		try {
			Document dom = Util.readXml(S.xmlStrings);
			appName = dom.getElementsByTagName("string").item(0).getTextContent();

			System.out.println("Name: " + appName);

		} catch (Exception e) {
			e.printStackTrace();
		}

		// Set name of the final output file
		S.apkRedistributable = new File(S.dirRoot, appName + ".apk");

		return appName;
	}

	String getAppTheme() {
		try {
			Document dom = Util.readXml(S.xmlStyles);

			NodeList lstNode = dom.getElementsByTagName("style");
			for (int i = 0; i < lstNode.getLength(); i++) {
				Node node = lstNode.item(i);

				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element elmnt = (Element) node;
					appTheme = elmnt.getAttribute("parent").replace("android:", "");

					System.out.println("Theme: " + appTheme);
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return appTheme;
	}

	Integer getAppThemeId() throws Exception {
		String fieldName = appTheme.replace(".", "_");
		System.out.println("android.R.style." + fieldName);

		Field f = android.R.style.class.getField(fieldName);
		Class<?> t = f.getType();
		if (t == int.class) {
			return f.getInt(null);
		}
		return null;
	}

	String getAppPackage() {
		try {
			Document dom = Util.readXml(S.xmlMan);
			appPackage = dom.getDocumentElement().getAttributes().getNamedItem("package").getNodeValue();

			System.out.println("Package: " + appPackage);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return appPackage;
	}

	////////////////// SETTERS ////////////////////

	void setAppIcon(Bitmap icon) {
		appIcon = icon;

		if (isAdded()) {
			SelfCompileActivity activity = (SelfCompileActivity) getActivity();
			activity.taskManager.modifyDrawables(activity, null);
		}
	}

	void setAppName(String name) {
		appName = name;

		if (isAdded()) {
			SelfCompileActivity activity = (SelfCompileActivity) getActivity();
			activity.taskManager.modifyStrings(activity, null);
		}
	}

	void setAppTheme(String theme) {
		appTheme = theme;

		if (isAdded()) {
			SelfCompileActivity activity = (SelfCompileActivity) getActivity();
			activity.taskManager.modifyStyles(activity, null);
		}
	}

	void setAppPackage(String packagePath) {
		appPackage = packagePath;

		if (isAdded()) {
			SelfCompileActivity activity = (SelfCompileActivity) getActivity();
			activity.taskManager.modifySource(activity, null);
		}
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

	void cancelTask(SelfCompileActivity activity, Intent done) {
		if (!isIdle()) {
			strStatus = activity.getString(R.string.stsCancel);
			activity.updateGui(false);
			this.done = done;
			runningTask.interrupt();
		}
	}

	void startClean(SelfCompileActivity activity, Intent done) {
		if (isIdle()) {
			activity.updateGui(false);
			this.done = done;
			runningTask = new Thread(new CleanTask(activity.userInput, activity.getApplicationContext(), listener));
			runningTask.start();
		}
	}

	void modifyDrawables(SelfCompileActivity activity, Intent done) {
		if (isIdle()) {
			activity.updateGui(false);
			this.done = done;
			runningTask = new Thread(
					new ModifyDrawables(activity.userInput, activity.getApplicationContext(), listener));
			runningTask.start();
		}
	}

	void modifyStrings(SelfCompileActivity activity, Intent done) {
		if (isIdle()) {
			activity.updateGui(false);
			this.done = done;
			runningTask = new Thread(new ModifyStrings(activity.userInput, activity.getApplicationContext(), listener));
			runningTask.start();
		}
	}

	void modifyStyles(SelfCompileActivity activity, Intent done) {
		if (isIdle()) {
			activity.updateGui(false);
			this.done = done;
			runningTask = new Thread(new ModifyStyles(activity.userInput, activity.getApplicationContext(), listener));
			runningTask.start();
		}
	}

	void modifySource(SelfCompileActivity activity, Intent done) {
		if (isIdle()) {
			activity.updateGui(false);
			this.done = done;
			runningTask = new Thread(new ModifySource(activity.userInput, activity.getApplicationContext(), listener));
			runningTask.start();
		}
	}

	void startBuild(SelfCompileActivity activity, Intent done) {
		if (isIdle()) {
			activity.updateGui(false);
			this.done = done;
			runningTask = new Thread(new BuildTask(activity.userInput, activity.getApplicationContext(), listener));
			runningTask.start();
		}
	}

}
