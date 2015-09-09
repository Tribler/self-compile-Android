package nl.tudelft.selfcompileapp;

import java.io.File;

import org.w3c.dom.Document;

import android.content.Context;
import android.os.Handler;

public class ModifySource extends ProgressTask {

	ModifySource(UserInputFragment userInput, Context appContext, Handler listener) {
		super(userInput, appContext, listener);
	}

	private void modifyManifest() throws Exception {
		Document dom = Util.readXml(S.xmlMan);

		dom.getDocumentElement().getAttributes().getNamedItem("package").setNodeValue(userInput.appPackage);

		Util.writeXml(dom, S.xmlMan);
	}

	private void modifyJavaFiles() {
		int percent = 10;

		for (File javaFile : S.dirSrc.listFiles()) {

			if (!javaFile.isFile() || !javaFile.getName().endsWith(".java")) {
				continue;
			}
			Util.replaceFirstLine("package " + userInput.appPackage + ";", javaFile);

			percent += 5;
			if (setProgress(percent)) {
				return;
			}
		}
	}

	public void run() {

		try {
			if (setProgress(1, R.string.stsModifySource)) {
				return;
			}
			modifyManifest();

			if (setProgress(10, R.string.stsModifySource)) {
				return;
			}
			modifyJavaFiles();

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
