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
package com.gorillalogic.monkeytalk.web;

import java.io.File;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ResourceHandler;

/**
 * Simple Jetty server that serves up a folder of static assets. Very useful for testing...
 * 
 * Set the web dir by trying the following (in order):
 * <ol>
 * <li>Use the given web dir</li>
 * <li>Use ./web folder</li>
 * <li>Use the web folder inside the jar</li>
 * <ol>
 */
public class WebServer {
	private static final int PORT = 9001;
	private static final String WEBDIR = "web";

	public static void main(String[] args) throws Exception {
		System.out.println("WebServer starting...");
		System.out.println("  workingDir=" + new File("").getAbsolutePath());

		// set port
		int port = PORT;
		if (args.length > 0) {
			try {
				port = Integer.parseInt(args[0]);
			} catch (NumberFormatException ex) {
				halt("bad port");
			}
		}
		System.out.println("  port=" + port);

		// set web dir
		String dir = WebServer.class.getClassLoader().getResource(WEBDIR).toExternalForm();
		
		if (args.length > 1) {
			// first, we use the given web dir
			File f = new File(args[1]);
			if (!f.exists()) {
				halt("webDir does not exist - looked here: " + f.getAbsolutePath());
			} else if (!f.isDirectory()) {
				halt("webDir must be directory - looked here: " + f.getAbsolutePath());
			}
			dir = args[1];
		} else {
			// second, we check if a local web folder exists
			File f = new File("web");
			if (f.exists() && f.isDirectory()) {
				dir = "web";
			}
		}
		System.out.println("  webDir=" + dir);

		// init jetty
		ResourceHandler resourceHandler = new ResourceHandler();
		resourceHandler.setWelcomeFiles(new String[] { "index.html" });
		resourceHandler.setResourceBase(dir);
		Server server = new Server(port);
		server.setHandler(resourceHandler);

		// start it, and wait...
		try {
			server.start();
			System.out.println("WebServer started!\n");
			server.join();
		} catch (Exception ex) {
			System.out.println("WebServer failed to start...");
			ex.printStackTrace();
			System.out.println("WebServer start failed!\n");
		}
		
		System.out.println("WebServer stopped!");
	}

	private static void halt(String err) {
		System.out.println("ERROR: " + err
				+ "\n\nUsage: java -jar monkeytalk-web-tester.jar <port> <webDir>\n");
		System.exit(1);
	}
}
