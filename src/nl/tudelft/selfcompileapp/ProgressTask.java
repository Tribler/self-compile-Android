package nl.tudelft.selfcompileapp;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

abstract public class ProgressTask implements Runnable {

	Context appContext;
	Handler listener;

	ProgressTask(Context appContext, Handler listener) {
		this.appContext = appContext;
		this.listener = listener;
	}

	boolean setProgress(int percent) {
		return setProgress(percent, 0);
	}

	boolean setProgress(int percent, int idString) {
		boolean stop = Thread.currentThread().isInterrupted();
		Message progress = new Message();
		if (stop) {
			progress.what = TaskManagerFragment.TASK_CANCELED;
		} else if (percent >= 100) {
			progress.what = TaskManagerFragment.TASK_FINISHED;
		} else {
			progress.what = TaskManagerFragment.TASK_PROGRESS;
		}
		progress.arg1 = percent;
		progress.arg2 = idString;
		listener.sendMessage(progress);
		return stop;
	}
}
