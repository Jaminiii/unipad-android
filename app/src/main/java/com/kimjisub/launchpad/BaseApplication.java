package com.kimjisub.launchpad;

import android.app.Application;
import android.os.Build;
import android.support.multidex.MultiDexApplication;

import com.kimjisub.launchpad.manager.NotificationManager;

public class BaseApplication extends MultiDexApplication {
	@Override
	public void onCreate() {
		super.onCreate();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationManager.createChannel(this);
		}
	}
}