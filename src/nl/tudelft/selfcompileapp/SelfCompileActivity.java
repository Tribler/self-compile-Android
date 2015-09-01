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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

class UserInputFragment extends Fragment {

	protected Bitmap appIcon;
	protected String appName;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}
}

public class SelfCompileActivity extends Activity {

	UserInputFragment userInput;
	ProgressFragment progress;

	ImageButton btnAppIcon = (ImageButton) findViewById(R.id.btnAppIcon);
	EditText txtAppName = (EditText) findViewById(R.id.txtAppName);

	Button btnReset = (Button) findViewById(R.id.btnReset);
	Button btnCancel = (Button) findViewById(R.id.btnCancel);
	Button btnInstall = (Button) findViewById(R.id.btnInstall);

	static final int SELECT_PHOTO = 1;
	static final int ICON_WIDTH = 150;
	static final int ICON_HEIGHT = 150;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_morph);

		initUserInput();
		initProgressListener();

		boolean working = progress.inProgess();
		btnReset.setEnabled(!working);
		btnCancel.setEnabled(working);
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
		progress = (ProgressFragment) fm.findFragmentByTag("progress");

		if (progress == null) {
			progress = new ProgressFragment();

			fm.beginTransaction().add(progress, "progess").commit();
		}
	}

	public void btnReset(View btnReset) {
	}

	public void btnCancel(View btnCancel) {

	}

	public void btnInstall(View btnInstall) {

	}

}
