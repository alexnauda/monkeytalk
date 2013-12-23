/*  MonkeyTalk - a cross-platform functional testing tool
    Copyright (C) 2012 Gorilla Logic, Inc.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>. */
package com.gorillalogic.fonemonkey.aspects;

import java.lang.reflect.Method;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.PopupWindow;

import com.gorillalogic.fonemonkey.ActivityManager;
import com.gorillalogic.fonemonkey.FunctionalityAdder;
import com.gorillalogic.fonemonkey.Log;
import com.gorillalogic.fonemonkey.Recorder;

/**
 * Currently we rely on OnGlobalLayoutChange notifications to let us know about components being
 * added to tree but since we global layout changes are apparently not triggered by Dialogs and
 * PopUpWindows, the hacks here are used instead.
 * 
 * The onCreate Activity stuff is currently needed to boostrap MonkeyTalk.
 */
aspect OnCreate {
	String firstActivity;

	pointcut captureOnCreate() : (execution(* onCreate(Bundle)));

	before(): captureOnCreate()
	{
		Object target = thisJoinPoint.getTarget();

		if (firstActivity == null)
			firstActivity = target.getClass().getName();
	}

	after(): captureOnCreate()
	{
		Object target = thisJoinPoint.getTarget();

		if (!(target instanceof Activity))
			return;

		Activity a = (Activity) target;

		if (firstActivity.equals(target.getClass().getName())) {
			Recorder.setSomeActivity((Activity) target);
		}

		ActivityManager.addActivity(a);

		FunctionalityAdder.walkTree(a.getWindow().getDecorView().getRootView());
	}

	pointcut captureCreate() : (call(* create()));

	after() returning (AlertDialog d): captureCreate()
	{
		ActivityManager.setCurrentDialog(d);
	}

	pointcut captureOnCreateDialog() : (execution(* onCreateDialog(int)));

	after() returning (AlertDialog d): captureOnCreateDialog()
	{
		ActivityManager.setCurrentDialog(d);
	}

	pointcut captureShow() : (call(* showDialog(int)));

	after() returning: captureShow()
	{
		try {
			FunctionalityAdder.walkTree(ActivityManager.getCurrentDialog().getWindow()
					.getDecorView().getRootView());
		} catch (Exception ex) {
			System.err.println("Error in OnCreate aspect");
			ex.printStackTrace();
		}
	}

	pointcut capturePopShow(PopupWindow p): target(p) && (call(* show*(..)));

	after(PopupWindow p) returning: capturePopShow(p)
	{
		FunctionalityAdder.walkTree(p.getContentView());
	}

	pointcut captureDialogShow(Dialog p): target(p) && (call(* show*(..)));

	after(Dialog p) returning: captureDialogShow(p)
	{
		if (p.getWindow() != null) {
			View v = p.getWindow().getDecorView();
			FunctionalityAdder.walkTree(v);
		}
	}
	
	pointcut captureDialogFragmentShow(Object dialogFragment): target(dialogFragment) && (call(* show(..)));

	after(Object dialogFragment) returning: captureDialogFragmentShow(dialogFragment)
	{
		Class<?> clazz;
		for (clazz = dialogFragment.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
			if (clazz.getName().endsWith(".DialogFragment")) {
				break;
			}
		}
		if (clazz == null) {
			return;
		}
		Dialog dialog;
		try {
			Method m = dialogFragment.getClass().getMethod("getFragmentManager", (Class<?>[]) null);
			Object fm = m.invoke(dialogFragment, (Object[]) null);
			m = fm.getClass().getMethod("executePendingTransactions", (Class<?>[]) null);
			m.invoke(fm, (Object[]) null);
			m = dialogFragment.getClass().getMethod("getDialog", (Class<?>[]) null);
			dialog = (Dialog) m.invoke(dialogFragment, (Object[]) null);
			if (dialog.getWindow() != null) {
				View v = dialog.getWindow().getDecorView();
				FunctionalityAdder.walkTree(v);
			}
		} catch (Exception e) {
			Log.log("Unable to handle dialog fragment", e);
		}

	}
	
	pointcut captureShowAlert() : (call(* show()));
	
	after() returning (AlertDialog d): captureShowAlert()
	{
		ActivityManager.setCurrentDialog(d);
		View v = d.getWindow().getDecorView();
		FunctionalityAdder.walkTree(v);
	}
}