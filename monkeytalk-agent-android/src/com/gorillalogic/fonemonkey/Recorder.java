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
package com.gorillalogic.fonemonkey;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import android.app.Activity;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gorillalogic.fonemonkey.automators.AutomationManager;
import com.gorillalogic.fonemonkey.automators.IAutomator;
import com.gorillalogic.fonemonkey.server.PlaybackServer;
import com.gorillalogic.fonemonkey.utils.HttpUtils;
import com.gorillalogic.monkeytalk.Command;
import com.gorillalogic.monkeytalk.sender.CommandSender;
import com.gorillalogic.monkeytalk.sender.CommandSenderFactory;
import com.gorillalogic.monkeytalk.server.ServerConfig;

public class Recorder {
	private static PlaybackServer playbackServer;
	private static CommandSender recordSender;
	private static BlockingQueue<Command> queue;
	private static BlockingQueue<Command> remoteRecordingQueue;
	private static QueueMonitor queueMonitor;

	static {
		// Note that this is THE PlaybackServer for the agent -- it catches error when an existing
		// playback server exists, which prevents the application from crashing.
		startPlaybackServer();
		queue = new LinkedBlockingQueue<Command>();
		remoteRecordingQueue = new LinkedBlockingQueue<Command>();

	}

	/**
	 * If an existing Playback server is running in another app, it is shutdown when a record event
	 * is called from AutomatorBase. sendStopCommand is always called because the first time we
	 * start the app we don't know if another app has control of the port and it doesn't hurt
	 * anything to try sending a post request. If this func is called later on in the app then the
	 * goal is to restart the Playback server because another app killed it.
	 * 
	 */
	private static void startPlaybackServer() {
		sendStopCommand();
		try {
			playbackServer = new PlaybackServer();
		} catch (IOException e) {
			Log.log("Unable to start playback server", e);
		}
	}

