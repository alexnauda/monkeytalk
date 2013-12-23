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

import com.gorillalogic.monkeytalk.Command;
import com.gorillalogic.monkeytalk.CommandWorld;
import com.gorillalogic.monkeytalk.agents.IAgent;

/**
 * Base class for all processors.
 */
public class BaseProcessor {
	protected static final String ABORT_BY_REQUEST = "playback aborted";

	protected CommandWorld world;
	protected IAgent agent;
	protected int thinktime = -1;
	protected int timeout = -1;

	private boolean screenshotOnError = true;
	private boolean takeAfterScreenshot = false;
	private boolean takeAfterMetrics = false;

	/**
	 * Instantiate a processor from the given processor by picking out its command world and agent.
	 * 
	 * @param processor
	 *            the processor
	 */
	public BaseProcessor(BaseProcessor processor) {
		this(processor.getWorld(), processor.getAgent());
	}

	/**
	 * Instantiate a processor with the project root folder and agent.
	 * 
	 * @param rootDir
	 *            the project root directory
	 * @param agent
	 *            the agent
	 */
	public BaseProcessor(File rootDir, IAgent agent) {
		this(new CommandWorld(rootDir), agent);
	}

	/**
	 * Instantiate a processor with the given command world and agent.
	 * 
	 * @param world
	 *            the command world for the project
	 * @param agent
	 *            the agent
	 */
	public BaseProcessor(CommandWorld world, IAgent agent) {
		this.world = world;
		this.agent = agent;
	}

	/**
	 * Get the target host from the agent.
	 * 
	 * @return the host
	 */
	public String getHost() {
		return (agent != null ? agent.getHost() : null);
	}

	/**
	 * Get the target port from the agent.
	 * 
	 * @return the port
	 */
	public int getPort() {
		return (agent != null ? agent.getPort() : -1);
	}

	/**
	 * Get the command world for the project.
	 * 
	 * @return the command world
	 */
	public CommandWorld getWorld() {
		return world;
	}

	/**
	 * Get the agent.
	 * 
	 * @return the agent
	 */
	public IAgent getAgent() {
		return agent;
	}

	/**
	 * Get the global timeout -- the amount of time (in milliseconds) to continue to retry playing a
	 * MonkeyTalk command before failing. Defaults to 2000ms.
	 * 
	 * @see com.gorillalogic.monkeytalk.Command#DEFAULT_TIMEOUT
	 * 
	 * @return the timeout (in ms)
	 */
	public int getGlobalTimeout() {
		return (timeout < 0 ? Command.DEFAULT_TIMEOUT : timeout);
	}

	/**
	 * Set the global timeout.
	 * 
	 * @param timeout
	 *            the timeout (in ms)
	 */
	public void setGlobalTimeout(int timeout) {
		this.timeout = timeout;
	}

	/**
	 * Get the global thinktime -- the amount of time (in millis) to wait before playing a
	 * MonkeyTalk command for the first time. Defaults to 500ms.
	 * 
	 * @see com.gorillalogic.monkeytalk.Command#DEFAULT_THINKTIME
	 * 
	 * @return the thinktime (in ms)
	 */
	public int getGlobalThinktime() {
		return (thinktime < 0 ? Command.DEFAULT_THINKTIME : thinktime);
	}

	/**
	 * Set the global thinktime.
	 * 
	 * @param thinktime
	 *            the thinktime (in ms)
	 */
	public void setGlobalThinktime(int thinktime) {
		this.thinktime = thinktime;
	}

	/**
	 * True if screenshot on error is on (aka if any command sent by this processor causes an error,
	 * then the Agent will return a screenshot along with the response), otherwise false. Defaults
	 * to {@code true}.
	 * 
	 * @return true if screenshot on error is on
	 */
	public boolean isGlobalScreenshotOnError() {
		return screenshotOnError;
	}

	/**
	 * Set screenshot on error, true to turn on, false to turn off.
	 * 
	 * @param screenshotOnError
	 *            true to turn on screenshot on error
	 */
	public void setGlobalScreenshotOnError(boolean screenshotOnError) {
		this.screenshotOnError = screenshotOnError;
	}

	/**
	 * True if take after screenshots is on, otherwise false. Defaults to {@code false}.
	 * 
	 * @return true if take after screenshot is on
	 */
	public boolean isTakeAfterScreenshot() {
		return takeAfterScreenshot;
	}

	/**
	 * Set take after screenshot on every command, true to turn on, false to turn off.
	 * 
	 * @param takeAfterScreenshot
	 *            true to turn on take After Screenshot
	 */
	public void setTakeAfterScreenshot(boolean takeAfterScreenshot) {
		this.takeAfterScreenshot = takeAfterScreenshot;
	}

	/**
	 * True if take after system metrics is on, otherwise false. Defaults to {@code false}.
	 * 
	 * @return true if take after metrics is on
	 */
	public boolean isTakeAfterMetrics() {
		return takeAfterMetrics;
	}

	/**
	 * Set take after metrics on every command, true to turn on, false to turn off.
	 * 
	 * @param takeAfterMetrics
	 *            true to turn on take After metrics
	 */
	public void setTakeAfterMetrics(boolean takeAfterMetrics) {
		this.takeAfterMetrics = takeAfterMetrics;
	}

	/**
	 * Helper to create a copy of the given result using the given scope and start time.
	 * 
	 * @param result
	 *            the result to be copied
	 * @param scope
	 *            the scope to be used by the copy
	 * @param startTime
	 *            the start time to be used by the copy
	 * @return the result copy
	 */
	protected PlaybackResult copyResult(PlaybackResult result, Scope scope, long startTime) {
		PlaybackResult copy = new PlaybackResult(PlaybackStatus.OK, result.getMessage(), scope);
		copy.setStartTime(startTime);
		copy.setStatus(result.getStatus());
		copy.setWarning(result.getWarning());
		copy.setDebug(result.getDebug());
		copy.setImageFile(result.getImageFile());
		if (result.getImages() != null) {
			for (String image : result.getImages()) {
				copy.addImage(image);
			}
		}
		return copy;
	}

	@Override
	public String toString() {
		return world.toString();
	}
}