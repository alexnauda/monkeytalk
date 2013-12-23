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
package com.gorillalogic.fonemonkey.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.view.View;

import com.gorillalogic.fonemonkey.ActivityManager;
import com.gorillalogic.fonemonkey.Log;
import com.gorillalogic.fonemonkey.Recorder;
import com.gorillalogic.fonemonkey.automators.AutomationManager;
import com.gorillalogic.fonemonkey.automators.DeviceAutomator;
import com.gorillalogic.fonemonkey.automators.IAutomator;
import com.gorillalogic.fonemonkey.exceptions.FoneMonkeyErrorException;
import com.gorillalogic.fonemonkey.exceptions.FoneMonkeyFailureException;
import com.gorillalogic.fonemonkey.exceptions.FoneMonkeyScriptFailure;
import com.gorillalogic.monkeytalk.BuildStamp;
import com.gorillalogic.monkeytalk.Command;
import com.gorillalogic.monkeytalk.automators.AutomatorConstants;
import com.gorillalogic.monkeytalk.server.JsonServer;
import com.gorillalogic.monkeytalk.server.ServerConfig;

public class PlaybackServer extends JsonServer {
	private static final Pattern VERIFY_FAIL_PATTERN = Pattern.compile("but found \"(.*)\"");

	public PlaybackServer() throws IOException {
		super(ServerConfig.DEFAULT_PLAYBACK_PORT_ANDROID);
		Log.log("starting PlaybackServer on port " + this.getPort());
	}

	@Override
	public Response serve(String uri, String method, Map<String, String> headers, JSONObject json) {

		if ("GET".equals(method)) {
			return super.serve(uri, method, headers, json);
		}
		if (json == null) {
			return new Response(HttpStatus.INTERNAL_ERROR, "json body is null");
		}

		String mtcommand = json.optString("mtcommand");
		String result = "OK";
		String message = "";
		String warning = "";

		if ("PING".equals(mtcommand)) {
			if (json.has("record")) {
				String record = json.optString("record");
				boolean isRecording = "on".equalsIgnoreCase(record);
				Recorder.setRecording(isRecording);

				if (json.has("recordhost") && json.has("recordport")) {
					String recordHost = json.optString("recordhost");
					int recordPort = json.optInt("recordport");
					Recorder.setRecordServer(recordHost, recordPort);
				}
				message = "{"
						+ "os:\"Android\""
						+ ",record:\""
						+ (isRecording ? "ON" : "OFF")
						+ "\""
						+ ",mtversion:\""
						+ BuildStamp.VERSION
						+ ((BuildStamp.BUILD_NUMBER != null && BuildStamp.BUILD_NUMBER.length() > 0) ? "_"
								: "") + BuildStamp.BUILD_NUMBER + " - " + BuildStamp.TIMESTAMP
						+ "\"" + "}";
				if (isRecording) {
					List<Command> list = Recorder.pollQueue();
					if (list != null && list.size() > 0) {
						StringBuilder jlist = new StringBuilder("[");

						try {
							for (Command command : list) {
								if (jlist.length() > 1) {
									jlist.append(',');
								}
								String c = command.getCommandAsJSON(false).toString();
								jlist.append(c);
							}
							jlist.append(']');
							message = jlist.toString();
						} catch (Exception e) {
							Log.log(e);
						}
					}
				} else {
					Recorder.clearQueue();
				}
			} else {
				result = "ERROR";
				message = "ping is missing the 'record' key";
			}
		} else if ("PLAY".equals(mtcommand)) {

			Command cmd = new Command(json);
			// any PLAY turns off recording unless echo is on
			if (!"true".equals(cmd.getModifiers().get("echo"))) {
				// Log.log("Turning recording off for " + cmd);
				Recorder.setRecording(false);
			} else {
				// Log.log("Turning recording on for " + cmd);
				Recorder.setRecording(true);
			}

			// Find clipped views and throw error
			if (ActivityManager.getClippedViews() != null) {
				for (int i = 0; i < ActivityManager.getClippedViews().size(); i++) {
					IAutomator automator = AutomationManager.findAutomator(ActivityManager
							.getClippedViews().get(i));

					// If view does not have a monkeyID ignore it
					if (automator.getMonkeyID().length() > 0) {
						result = "OK";
						message = message + automator.getComponentType() + " with monkeyID "
								+ automator.getMonkeyID() + " is being clipped. ";
					}
				}

				ActivityManager.clearClippedViews();
			}

			Log.log("PLAYBACK - " + cmd.toString());

			try {
				String val = play(cmd);
				if (val != null) {
					message = val;
				}
			} catch (FoneMonkeyFailureException ex) {
				result = "FAILURE";
				message = (cmd.isScreenshotOnError() ? screenshotOnError(ex.getMessage()) : ex
						.getMessage());
			} catch (FoneMonkeyErrorException ex) {
				result = "ERROR";
				message = (cmd.isScreenshotOnError() ? screenshotOnError(ex.getMessage()) : ex
						.getMessage());
			}
			// } else if ("RECORD".equals(mtcommand)) {
			// // IDE polling for recorded commands (from behind firewall)
			// List<Command> list = Recorder.pollQueue();
			// StringBuilder jlist = new StringBuilder("[");
			// if (list != null && list.size() > 0) {
			//
			// try {
			// for (Command command : list) {
			// if (jlist.length() > 1) {
			// jlist.append(',');
			// }
			// String c = command.getCommandAsJSON(false).toString();
			// jlist.append(c);
			// }
			// jlist.append(']');
			// message = jlist.toString();
			// } catch (Exception e) {
			// Log.log(e);
			// }
			// }

		} else if ("DUMPTREE".equals(mtcommand)) {
			message = AutomationManager.dumpViewTree();
		} else if ("STOP".equals(mtcommand)) {
			// stop messages usually sent from newly started MT app to free up playback server port.
			stop();
			message = "STOP";
			Log.log("Playback server stopped");
		} else {
			Log.log("UNKNOWN - " + json.toString());
			result = "ERROR";
			message = "unknown mtcommand=" + mtcommand;
		}

		JSONObject resp = new JSONObject();
		try {
			resp.put("result", result);

			if (warning.length() > 0) {
				resp.put("warning", warning);
			} else {
				if (!"PING".equals(mtcommand)) {
					Log.log("sending " + mtcommand + " response: " + message);
				}

				resp.put(
						"message",
						(message.startsWith("{") ? new JSONObject(message) : message
								.startsWith("[") ? new JSONArray(message) : message));
			}
		} catch (JSONException ex) {
			resp = new JSONObject();
		}

		return new Response(HttpStatus.OK, resp);
	}

