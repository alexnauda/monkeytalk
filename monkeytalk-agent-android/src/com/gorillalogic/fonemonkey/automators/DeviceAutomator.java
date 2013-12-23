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
package com.gorillalogic.fonemonkey.automators;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.View;

import com.gorillalogic.fonemonkey.Log;
import com.gorillalogic.monkeytalk.automators.AutomatorConstants;

/**
 */
public class DeviceAutomator extends AutomatorBase {

	private static final int SCREENSHOT_RETRIES = 5;
	private static final long SCREENSHOT_RETRIES_DELAY = 2000;

	static {
		Log.log("Initializing DeviceAutomator");
	}

	@Override
	public String getComponentType() {
		return AutomatorConstants.TYPE_DEVICE;
	}

	@Override
	public String play(String action, final String... args) {

		if (action.equalsIgnoreCase(AutomatorConstants.ACTION_BACK)) {
			return back();
		} else if (action.equalsIgnoreCase(AutomatorConstants.ACTION_MENU)) {
			final Activity activity = AutomationManager.getTopActivity();
			if (activity != null) {
				AutomationManager.runOnUIThread(new Runnable() {
					public void run() {
						activity.openOptionsMenu();
					}
				});

				// HACK: wait a little for menu to open
				try {
					Thread.sleep(500);
				} catch (InterruptedException ex) {
					// ignore
				}
			}
			return null;
		} else if (action.equalsIgnoreCase(AutomatorConstants.ACTION_DUMP)) {
			// Dump component tree to logcat
			return AutomationManager.dumpViewTree();
		} else if (action.equalsIgnoreCase(AutomatorConstants.ACTION_ROTATE)) {
			final Activity activity = AutomationManager.getTopActivity();
			if (activity != null) {
				assertArgCount(AutomatorConstants.ACTION_ROTATE, args, 1);
				if (!("landscape".equalsIgnoreCase(args[0]) || "portrait".equalsIgnoreCase(args[0]))) {
					throw new IllegalArgumentException(
							"Expected \"portrait\" or \"landscape\" but found " + args[0]);
				}
				AutomationManager.runOnUIThread(new Runnable() {
					public void run() {
						if ("landscape".equalsIgnoreCase(args[0])) {
							activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
						} else if ("portrait".equalsIgnoreCase(args[0])) {
							activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
						} else {
							Log.log("Unexpected rotation value: " + args[0]);
						}
					}
				});

				// wait for rotation
				try {
					Thread.sleep(250);
				} catch (InterruptedException ex) {
					// ignore
				}
			}
			return null;
		} else if (action.equalsIgnoreCase(AutomatorConstants.ACTION_SCREENSHOT)) {
			for (int i = 0; i < SCREENSHOT_RETRIES; i++) {
				printMemoryInfo();
				System.gc();
				try {
					final Activity activity = AutomationManager.getTopActivity();
					if (activity != null) {

						// get the root view from the activity
						View v = activity.getWindow().getDecorView()
								.findViewById(android.R.id.content).getRootView();

						boolean enabled = v.isDrawingCacheEnabled();
						Bitmap bitmap;
						try {

							v.setDrawingCacheEnabled(true);
							// Android 3.2 mdpi and 4.0.3 1280x800 mdpi nullpointer
							Bitmap dc = v.getDrawingCache();
							if (dc == null) {
								// throw new
								// IllegalStateException("No screenshot available (unable to access drawing cache).");
								bitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(),
										Bitmap.Config.ARGB_8888);
								Canvas canvas = new Canvas(bitmap);
								v.draw(canvas);
							} else {
								bitmap = Bitmap.createBitmap(dc);
							}
						} catch (Exception e) {
							Log.log(e);
							throw new IllegalStateException("No screenshot available.");
						} finally {
							v.setDrawingCacheEnabled(enabled);
						}
						try {
							// write the bitmap to bytes
							ByteArrayOutputStream out = new ByteArrayOutputStream();
							boolean success = bitmap.compress(Bitmap.CompressFormat.PNG, 0, out);
							out.flush();
							out.close();
							bitmap.recycle();
							bitmap = null;
							System.gc();
							if (success) {
								// return the base64 encoded bytes...
								return "{screenshot:\""
										+ Base64.encodeToString(out.toByteArray(), Base64.DEFAULT)
										+ "\"}";
							} else {
								Log.log("Screenshot unsuccessful.");
								throw new IllegalStateException("No screenshot available.");
							}
						} catch (IOException ex) {
							Log.log(ex);
							throw new IllegalStateException("No screenshot available. - "
									+ ex.getMessage());
						}
					}
					Log.log("No root activity for screenshot");
					throw new IllegalStateException("No screenshot available.");
				} catch (Exception e) {
					if (i + 1 == SCREENSHOT_RETRIES) {
						throw new RuntimeException(e);
					} else {
						try {
							Thread.sleep(SCREENSHOT_RETRIES_DELAY);
						} catch (InterruptedException e1) {
						}
					}
				}
			}
		}

