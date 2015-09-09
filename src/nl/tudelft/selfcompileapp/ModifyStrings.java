package nl.tudelft.selfcompileapp;

import org.w3c.dom.Document;

import android.content.Context;
import android.os.Handler;

public class ModifyStrings extends ProgressTask {

	ModifyStrings(UserInputFragment userInput, Context appContext, Handler listener) {
		super(userInput, appContext, listener);
	}

	private void modifyStrings() throws Exception {
		Document dom = Util.readXml(S.xmlStrings);

		dom.getElementsByTagName("string").item(0).setTextContent(userInput.appName);

		Util.writeXml(dom, S.xmlStrings);
	}

	public void run() {

		try {
			if (setProgress(1, R.string.stsModifySource)) {
				return;
			}
			modifyStrings();

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