	private String play(Command cmd) throws FoneMonkeyErrorException, FoneMonkeyFailureException {
		// thinktime, before starting
		try {
			Thread.sleep(cmd.getThinktime());
		} catch (InterruptedException ex) {
			// ignore this
		}

		long start = System.currentTimeMillis();

		// play the command
		while (true) {
			boolean error = false;
			String msg = null;

			// handle wildcard monkey id
			if (isWildcardMonkeyIdVerify(cmd)) {
				List<String> fails = new ArrayList<String>();
				List<IAutomator> automators = null;

				try {
					automators = AutomationManager.findAllWildcardMonkeyIdAutomators(
							cmd.getComponentType(), cmd.getMonkeyId());
				} catch (IllegalArgumentException ex) {
					automators = null;
					error = true;
					msg = ex.getMessage();
					Log.log("Error: " + msg);
				} catch (Exception ex) {
					automators = null;
					error = true;
					msg = ex.getClass().getName()
							+ (ex.getMessage() != null ? " : " + ex.getMessage() : "");
					Log.log("Error: " + msg, ex);
				}

				if (automators == null) {
					// ignore, msg already set
				} else if (automators.isEmpty()) {
					if (cmd.getArgs().size() == 0 && cmd.getAction().equalsIgnoreCase("verifynot")) {
						// success! - no automators & we are verifyNot
						return "";
					} else {
						// Log.log("WildcardMonkeyIdVerify: no matches");
						msg = "Unable to find " + printName(cmd);
					}
				} else {
					boolean verifyNotFailed = false;
					// Log.log("WildcardMonkeyIdVerify: # of matches = " + automators.size());
					for (IAutomator automator : automators) {
						if (automator != null) {
							try {
								Object view = automator.getComponent();

								if (view != null && view instanceof View
										&& !((View) view).isShown()) {
									// found view, but not visible
								} else {
									Log.log("Play " + cmd.getComponentType() + "."
											+ cmd.getAction() + " on "
											+ automator.getComponentType() + "("
											+ automator.getMonkeyID() + ")");
									msg = automator
											.play(cmd.getAction(),
													cmd.getArgs().toArray(
															new String[cmd.getArgs().size()]));
									// Log.log("WildcardMonkeyIdVerify: msg=" + msg);
								}
							} catch (FoneMonkeyScriptFailure ex) {
								// build a better error message
								Matcher m = VERIFY_FAIL_PATTERN.matcher(ex.getMessage());

								if (m.find() && !fails.contains(m.group(1))) {
									fails.add(m.group(1));
								}
								// Log.log("WildcardMonkeyIdVerify: failMsg=" + ex.getMessage());
								msg = null;
							} catch (IllegalArgumentException ex) {
								error = true;
								msg = ex.getMessage();
								Log.log("Error: " + msg);
							} catch (Exception ex) {
								msg = null;
							}
						}

						if (error) {
							//Its error. Not a failure case.
						} else if (cmd.getAction().toLowerCase().startsWith("verifynot")) {
							if (msg == null) {
								// fail! as least one verifyNot has failed
								verifyNotFailed = true;
								// Log.log("WildcardMonkeyIdVerify: verifyNot=true");
							}
						} else {
							if (msg != null && msg.length() == 0) {
								// success! at least one verify succeeded
								return "";
							}
						}
					}

					// all verifies failed
					if (error) {
						//Its error. Not a failure case.
					} else if (cmd.getArgs().size() == 0) {
						if (cmd.getAction().equalsIgnoreCase("verifynot")) {
							msg = "Found " + printName(cmd);
						} else {
							msg = "Unable to find " + printName(cmd);
						}
					} else {
						if (cmd.getAction().toLowerCase().startsWith("verifynot")) {
							if (verifyNotFailed) {
								msg = "Found \"" + cmd.getArgs().get(0) + "\" in " + printName(cmd);
							} else {
								// success! all verifyNots succeeded
								// Log.log("WildcardMonkeyIdVerify: all verifyNots succeeded");
								return "";
							}
						} else {
							msg = "Expected \"" + cmd.getArgs().get(0) + "\", but found " + fails;
						}
					}
					// Log.log("WildcardMonkeyIdVerify: failed - " + msg);
				}
			} else {
				// not wildcard monkeyId verify, so just find 1st match
				try {
					IAutomator automator = AutomationManager.find(cmd.getComponentType(),
							cmd.getMonkeyId(), true);
					if (automator == null) {
						// verify that the component does NOT exist (we must do this here)
						if (cmd.getAction().toLowerCase().startsWith("verifynot")
								&& cmd.getArgs().size() == 0) {
							return "";
						} else {
							msg = "Unable to find " + printName(cmd);
						}
					} else {
						Object view = automator.getComponent();

						if (view != null && view instanceof View && !((View) view).isShown()) {
							msg = "Found " + printName(cmd) + ", but not visible";
						} else {
							Log.log("Play " + cmd.getComponentType() + "." + cmd.getAction()
									+ " on " + automator.getComponentType() + "("
									+ automator.getMonkeyID() + ")");
							return automator.play(cmd.getAction(),
									cmd.getArgs().toArray(new String[cmd.getArgs().size()]));
						}
					}
				} catch (FoneMonkeyScriptFailure ex) {
					msg = ex.getMessage();
				} catch (IllegalArgumentException ex) {
					error = true;
					msg = ex.getMessage();
					Log.log("Error: " + msg);
				} catch (Exception ex) {
					error = true;
					msg = ex.getClass().getName()
							+ (ex.getMessage() != null ? " : " + ex.getMessage() : "");
					Log.log("Error: " + msg, ex);
				}
			}

			// timeout?
			if (System.currentTimeMillis() - start > cmd.getTimeout()) {
				if (error) {
					throw new FoneMonkeyErrorException(msg);
				}

				// not an error, so we must have a failure here
				throw new FoneMonkeyFailureException(msg);
			}

			// sleep, then loop again
			try {
				Thread.sleep(cmd.getRetryDelay());
			} catch (InterruptedException ex) {
				// ignore this
			}
		}
	}

