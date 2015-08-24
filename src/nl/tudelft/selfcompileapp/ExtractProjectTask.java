package nl.tudelft.selfcompileapp;

import android.content.Context;
import android.content.res.AssetManager;

public class ExtractProjectTask extends ProgressStatusTask {

	public ExtractProjectTask(Context app) {
		super(app, 6);
	}

	@Override
	protected Object doInBackground(Object... params) {
		try {
			if (setMsg("CLEAN PROJECT FOLDER"))
				return null;
			Util.deleteRecursive(S.dirProj);
			S.mkDirs();
			AssetManager asset = app.getAssets();

			if (setMsg("EXTRACT PROJECT SOURCE"))
				return null;
			Util.unzip(asset.open("src.zip"), S.dirSrc);
			Util.copy(asset.open(S.xmlMan.getName()), S.xmlMan);
			Util.copy(asset.open(S.jksEmbedded.getName()), S.jksEmbedded);

			if (setMsg("EXTRACT PROJECT RESOURCES"))
				return null;
			Util.unzip(asset.open("res.zip"), S.dirRes);
			Util.copy(asset.open(S.pngAppIcon.getName()), S.pngAppIcon);

			if (setMsg("EXTRACT PROJECT DEPENDENCIES"))
				return null;
			Util.unzip(asset.open("libs.zip"), S.dirLibs);
			Util.unzip(asset.open("dexedLibs.zip"), S.dirDexedLibs);

			if (setMsg("EXTRACT PROJECT PLATFORM"))
				return null;
			Util.copy(asset.open(S.jarAndroid.getName()), S.jarAndroid);

		} catch (Exception e) {

			e.printStackTrace();
		}
		return null;
	}

}