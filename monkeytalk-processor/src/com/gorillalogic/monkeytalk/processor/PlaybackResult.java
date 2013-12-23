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
package com.gorillalogic.monkeytalk.processor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.gorillalogic.monkeytalk.sender.Response;
import com.gorillalogic.monkeytalk.utils.Base64;

/**
 * The result of command processor playback is a status and a message.
 */
public class PlaybackResult {
	private PlaybackStatus status;
	private String message;
	private String warning;
	private String debug;
	private String image;
	private String cpu;
	private String memory;
	private String diskSpace;
	private String battery;
	private Scope scope;
	private ArrayList<String> images;
	private File imageFile;
	private File beforeImageFile;
	private File afterImageFile;
	private long startTime;
	private long stopTime;
	private List<Step> steps;

	/**
	 * Instantiate a new {@code PlaybackResult} with {@code OK} status.
	 */
	public PlaybackResult() {
		this(PlaybackStatus.OK, null);
	}

	/**
	 * Instantiate a new {@code PlaybackResult} from the given status.
	 * 
	 * @param status
	 *            the playback status
	 */
	public PlaybackResult(PlaybackStatus status) {
		this(status, null, null, null);
	}

	/**
	 * Instantiate a new {@code PlaybackResult} from the given status and
	 * message.
	 * 
	 * @param status
	 *            the playback status
	 * @param message
	 *            the playback message
	 */
	public PlaybackResult(PlaybackStatus status, String message) {
		this(status, message, null, null);
	}

	/**
	 * Instantiate a new {@code PlaybackResult} from the given status, message,
	 * and warning.
	 * 
	 * @param status
	 *            the playback status
	 * @param message
	 *            (optional) the playback message
	 * @param scope
	 *            (optional) the scope
	 */
	public PlaybackResult(PlaybackStatus status, String message, Scope scope) {
		this(status, message, scope, null, null);
	}

	/**
	 * Instantiate a new {@code PlaybackResult} from the given status, message,
	 * and warning.
	 * 
	 * @param status
	 *            the playback status
	 * @param message
	 *            (optional) the playback message
	 * @param warning
	 *            (optional) the playback warning
	 */
	public PlaybackResult(PlaybackStatus status, String message, Scope scope, String warning) {
		this(status, message, scope, warning, null);
	}

	/**
	 * Instantiate a new {@code PlaybackResult} from the given status, message,
	 * warning, and image.
	 * 
	 * @param status
	 *            the playback status
	 * @param message
	 *            (optional) the playback message
	 * @param scope
	 *            (optional) the scope
	 * @param warning
	 *            (optional) the playback warning
	 * @param image
	 *            (optional) the playback image
	 */
	public PlaybackResult(PlaybackStatus status, String message, Scope scope, String warning,
			String image) {
		this(status, message, scope, warning, image, null);
	}

	/**
	 * Instantiate a new {@code PlaybackResult} from the given status, message,
	 * warning, image, and list of steps processed
	 * 
	 * @param status
	 *            the playback status
	 * @param message
	 *            (optional) the playback message
	 * @param scope
	 *            (optional) the scope
	 * @param warning
	 *            (optional) the playback warning
	 * @param image
	 *            (optional) the playback image
	 * @param steps
	 *            (optional) the list of steps
	 */
	public PlaybackResult(PlaybackStatus status, String message, Scope scope, String warning,
			String image, List<Step> steps) {
		this.status = status;
		this.message = message;
		this.scope = scope;
		this.warning = warning;
		this.image = image;
		this.startTime = System.currentTimeMillis();
		this.stopTime = this.startTime + 1;
		this.debug = null;
		if (image != null) {
			try {
				imageFile = File.createTempFile("screenshot_", ".png");
				Base64.decodeToFile(image, imageFile.getAbsolutePath());
			} catch (IOException e) {
				throw new IllegalStateException("Error converting image to Base64 "
						+ e.getMessage());
			}
		}

		this.images = new ArrayList<String>();
		this.steps = steps;
	}

