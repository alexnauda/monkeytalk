package com.gorillalogic.monkeytalk.utils;

import java.io.File;
import java.util.Map;

import com.gorillalogic.monkeytalk.utils.exec.Exec;
import com.gorillalogic.monkeytalk.utils.exec.ExecResult;
import com.gorillalogic.monkeytalk.utils.exec.ExecStatus;

/**
 * Helper class to find the path to the Android SDK, and other Android tools like ADB.
 */
public class AndroidUtils {
	public static final String ADB_LOCATION = "platform-tools" + File.separator + "adb";
	public static final long ADB_RETRY_TIME = 1000;
	protected static final String SDK_PATH = null;

	/**
	 * Helper to get the path to ADB if the ANDROID_SDK (or ANDROID_HOME) environment variable is
	 * set.
	 * 
	 * @return the path to ADB, or {@code null}.
	 */
	public static File getAdb() {
		return getAdb(getSdk());
	}

	/**
	 * Helper to get the path to ADB as a String if the ANDROID_SDK (or ANDROID_HOME) environment
	 * variable is set.
	 * 
	 * @return the path to ADB as a String, or {@code null}.
	 */
	public static String getAdbPath() {
		File adb = getAdb(getSdk());
		return (adb != null ? adb.getAbsolutePath() : null);
	}

	/**
	 * Helper to get the path to ADB given the path to the Android SDK.
	 * 
	 * @param sdk
	 *            the path to the Android SDK
	 * @return the path to ADB, or {@code null}.
	 */
	public static File getAdb(File sdk) {
		if (sdk != null && sdk.exists() && sdk.isDirectory()) {
			File adb = new File(sdk, ADB_LOCATION);
			if (adb.exists() && adb.isFile()) {
				return adb;
			}
		}
		return null;
	}

	/**
	 * Helper to get the path to the Android SDK if the ANDROID_SDK (or ANDROID_HOME) environment
	 * variable is set.
	 * 
	 * @return the path to the Android SDK, or {@code null}.
	 */
	public static File getSdk() {
		String path = getSdkPath();
		if (path != null) {
			File sdk = new File(path);
			if (sdk != null && sdk.exists() && sdk.isDirectory()) {
				return sdk;
			}
		}
		return null;
	}

	/**
	 * Helper to get the path to the Android SDK as a String if the ANDROID_SDK (or ANDROID_HOME)
	 * environment variable is set.
	 * 
	 * @return the path to the Android SDK as a String, or {@code null}.
	 */
	public static String getSdkPath() {
		if (SDK_PATH!=null) {
			return SDK_PATH;
		}
		Map<String, String> env = System.getenv();
		if (env.containsKey("ANDROID_SDK")) {
			return env.get("ANDROID_SDK");
		} else if (env.containsKey("ANDROID_HOME")) {
			return env.get("ANDROID_HOME");
		}
		return null;
	}
	
	protected ExecResult runAdbCommand(String[] cmd) {
		return runAdbCommand(cmd, null, 0);
	}
	
	protected ExecResult runAdbCommand(String[] cmd, Long timeout) {
		return runAdbCommand(cmd, timeout, 0);
	}
	
	protected ExecResult runAdbCommand(String[] cmd, Long timeout, int retries) {
		ExecResult result = null;
		StringBuilder sb=new StringBuilder();
		
		int remainingAttempts = retries + 1;
		while (remainingAttempts > 0) {

			result = Exec.run(cmd, timeout);

			if (result.timedOut()) {
				sb.append("adb command timed out, killing the server.....\n");
				killAdbServer();
			} else if (result.getStderr() != null
					&& (result.getStderr().contains("device not found") || result.getStderr()
							.contains("device offline"))) {
				sb.append("adb returned \"" + result.getStderr() + "\", killing the server.....\n");
				killAdbServer();
			} else if (result.getStderr() != null
					&& result.getStderr().contains("waiting for device")) {
				// fall thru to retry, need to check this before checking status
			} else if (result.getStatus().equals(ExecStatus.OK)) {
				break;
			}

			sb.append("adb returned status=" + result.getStatus() + "  " + (retries - 1)
					+ " retries remaining:" + "  exitValue=" + result.getExitValue() + "  message="
					+ result.getMessage() + "  stdout=" + result.getStdout() + "  stderr="
					+ result.getStderr() + "   timedOut=" + result.timedOut() + "\n");

			
			remainingAttempts--;
			if (remainingAttempts>0) {
				pause(ADB_RETRY_TIME);
			}
		}
		if (retries == 0) {
			sb.append("adb command did not succeed after " + (retries+1) + " attempts.\n");
		}
		return result;
	}

	private void killAdbServer() {
		String[] nuke = null;
		nuke = new String[] { getAdb().getAbsolutePath(), "kill-server" };
		Exec.run(nuke, new Long(5000));
	}
	
	protected void pause(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException ex) {
			// do nothing
		}
	}
}
