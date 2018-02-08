package com.kimjisub.launchpad;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Process;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;

import static com.kimjisub.launchpad.manage.Tools.lang;
import static com.kimjisub.launchpad.manage.Tools.log;

/**
 * Created by kimjisub on 2017-02-11.
 * Refactoring...
 */

public class BaseActivity extends AppCompatActivity {

	public static ArrayList<Activity> aList = new ArrayList();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	static void startActivity(Activity activity) {
		aList.add(activity);
		printActivityLog(activity.getLocalClassName() + " start");
	}

	static void finishActivity(Activity activity) {
		boolean exist = false;
		int size = aList.size();
		for (int i = 0; i < size; i++) {
			if (aList.get(i) == activity) {
				aList.get(i).finish();
				aList.remove(i);
				exist = true;
				break;
			}
		}
		printActivityLog(activity.getLocalClassName() + " finish" + (exist ? "" : " error"));
	}

	static void restartApp(Activity activity) {

		int size = aList.size();
		for (int i = size - 1; i >= 0; i--) {
			aList.get(i).finish();
			aList.remove(i);
		}
		activity.startActivity(new Intent(activity, Intro.class));
		printActivityLog(activity.getLocalClassName() + " requestRestart");

		Process.killProcess(Process.myPid());
	}

	static void printActivityLog(String log) {
		String str = "ACTIVITY STACK - " + log;
		int size = aList.size();
		for (int i = 0; i < size; i++) {
			Activity activity = aList.get(i);
			str += "\n" + activity.getLocalClassName();
		}
		log(str);
	}

	static void requestRestart(final Context context) {
		new AlertDialog.Builder(context)
			.setTitle(lang(context, R.string.requireRestart))
			.setMessage(lang(context, R.string.doYouWantToRestartApp))
			.setPositiveButton(lang(context, R.string.restart), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					restartApp((Activity) context);
					dialog.dismiss();
				}
			})
			.setNegativeButton(lang(context, R.string.cancel), new AlertDialog.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					((Activity) context).finish();
				}
			})
			.show();
	}
}