		return super.play(action, args);
	}

	public static String back() {
		final Activity activity = AutomationManager.getTopActivity();
		if (activity != null) {
			AutomationManager.runOnUIThread(new Runnable() {
				public void run() {
					activity.onBackPressed();
				}
			});
		}
		return null;
	}

	@Override
	public String getValue() {
		return getOs();
	}

	@Override
	public String getValue(String path) {
		// Devices have no "native" properties. Call super to throw exception
		return super.getProperty(path);
	}

	public String getOs() {
		return "Android";
	}

	public String getVersion() {
		return Build.VERSION.RELEASE;
	}

	public String getResolution() {
		final Activity activity = AutomationManager.getTopActivity();
		if (activity != null) {
			DisplayMetrics metrics = new DisplayMetrics();
			activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
			return metrics.widthPixels + "x" + metrics.heightPixels;
		}
		return "unknown";
	}

	public String getDensityDpi() {
		final Activity activity = AutomationManager.getTopActivity();
		if (activity != null) {
			DisplayMetrics metrics = new DisplayMetrics();
			activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
			return String.valueOf(metrics.densityDpi);
		}
		return "unknown";
	}

	public String getName() {
		return Build.MODEL;
	}

	public String getOrientation() {
		Activity activity = AutomationManager.getTopActivity();
		return (activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? "landscape"
				: "portrait");
	}

	@Override
	protected String getProperty(String prop) {
		if ("os".equals(prop)) {
			return getOs();
		} else if ("version".equals(prop)) {
			return getVersion();
		} else if ("resolution".equals(prop)) {
			return getResolution();
		} else if ("name".equals(prop)) {
			return getName();
		} else if ("orientation".equals(prop)) {
			return getOrientation();
		} else if ("battery".equals(prop)) {
			return getBattery();
		} else if ("memory".equals(prop)) {
			return getMemory(false);
		} else if ("cpu".equals(prop)) {
			return getCPU();
		} else if ("diskspace".equals(prop)) {
			return getDiskSpace();
		} else if ("allinfo".equals(prop)) {
			return getMemory(false) + "," + getCPU() + "," + getDiskSpace() + "," + getBattery();
		} else if ("totalDiskSpace".equals(prop)) {
			return getTotalDiskSpace();
		} else if ("totalMemory".equals(prop)) {
			return getMemory(true);
			// } else if ("densityDpi".equals(prop)) {
			// return getDensityDpi();
		}

		return super.getProperty(prop);
	}

	/**
	 * @return the percentage of the disk (internal storage, possibly sdcard) that is in use
	 */
	private String getDiskSpace() {
		// get the root directory to measure memory from
		StatFs statFs = new StatFs(Environment.getRootDirectory().getAbsolutePath());
		// available blocks are blocks that are available to anything - free blocks can be reserved
		int usedMem = (statFs.getBlockCount() - statFs.getAvailableBlocks());
		return (usedMem * 100) / statFs.getBlockCount() + "%";
	}

	/**
	 * Get the total disk space in the system
	 * 
	 * @return the total disk space in kB
	 */
	private String getTotalDiskSpace() {
		StatFs statFs = new StatFs(Environment.getRootDirectory().getAbsolutePath());
		return (statFs.getBlockCount() * statFs.getBlockSize()) + " bytes";
	}

	/**
	 * Gets the percentage of the battery (0% discarged, 100% fully charged)
	 * 
	 * @return a string representing the percentage of the battery that is charged
	 */
	private String getBattery() {
		Context context = AutomationManager.getTopActivity().getApplicationContext();
		Intent batteryStatus = context.registerReceiver(null, new IntentFilter(
				Intent.ACTION_BATTERY_CHANGED));
		int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
		int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
		return (level * 100) / scale + "%";
	}

	/**
	 * Gets the ram currently in use by the system and returns it as a percentage
	 * 
	 * @return a string with the percentage of the ram currently in use, or the total kB of ram in
	 *         the system
	 * @param total
	 *            whether to get a percentage or the total memory
	 */
	private String getMemory(boolean total) {
		// we're not using the MemoryInfo class because totalMemory is only available in SDK level
		// 14+
		try {
			// read the proc meminfo file
			RandomAccessFile reader = new RandomAccessFile("/proc/meminfo", "r");
			try {
				String memTotalRaw = reader.readLine();
				String memFreeRaw = reader.readLine();
				if (!memTotalRaw.contains("MemTotal") || !memFreeRaw.contains("MemFree")) {
					return "error";
				}
				int memTotal = Integer.parseInt(memTotalRaw.split(":")[1].replaceAll(" ", "")
						.replaceAll("kB", ""));
				if (total) {
					return (memTotal * 1024) + " bytes";
				}
				int memFree = Integer.parseInt(memFreeRaw.split(":")[1].replaceAll(" ", "")
						.replaceAll("kB", ""));
				return (memFree * 100) / memTotal + "%";
			} catch (Exception e) {
				return "error";
			} finally {
				reader.close();
			}
		} catch (Exception e) {
			return "error";
		}
	}

	/**
	 * @return a string with the percentage of the CPU cycles in use over a sample period of 300ms
	 */
	private String getCPU() {
		try {
			RandomAccessFile statReader = new RandomAccessFile("/proc/stat", "r");
			try {
				String line = statReader.readLine();
				// only if the line contains cpu (with a space) is it a summary
				// there should be 10 or eleven elements
				if (!line.contains("cpu ")
						|| (line.split("\\s+").length != 10 && line.split("\\s+").length != 11)) {
					return "unknown-unexpected stat";
				}
				/*- Information from proc is a aggregate since bootup
				 * cpuComponents should be as follows:
				 * 0 - cpu name (cpu0, cpu1 or cpu for aggregate)
				 * 1 - user mode processor cycles
				 * 2 - "nice" processes
				 * 3 - system processes
				 * 4 - idle time
				 * 5 - time spent waiting on io
				 * 6 - servicing interrupts
				 * 7 - softirq
				 */
				String[] cpuComponents = line.split("\\s+");
				// all time spent anywhere but 4 (idle) we will consider in use
				long inUse1 = Long.parseLong(cpuComponents[1]) + Long.parseLong(cpuComponents[2])
						+ Long.parseLong(cpuComponents[3]) + Long.parseLong(cpuComponents[5])
						+ Long.parseLong(cpuComponents[6]) + Long.parseLong(cpuComponents[7]);
				long idle1 = Long.parseLong(cpuComponents[4]);
				// because proc numbers are since startup, we need to sample over a timeperiod
				try {
					Thread.sleep(360);
				} catch (Exception e) {
					return "error sleeping";
				}
				// second sample
				statReader.seek(0);
				line = statReader.readLine();
				cpuComponents = line.split("\\s+");
				long inUse2 = Long.parseLong(cpuComponents[1]) + Long.parseLong(cpuComponents[2])
						+ Long.parseLong(cpuComponents[3]) + Long.parseLong(cpuComponents[5])
						+ Long.parseLong(cpuComponents[6]) + Long.parseLong(cpuComponents[7]);
				long idle2 = Long.parseLong(cpuComponents[4]);

				return ((inUse2 - inUse1) * 100) / ((inUse2 + idle2) - (inUse1 + idle1)) + "%";
			} catch (Exception e) {
				return "unknown-error parsing" + e;
			} finally {
				statReader.close();
			}
		} catch (Exception e) {
			return "unknown-error reading proc";
		}
	}

	private void printMemoryInfo() {
		ActivityManager am = (ActivityManager) AutomationManager.getTopActivity().getSystemService(
				Service.ACTIVITY_SERVICE);
		Log.log("Memory Class: " + am.getMemoryClass());
		MemoryInfo mi = new MemoryInfo();
		am.getMemoryInfo(mi);
		Log.log("Available memory: " + mi.availMem);
		Log.log("Low memory: " + mi.lowMemory);

	}

	@Override
	protected Rect getBoundingRectangle() {
		int x = 0;
		int y = 0;
		int w = 1;
		int h = 1;
		final Activity activity = AutomationManager.getTopActivity();
		if (activity != null) {
			DisplayMetrics metrics = new DisplayMetrics();
			activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
			w = metrics.widthPixels;
			h = metrics.heightPixels;
		}
		return new Rect(0, 0, w, h);
	}

}