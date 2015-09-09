package nl.tudelft.selfcompileapp;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;

public class ModifyDrawables extends ProgressTask {

	static final int MDPI_ICON_PIXELS = 48;
	static final int HDPI_ICON_PIXELS = 72;
	static final int XHDPI_ICON_PIXELS = 96;
	static final int XXHDPI_ICON_PIXELS = 144;

	ModifyDrawables(UserInputFragment userInput, Context appContext, Handler listener) {
		super(userInput, appContext, listener);
	}

	private void writePngToAssetsDir() {

		try {
			Bitmap icon = userInput.appIcon;
			FileOutputStream pngIcon = new FileOutputStream(S.pngAppIcon);
			icon.compress(Bitmap.CompressFormat.PNG, 100, new BufferedOutputStream(pngIcon));
			pngIcon.close();

			if (setProgress(20)) {
				return;
			}
			icon = Bitmap.createScaledBitmap(userInput.appIcon, MDPI_ICON_PIXELS, MDPI_ICON_PIXELS, false);
			pngIcon = new FileOutputStream(S.pngAppIconMdpi);
			icon.compress(Bitmap.CompressFormat.PNG, 100, new BufferedOutputStream(pngIcon));
			pngIcon.close();
			icon.recycle();

			if (setProgress(40)) {
				return;
			}
			icon = Bitmap.createScaledBitmap(userInput.appIcon, HDPI_ICON_PIXELS, HDPI_ICON_PIXELS, false);
			pngIcon = new FileOutputStream(S.pngAppIconHdpi);
			icon.compress(Bitmap.CompressFormat.PNG, 100, new BufferedOutputStream(pngIcon));
			pngIcon.close();
			icon.recycle();

			if (setProgress(60)) {
				return;
			}
			icon = Bitmap.createScaledBitmap(userInput.appIcon, XHDPI_ICON_PIXELS, XHDPI_ICON_PIXELS, false);
			pngIcon = new FileOutputStream(S.pngAppIconXhdpi);
			icon.compress(Bitmap.CompressFormat.PNG, 100, new BufferedOutputStream(pngIcon));
			pngIcon.close();
			icon.recycle();

			if (setProgress(80)) {
				return;
			}
			icon = Bitmap.createScaledBitmap(userInput.appIcon, XXHDPI_ICON_PIXELS, XXHDPI_ICON_PIXELS, false);
			pngIcon = new FileOutputStream(S.pngAppIconXxhdpi);
			icon.compress(Bitmap.CompressFormat.PNG, 100, new BufferedOutputStream(pngIcon));
			pngIcon.close();
			icon.recycle();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void run() {

		try {
			if (setProgress(1, R.string.stsModifySource)) {
				return;
			}
			writePngToAssetsDir();

			if (setProgress(100)) {
				return;
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			Thread.currentThread().interrupt();
			setProgress(-1);
		}
	}

}
