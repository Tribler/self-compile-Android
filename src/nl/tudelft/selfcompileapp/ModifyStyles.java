package nl.tudelft.selfcompileapp;

import java.io.File;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.content.Context;
import android.os.Handler;

public class ModifyStyles extends ProgressTask {

	ModifyStyles(UserInputFragment userInput, Context appContext, Handler listener) {
		super(userInput, appContext, listener);
	}

	private void modifyStyles(File xmlStyles) throws Exception {
		try {
			Document dom = Util.readXml(xmlStyles);

			NodeList lstNode = dom.getElementsByTagName("style");
			for (int i = 0; i < lstNode.getLength(); i++) {
				Node node = lstNode.item(i);

				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element elmnt = (Element) node;
					elmnt.setAttribute("parent", "android:" + userInput.appTheme);
				}
			}

			Util.writeXml(dom, S.xmlMan);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void run() {

		try {
			if (setProgress(1, R.string.stsModifySource)) {
				return;
			}
			modifyStyles(S.xmlStyles);

			if (setProgress(33, R.string.stsModifySource)) {
				return;
			}
			modifyStyles(S.xmlStyles11);

			if (setProgress(66, R.string.stsModifySource)) {
				return;
			}
			modifyStyles(S.xmlStyles14);

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
