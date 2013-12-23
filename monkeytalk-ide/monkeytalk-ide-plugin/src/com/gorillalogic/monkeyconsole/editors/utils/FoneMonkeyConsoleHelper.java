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
package com.gorillalogic.monkeyconsole.editors.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

/**
 * The fonemonkey console help helps you write messages to the plugin console
 * 
 * @author digitalogic8
 * 
 */
public class FoneMonkeyConsoleHelper {
	private static final SimpleDateFormat FMT = new SimpleDateFormat("HH:mm:ss.SSS");

	private IEditorSite editorSite;

	/**
	 * Constructor
	 * 
	 * @param e
	 *            In order to find the correct view you have to pass in an EditorSite if you don't
	 *            send one the message will be printed to the console, but the console will not be
	 *            brought to the foreground
	 */
	public FoneMonkeyConsoleHelper(IEditorSite editorSite) {
		this.editorSite = editorSite;
	}

	public IEditorSite getEditorSite() {
		return editorSite;
	}

	public void setEditorSite(IEditorSite editorSite) {
		this.editorSite = editorSite;
	}

	/**
	 * Convenience method to allow printing without specifying whether or not there is an error
	 * 
	 * @param message
	 */
	public void writeToConsole(String message) {
		writeToConsole(message, SWT.COLOR_BLACK);
	}

	/**
	 * Write a message to the console
	 * 
	 * @param message
	 *            the message to write
	 * @param error
	 *            true for this message to be printed in red, false otherwise
	 */
	public void writeToConsole(final String message, final boolean error) {
		writeToConsole(message, error ? SWT.COLOR_RED : SWT.COLOR_BLACK);
	}

	/**
	 * Write a message to the console.
	 * 
	 * @param message
	 *            the message to write
	 * @param color
	 *            the message color (default is black, error are red, prints are dark blue)
	 */
	public void writeToConsole(final String message, final int color) {
		final Display display = Display.getDefault();
		display.asyncExec(new Runnable() {
			MessageConsole myConsole = findConsole("MonkeyTalk Output");
			MessageConsoleStream out = myConsole.newMessageStream();

			public void run() {
				out.setColor(display.getSystemColor(color));
				out.println(FMT.format(new Date()) + ": " + message);
			}
		});
	}

	/**
	 * Bring the ConsoleView to the front and show this console.
	 */
	public void bringToFront() {
		MessageConsole myConsole = findConsole("MonkeyTalk Output");
		if (myConsole != null) {
			myConsole.activate();
		}
	}

	/**
	 * Helper to find the console or created it if it has not been created.
	 * 
	 * @param name
	 *            the name of the console
	 * @return the console
	 */
	private MessageConsole findConsole(String name) {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager consoleMgr = plugin.getConsoleManager();
		IConsole[] existing = consoleMgr.getConsoles();
		for (int i = 0; i < existing.length; i++) {
			if (name.equals(existing[i].getName())) {
				return (MessageConsole) existing[i];
			}
		}

		// no console found, so create a new one
		MessageConsole msgConsole = new MessageConsole(name, null);
		consoleMgr.addConsoles(new IConsole[] { msgConsole });
		return msgConsole;
	}
}