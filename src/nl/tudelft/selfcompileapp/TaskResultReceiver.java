package nl.tudelft.selfcompileapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

public class TaskResultReceiver extends ResultReceiver {

	public interface Receiver {
		public void onReceiveResult(int resultCode, Bundle resultData);
	}

	private Receiver receiver;

	public TaskResultReceiver(Handler handler) {
		super(handler);
	}

	public void setReceiver(Receiver receiver) {
		this.receiver = receiver;
	}

	@Override
	protected void onReceiveResult(int resultCode, Bundle resultData) {
		if (receiver != null) {
			receiver.onReceiveResult(resultCode, resultData);
		}
	}
}