	/**
	 * Instantiate a new {@code PlaybackResult} from the given {@link Response}.
	 * 
	 * @param resp
	 *            the response
	 */
	public PlaybackResult(Response resp) {
		this(resp, null);
	}

	/**
	 * Instantiate a new {@code PlaybackResult} from the given {@link Response}.
	 * 
	 * @param resp
	 *            the response
	 * @param scope
	 *            (optional) the scope
	 */
	public PlaybackResult(Response resp, Scope scope) {
		status = PlaybackStatus.getStatusFromResponse(resp);
		message = resp.getMessage();
		this.scope = scope;
		warning = resp.getWarning();
		image = resp.getImage();
		imageFile = resp.getImageFile();

	}

	/**
	 * Instantiate a new {@code PlaybackResult} from the given {@link Response}.
	 * 
	 * @param resp
	 *            the response
	 * @param scope
	 *            (optional) the scope
	 * @param beforeImg
	 *            (optional) before screenshot
	 * @param afterImg
	 *            (optional) after screenshot
	 * @param metrics
	 *            (optional) system metrics
	 */
	public PlaybackResult(Response resp, Scope scope, File beforeImg, File afterImg, String metrics) {
		status = PlaybackStatus.getStatusFromResponse(resp);
		message = resp.getMessage();
		this.scope = scope;
		warning = resp.getWarning();
		image = resp.getImage();
		imageFile = resp.getImageFile();
		beforeImageFile = beforeImg;
		afterImageFile = afterImg;
		// set system metrics
		if (metrics != null && !metrics.isEmpty()) {
			String[] systemMetrics = metrics.split(",");
			memory = systemMetrics[0];
			cpu = systemMetrics[1];
			diskSpace = systemMetrics[2];
			battery = systemMetrics[3];
		}
	}

	/**
	 * Get the playback status (OK, ERROR, or FAILURE).
	 * 
	 * @return the status
	 */
	public PlaybackStatus getStatus() {
		return status;
	}

	/**
	 * Set the playback status (OK, ERROR, or FAILURE).
	 * 
	 * @param status
	 *            the status
	 */
	public void setStatus(PlaybackStatus status) {
		this.status = status;
	}

	/**
	 * Get the playback message.
	 * 
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Set the playback message.
	 * 
	 * @param message
	 *            the message
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * Get the playback warning.
	 * 
	 * @return the warning
	 */
	public String getWarning() {
		return warning;
	}

	/**
	 * Set the playback warning message.
	 * 
	 * @param warning
	 *            the warning
	 */
	public void setWarning(String warning) {
		this.warning = warning;
	}

	/**
	 * Get the playback image (as a base64 encoded string).
	 * 
	 * @return the image
	 */
	public String getImage() {
		if (image == null) {
			if (getImageFile() == null) {
				return null;
			}

			try {
				image = Base64.encodeFromFile(getImageFile().getAbsolutePath());
			} catch (IOException e) {
				throw new IllegalStateException("Unable to base64-encode "
						+ getImageFile().getAbsolutePath());
			}
		}
		return image;
	}

	/**
	 * Get the playback images (as a base64 encoded strings).
	 * 
	 * @return the image
	 */
	public ArrayList<String> getImages() {
		return images;
	}

	public void addImage(String image) {
		if (images == null)
			images = new ArrayList<String>();

		images.add(image);
	}

	/**
	 * Set the scope (at the time of the result).
	 * 
	 * @param scope
	 *            the scope
	 */
	public void setScope(Scope scope) {
		this.scope = scope;
	}

	/**
	 * Get the scope
	 * 
	 * @return the scope
	 */
	public Scope getScope() {
		return scope;
	}

	/**
	 * Set the startTime as UNIX time in milliseconds - same as Java
	 * "new Date().time()"
	 * 
	 * @param startTime
	 *            the time
	 */
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	/**
	 * Get the startTime as UNIX time in milliseconds - same as Java
	 * "new Date().time()"
	 * 
	 */
	public long getStartTime() {
		return this.startTime;
	}