	private String printName(Command cmd) {
		return cmd.getComponentType() + "(" + cmd.getMonkeyId() + ")";
	}

	private String screenshotOnError(String msg) {
		if (msg == null) {
			msg = "no message";
		}
		Log.log("SCREENSHOT - " + msg + " - taking screenshot...");
		DeviceAutomator device = (DeviceAutomator) AutomationManager.findAutomatorByType("Device");

		if (device != null) {
			try {
				String screenshot = device.play(AutomatorConstants.ACTION_SCREENSHOT);
				if (screenshot != null && screenshot.startsWith("{screenshot")) {
					Log.log("SCREENSHOT - done!");
					return "{message:\"" + msg.replaceAll("\"", "'") + "\","
							+ screenshot.substring(1);
				}
			} catch (Exception ex) {
				String exMsg = ex.getMessage();
				if (exMsg != null) {
					exMsg = exMsg.replaceAll("\"", "'");
				} else {
					exMsg = ex.getClass().getName();
				}
				return msg + " -- " + exMsg;
			}
		}
		return msg;
	}

	/** Helper to check if the given command is a Wildcard MonkeyId Verify command. */
	private boolean isWildcardMonkeyIdVerify(Command cmd) {
		return cmd != null && cmd.getAction() != null && cmd.getMonkeyId() != null
				&& cmd.getAction().toLowerCase().startsWith("verify")
				&& !cmd.getAction().equalsIgnoreCase("verifyimage")
				&& (cmd.getMonkeyId().contains("*") || cmd.getMonkeyId().contains("?"))
				&& !cmd.getMonkeyId().toLowerCase().startsWith("xpath=")
				&& !cmd.getComponentType().equalsIgnoreCase("device");
	}
}