	/**
	 * Stop an existing playback server to free up the port (as defined by
	 * {@link ServerConfig.DEFAULT_PLAYBACK_PORT_ANDROID}).
	 */
	private static void sendStopCommand() {
		// Sending message to another MT application on device. Localhost and default port.
		URI url = null;
		try {
			url = new URI("http", null, "localhost", ServerConfig.DEFAULT_PLAYBACK_PORT_ANDROID,
					"/", null, null);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		// Send the post request, response can be used for debugging
		new HttpUtils().post(url, "{\"mtcommand\": \"STOP\"}");
	}

	public static PlaybackServer getPlaybackServer() {
		return playbackServer;
	}

	private static boolean recording = false;

	public static boolean isRecording() {
		return recording;
	}

	public static boolean isPlayingBack() {
		return !recording;
	}

	public synchronized static void setRecording(boolean recording) {
		Recorder.recording = recording;
	}

	public static void setRecordServer(String recordHost, int recordPort) {
		recordSender = CommandSenderFactory.createCommandSender(recordHost, recordPort);
	}

	public static View findViewByResourceID(String resourceID) {
		for (View root : Recorder.getRoots()) {
			// Log.log("ROOT = " + root + "  RESID = " + resourceID +
			// "  IS SHOWN? " + root.isShown());
			if (!root.isShown())
				continue;
			View v = _findViewByResourceID(root, resourceID);
			if (v != null)
				return v;
		}

		Log.log("RESID " + resourceID + " NOT FOUND IN ANY ROOT");
		return null;
	}

	private static View _findViewByResourceID(View root, String resourceID) {
		int id = root.getId();

		if (id != View.NO_ID) {
			String name = root.getContext().getResources().getResourceName(id);

			if (name.equals(resourceID))
				return root;
		}

		if (!(root instanceof ViewGroup))
			return null;

		ViewGroup vg = (ViewGroup) root;
		for (int i = 0; i < vg.getChildCount(); ++i) {
			View v = _findViewByResourceID(vg.getChildAt(i), resourceID);
			if (v != null)
				return v;
		}

		return null;
	}

	public static View findViewByTextID(String textID) {
		for (View root : getRoots()) {
			// Log.log("ROOT = " + root + "  RESID = " + resourceID +
			// "  IS SHOWN? " + root.isShown());
			if (!root.isShown())
				continue;
			View v = _findViewByTextID(root, textID);
			if (v != null)
				return v;
		}

		Log.log("TEXTID " + textID + " NOT FOUND IN ANY ROOT");
		return null;
	}

	private static View _findViewByTextID(View root, String textID) {
		if (root instanceof TextView) {
			CharSequence text = ((TextView) root).getText();

			// String idName = StringResourceFinder.getIdName(rootActivity, text);
			String idName = StringResourceFinder
					.getIdName(AutomationManager.getTopActivity(), text);

			if (textID.equals(idName))
				return root;
		}

		if (!(root instanceof ViewGroup))
			return null;

		ViewGroup vg = (ViewGroup) root;
		for (int i = 0; i < vg.getChildCount(); ++i) {
			View v = _findViewByTextID(vg.getChildAt(i), textID);
			if (v != null)
				return v;
		}

		return null;
	}

	public static View findViewByItemText(CharSequence itemText) {
		for (View root : getRoots()) {
			// Log.log("ROOT = " + root + "  IS SHOWN? " + root.isShown());
			if (!root.isShown())
				continue;
			View v = _findViewByItemText(root, itemText);
			if (v != null)
				return v;
		}

		Log.log("ITEMTEXT " + itemText + " NOT FOUND IN ANY ROOT");
		return null;
	}

	private static View _findViewByItemText(View root, CharSequence itemText) {
		if (root instanceof TextView) {
			CharSequence text = ((TextView) root).getText();
			if (itemText.equals(text))
				return root;
		}

		if (!(root instanceof ViewGroup))
			return null;

		ViewGroup vg = (ViewGroup) root;
		for (int i = 0; i < vg.getChildCount(); ++i) {
			View v = _findViewByItemText(vg.getChildAt(i), itemText);
			if (v != null)
				return v;
		}

		return null;
	}

	public static View findViewByClassName(String className) {
		for (View root : getRoots()) {
			// Log.log("ROOT = " + root + "  RESID = " + resourceID +
			// "  IS SHOWN? " + root.isShown());
			if (!root.isShown())
				continue;
			View v = _findViewByClassName(root, className);
			if (v != null)
				return v;
		}

		Log.log("CLASSNAME " + className + " NOT FOUND IN ANY ROOT");
		return null;
	}

	private static View _findViewByClassName(View root, String className) {
		if (root.getClass().getName().equals(className))
			return root;

		if (!(root instanceof ViewGroup))
			return null;

		ViewGroup vg = (ViewGroup) root;
		for (int i = 0; i < vg.getChildCount(); ++i) {
			View v = _findViewByClassName(vg.getChildAt(i), className);
			if (v != null)
				return v;
		}

		return null;
	}

	public static View findViewByProperty(String methodName, Object customProperty) {
		for (View root : getRoots()) {
			// Log.log("ROOT = " + root + "  RESID = " + resourceID +
			// "  IS SHOWN? " + root.isShown());
			if (!root.isShown())
				continue;
			View v = _findViewByProperty(root, methodName, customProperty);
			if (v != null)
				return v;
		}

		Log.log("PROPERTY " + customProperty + " NOT FOUND IN ANY ROOT");
		return null;
	}

	private static View _findViewByProperty(View root, String methodName, Object customProperty) {
		try {
			Method[] m = root.getClass().getMethods();

			for (int i = 0; i < m.length; ++i) {
				if (m[i].getName().equals(methodName) && m[i].getParameterTypes().length == 0) {
					if (m[i].invoke(root, (Object[]) null).equals(customProperty))
						return root;
				}
			}
		} catch (SecurityException e) {
			throw new IllegalStateException(e);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		} catch (InvocationTargetException e) {
			throw new IllegalStateException(e);
		}

		if (!(root instanceof ViewGroup))
			return null;

		ViewGroup vg = (ViewGroup) root;
		for (int i = 0; i < vg.getChildCount(); ++i) {
			View v = _findViewByProperty(vg.getChildAt(i), methodName, customProperty);
			if (v != null)
				return v;
		}

		return null;
	}

	// TODO: This logic only works for a single ROOT (or if we get lucky).
	// Need to expand this to include the correct root view in the ordinal.
	static View findViewByOrdinal(String ordinal) {
		for (View root : getRoots()) {
			// Log.log("ORDINAL CHECKING NEXT ROOT");
			if (!root.isShown())
				continue;
			View v = _findViewByOrdinal(root, ordinal);
			if (v != null)
				return v;
		}

		// Log.log("ORDINAL " + ordinal + " NOT FOUND IN ANY ROOT");
		return null;
	}

	static View _findViewByOrdinal(View root, String ordinal) {
		// Log.log("ROOT = " + root + "  ORD = " + ordinal);
		if (root == null)
			return null;

		if (!(root instanceof ViewGroup))
			throw new IllegalStateException("Root view must be ViewGroup");

		int n;
		boolean isLast = false;

		int dot = ordinal.indexOf('.');

		if (dot < 0) {
			n = Integer.parseInt(ordinal);
			isLast = true;
		} else
			n = Integer.parseInt(ordinal.substring(0, dot));

		ViewGroup vg = (ViewGroup) root;
		View v = vg.getChildAt(n);

		if (isLast)
			return v;

		return _findViewByOrdinal(v, ordinal.substring(dot + 1));
	}

	private static Activity someActivity;

	// private static Object queueService;

	public static Activity getSomeActivity() {
		return someActivity;
	}

	public static void setSomeActivity(Activity a) {
		// ViewGroup vg = getTopMostParent(a.getWindow().getDecorView(), true);
		// root = a.getWindow().getDecorView().getRootView();
		someActivity = a;
	}

	public static void prViewTree() {
		for (View root : getRoots()) {
			_prViewTree(root, "");
			Log.log("-------------------------------------------------------");
		}
	}

	private static void _prViewTree(View v, String indent) {
		Log.log(indent + v.getClass().getName());
		if (v instanceof ViewGroup) {
			ViewGroup vg = (ViewGroup) v;
			for (int i = 0; i < vg.getChildCount(); ++i) {
				_prViewTree(vg.getChildAt(i), indent + "    ");
			}
		}
	}

	public static void record(String action, IAutomator automator, String[] args) {
		// Check to ensure another app hasn't killed the Playback server
		if (!playbackServer.isRunning()) {
			startPlaybackServer();
		}

		String monkeyId = AutomationManager.findIndexedMonkeyIdIfAny(automator);
		Command cmd = new Command(automator.getComponentType(), monkeyId, action,
				(args != null ? Arrays.asList(args) : null), null);

		recordCommand(cmd);
	}

	public static void recordCommand(Command cmd) {
		// Turn on real-time recording if IDE is reachable
		if (recordSender == null) {
			recordSender = CommandSenderFactory.createCommandSender("localhost", ServerConfig.DEFAULT_RECORD_PORT);
		}
		if (queueMonitor == null) {
			queueMonitor = new QueueMonitor();
		}
		if (recording) {
			// if (queueMonitor == null && queueService == null) {
			// try {
			// queueService = new QueueService(CLOUD_RECORD_QUEUE_PORT);
			// } catch (IOException e) {
			// Log.log("Unable to enable remote MonkeyTalk IDE recording");
			// }
			// }
			// Log.log("Recording is on: " + action);
			Log.log("ENQUEUEING - " + cmd);
			try {
				queue.put(cmd);
			} catch (InterruptedException ex) {
				Log.log(ex);
			}

		} else {
			// Log.log("Recording is off: " + action);
		}
	}

	// /////////////////////////////////////////////////////////////////////////////////////

	public static boolean isAppInForeground() {
		try {
			android.app.ActivityManager activityManager = (android.app.ActivityManager) someActivity
					.getSystemService(Context.ACTIVITY_SERVICE);
			List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
			if (appProcesses == null) {
				return false;
			}

			final String packageName = someActivity.getPackageName(); // ?
			for (RunningAppProcessInfo appProcess : appProcesses) {
				if (appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND
						&& appProcess.processName.equals(packageName)) {
					return true;
				}
			}
			return false;
		} catch (Exception e) {
			Log.log(e);
		}

		return false;
	}

	private static Set<View> getRoots() {
		return FunctionalityAdder.getRoots();
	}

	public static class QueueMonitor {

		public QueueMonitor() {
			new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						while (true) {
							Command cmd = queue.take();
							Log.log("SENDING - " + cmd);
							// Response resp = recordSender.record(cmd);
							// if (resp.getCode() != 200) {
							// Log.log("Unable to record: "
							// + cmd.getCommand()
							// +
							// " - If you're running on a device, you must connect to Wi-Fi. You cannot record over a tether: "
							// + resp.getMessage());
							if (remoteRecordingQueue == null) {
								Log.log("RRQ Null");
							}
							if (cmd == null) {
								Log.log("CMD Null");
							}
							// try {
							// remoteRecordingQueue.put(cmd);
							// } catch (Exception e) {
							// Log.log("Remote Recording Queue error", e);
							// }
							remoteRecordingQueue.put(cmd);
						}
						// }
					} catch (InterruptedException ex) {
						Log.log(ex);
					}
				}
			}).start();
		}
	}

	public synchronized static List<Command> pollQueue() {
		List<Command> list = new ArrayList<Command>();
		Command c = null;
		while (!remoteRecordingQueue.isEmpty()) {
			try {
				// Block until there's something in the queue
				c = remoteRecordingQueue.take();
				list.add(c);
			} catch (InterruptedException e) {
				//
			}
			// Keep reading until empty
		}

		return list;
	}

	public static void clearQueue() {
		Recorder.remoteRecordingQueue.clear();
	}
}