	/**
	 * Set the stopTime as UNIX time in milliseconds - same as Java
	 * "Date().time()"
	 * 
	 * @param stopTime
	 *            the time
	 */
	public void setStopTime(long stopTime) {
		this.stopTime = stopTime;
	}

	/**
	 * Get the stopTime as UNIX time in milliseconds - same as Java
	 * "Date().time()"
	 * 
	 */
	public long getStopTime() {
		return this.stopTime;
	}

	/**
	 * Set the list of processor steps
	 * 
	 * @param steps
	 *            the list of steps
	 */
	public void setSteps(List<Step> steps) {
		this.steps = steps;
	}

	/**
	 * Gets the list of processor steps
	 * 
	 * @return the list of steps
	 */
	public List<Step> getSteps() {
		return this.steps;
	}

	/**
	 * Get the duration in seconds
	 * 
	 */
	public double getDuration() {
		return ((double) (this.stopTime - this.startTime)) / 1000;
	}

	/**
	 * Get the output of the Debug command
	 * 
	 */
	public String getDebug() {
		return debug;
	}

	/**
	 * Set the output of the Debug command
	 * 
	 */
	public void setDebug(String debug) {
		this.debug = debug;
	}

	/**
	 * Get the screenshot image file of the command
	 * 
	 */
	public File getImageFile() {
		return imageFile;
	}

	/**
	 * Set the image file
	 * 
	 */
	public void setImageFile(File imageFile) {
		this.imageFile = imageFile;
	}

	/**
	 * Get the before screenshot image file of the command
	 * 
	 */
	public File getBeforeImageFile() {
		return beforeImageFile;
	}

	/**
	 * Set the before image file
	 * 
	 */
	public void setBeforeImageFile(File beforeImageFile) {
		this.beforeImageFile = beforeImageFile;
	}

	/**
	 * Get the after screenshot image file of the command
	 * 
	 */
	public File getAfterImageFile() {
		return afterImageFile;
	}

	/**
	 * Set the after image file
	 * 
	 */
	public void setAfterImageFile(File afterImageFile) {
		this.afterImageFile = afterImageFile;
	}

	/**
	 * Get the CPU system metric
	 * 
	 */
	public String getCpu() {
		return cpu;
	}

	/**
	 * Set the CPU system metric
	 * 
	 */
	public void setCpu(String cpu) {
		this.cpu = cpu;
	}

	/**
	 * Get the memory system metric
	 * 
	 */
	public String getMemory() {
		return memory;
	}

	/**
	 * Set the memory system metric
	 * 
	 */
	public void setMemory(String memory) {
		this.memory = memory;
	}

	/**
	 * Get the storage system metric
	 * 
	 */
	public String getDiskSpace() {
		return diskSpace;
	}

	/**
	 * Set the storage system metric
	 * 
	 */
	public void setDiskSpace(String diskSpace) {
		this.diskSpace = diskSpace;
	}

	/**
	 * Get the battery system metric
	 * 
	 */
	public String getBattery() {
		return battery;
	}

	/**
	 * Set the battery system metric
	 * 
	 */
	public void setBattery(String battery) {
		this.battery = battery;
	}

	@Override
	public String toString() {
		return toString(false, false);
	}

	/**
	 * Print the playback result's status, message, warning, and image.
	 * 
	 * @param showWarning
	 *            if true, print the warning
	 * @param showImage
	 *            if true, print the image
	 * @return the playback result as a string
	 */
	public String toString(boolean showWarning, boolean showImage) {
		return status
				+ (message != null && message.length() > 0 ? " : " + message : "")
				+ (showWarning && warning != null && warning.length() > 0 ? " - warning: "
						+ warning : "")
				+ (showImage && image != null && image.length() > 0 ? " - image: " + image : "");
	}
}