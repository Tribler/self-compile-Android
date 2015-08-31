package nl.tudelft.selfcompileapp;

import android.app.Activity;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

public class SingleTaskService extends IntentService {

	private Context appContext;

	// Must create a default constructor
	public SingleTaskService() {

		// Used to name the worker thread, important only for debugging.
		super("test-service");
	}

	public Context getContext() {
		return appContext;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		appContext = getApplicationContext();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// Extract the receiver passed into the service
		ResultReceiver receiver = intent.getParcelableExtra("receiver");

		// Extract additional values from the bundle
		String val = intent.getStringExtra("foo");

		// To send a message to the Activity, create and pass a Bundle
		Bundle bundle = new Bundle();
		bundle.putString("resultValue", "My Result Value. Passed in: " + val);

		// Here we call send passing a resultCode and the bundle of extras
		receiver.send(Activity.RESULT_OK, bundle);
	}
}